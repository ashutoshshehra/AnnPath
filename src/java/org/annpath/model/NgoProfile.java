package org.annpath.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class NgoProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String organizationName;
    private String registrationNumber;
    private String registrationAuthority;
    private int establishedYear;
    private String representativeName;
    private String representativeDesignation;
    private String website;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateRegion;
    private String postalCode;
    private String countryCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal serviceRadiusKm;
    private String verificationStatus; // PENDING_VERIFICATION, VERIFIED, REJECTED, SUSPENDED
    private Timestamp verifiedAt;
    private String rejectionReason;
    private BigDecimal maxDailyCapacityKg;

    public NgoProfile() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getRegistrationAuthority() { return registrationAuthority; }
    public void setRegistrationAuthority(String registrationAuthority) { this.registrationAuthority = registrationAuthority; }

    public int getEstablishedYear() { return establishedYear; }
    public void setEstablishedYear(int establishedYear) { this.establishedYear = establishedYear; }

    public String getRepresentativeName() { return representativeName; }
    public void setRepresentativeName(String representativeName) { this.representativeName = representativeName; }

    public String getRepresentativeDesignation() { return representativeDesignation; }
    public void setRepresentativeDesignation(String representativeDesignation) { this.representativeDesignation = representativeDesignation; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

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

    public BigDecimal getServiceRadiusKm() { return serviceRadiusKm; }
    public void setServiceRadiusKm(BigDecimal serviceRadiusKm) { this.serviceRadiusKm = serviceRadiusKm; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public Timestamp getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Timestamp verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public BigDecimal getMaxDailyCapacityKg() { return maxDailyCapacityKg; }
    public void setMaxDailyCapacityKg(BigDecimal maxDailyCapacityKg) { this.maxDailyCapacityKg = maxDailyCapacityKg; }

    public boolean isVerified() {
        return "VERIFIED".equalsIgnoreCase(verificationStatus);
    }
}
