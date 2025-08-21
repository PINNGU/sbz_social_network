package myapp.controller;

import myapp.model.Post;
import myapp.model.User;
import myapp.repository.UserRepository;
import myapp.service.PostService;
import myapp.payload.CreatePostRequest;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/posts")
public class PostController {
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KieContainer kieContainer;


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
    // Insert ALL posts, including the current user's own posts
    List<Post> candidates = allPosts;

        // Drools session
        KieSession kieSession = kieContainer.newKieSession("ksession-rules");
        kieSession.insert(currentUser);
    List<Post> filteredPosts = new java.util.ArrayList<>();
    java.util.Map<Long, java.util.Set<String>> postReasons = new java.util.HashMap<>();
    kieSession.setGlobal("filteredPosts", filteredPosts);
    kieSession.setGlobal("postReasons", postReasons);
        for (Post post : candidates) {
            System.out.println("Inserting post into Drools: id=" + post.getId() + ", likes=" + post.getNumberOfLikes() + ", date=" + post.getDateOfCreation() + ", hashtags=" + post.getHashtags() + ", userId=" + post.getUser().getId());
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

    // Helper to compare reason priorities
    private boolean isHigherPriority(String newReason, String oldReason) {
        return getPriority(newReason) > getPriority(oldReason);
    }

    private static int getPriority(String r) {
        switch (r) {
            case "friend": return 5;
            case "for_you": return 4;
            case "popular": return 3;
            case "suggested": return 2;
            case "all": return 1;
            default: return 0;
        }
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
        post.setNumberOfLikes(post.getNumberOfLikes() + 1);
        postService.savePost(post);
        return ResponseEntity.ok("Liked");
    }
}
