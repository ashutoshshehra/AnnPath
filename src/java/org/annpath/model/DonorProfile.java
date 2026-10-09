package org.annpath.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class DonorProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String donorType; // INDIVIDUAL, RESTAURANT, HOTEL, SUPERMARKET, CORPORATE_CAFETERIA, EVENT_CATERER
    private String organizationName;
    private String contactPerson;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateRegion;
    private String postalCode;
    private String countryCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String defaultPickupInstructions;

    public DonorProfile() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDonorType() { return donorType; }
    public void setDonorType(String donorType) { this.donorType = donorType; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

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

    public String getDefaultPickupInstructions() { return defaultPickupInstructions; }
    public void setDefaultPickupInstructions(String defaultPickupInstructions) { this.defaultPickupInstructions = defaultPickupInstructions; }

    public String getDisplayName() {
        if (organizationName != null && !organizationName.trim().isEmpty()) {
            return organizationName;
        }
        return contactPerson;
    }
}
