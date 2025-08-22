

package myapp.service;

import myapp.model.Post;
import myapp.repository.PostRepository;
import myapp.repository.LikeQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeQueryRepository likeQueryRepository;


    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    public java.util.List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public java.util.List<Long> getLikerUserIdsByPostId(Long postId) {
        return likeQueryRepository.findLikerUserIdsByPostId(postId);
    }

}
