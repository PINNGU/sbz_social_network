package myapp.payload;

import myapp.model.User;
import java.time.LocalDateTime;
import java.util.List;

public class BadUserDetection {
    private User user;
    private List<UserActivity> activities;
    private boolean isSuspicious;
    private String suspicionReason;
    private LocalDateTime detectionTime;
    private int reportCount;
    private int blockCount;
    private int postCount;

    public BadUserDetection() {}

    public BadUserDetection(User user, List<UserActivity> activities) 
    {
        this.user = user;
        this.activities = activities;
        this.isSuspicious = false;
        this.detectionTime = LocalDateTime.now();
    }

    // Getters and setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<UserActivity> getActivities() { return activities; }
    public void setActivities(List<UserActivity> activities) { this.activities = activities; }

    public boolean isSuspicious() { return isSuspicious; }
    public void setSuspicious(boolean suspicious) { isSuspicious = suspicious; }

    public String getSuspicionReason() { return suspicionReason; }
    public void setSuspicionReason(String suspicionReason) { this.suspicionReason = suspicionReason; }

    public LocalDateTime getDetectionTime() { return detectionTime; }
    public void setDetectionTime(LocalDateTime detectionTime) { this.detectionTime = detectionTime; }

    public int getReportCount() { return reportCount; }
    public void setReportCount(int reportCount) { this.reportCount = reportCount; }

    public int getBlockCount() { return blockCount; }
    public void setBlockCount(int blockCount) { this.blockCount = blockCount; }

    public int getPostCount() { return postCount; }
    public void setPostCount(int postCount) { this.postCount = postCount; }
}