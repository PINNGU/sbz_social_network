package myapp.service;

import myapp.model.Post;
import myapp.model.User;
import myapp.repository.PostRepository;
import myapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public Post createPost(Post post, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        post.setUser(user);
        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    @Transactional
    public Post likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));
        // Here, you might want to add logic to prevent a user from liking a post multiple times
        // For simplicity, we just increment the count.
        post.setNumberOfLikes(post.getNumberOfLikes() + 1);
        return postRepository.save(post);
    }

    @Transactional
    public Post reportPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));
        if (!post.getReports().contains(userId)) {
            post.getReports().add(userId);
            return postRepository.save(post);
        } else {
            throw new IllegalArgumentException("User already reported this post.");
        }
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}
