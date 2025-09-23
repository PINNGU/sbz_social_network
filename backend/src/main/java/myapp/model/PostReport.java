package myapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Embeddable
public class PostReport {
    
    @Column(name = "reporter_user_id")
    private Long reporterUserId;
    
    @Column(name = "report_timestamp")
    private LocalDateTime reportTimestamp;
    
    public PostReport() {}
    
    public PostReport(Long reporterUserId, LocalDateTime reportTimestamp) {
        this.reporterUserId = reporterUserId;
        this.reportTimestamp = reportTimestamp != null ? reportTimestamp : LocalDateTime.now();
    }
    
    @PrePersist
    @PreUpdate
    protected void ensureTimestamp() {
        if (this.reportTimestamp == null) {
            this.reportTimestamp = LocalDateTime.now();
        }
    }
    
    // Getters and setters
    public Long getReporterUserId() {
        return reporterUserId;
    }
    
    public void setReporterUserId(Long reporterUserId) {
        this.reporterUserId = reporterUserId;
    }
    
    public LocalDateTime getReportTimestamp() {
        return reportTimestamp != null ? reportTimestamp : LocalDateTime.now();
    }
    
    public void setReportTimestamp(LocalDateTime reportTimestamp) {
        this.reportTimestamp = reportTimestamp != null ? reportTimestamp : LocalDateTime.now();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostReport that = (PostReport) o;
        return reporterUserId != null && reporterUserId.equals(that.reporterUserId);
    }
    
    @Override
    public int hashCode() {
        return reporterUserId != null ? reporterUserId.hashCode() : 0;
    }
}