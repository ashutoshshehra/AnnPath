package org.annpath.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class FoodListing implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String rescueCode;
    private Long donorUserId;
    private String title;
    private Integer foodCategoryId;
    private String dietaryType; // VEGETARIAN, VEGAN, NON_VEGETARIAN, HALAL, KOSHER, GLUTEN_FREE, DAIRY_FREE
    private BigDecimal totalQuantity;
    private BigDecimal remainingQuantity;
    private BigDecimal reservedQuantity;
    private Integer unitId;
    private String storageCondition; // ROOM_TEMP, REFRIGERATED, FROZEN, WARM_HEATED
    private String packagingType;
    private String description;
    private String ingredients;
    private String allergens;
    private Timestamp preparationTime;
    private Timestamp availableFrom;
    private Timestamp availableUntil;
    private String urgencyStatus; // FRESH, EXPIRING_SOON, CRITICAL, EXPIRED
    private String listingStatus; // DONATED, MATCHED, REQUESTED, APPROVED, VOLUNTEER_ASSIGNED, NGO_SELF_PICKUP_ASSIGNED, PICKED_UP, DELIVERED, RECEIVED, DISTRIBUTED, REJECTED, CANCELLED, EXPIRED
    private String pickupAddress;
    private String pickupCity;
    private String pickupRegion;
    private String pickupPostalCode;
    private String pickupCountryCode;
    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;
    private String pickupInstructions;
    private String contactName;
    private String contactPhone;
    private String foodImagePath;
    private BigDecimal priorityScore;
    private String priorityExplanation;
    private int version;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Joined Presentation Fields
    private String categoryName;
    private String unitName;
    private String donorName;
    private String donorType;
    private Double distanceKm;

    public FoodListing() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRescueCode() { return rescueCode; }
    public void setRescueCode(String rescueCode) { this.rescueCode = rescueCode; }

    public String getFormattedRescueId() {
        if (rescueCode != null && !rescueCode.trim().isEmpty()) {
            return rescueCode;
        }
        if (id != null) {
            int year = (createdAt != null) ? (createdAt.toLocalDateTime().getYear()) : 2026;
            return String.format("ANP-%d-%06d", year, id);
        }
        return "ANP-PENDING";
    }

    public Long getDonorUserId() { return donorUserId; }
    public void setDonorUserId(Long donorUserId) { this.donorUserId = donorUserId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getFoodCategoryId() { return foodCategoryId; }
    public void setFoodCategoryId(Integer foodCategoryId) { this.foodCategoryId = foodCategoryId; }

    public String getDietaryType() { return dietaryType; }
    public void setDietaryType(String dietaryType) { this.dietaryType = dietaryType; }

    public BigDecimal getTotalQuantity() { return totalQuantity; }
    public BigDecimal getQuantity() { return totalQuantity; }
    public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }

    public BigDecimal getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(BigDecimal remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public BigDecimal getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(BigDecimal reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public Integer getUnitId() { return unitId; }
    public void setUnitId(Integer unitId) { this.unitId = unitId; }

    public String getStorageCondition() { return storageCondition; }
    public void setStorageCondition(String storageCondition) { this.storageCondition = storageCondition; }

    public String getPackagingType() { return packagingType; }
    public void setPackagingType(String packagingType) { this.packagingType = packagingType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getAllergens() { return allergens; }
    public void setAllergens(String allergens) { this.allergens = allergens; }

    public Timestamp getPreparationTime() { return preparationTime; }
    public void setPreparationTime(Timestamp preparationTime) { this.preparationTime = preparationTime; }

    public Timestamp getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(Timestamp availableFrom) { this.availableFrom = availableFrom; }

    public Timestamp getAvailableUntil() { return availableUntil; }
    public void setAvailableUntil(Timestamp availableUntil) { this.availableUntil = availableUntil; }

    public String getUrgencyStatus() { return urgencyStatus; }
    public void setUrgencyStatus(String urgencyStatus) { this.urgencyStatus = urgencyStatus; }

    public String getListingStatus() { return listingStatus; }
    public void setListingStatus(String listingStatus) { this.listingStatus = listingStatus; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getPickupCity() { return pickupCity; }
    public void setPickupCity(String pickupCity) { this.pickupCity = pickupCity; }

    public String getPickupRegion() { return pickupRegion; }
    public void setPickupRegion(String pickupRegion) { this.pickupRegion = pickupRegion; }

    public String getPickupPostalCode() { return pickupPostalCode; }
    public void setPickupPostalCode(String pickupPostalCode) { this.pickupPostalCode = pickupPostalCode; }

    public String getPickupCountryCode() { return pickupCountryCode; }
    public void setPickupCountryCode(String pickupCountryCode) { this.pickupCountryCode = pickupCountryCode; }

    public BigDecimal getPickupLatitude() { return pickupLatitude; }
    public void setPickupLatitude(BigDecimal pickupLatitude) { this.pickupLatitude = pickupLatitude; }

    public BigDecimal getPickupLongitude() { return pickupLongitude; }
    public void setPickupLongitude(BigDecimal pickupLongitude) { this.pickupLongitude = pickupLongitude; }

    public String getPickupInstructions() { return pickupInstructions; }
    public void setPickupInstructions(String pickupInstructions) { this.pickupInstructions = pickupInstructions; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getFoodImagePath() { return foodImagePath; }
    public void setFoodImagePath(String foodImagePath) { this.foodImagePath = foodImagePath; }

    public BigDecimal getPriorityScore() { return priorityScore; }
    public void setPriorityScore(BigDecimal priorityScore) { this.priorityScore = priorityScore; }

    public String getPriorityExplanation() { return priorityExplanation; }
    public void setPriorityExplanation(String priorityExplanation) { this.priorityExplanation = priorityExplanation; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorType() { return donorType; }
    public void setDonorType(String donorType) { this.donorType = donorType; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public boolean isRequestable() {
        return !"EXPIRED".equalsIgnoreCase(urgencyStatus)
                && !"EXPIRED".equalsIgnoreCase(listingStatus)
                && !"CANCELLED".equalsIgnoreCase(listingStatus)
                && remainingQuantity != null && remainingQuantity.compareTo(BigDecimal.ZERO) > 0;
    }
}
