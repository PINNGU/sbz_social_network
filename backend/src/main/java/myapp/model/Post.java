package myapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonGetter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @ElementCollection
    @CollectionTable(name = "post_likes", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "liker_user_id")
    private List<Long> likes = new ArrayList<>(); // Store user IDs who liked the post

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;


    @ElementCollection
    @CollectionTable(name = "post_hashtags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "hashtag")
    private List<String> hashtags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "post_reports", joinColumns = @JoinColumn(name = "post_id"))
    private List<PostReport> reports = new ArrayList<>(); // Store reports with timestamp

    @Column(nullable = false)
    private LocalDateTime dateOfCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        dateOfCreation = LocalDateTime.now();
    }
    
    // Helper method for backwards compatibility - checks if user has already reported
    public boolean isReportedBy(Long userId) {
        return reports.stream().anyMatch(report -> report.getReporterUserId().equals(userId));
    }
    
    // Helper method to add a new report with timestamp
    public void addReport(Long reporterUserId, LocalDateTime timestamp) {
        if (!isReportedBy(reporterUserId)) {
            reports.add(new PostReport(reporterUserId, timestamp));
        }
    }
    
    // Helper method to get list of reporter user IDs (for backwards compatibility)
    @JsonGetter("reports")
    public List<Long> getReporterIds() {
        return reports.stream().map(PostReport::getReporterUserId).toList();
    }
}
