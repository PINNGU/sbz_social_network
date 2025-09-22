package myapp.payload;

import java.time.LocalDateTime;

public class Suspension 
{
    private Long userId;
    private String suspensionType; // "POST_BAN", "LOGIN_BAN", "FULL_SUSPENSION"
    private LocalDateTime suspensionStart;
    private LocalDateTime suspensionEnd;
    private String reason;
    private Integer daysBanned;

    public Suspension() {}

    public Suspension(Long userId, String suspensionType, LocalDateTime suspensionStart, 
                     LocalDateTime suspensionEnd, String reason, Integer daysBanned) {
        this.userId = userId;
        this.suspensionType = suspensionType;
        this.suspensionStart = suspensionStart;
        this.suspensionEnd = suspensionEnd;
        this.reason = reason;
        this.daysBanned = daysBanned;
    }

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getSuspensionType() { return suspensionType; }
    public void setSuspensionType(String suspensionType) { this.suspensionType = suspensionType; }

    public LocalDateTime getSuspensionStart() { return suspensionStart; }
    public void setSuspensionStart(LocalDateTime suspensionStart) { this.suspensionStart = suspensionStart; }

    public LocalDateTime getSuspensionEnd() { return suspensionEnd; }
    public void setSuspensionEnd(LocalDateTime suspensionEnd) { this.suspensionEnd = suspensionEnd; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Integer getDaysBanned() { return daysBanned; }
    public void setDaysBanned(Integer daysBanned) { this.daysBanned = daysBanned; }
}