package org.annpath.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class VolunteerProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String firstName;
    private String lastName;
    private String addressLine1;
    private String city;
    private String stateRegion;
    private String postalCode;
    private String countryCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal maxTravelDistanceKm;
    private String vehicleType; // NONE_WALKING, BICYCLE, MOTORCYCLE, CAR, VAN, TRUCK
    private String availabilityStatus; // AVAILABLE, BUSY, OFFLINE
    private int activeAssignmentsCount;
    private int maxConcurrentAssignments;
    private String emergencyContactPhone;

    public VolunteerProfile() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStateRegion() { return stateRegion; }
    public void setStateRegion(String stateRegion) { this.stateRegion = stateRegion; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public BigDecimal getMaxTravelDistanceKm() { return maxTravelDistanceKm; }
    public void setMaxTravelDistanceKm(BigDecimal maxTravelDistanceKm) { this.maxTravelDistanceKm = maxTravelDistanceKm; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public int getActiveAssignmentsCount() { return activeAssignmentsCount; }
    public void setActiveAssignmentsCount(int activeAssignmentsCount) { this.activeAssignmentsCount = activeAssignmentsCount; }

    public int getMaxConcurrentAssignments() { return maxConcurrentAssignments; }
    public void setMaxConcurrentAssignments(int maxConcurrentAssignments) { this.maxConcurrentAssignments = maxConcurrentAssignments; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(availabilityStatus);
    }

    public void setAvailable(boolean available) {
        this.availabilityStatus = available ? "AVAILABLE" : "UNAVAILABLE";
    }

    public boolean isAvailableForDispatch() {
        return "AVAILABLE".equalsIgnoreCase(availabilityStatus) && activeAssignmentsCount < maxConcurrentAssignments;
    }
}
