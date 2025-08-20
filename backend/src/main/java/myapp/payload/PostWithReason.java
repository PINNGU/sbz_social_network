package myapp.payload;

import myapp.model.Post;

import java.util.Set;

public class PostWithReason {
    private Post post;
    private Set<String> reasons;

    public PostWithReason(Post post, Set<String> reasons) {
        this.post = post;
        this.reasons = reasons;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Set<String> getReasons() {
        return reasons;
    }

    public void setReasons(Set<String> reasons) {
        this.reasons = reasons;
    }
}
