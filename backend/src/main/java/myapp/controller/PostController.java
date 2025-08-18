package myapp.controller;

import myapp.model.Post;
import myapp.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Post> createPost(@RequestBody Post post, @PathVariable Long userId) {
        Post createdPost = postService.createPost(post, userId);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return postService.getPostById(id)
                .map(post -> new ResponseEntity<>(post, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/{postId}/like/{userId}")
    public ResponseEntity<Post> likePost(@PathVariable Long postId, @PathVariable Long userId) {
        try {
            Post likedPost = postService.likePost(postId, userId);
            return new ResponseEntity<>(likedPost, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{postId}/report/{userId}")
    public ResponseEntity<Post> reportPost(@PathVariable Long postId, @PathVariable Long userId) {
        try {
            Post reportedPost = postService.reportPost(postId, userId);
            return new ResponseEntity<>(reportedPost, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
