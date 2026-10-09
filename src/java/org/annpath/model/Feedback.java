package org.annpath.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Feedback implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long foodListingId;
    private Long pickupId;
    private Long submitterUserId;
    private Long targetUserId;
    private int ratingStars;
    private String comment;
    private String feedbackType; // DONOR_RATING, NGO_RATING, VOLUNTEER_RATING, FOOD_QUALITY
    private Timestamp createdAt;

    // Joined Presentation Fields
    private String submitterName;
    private String targetName;

    public Feedback() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFoodListingId() { return foodListingId; }
    public void setFoodListingId(Long foodListingId) { this.foodListingId = foodListingId; }

    public Long getPickupId() { return pickupId; }
    public void setPickupId(Long pickupId) { this.pickupId = pickupId; }

    public Long getSubmitterUserId() { return submitterUserId; }
    public void setSubmitterUserId(Long submitterUserId) { this.submitterUserId = submitterUserId; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public int getRatingStars() { return ratingStars; }
    public void setRatingStars(int ratingStars) { this.ratingStars = ratingStars; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getSubmitterName() { return submitterName; }
    public void setSubmitterName(String submitterName) { this.submitterName = submitterName; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
}
