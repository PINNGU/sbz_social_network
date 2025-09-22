package myapp.payload;

import java.time.LocalDateTime;

public class UserActivity 
{
    private Long userId;
    private String activityType; // "POST_REPORTED", "USER_BLOCKED", "EXCESSIVE_POSTING", "SPAM_ACTIVITY"
    private LocalDateTime timestamp;
    private String details;
    private Long targetId; // ID of post/user involved in the activity

    public UserActivity() {}

    public UserActivity(Long userId, String activityType, LocalDateTime timestamp, String details, Long targetId) 
    {
        this.userId = userId;
        this.activityType = activityType;
        this.timestamp = timestamp;
        this.details = details;
        this.targetId = targetId;
    }

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
}