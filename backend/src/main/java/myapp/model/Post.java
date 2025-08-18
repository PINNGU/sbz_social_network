package myapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    private int numberOfLikes = 0;

    @ElementCollection
    @CollectionTable(name = "post_hashtags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "hashtag")
    private List<String> hashtags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "post_reports", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "reporter_user_id")
    private List<Long> reports = new ArrayList<>(); // Store user IDs who reported the post

    @Column(nullable = false)
    private LocalDateTime dateOfCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        dateOfCreation = LocalDateTime.now();
    }
}
