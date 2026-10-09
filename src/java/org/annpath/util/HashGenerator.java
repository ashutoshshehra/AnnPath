package org.annpath.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * SHA-256 and Cryptographic Token generation for Receipts, Documents, and OTPs.
 */
public final class HashGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private HashGenerator() {}

    /**
     * Compute SHA-256 hash string for raw byte array (safe for binary files).
     */
    public static String sha256(byte[] bytes) {
        if (bytes == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(bytes);
            StringBuilder hexString = new StringBuilder(64);
            for (byte b : encoded) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Compute SHA-256 hash string for an input string.
     */
    public static String sha256(String input) {
        if (input == null) return null;
        return sha256(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a 6-digit numeric OTP code.
     */
    public static String generate6DigitOtp() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Generate a unique rescue receipt number (e.g. ANN-REC-2026-004821).
     */
    public static String generateReceiptNumber() {
        int year = java.time.Year.now().getValue();
        int randomSeq = 100000 + RANDOM.nextInt(900000);
        return "ANN-REC-" + year + "-" + randomSeq;
    }
}
