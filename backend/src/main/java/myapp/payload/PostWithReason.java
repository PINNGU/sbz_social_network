package myapp.payload;

import myapp.model.Post;

public class PostWithReason {
    private Post post;
    private String reason;

    public PostWithReason(Post post, String reason) {
        this.post = post;
        this.reason = reason;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
