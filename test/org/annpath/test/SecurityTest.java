package org.annpath.test;

import org.annpath.security.InputSanitizer;
import org.annpath.security.PasswordHasher;
import org.annpath.util.HashGenerator;
import org.junit.Assert;
import org.junit.Test;

/**
 * Enterprise test suite verifying cryptographic security, password hashing,
 * XSS escaping, token generation, and input sanitation.
 */
public class SecurityTest {

    @Test
    public void testPasswordHashingAndVerification() {
        String password = "SecurePlatformPassword2026!";
        String salt = PasswordHasher.generateSalt();

        Assert.assertNotNull("Salt must not be null", salt);
        Assert.assertFalse("Salt must not be empty", salt.trim().isEmpty());

        String hash = PasswordHasher.hashPassword(password, salt);
        Assert.assertNotNull("Hash must not be null", hash);
        Assert.assertEquals("PBKDF2-SHA512 hex hash should be 128 hex characters (512 bits)", 128, hash.length());

        // Verification with correct password
        boolean valid = PasswordHasher.verifyPassword(password, salt, hash);
        Assert.assertTrue("Password verification must succeed for matching password", valid);

        // Verification with incorrect password
        boolean invalid = PasswordHasher.verifyPassword("WrongPassword123!", salt, hash);
        Assert.assertFalse("Password verification must fail for incorrect password", invalid);

        // Verification with null values
        Assert.assertFalse("Verification should fail gracefully on null password", PasswordHasher.verifyPassword(null, salt, hash));
        Assert.assertFalse("Verification should fail gracefully on null salt", PasswordHasher.verifyPassword(password, null, hash));
        Assert.assertFalse("Verification should fail gracefully on null hash", PasswordHasher.verifyPassword(password, salt, null));
    }

    @Test
    public void testSaltRandomnessAndUniqueness() {
        String salt1 = PasswordHasher.generateSalt();
        String salt2 = PasswordHasher.generateSalt();

        Assert.assertNotEquals("Consecutive salts must be cryptographically distinct", salt1, salt2);

        String password = "CommonPassword99";
        String hash1 = PasswordHasher.hashPassword(password, salt1);
        String hash2 = PasswordHasher.hashPassword(password, salt2);

        Assert.assertNotEquals("Identical passwords with different salts must yield completely different hashes", hash1, hash2);
    }

    @Test
    public void testHtmlXssEscaping() {
        String maliciousInput = "<script>alert('XSS Attack');</script>";
        String escaped = InputSanitizer.escapeHtml(maliciousInput);

        Assert.assertFalse("Escaped output must not contain raw script tag", escaped.contains("<script>"));
        Assert.assertTrue("Output must contain escaped angle brackets", escaped.contains("&lt;script&gt;"));
        Assert.assertTrue("Output must contain escaped quotes", escaped.contains("&#x27;"));

        // Quotes and ampersands
        String quoteTest = "\"Tom & Jerry\"";
        String escapedQuotes = InputSanitizer.escapeHtml(quoteTest);
        Assert.assertEquals("&quot;Tom &amp; Jerry&quot;", escapedQuotes);

        // Null safe
        Assert.assertEquals("", InputSanitizer.escapeHtml(null));
    }

    @Test
    public void testInputSanitizerEmailValidation() {
        Assert.assertTrue(InputSanitizer.isValidEmail("donor@annpath.org"));
        Assert.assertTrue(InputSanitizer.isValidEmail("verified.ngo+food@rescue-network.org"));
        Assert.assertTrue(InputSanitizer.isValidEmail("user_123@sub.domain.co.uk"));

        Assert.assertFalse(InputSanitizer.isValidEmail(null));
        Assert.assertFalse(InputSanitizer.isValidEmail(""));
        Assert.assertFalse(InputSanitizer.isValidEmail("plainaddress"));
        Assert.assertFalse(InputSanitizer.isValidEmail("missing.domain@"));
        Assert.assertFalse(InputSanitizer.isValidEmail("@missingusername.org"));
        Assert.assertFalse(InputSanitizer.isValidEmail("spaces in@email.org"));
    }

    @Test
    public void testInputSanitizerPhoneValidation() {
        Assert.assertTrue(InputSanitizer.isValidPhone("+1234567890"));
        Assert.assertTrue(InputSanitizer.isValidPhone("+91 98765 43210"));
        Assert.assertTrue(InputSanitizer.isValidPhone("123-456-7890"));

        Assert.assertFalse(InputSanitizer.isValidPhone(null));
        Assert.assertFalse(InputSanitizer.isValidPhone(""));
        Assert.assertFalse(InputSanitizer.isValidPhone("123")); // Too short (< 7)
        Assert.assertFalse(InputSanitizer.isValidPhone("phone_with_letters"));
    }

    @Test
    public void testSha256ReceiptSignatureGeneration() {
        String payload = "ANN-REC-2026-1001|DONOR-42|NGO-8|KG|250.50|2026-09-20T12:00:00Z";
        String hash1 = HashGenerator.sha256(payload);
        String hash2 = HashGenerator.sha256(payload);

        Assert.assertNotNull("SHA-256 hash must not be null", hash1);
        Assert.assertEquals("SHA-256 hex string must be exactly 64 characters", 64, hash1.length());
        Assert.assertEquals("SHA-256 must be deterministic for identical content", hash1, hash2);

        String tamperedPayload = "ANN-REC-2026-1001|DONOR-42|NGO-8|KG|999.99|2026-09-20T12:00:00Z";
        String tamperedHash = HashGenerator.sha256(tamperedPayload);
        Assert.assertNotEquals("Tampered receipt data must produce completely different signature", hash1, tamperedHash);
    }

    @Test
    public void testOtpAndReceiptNumberGeneration() {
        for (int i = 0; i < 20; i++) {
            String otp = HashGenerator.generate6DigitOtp();
            Assert.assertNotNull(otp);
            Assert.assertEquals("OTP must be exactly 6 digits", 6, otp.length());
            int val = Integer.parseInt(otp);
            Assert.assertTrue("OTP must be between 100000 and 999999", val >= 100000 && val <= 999999);

            String receiptNum = HashGenerator.generateReceiptNumber();
            Assert.assertNotNull(receiptNum);
            Assert.assertTrue("Receipt number must start with ANN-REC-", receiptNum.startsWith("ANN-REC-"));
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("RUNNING SECURITY TEST SUITE");
        System.out.println("=================================================");
        SecurityTest test = new SecurityTest();
        int passed = 0;
        int total = 6;

        try {
            System.out.print("[1/6] Testing Password Hashing & PBKDF2 Verification... ");
            test.testPasswordHashingAndVerification();
            System.out.println("PASSED");
            passed++;

            System.out.print("[2/6] Testing Salt Randomness & Defense... ");
            test.testSaltRandomnessAndUniqueness();
            System.out.println("PASSED");
            passed++;

            System.out.print("[3/6] Testing HTML XSS Escaping... ");
            test.testHtmlXssEscaping();
            System.out.println("PASSED");
            passed++;

            System.out.print("[4/6] Testing Email and Phone Sanitization... ");
            test.testInputSanitizerEmailValidation();
            test.testInputSanitizerPhoneValidation();
            System.out.println("PASSED");
            passed++;

            System.out.print("[5/6] Testing SHA-256 Digital Signatures... ");
            test.testSha256ReceiptSignatureGeneration();
            System.out.println("PASSED");
            passed++;

            System.out.print("[6/6] Testing OTP & Receipt Generator... ");
            test.testOtpAndReceiptNumberGeneration();
            System.out.println("PASSED");
            passed++;

            System.out.println("-------------------------------------------------");
            System.out.println("SECURITY TEST SUITE: " + passed + "/" + total + " PASSED (100% SUCCESS)");
            System.out.println("=================================================\n");
        } catch (Throwable t) {
            System.err.println("FAILED: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }
}
