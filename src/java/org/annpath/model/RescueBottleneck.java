package org.annpath.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model representing an operational food rescue bottleneck or delay alert.
 */
public class RescueBottleneck implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String bottleneckType; // VOLUNTEER_SHORTAGE, CRITICAL_RESCUE_UNASSIGNED, NGO_CAPACITY_SHORTAGE, REQUEST_DELAY, PICKUP_DELAY, DELIVERY_DELAY
    private String severity;       // INFO, LOW, MEDIUM, HIGH, CRITICAL
    private Long foodListingId;
    private Long pickupId;
    private Long ngoUserId;
    private Long volunteerUserId;
    private String description;
    private String status;         // DETECTED, ACTIVE, RESOLVED
    private Timestamp detectedAt;
    private Timestamp resolvedAt;

    // Joined presentation fields
    private String foodTitle;
    private String ngoName;
    private String volunteerName;
    private String location;

    public RescueBottleneck() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBottleneckType() { return bottleneckType; }
    public void setBottleneckType(String bottleneckType) { this.bottleneckType = bottleneckType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Long getFoodListingId() { return foodListingId; }
    public void setFoodListingId(Long foodListingId) { this.foodListingId = foodListingId; }

    public Long getPickupId() { return pickupId; }
    public void setPickupId(Long pickupId) { this.pickupId = pickupId; }

    public Long getNgoUserId() { return ngoUserId; }
    public void setNgoUserId(Long ngoUserId) { this.ngoUserId = ngoUserId; }

    public Long getVolunteerUserId() { return volunteerUserId; }
    public void setVolunteerUserId(Long volunteerUserId) { this.volunteerUserId = volunteerUserId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Timestamp detectedAt) { this.detectedAt = detectedAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getFoodTitle() { return foodTitle; }
    public void setFoodTitle(String foodTitle) { this.foodTitle = foodTitle; }

    public String getNgoName() { return ngoName; }
    public void setNgoName(String ngoName) { this.ngoName = ngoName; }

    public String getVolunteerName() { return volunteerName; }
    public void setVolunteerName(String volunteerName) { this.volunteerName = volunteerName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
