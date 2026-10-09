package org.annpath.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise-grade password hashing using PBKDF2-HMAC-SHA512 with per-user cryptographic salts.
 * Eliminates external dependency issues while providing robust defense against GPU/ASIC rainbow attacks.
 */
public final class PasswordHasher {

    private static final Logger LOGGER = Logger.getLogger(PasswordHasher.class.getName());
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 512;
    private static final int SALT_BYTES = 24;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {}

    /**
     * Generate a cryptographically secure random salt encoded in Base64.
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Compute PBKDF2-HMAC-SHA512 hash encoded as a hexadecimal string.
     */
    public static String hashPassword(String password, String saltBase64) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null.");
        }
        try {
            byte[] salt = (saltBase64 != null && !saltBase64.trim().isEmpty())
                    ? Base64.getDecoder().decode(saltBase64)
                    : new byte[SALT_BYTES];
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return toHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.log(Level.SEVERE, "Failed to hash password via PBKDF2: " + e.getMessage(), e);
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * Cryptographically verify candidate password against expected hash using PBKDF2-HMAC-SHA512
     * with constant-time comparison to prevent timing side-channel attacks.
     */
    public static boolean verifyPassword(String candidatePassword, String saltBase64, String expectedHash) {
        if (candidatePassword == null || expectedHash == null) {
            return false;
        }
        try {
            if (saltBase64 != null && !saltBase64.trim().isEmpty()) {
                byte[] salt = Base64.getDecoder().decode(saltBase64);
                PBEKeySpec spec = new PBEKeySpec(candidatePassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
                SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
                byte[] hash = skf.generateSecret(spec).getEncoded();
                String computedHash = toHex(hash);
                return slowEquals(computedHash, expectedHash);
            }
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Constant-time string equality comparison to prevent timing attacks.
     */
    private static boolean slowEquals(String a, String b) {
        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();
        int diff = aBytes.length ^ bBytes.length;
        for (int i = 0; i < aBytes.length && i < bBytes.length; i++) {
            diff |= aBytes[i] ^ bBytes[i];
        }
        return diff == 0;
    }

    private static String toHex(byte[] array) {
        StringBuilder sb = new StringBuilder(array.length * 2);
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
