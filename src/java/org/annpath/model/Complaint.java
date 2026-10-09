package org.annpath.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Complaint implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long reporterUserId;
    private Long reportedUserId;
    private Long foodListingId;
    private Long pickupId;
    private String category; // HYGIENE, MISREPRESENTATION, NO_SHOW, SPOILED_FOOD, BEHAVIOR, OTHER
    private String description;
    private String status; // OPEN, UNDER_REVIEW, RESOLVED, DISMISSED
    private String resolutionNotes;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Joined Presentation Fields
    private String reporterName;
    private String reportedName;

    public Complaint() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReporterUserId() { return reporterUserId; }
    public void setReporterUserId(Long reporterUserId) { this.reporterUserId = reporterUserId; }

    public Long getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(Long reportedUserId) { this.reportedUserId = reportedUserId; }

    public Long getFoodListingId() { return foodListingId; }
    public void setFoodListingId(Long foodListingId) { this.foodListingId = foodListingId; }

    public Long getPickupId() { return pickupId; }
    public void setPickupId(Long pickupId) { this.pickupId = pickupId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReportedName() { return reportedName; }
    public void setReportedName(String reportedName) { this.reportedName = reportedName; }
}
