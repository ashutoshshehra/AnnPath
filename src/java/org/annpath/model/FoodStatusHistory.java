package org.annpath.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class FoodStatusHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long foodListingId;
    private Long pickupId;
    private String previousStatus;
    private String newStatus;
    private Long actorUserId;
    private String actorRole;
    private String transitionReason;
    private String transitionMetadataJson;
    private Timestamp createdAt;

    // Presentation field
    private String actorName;

    public FoodStatusHistory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFoodListingId() { return foodListingId; }
    public void setFoodListingId(Long foodListingId) { this.foodListingId = foodListingId; }

    public Long getPickupId() { return pickupId; }
    public void setPickupId(Long pickupId) { this.pickupId = pickupId; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public String getTransitionReason() { return transitionReason; }
    public void setTransitionReason(String transitionReason) { this.transitionReason = transitionReason; }

    public String getTransitionMetadataJson() { return transitionMetadataJson; }
    public void setTransitionMetadataJson(String transitionMetadataJson) { this.transitionMetadataJson = transitionMetadataJson; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
}
