package org.annpath.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class ImpactRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long foodListingId;
    private Long pickupId;
    private Long ngoUserId;
    private Long donorUserId;
    private Long volunteerUserId;
    private BigDecimal rescuedQuantityKg;
    private int estimatedPeopleFed;
    private BigDecimal co2EmissionsPreventedKg;
    private Timestamp recordedAt;

    public ImpactRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFoodListingId() { return foodListingId; }
    public void setFoodListingId(Long foodListingId) { this.foodListingId = foodListingId; }

    public Long getPickupId() { return pickupId; }
    public void setPickupId(Long pickupId) { this.pickupId = pickupId; }

    public Long getNgoUserId() { return ngoUserId; }
    public void setNgoUserId(Long ngoUserId) { this.ngoUserId = ngoUserId; }

    public Long getDonorUserId() { return donorUserId; }
    public void setDonorUserId(Long donorUserId) { this.donorUserId = donorUserId; }

    public Long getVolunteerUserId() { return volunteerUserId; }
    public void setVolunteerUserId(Long volunteerUserId) { this.volunteerUserId = volunteerUserId; }

    public BigDecimal getRescuedQuantityKg() { return rescuedQuantityKg; }
    public void setRescuedQuantityKg(BigDecimal rescuedQuantityKg) { this.rescuedQuantityKg = rescuedQuantityKg; }

    public int getEstimatedPeopleFed() { return estimatedPeopleFed; }
    public void setEstimatedPeopleFed(int estimatedPeopleFed) { this.estimatedPeopleFed = estimatedPeopleFed; }

    public BigDecimal getCo2EmissionsPreventedKg() { return co2EmissionsPreventedKg; }
    public void setCo2EmissionsPreventedKg(BigDecimal co2EmissionsPreventedKg) { this.co2EmissionsPreventedKg = co2EmissionsPreventedKg; }

    public Timestamp getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Timestamp recordedAt) { this.recordedAt = recordedAt; }
}
