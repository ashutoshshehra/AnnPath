package org.annpath.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class Pickup implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long foodListingId;
    private Long foodRequestId;
    private Long donorUserId;
    private Long ngoUserId;
    private String pickupMethod; // VOLUNTEER, NGO_SELF_PICKUP
    private Long volunteerUserId;
    private String selfPickupPersonName;
    private String selfPickupPersonPhone;
    private String selfPickupIdType;
    private String selfPickupIdNumber;
    private Timestamp pickupWindowStart;
    private Timestamp pickupWindowEnd;
    private String pickupOtpHash;
    private String deliveryOtpHash;
    private String pickupNotes;
    private String deliveryNotes;
    private String proofImagePath;
    private String status; // ASSIGNED, REASSIGNING, VOLUNTEER_UNAVAILABLE, NGO_SELF_PICKUP_ASSIGNED, PICKED_UP, DELIVERED, RECEIVED, DISTRIBUTED, PICKUP_FAILED, DELIVERY_FAILED, CANCELLED
    private Timestamp assignedAt;
    private Timestamp actualPickedUpAt;
    private Timestamp actualDeliveredAt;
    private Timestamp ngoReceivedAt;
    private Timestamp ngoDistributedAt;
    private int version;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Joined Presentation Fields
    private String foodTitle;
    private String foodCategory;
    private BigDecimal quantity;
    private String unitName;
    private String donorName;
    private String donorPhone;
    private String pickupAddress;
    private String ngoName;
    private String ngoPhone;
    private String ngoAddress;
    private String volunteerName;
    private String volunteerPhone;

    public Pickup() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFoodListingId() { return foodListingId; }
    public void setFoodListingId(Long foodListingId) { this.foodListingId = foodListingId; }

    public Long getFoodRequestId() { return foodRequestId; }
    public void setFoodRequestId(Long foodRequestId) { this.foodRequestId = foodRequestId; }

    public Long getDonorUserId() { return donorUserId; }
    public void setDonorUserId(Long donorUserId) { this.donorUserId = donorUserId; }

    public Long getNgoUserId() { return ngoUserId; }
    public void setNgoUserId(Long ngoUserId) { this.ngoUserId = ngoUserId; }

    public String getPickupMethod() { return pickupMethod; }
    public void setPickupMethod(String pickupMethod) { this.pickupMethod = pickupMethod; }

    public Long getVolunteerUserId() { return volunteerUserId; }
    public void setVolunteerUserId(Long volunteerUserId) { this.volunteerUserId = volunteerUserId; }

    public String getSelfPickupPersonName() { return selfPickupPersonName; }
    public void setSelfPickupPersonName(String selfPickupPersonName) { this.selfPickupPersonName = selfPickupPersonName; }

    public String getSelfPickupPersonPhone() { return selfPickupPersonPhone; }
    public void setSelfPickupPersonPhone(String selfPickupPersonPhone) { this.selfPickupPersonPhone = selfPickupPersonPhone; }

    public String getSelfPickupIdType() { return selfPickupIdType; }
    public void setSelfPickupIdType(String selfPickupIdType) { this.selfPickupIdType = selfPickupIdType; }

    public String getSelfPickupIdNumber() { return selfPickupIdNumber; }
    public void setSelfPickupIdNumber(String selfPickupIdNumber) { this.selfPickupIdNumber = selfPickupIdNumber; }

    public Timestamp getPickupWindowStart() { return pickupWindowStart; }
    public void setPickupWindowStart(Timestamp pickupWindowStart) { this.pickupWindowStart = pickupWindowStart; }

    public Timestamp getPickupWindowEnd() { return pickupWindowEnd; }
    public void setPickupWindowEnd(Timestamp pickupWindowEnd) { this.pickupWindowEnd = pickupWindowEnd; }

    public String getPickupOtpHash() { return pickupOtpHash; }
    public void setPickupOtpHash(String pickupOtpHash) { this.pickupOtpHash = pickupOtpHash; }

    public String getDeliveryOtpHash() { return deliveryOtpHash; }
    public void setDeliveryOtpHash(String deliveryOtpHash) { this.deliveryOtpHash = deliveryOtpHash; }

    public String getPickupNotes() { return pickupNotes; }
    public void setPickupNotes(String pickupNotes) { this.pickupNotes = pickupNotes; }

    public String getDeliveryNotes() { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }

    public String getProofImagePath() { return proofImagePath; }
    public void setProofImagePath(String proofImagePath) { this.proofImagePath = proofImagePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Timestamp assignedAt) { this.assignedAt = assignedAt; }

    public Timestamp getActualPickedUpAt() { return actualPickedUpAt; }
    public void setActualPickedUpAt(Timestamp actualPickedUpAt) { this.actualPickedUpAt = actualPickedUpAt; }

    public Timestamp getActualDeliveredAt() { return actualDeliveredAt; }
    public void setActualDeliveredAt(Timestamp actualDeliveredAt) { this.actualDeliveredAt = actualDeliveredAt; }

    public Timestamp getNgoReceivedAt() { return ngoReceivedAt; }
    public void setNgoReceivedAt(Timestamp ngoReceivedAt) { this.ngoReceivedAt = ngoReceivedAt; }

    public Timestamp getNgoDistributedAt() { return ngoDistributedAt; }
    public void setNgoDistributedAt(Timestamp ngoDistributedAt) { this.ngoDistributedAt = ngoDistributedAt; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getFoodTitle() { return foodTitle; }
    public void setFoodTitle(String foodTitle) { this.foodTitle = foodTitle; }

    public String getFoodCategory() { return foodCategory; }
    public void setFoodCategory(String foodCategory) { this.foodCategory = foodCategory; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorPhone() { return donorPhone; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getNgoName() { return ngoName; }
    public void setNgoName(String ngoName) { this.ngoName = ngoName; }

    public String getNgoPhone() { return ngoPhone; }
    public void setNgoPhone(String ngoPhone) { this.ngoPhone = ngoPhone; }

    public String getNgoAddress() { return ngoAddress; }
    public void setNgoAddress(String ngoAddress) { this.ngoAddress = ngoAddress; }

    public String getVolunteerName() { return volunteerName; }
    public void setVolunteerName(String volunteerName) { this.volunteerName = volunteerName; }

    public String getVolunteerPhone() { return volunteerPhone; }
    public void setVolunteerPhone(String volunteerPhone) { this.volunteerPhone = volunteerPhone; }
}
