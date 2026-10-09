package org.annpath.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class DigitalRescueReceipt implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String receiptNumber;
    private Long foodListingId;
    private Long pickupId;
    private Long donorUserId;
    private Long ngoUserId;
    private Long volunteerUserId;
    private BigDecimal rescuedQuantity;
    private String unitName;
    private String foodCategoryName;
    private Timestamp pickupTimestamp;
    private Timestamp deliveryTimestamp;
    private Timestamp distributionTimestamp;
    private BigDecimal distanceCoveredKm;
    private String verificationHashSha256;
    private Timestamp issuedAt;

    // Joined Presentation Fields
    private String foodTitle;
    private String donorName;
    private String donorAddress;
    private String ngoName;
    private String ngoAddress;
    private String volunteerName;
    private int estimatedPeopleFed;
    private BigDecimal co2SavedKg;

    public DigitalRescueReceipt() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public Long getFoodListingId() { return foodListingId; }
    public void setFoodListingId(Long foodListingId) { this.foodListingId = foodListingId; }

    public Long getPickupId() { return pickupId; }
    public void setPickupId(Long pickupId) { this.pickupId = pickupId; }

    public Long getDonorUserId() { return donorUserId; }
    public void setDonorUserId(Long donorUserId) { this.donorUserId = donorUserId; }

    public Long getNgoUserId() { return ngoUserId; }
    public void setNgoUserId(Long ngoUserId) { this.ngoUserId = ngoUserId; }

    public Long getVolunteerUserId() { return volunteerUserId; }
    public void setVolunteerUserId(Long volunteerUserId) { this.volunteerUserId = volunteerUserId; }

    public BigDecimal getRescuedQuantity() { return rescuedQuantity; }
    public void setRescuedQuantity(BigDecimal rescuedQuantity) { this.rescuedQuantity = rescuedQuantity; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getFoodCategoryName() { return foodCategoryName; }
    public void setFoodCategoryName(String foodCategoryName) { this.foodCategoryName = foodCategoryName; }

    public Timestamp getPickupTimestamp() { return pickupTimestamp; }
    public void setPickupTimestamp(Timestamp pickupTimestamp) { this.pickupTimestamp = pickupTimestamp; }

    public Timestamp getDeliveryTimestamp() { return deliveryTimestamp; }
    public void setDeliveryTimestamp(Timestamp deliveryTimestamp) { this.deliveryTimestamp = deliveryTimestamp; }

    public Timestamp getDistributionTimestamp() { return distributionTimestamp; }
    public void setDistributionTimestamp(Timestamp distributionTimestamp) { this.distributionTimestamp = distributionTimestamp; }

    public BigDecimal getDistanceCoveredKm() { return distanceCoveredKm; }
    public void setDistanceCoveredKm(BigDecimal distanceCoveredKm) { this.distanceCoveredKm = distanceCoveredKm; }

    public String getVerificationHashSha256() { return verificationHashSha256; }
    public void setVerificationHashSha256(String verificationHashSha256) { this.verificationHashSha256 = verificationHashSha256; }

    public Timestamp getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Timestamp issuedAt) { this.issuedAt = issuedAt; }

    public String getFoodTitle() { return foodTitle; }
    public void setFoodTitle(String foodTitle) { this.foodTitle = foodTitle; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorAddress() { return donorAddress; }
    public void setDonorAddress(String donorAddress) { this.donorAddress = donorAddress; }

    public String getNgoName() { return ngoName; }
    public void setNgoName(String ngoName) { this.ngoName = ngoName; }

    public String getNgoAddress() { return ngoAddress; }
    public void setNgoAddress(String ngoAddress) { this.ngoAddress = ngoAddress; }

    public String getVolunteerName() { return volunteerName; }
    public void setVolunteerName(String volunteerName) { this.volunteerName = volunteerName; }

    public int getEstimatedPeopleFed() { return estimatedPeopleFed; }
    public void setEstimatedPeopleFed(int estimatedPeopleFed) { this.estimatedPeopleFed = estimatedPeopleFed; }

    public BigDecimal getCo2SavedKg() { return co2SavedKg; }
    public void setCo2SavedKg(BigDecimal co2SavedKg) { this.co2SavedKg = co2SavedKg; }

    // Convenience Alias Getters for JSON API & Serialization
    public BigDecimal getQuantityKg() { return rescuedQuantity; }
    public int getMealsProvided() { return estimatedPeopleFed; }
    public String getDigitalSignatureSha256() { return verificationHashSha256; }
    public String getVerificationUrl() { return "/receipt/view?id=" + receiptNumber; }
}
