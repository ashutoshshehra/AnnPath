package org.annpath.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String phoneNumber;
    private String phoneCountryCode;
    private String passwordHash;
    private String passwordSalt;
    private String role; // DONOR, NGO, VOLUNTEER
    private String status; // ACTIVE, SUSPENDED, PENDING_VERIFICATION
    private String preferredLanguage;
    private String preferredTimezone;
    private boolean emailVerified;
    private boolean phoneVerified;
    private int failedLoginAttempts;
    private Timestamp lockedUntil;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Helper display fields
    private String displayName;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return email; }
    public void setUsername(String username) { this.email = username; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPhoneCountryCode() { return phoneCountryCode; }
    public void setPhoneCountryCode(String phoneCountryCode) { this.phoneCountryCode = phoneCountryCode; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPasswordSalt() { return passwordSalt; }
    public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public String getPreferredTimezone() { return preferredTimezone; }
    public void setPreferredTimezone(String preferredTimezone) { this.preferredTimezone = preferredTimezone; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public boolean isPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(boolean phoneVerified) { this.phoneVerified = phoneVerified; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public Timestamp getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Timestamp lockedUntil) { this.lockedUntil = lockedUntil; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getDisplayName() { return displayName != null ? displayName : email; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.after(new Timestamp(System.currentTimeMillis()));
    }
}
