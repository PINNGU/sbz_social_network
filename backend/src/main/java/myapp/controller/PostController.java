package myapp.controller;

import myapp.model.Post;
import myapp.model.User;
import myapp.repository.UserRepository;
import myapp.service.PostService;
import myapp.service.FriendsService;
import myapp.payload.CreatePostRequest;
import myapp.util.RecommendationAgent;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/posts")
public class PostController {
    


    @Autowired
    private PostService postService;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KieContainer kieContainer;

    @Autowired
    private FriendsService friendsService;

        // Get likes for a post directly from post_likes table
    @GetMapping("/{postId}/likes")
    public ResponseEntity<?> getLikesForPost(@PathVariable("postId") Long postId) {
        List<Long> likerIds = postService.getLikerUserIdsByPostId(postId);
        return ResponseEntity.ok(likerIds);
    }


    // Optionally, add more endpoints for fetching posts, etc.

    @PostMapping("/create")
    public ResponseEntity<Post> createPost(@RequestBody CreatePostRequest req) {
        if (req.getUserId() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        User user = userRepository.findById(req.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(null);
        }
    Post post = new Post();
    post.setDescription(req.getDescription());
    post.setHashtags(req.getHashtags());
    post.setUser(user);
    Post savedPost = postService.savePost(post);
    return ResponseEntity.ok(savedPost);
    }

    @GetMapping("")
    public ResponseEntity<?> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    // Get global posts for a user (from friends, not older than a day, using Drools)
    @GetMapping("/global")
    public ResponseEntity<?> getGlobalPosts(@RequestParam("userId") Long userId) {
        User currentUser = userRepository.findById(userId).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        List<Post> allPosts = postService.getAllPosts();
        List<Post> candidates = allPosts;

        RecommendationAgent.setAllUsers(userRepository.findAll());
        RecommendationAgent.setAllPosts(allPosts);

        // Drools session
        KieSession kieSession = kieContainer.newKieSession("ksession-rules");
        kieSession.insert(currentUser);

        // Build friends map from FriendsService (userId -> Set<friendId>)
        java.util.Map<Long, java.util.Set<Long>> friendsMap = new java.util.HashMap<>();
        for (myapp.model.User u : userRepository.findAll()) {
            java.util.List<myapp.model.User> friendsList = friendsService.getFriendsByUserId(u.getId());
            java.util.Set<Long> set = new java.util.HashSet<>();
            for (myapp.model.User f : friendsList) {
                if (f != null && f.getId() != null) set.add(f.getId());
            }
            friendsMap.put(u.getId(), set);
        }

        // DEBUG: print friendsMap entry for current user to verify mapping and types
        try {
            System.out.println("DEBUG friendsMap entry for currentUser " + currentUser.getId() + " -> " + friendsMap.get(currentUser.getId()));
        } catch (Exception e) {
            System.out.println("DEBUG friendsMap print error: " + e.getMessage());
        }

        List<Post> filteredPosts = new java.util.ArrayList<>();
        java.util.Map<Long, java.util.Set<String>> postReasons = new java.util.HashMap<>();

        // DEBUG: print KieBase / KiePackage globals to verify which globals are declared
        try {
            org.kie.api.KieBase kb = kieSession.getKieBase();
            for (org.kie.api.definition.KiePackage kp : kb.getKiePackages()) {
                StringBuilder sb = new StringBuilder();
                sb.append("KIE PACKAGE: ").append(kp.getName()).append(" rules=[");
                for (org.kie.api.definition.rule.Rule r : kp.getRules()) {
                    sb.append(r.getName()).append(",");
                }
                sb.append("]");
                System.out.println(sb.toString());
            }
        } catch (Exception e) {
            System.out.println("DEBUG: error printing KieBase globals: " + e.getMessage());
        }

        kieSession.setGlobal("filteredPosts", filteredPosts);
        kieSession.setGlobal("postReasons", postReasons);
        kieSession.setGlobal("friendsMap", friendsMap);

        for (Post post : candidates) {
            System.out.println("Inserting post into Drools: id=" + post.getId() + ", likes=" + (post.getLikes() != null ? post.getLikes().size() : 0) + ", date=" + post.getDateOfCreation() + ", hashtags=" + post.getHashtags() + ", userId=" + post.getUser().getId());
            kieSession.insert(post);
        }
        kieSession.fireAllRules();
        kieSession.dispose();

        // Remove duplicates by post ID, keep highest priority reason
        java.util.Map<Long, myapp.payload.PostWithReason> resultMap = new java.util.LinkedHashMap<>();
        for (Post p : filteredPosts) {
            java.util.Set<String> reasons = postReasons.getOrDefault(p.getId(), java.util.Collections.emptySet());
            resultMap.put(p.getId(), new myapp.payload.PostWithReason(p, reasons));
        }
        // Sort by dateOfCreation descending
        java.util.List<myapp.payload.PostWithReason> result = new java.util.ArrayList<>(resultMap.values());
        result.sort((a, b) -> b.getPost().getDateOfCreation().compareTo(a.getPost().getDateOfCreation()));
        // Filter out the current user's own posts
        result = result.stream()
            .filter(pwr -> !pwr.getPost().getUser().getId().equals(currentUser.getId()))
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);

    }


    // Like a post
    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable("postId") Long postId, @RequestParam("userId") Long userId) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            return ResponseEntity.badRequest().body("Post not found");
        }
        if (post.getLikes().contains(userId)) {
            return ResponseEntity.badRequest().body("User already liked this post");
        }
    post.getLikes().add(userId);
    postService.savePost(post);
    return ResponseEntity.ok("Liked");
    }
    @PostMapping("/{postId}/report")
    public ResponseEntity<?> reportPost(@PathVariable("postId") Long postId, @RequestParam("userId") Long userId) 
    {
        Post post = postService.getPostById(postId);
        if (post == null) 
        {
            return ResponseEntity.badRequest().body("Post not found");
        }
        if (post.getReports().contains(userId)) 
        {
            return ResponseEntity.badRequest().body("User already reported this post");
        }
        post.getReports().add(userId);
        postService.savePost(post);
        return ResponseEntity.ok("Reported");
    }
}