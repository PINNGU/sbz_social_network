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
        // Filter out user's own posts
        List<Post> candidates = allPosts.stream()
                .filter(p -> !p.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        // Drools session
        KieSession kieSession = kieContainer.newKieSession("ksession-rules");
        kieSession.setGlobal("userIdToShow", userId);
        kieSession.insert(currentUser);
        List<Post> filteredPosts = new java.util.ArrayList<>();
        kieSession.setGlobal("filteredPosts", filteredPosts);
        for (Post post : candidates) {
            kieSession.insert(post);
        }
        kieSession.fireAllRules();
        kieSession.dispose();
        return ResponseEntity.ok(filteredPosts);
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
