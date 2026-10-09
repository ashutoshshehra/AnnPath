package org.annpath.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class OtpVerification implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String identifier; // email or phone
    private String channel; // EMAIL, SMS
    private String purpose; // REGISTRATION, LOGIN, PASSWORD_RESET, PICKUP_CONFIRMATION, DELIVERY_CONFIRMATION
    private String otpHash;
    private String otpSalt;
    private int attemptsCount;
    private int maxAttempts;
    private Timestamp expiresAt;
    private Timestamp verifiedAt;
    private Timestamp cooldownUntil;
    private boolean invalidated;

    public OtpVerification() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getOtpHash() { return otpHash; }
    public void setOtpHash(String otpHash) { this.otpHash = otpHash; }

    public String getOtpSalt() { return otpSalt; }
    public void setOtpSalt(String otpSalt) { this.otpSalt = otpSalt; }

    public int getAttemptsCount() { return attemptsCount; }
    public void setAttemptsCount(int attemptsCount) { this.attemptsCount = attemptsCount; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }

    public Timestamp getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Timestamp verifiedAt) { this.verifiedAt = verifiedAt; }

    public Timestamp getCooldownUntil() { return cooldownUntil; }
    public void setCooldownUntil(Timestamp cooldownUntil) { this.cooldownUntil = cooldownUntil; }

    public boolean isInvalidated() { return invalidated; }
    public void setInvalidated(boolean invalidated) { this.invalidated = invalidated; }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.before(new Timestamp(System.currentTimeMillis()));
    }

    public boolean isCooldownActive() {
        return cooldownUntil != null && cooldownUntil.after(new Timestamp(System.currentTimeMillis()));
    }
}
