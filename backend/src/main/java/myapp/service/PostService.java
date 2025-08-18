package myapp.service;

import myapp.model.Post;
import myapp.model.User;
import myapp.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

        public Post savePost(Post post) {
            return postRepository.save(post);
        }

        public java.util.List<Post> getAllPosts() {
            return postRepository.findAll();
        }

}
