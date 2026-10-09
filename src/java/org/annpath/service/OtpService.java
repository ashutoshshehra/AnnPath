package org.annpath.service;

import org.annpath.config.AppConfig;
import org.annpath.dao.OtpDao;
import org.annpath.exception.ValidationException;
import org.annpath.model.OtpVerification;
import org.annpath.security.PasswordHasher;
import org.annpath.util.HashGenerator;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Logger;

public class OtpService {

    private static final Logger LOGGER = Logger.getLogger(OtpService.class.getName());
    private final OtpDao otpDao = new OtpDao();
    private final NotificationGatewayService notificationGateway = new NotificationGatewayService();

    /**
     * Generate and persist a new 6-digit OTP. Returns the plain OTP string for delivery.
     */
    public String generateAndSendOtp(Long userId, String identifier, String channel, String purpose) throws ValidationException, SQLException {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new ValidationException("Identifier is required for OTP generation.");
        }

        // Check if an existing valid OTP is in active cooldown
        OtpVerification existing = otpDao.findLatestValid(identifier, purpose);
        if (existing != null && existing.isCooldownActive()) {
            long remainingSecs = (existing.getCooldownUntil().getTime() - System.currentTimeMillis()) / 1000L;
            throw new ValidationException("Please wait " + Math.max(1, remainingSecs) + " seconds before requesting a new OTP.");
        }

        String plainOtp = HashGenerator.generate6DigitOtp();
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(plainOtp, salt);

        int expiryMinutes = AppConfig.getInt("otp.expiryMinutes", 10);
        int cooldownSeconds = AppConfig.getInt("otp.resendCooldownSeconds", 60);

        long now = System.currentTimeMillis();
        Timestamp expiresAt = new Timestamp(now + (expiryMinutes * 60L * 1000L));
        Timestamp cooldownUntil = new Timestamp(now + (cooldownSeconds * 1000L));

        OtpVerification otp = new OtpVerification();
        otp.setUserId(userId);
        otp.setIdentifier(identifier);
        otp.setChannel(channel);
        otp.setPurpose(purpose);
        otp.setOtpHash(hash);
        otp.setOtpSalt(salt);
        otp.setMaxAttempts(AppConfig.getInt("otp.maxAttempts", 3));
        otp.setExpiresAt(expiresAt);
        otp.setCooldownUntil(cooldownUntil);

        // Invalidate any previously active OTPs for this identifier and purpose
        otpDao.invalidatePreviousOtps(identifier, purpose);

        otpDao.save(otp);

        LOGGER.info("[OTP GENERATED] Verification code generated for " + identifier + " (" + purpose + ") - Expires at: " + expiresAt);

        // Dispatch via Notification Gateway API (Email SMTP or SMS Gateway)
        if ("EMAIL".equalsIgnoreCase(channel) || identifier.contains("@")) {
            notificationGateway.sendOtpEmailAsync(identifier, plainOtp, purpose);
        } else {
            notificationGateway.sendOtpSmsAsync(identifier, plainOtp, purpose);
        }

        return plainOtp;
    }

    /**
     * Normalize phone numbers to standard format (e.g., +919876543210).
     */
    public static String normalizePhoneIdentifier(String phone, String phoneCountryCode) {
        if (phone == null) return "";
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return "";
        String prefix = (phoneCountryCode != null && !phoneCountryCode.trim().isEmpty()) ? phoneCountryCode.replaceAll("[^0-9]", "") : "91";
        if (digits.length() == 10) {
            return "+" + prefix + digits;
        }
        return "+" + digits;
    }

    /**
     * Find latest valid OTP for phone number handling multiple formatting variants.
     */
    public OtpVerification findLatestValidPhone(String phone, String phoneCountryCode, String purpose) throws SQLException {
        if (phone == null || phone.trim().isEmpty()) return null;
        String normalized = normalizePhoneIdentifier(phone, phoneCountryCode);
        OtpVerification otp = otpDao.findLatestValid(normalized, purpose);
        if (otp != null) return otp;

        otp = otpDao.findLatestValid(phone.trim(), purpose);
        if (otp != null) return otp;

        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() >= 10) {
            String last10 = digits.substring(digits.length() - 10);
            otp = otpDao.findLatestValid(last10, purpose);
            if (otp != null) return otp;
            otp = otpDao.findLatestValid("+91" + last10, purpose);
            if (otp != null) return otp;
        }
        return null;
    }

    /**
     * Generate and dispatch 6-digit Email OTP for account registration.
     */
    public String generateAndSendRegistrationOtp(String email) throws ValidationException, SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email address is required for verification.");
        }

        int expiryMinutes = AppConfig.getInt("otp.expiryMinutes", 10);
        int cooldownSeconds = AppConfig.getInt("otp.resendCooldownSeconds", 60);

        long now = System.currentTimeMillis();
        Timestamp expiresAt = new Timestamp(now + (expiryMinutes * 60L * 1000L));
        Timestamp cooldownUntil = new Timestamp(now + (cooldownSeconds * 1000L));

        String cleanEmail = email.trim().toLowerCase();

        // Check if an existing valid registration OTP is in active cooldown
        OtpVerification existing = otpDao.findLatestValid(cleanEmail, "REGISTRATION");
        if (existing != null && existing.isCooldownActive()) {
            long remainingSecs = (existing.getCooldownUntil().getTime() - System.currentTimeMillis()) / 1000L;
            throw new ValidationException("Please wait " + Math.max(1, remainingSecs) + " seconds before requesting a new OTP.");
        }

        // Invalidate previous registration OTPs for this email
        otpDao.invalidatePreviousOtps(cleanEmail, "REGISTRATION");

        // Generate & Persist Distinct OTP for Gmail / Email
        String emailOtp = HashGenerator.generate6DigitOtp();
        String emailSalt = PasswordHasher.generateSalt();
        String emailHash = PasswordHasher.hashPassword(emailOtp, emailSalt);

        OtpVerification otpEmail = new OtpVerification();
        otpEmail.setUserId(null);
        otpEmail.setIdentifier(cleanEmail);
        otpEmail.setChannel("EMAIL");
        otpEmail.setPurpose("REGISTRATION");
        otpEmail.setOtpHash(emailHash);
        otpEmail.setOtpSalt(emailSalt);
        otpEmail.setMaxAttempts(AppConfig.getInt("otp.maxAttempts", 3));
        otpEmail.setExpiresAt(expiresAt);
        otpEmail.setCooldownUntil(cooldownUntil);
        otpDao.save(otpEmail);

        LOGGER.info("[EMAIL OTP GENERATED] Verification code generated for Email: " + cleanEmail);
        notificationGateway.sendOtpEmailAsync(cleanEmail, emailOtp, "REGISTRATION");

        return emailOtp;
    }

    /**
     * Backward-compatible registration OTP dispatcher (Email verification only).
     */
    public String[] generateAndSendRegistrationOtp(String email, String phone, String phoneCountryCode) throws ValidationException, SQLException {
        String emailOtp = generateAndSendRegistrationOtp(email);
        return new String[]{emailOtp, null};
    }

    /**
     * Verify both Email OTP and Phone OTP independently for registration completion.
     */
    public boolean verifyDualRegistrationOtp(String email, String phone, String phoneCountryCode, String candidateEmailOtp, String candidatePhoneOtp) throws ValidationException, SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email address is required.");
        }
        if (candidateEmailOtp == null || candidateEmailOtp.trim().isEmpty()) {
            throw new ValidationException("Please enter the 6-digit Gmail / Email OTP code.");
        }

        boolean hasPhone = (phone != null && !phone.trim().isEmpty() && candidatePhoneOtp != null && !candidatePhoneOtp.trim().isEmpty());

        String cleanEmail = email.trim().toLowerCase();
        OtpVerification otpEmail = otpDao.findLatestValid(cleanEmail, "REGISTRATION");
        if (otpEmail == null) {
            throw new ValidationException("No valid Email OTP found or it has expired. Please click Resend Code.");
        }
        if (otpEmail.isExpired()) {
            otpDao.invalidatePreviousOtps(cleanEmail, "REGISTRATION");
            throw new ValidationException("Email OTP has expired. Please click Resend Code.");
        }
        if (otpEmail.getAttemptsCount() >= otpEmail.getMaxAttempts()) {
            otpDao.invalidatePreviousOtps(cleanEmail, "REGISTRATION");
            throw new ValidationException("Maximum verification attempts reached for Email OTP. Please click Resend Code.");
        }

        OtpVerification otpPhone = null;
        if (hasPhone) {
            otpPhone = findLatestValidPhone(phone, phoneCountryCode, "REGISTRATION");
            if (otpPhone == null) {
                throw new ValidationException("No valid Mobile Phone SMS OTP found or it has expired. Please click Resend Code.");
            }
            if (otpPhone.isExpired()) {
                String fullPhone = normalizePhoneIdentifier(phone, phoneCountryCode);
                otpDao.invalidatePreviousOtps(fullPhone, "REGISTRATION");
                throw new ValidationException("Mobile Phone SMS OTP has expired. Please click Resend Code.");
            }
            if (otpPhone.getAttemptsCount() >= otpPhone.getMaxAttempts()) {
                String fullPhone = normalizePhoneIdentifier(phone, phoneCountryCode);
                otpDao.invalidatePreviousOtps(fullPhone, "REGISTRATION");
                throw new ValidationException("Maximum verification attempts reached for Mobile SMS OTP. Please click Resend Code.");
            }
        }

        // Verify Email OTP Hash
        boolean emailMatches = PasswordHasher.verifyPassword(candidateEmailOtp.trim(), otpEmail.getOtpSalt(), otpEmail.getOtpHash());
        if (!emailMatches) {
            otpDao.incrementAttempts(otpEmail.getId());
            int remaining = otpEmail.getMaxAttempts() - (otpEmail.getAttemptsCount() + 1);
            if (remaining > 0) {
                throw new ValidationException("Invalid Gmail / Email OTP code. " + remaining + " attempts remaining.");
            } else {
                otpDao.invalidatePreviousOtps(cleanEmail, "REGISTRATION");
                throw new ValidationException("Invalid Gmail / Email OTP code. Maximum attempts reached. Please click Resend Code.");
            }
        }

        // Verify Phone OTP Hash if phone is present
        if (hasPhone && otpPhone != null) {
            boolean phoneMatches = PasswordHasher.verifyPassword(candidatePhoneOtp.trim(), otpPhone.getOtpSalt(), otpPhone.getOtpHash());
            if (!phoneMatches) {
                otpDao.incrementAttempts(otpPhone.getId());
                int remaining = otpPhone.getMaxAttempts() - (otpPhone.getAttemptsCount() + 1);
                if (remaining > 0) {
                    throw new ValidationException("Invalid Mobile Phone SMS OTP code. " + remaining + " attempts remaining.");
                } else {
                    String fullPhone = normalizePhoneIdentifier(phone, phoneCountryCode);
                    otpDao.invalidatePreviousOtps(fullPhone, "REGISTRATION");
                    throw new ValidationException("Invalid Mobile SMS OTP code. Maximum attempts reached. Please click Resend Code.");
                }
            }
        }

        // Both OTPs verified successfully! Mark both verified in database
        otpDao.markVerified(otpEmail.getId());
        if (hasPhone && otpPhone != null) {
            otpDao.markVerified(otpPhone.getId());
        }

        LOGGER.info("[DUAL OTP VERIFIED] Successfully verified distinct Email and Phone OTPs for: " + cleanEmail);
        return true;
    }

    /**
     * Validate candidate phone OTP.
     */
    public boolean verifyPhoneOtp(String phone, String purpose, String candidateOtp) throws ValidationException, SQLException {
        if (candidateOtp == null || candidateOtp.trim().isEmpty()) {
            throw new ValidationException("Please enter the 6-digit Mobile SMS OTP code.");
        }
        OtpVerification otp = findLatestValidPhone(phone, null, purpose);
        if (otp == null) {
            throw new ValidationException("No valid Mobile SMS OTP found or code expired.");
        }
        if (otp.isExpired()) {
            throw new ValidationException("Mobile SMS OTP has expired.");
        }
        if (otp.getAttemptsCount() >= otp.getMaxAttempts()) {
            throw new ValidationException("Maximum verification attempts reached for Mobile SMS OTP.");
        }
        otpDao.incrementAttempts(otp.getId());
        boolean matches = PasswordHasher.verifyPassword(candidateOtp.trim(), otp.getOtpSalt(), otp.getOtpHash());
        if (!matches) {
            throw new ValidationException("Invalid Mobile SMS OTP code.");
        }
        otpDao.markVerified(otp.getId());
        return true;
    }

    /**
     * Validate candidate OTP against stored hash.
     */
    public boolean verifyOtp(String identifier, String purpose, String candidateOtp) throws ValidationException, SQLException {
        if (candidateOtp == null || candidateOtp.trim().isEmpty()) {
            throw new ValidationException("Please enter the 6-digit OTP code.");
        }

        OtpVerification otp = otpDao.findLatestValid(identifier, purpose);
        if (otp == null) {
            throw new ValidationException("No valid OTP found or OTP has expired. Please request a new code.");
        }

        if (otp.isExpired()) {
            otpDao.invalidatePreviousOtps(identifier, purpose);
            throw new ValidationException("OTP has expired. Please request a new one.");
        }

        if (otp.getAttemptsCount() >= otp.getMaxAttempts()) {
            otpDao.invalidatePreviousOtps(identifier, purpose);
            throw new ValidationException("Maximum verification attempts exceeded. Please request a new OTP.");
        }

        // Increment attempt count
        otpDao.incrementAttempts(otp.getId());

        boolean matches = PasswordHasher.verifyPassword(candidateOtp.trim(), otp.getOtpSalt(), otp.getOtpHash());
        if (!matches) {
            int remaining = otp.getMaxAttempts() - (otp.getAttemptsCount() + 1);
            if (remaining > 0) {
                throw new ValidationException("Invalid OTP code. " + remaining + " attempts remaining.");
            } else {
                otpDao.invalidatePreviousOtps(identifier, purpose);
                throw new ValidationException("Invalid OTP code. Maximum attempts reached. Please request a new OTP.");
            }
        }

        // Mark verified
        otpDao.markVerified(otp.getId());
        return true;
    }
}
