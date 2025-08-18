package myapp.payload;

import lombok.Data;
import java.util.List;

@Data
public class CreatePostRequest {
    private String description;
    private List<String> hashtags;
    private Long userId;
}
