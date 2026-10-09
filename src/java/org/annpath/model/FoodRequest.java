package org.annpath.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class FoodRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long foodListingId;
    private Long ngoUserId;
    private BigDecimal requestedQuantity;
    private Integer unitId;
    private String requestNotes;
    private String status; // REQUESTED, APPROVED, REJECTED, CANCELLED, EXPIRED
    private String donorResponseNotes;
    private Timestamp respondedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Joined Presentation Fields
    private String foodTitle;
    private String foodCategory;
    private String unitName;
    private String ngoName;
    private String ngoPhone;
    private String donorName;
    private BigDecimal remainingQuantity;
    private String urgencyStatus;

    public FoodRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFoodListingId() { return foodListingId; }
    public void setFoodListingId(Long foodListingId) { this.foodListingId = foodListingId; }

    public Long getNgoUserId() { return ngoUserId; }
    public void setNgoUserId(Long ngoUserId) { this.ngoUserId = ngoUserId; }

    public BigDecimal getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(BigDecimal requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public Integer getUnitId() { return unitId; }
    public void setUnitId(Integer unitId) { this.unitId = unitId; }

    public String getRequestNotes() { return requestNotes; }
    public void setRequestNotes(String requestNotes) { this.requestNotes = requestNotes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDonorResponseNotes() { return donorResponseNotes; }
    public void setDonorResponseNotes(String donorResponseNotes) { this.donorResponseNotes = donorResponseNotes; }

    public Timestamp getRespondedAt() { return respondedAt; }
    public void setRespondedAt(Timestamp respondedAt) { this.respondedAt = respondedAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getFoodTitle() { return foodTitle; }
    public void setFoodTitle(String foodTitle) { this.foodTitle = foodTitle; }

    public String getFoodCategory() { return foodCategory; }
    public void setFoodCategory(String foodCategory) { this.foodCategory = foodCategory; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getNgoName() { return ngoName; }
    public void setNgoName(String ngoName) { this.ngoName = ngoName; }

    public String getNgoPhone() { return ngoPhone; }
    public void setNgoPhone(String ngoPhone) { this.ngoPhone = ngoPhone; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public BigDecimal getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(BigDecimal remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public String getUrgencyStatus() { return urgencyStatus; }
    public void setUrgencyStatus(String urgencyStatus) { this.urgencyStatus = urgencyStatus; }
}
