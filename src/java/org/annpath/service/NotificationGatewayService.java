package org.annpath.service;

import org.annpath.config.AppConfig;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise Notification Gateway Service.
 * Provides real-world API dispatch for:
 * 1. Email delivery via JavaMail SMTP (Gmail, Brevo, SendGrid, AWS SES) or HTTP API.
 * 2. SMS delivery via REST HTTP APIs (Fast2SMS, Twilio, Generic Webhook).
 * 3. Graceful asynchronous execution with simulated fallback for offline testing.
 */
public class NotificationGatewayService {

    private static final Logger LOGGER = Logger.getLogger(NotificationGatewayService.class.getName());
    private static final ExecutorService ASYNC_EXECUTOR = Executors.newFixedThreadPool(4);

    private static volatile boolean lastSmsDelivered = false;
    private static volatile String lastSmsNotice = null;
    private static volatile boolean lastEmailDelivered = false;
    private static volatile String lastEmailNotice = null;

    static {
        try {
            System.setProperty("java.net.preferIPv4Stack", "true");
        } catch (Exception ignore) {}
    }

    public static boolean isLastSmsDelivered() {
        return lastSmsDelivered;
    }

    public static String getLastSmsNotice() {
        return lastSmsNotice;
    }

    public static boolean isLastEmailDelivered() {
        return lastEmailDelivered;
    }

    public static String getLastEmailNotice() {
        return lastEmailNotice;
    }

    /**
     * Send OTP to user's registered email address asynchronously.
     */
    public void sendOtpEmailAsync(String toEmail, String otpCode, String purpose) {
        if (toEmail == null || toEmail.trim().isEmpty()) return;
        ASYNC_EXECUTOR.submit(() -> {
            try {
                sendOtpEmail(toEmail.trim(), otpCode, purpose);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to dispatch email OTP to " + toEmail + ": " + e.getMessage());
            }
        });
    }

    /**
     * Send OTP to user's registered phone number asynchronously via SMS API.
     */
    public void sendOtpSmsAsync(String phoneNumber, String otpCode, String purpose) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) return;
        ASYNC_EXECUTOR.submit(() -> {
            try {
                sendOtpSms(phoneNumber.trim(), otpCode, purpose);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to dispatch SMS OTP to " + phoneNumber + ": " + e.getMessage());
            }
        });
    }

    /**
     * Check if valid SMTP credentials (username and password) are configured.
     */
    public static boolean isEmailSmtpConfigured() {
        String username = AppConfig.getEmailUsername();
        String password = AppConfig.getEmailPassword();
        return AppConfig.isEmailEnabled() 
                && username != null && !username.trim().isEmpty() 
                && password != null && !password.trim().isEmpty();
    }

    /**
     * Synchronous Email Sender.
     * Supports EMAIL_HOST, EMAIL_PORT, EMAIL_USERNAME, EMAIL_PASSWORD, EMAIL_FROM, EMAIL_USE_TLS
     * with automatic multi-port failover (587 STARTTLS <-> 465 SSL) and IPv4 priority.
     */
    public boolean sendOtpEmail(String toEmail, String otpCode, String purpose) {
        try {
            System.setProperty("java.net.preferIPv4Stack", "true");
        } catch (Exception ignore) {}

        String host = AppConfig.getEmailHost();
        String port = AppConfig.getEmailPort();
        String username = AppConfig.getEmailUsername();
        String password = AppConfig.getEmailPassword();
        if (password != null) {
            password = password.replace(" ", "");
        }

        String fromAddress = AppConfig.getEmailFrom();
        if (fromAddress == null || fromAddress.trim().isEmpty()) {
            fromAddress = (username != null && !username.trim().isEmpty()) ? username : "noreply@annpath.org";
        }

        boolean useTls = AppConfig.getEmailUseTls();

        if (!isEmailSmtpConfigured()) {
            LOGGER.info("[EMAIL NOTIFICATION HELPER ACTIVE] To: " + toEmail + " | Purpose: " + purpose);
            return false;
        }

        boolean primaryIs465 = "465".equals(port);
        String fallbackPort = primaryIs465 ? "587" : "465";

        // Try primary port
        try {
            boolean sent = dispatchEmailAttempt(host, port, primaryIs465, useTls, username, password, fromAddress, toEmail, otpCode, purpose);
            if (sent) {
                lastEmailDelivered = true;
                lastEmailNotice = "Verification code delivered to " + toEmail;
                return true;
            }
        } catch (javax.mail.AuthenticationFailedException afe) {
            lastEmailDelivered = false;
            lastEmailNotice = "SMTP Authentication failed: " + afe.getMessage();
            LOGGER.warning("[EMAIL AUTH FAILED] SMTP authentication rejected for " + username + 
                ". App Password may need regeneration: " + afe.getMessage());
            return false;
        } catch (Exception ePrimary) {
            LOGGER.log(Level.WARNING, "Primary SMTP dispatch attempt on port " + port + " failed (" + ePrimary.getMessage() + "). Attempting auto-failover to port " + fallbackPort + "...");
            try {
                boolean sent = dispatchEmailAttempt(host, fallbackPort, !primaryIs465, useTls, username, password, fromAddress, toEmail, otpCode, purpose);
                if (sent) {
                    lastEmailDelivered = true;
                    lastEmailNotice = "Verification code delivered via fallback port " + fallbackPort + " to " + toEmail;
                    LOGGER.info("[REAL EMAIL API DISPATCHED VIA FAILOVER PORT " + fallbackPort + "] Successfully sent OTP to " + toEmail);
                    return true;
                }
            } catch (Exception eFallback) {
                lastEmailDelivered = false;
                lastEmailNotice = "Email dispatch failed on both ports (" + port + ", " + fallbackPort + "): " + eFallback.getMessage();
                LOGGER.log(Level.WARNING, "Both SMTP attempts failed to " + toEmail + ": " + eFallback.getMessage());
                return false;
            }
        }

        return false;
    }

    private boolean dispatchEmailAttempt(String host, String port, boolean isSsl, boolean useTls,
                                        String username, String password, String fromAddress,
                                        String toEmail, String otpCode, String purpose) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");

        if (isSsl || "465".equals(port)) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        }

        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        props.put("mail.smtp.writetimeout", "15000");

        final String finalUser = username.trim();
        final String finalPass = password.trim();
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(finalUser, finalPass);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromAddress, "AnnPath Platform"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Your AnnPath Email Verification Code: " + otpCode);

        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; border: 1px solid #176B4D; border-radius: 8px; padding: 20px;'>"
                + "<div style='text-align: center; margin-bottom: 20px;'>"
                + "<h2 style='color: #176B4D; margin: 0;'>AnnPath — Surplus to Need</h2>"
                + "<p style='color: #666; font-size: 14px;'>Global Food Surplus Redistribution Platform</p>"
                + "</div>"
                + "<p>Hello,</p>"
                + "<p>Your 6-digit <strong>Gmail / Email Verification Code</strong> for <strong>" + purpose + "</strong> is:</p>"
                + "<div style='text-align: center; margin: 25px 0;'>"
                + "<span style='font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #176B4D; background: #E8F3EE; padding: 10px 24px; border-radius: 6px; display: inline-block;'>"
                + otpCode + "</span>"
                + "</div>"
                + "<p style='font-size: 13px; color: #666;'>Please enter this code into the <strong>Email OTP</strong> field on the AnnPath verification page. (Valid for 10 minutes).</p>"
                + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>"
                + "<p style='font-size: 11px; color: #999; text-align: center;'>AnnPath Global Network &bull; Connecting Donors, NGOs & Volunteers</p>"
                + "</div>";

        message.setContent(htmlContent, "text/html; charset=utf-8");

        Transport.send(message);
        LOGGER.info("[REAL EMAIL API DISPATCHED (Port " + port + ")] Successfully sent OTP to " + toEmail);
        return true;
    }

    /**
     * Synchronous SMS Sender (Supports Fast2SMS and Twilio).
     */
    public boolean sendOtpSms(String phoneNumber, String otpCode, String purpose) {
        String envEnabled = System.getenv("SMS_ENABLED");
        boolean isSmsEnabled = (envEnabled != null && !envEnabled.trim().isEmpty()) ? Boolean.parseBoolean(envEnabled.trim()) : "true".equalsIgnoreCase(AppConfig.get("notification.sms.enabled", "true"));
        String envProvider = System.getenv("SMS_PROVIDER");
        String provider = (envProvider != null && !envProvider.trim().isEmpty()) ? envProvider.trim().toLowerCase() : AppConfig.get("notification.sms.provider", "fast2sms").toLowerCase();

        if (!isSmsEnabled) {
            LOGGER.info("[SMS NOTIFICATION] SMS disabled in configuration. To: " + phoneNumber + " (" + purpose + ")");
            return false;
        }

        if ("fast2sms".equals(provider)) {
            return sendFast2Sms(phoneNumber, otpCode, purpose);
        } else if ("twilio".equals(provider)) {
            return sendTwilioSms(phoneNumber, otpCode);
        } else {
            LOGGER.warning("Unknown SMS provider configured: " + provider);
            return false;
        }
    }

    /**
     * Fast2SMS REST API Integration (Popular in India for OTPs).
     */
    private boolean sendFast2Sms(String phoneNumber, String otpCode, String purpose) {
        String envKey = System.getenv("FAST2SMS_API_KEY");
        if (envKey == null || envKey.trim().isEmpty()) {
            envKey = System.getenv("SMS_API_KEY");
        }
        String apiKey = (envKey != null && !envKey.trim().isEmpty()) ? envKey.trim() : AppConfig.get("notification.sms.fast2sms.api.key", "");
        if (apiKey.isEmpty()) {
            LOGGER.info("[SMS DISPATCH NOTIFICATION] OTP dispatched for mobile: " + phoneNumber + " (" + purpose + ")");
            return false;
        }

        HttpURLConnection conn = null;
        try {
            // Strip any leading country codes or non-digits (e.g. +91)
            String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
            if (cleanPhone.length() > 10) {
                cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
            }

            URL url = new URL("https://www.fast2sms.com/dev/bulkV2");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("authorization", apiKey);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AnnPath/2.0");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            String postData = "variables_values=" + URLEncoder.encode(otpCode, "UTF-8") +
                              "&route=otp" +
                              "&numbers=" + URLEncoder.encode(cleanPhone, "UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            String responseBody = "";
            try (java.io.InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream()) {
                if (is != null) {
                    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                    responseBody = s.hasNext() ? s.next() : "";
                }
            } catch (Exception ignore) {}

            if (responseCode == 200 && responseBody.contains("\"return\":true")) {
                lastSmsDelivered = true;
                lastSmsNotice = "SMS successfully delivered via Fast2SMS cellular network.";
                LOGGER.info("[FAST2SMS CELLULAR SMS SENT] Mobile: " + cleanPhone + " | Response: " + responseBody);
                return true;
            } else {
                lastSmsDelivered = false;
                if (responseBody.contains("996") || responseBody.contains("website verification")) {
                    lastSmsNotice = "Fast2SMS Notice: 1-time Website Verification is required in your Fast2SMS dashboard (Dev API > OTP SMS) before cellular SMS can reach handsets (Status 996).";
                } else if (responseBody.contains("999") || responseBody.contains("transaction of 100")) {
                    lastSmsNotice = "Fast2SMS Notice: Minimum ₹100 transaction required to use direct API route (Status 999).";
                } else {
                    lastSmsNotice = "Fast2SMS Gateway Notice (HTTP " + responseCode + "): " + responseBody;
                }
                LOGGER.warning("[FAST2SMS GATEWAY NOTICE (HTTP " + responseCode + ")] " + responseBody);
                return false;
            }
        } catch (Exception e) {
            lastSmsDelivered = false;
            lastSmsNotice = "Fast2SMS connection error: " + e.getMessage();
            LOGGER.log(Level.SEVERE, "Fast2SMS connection error: " + e.getMessage(), e);
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * Twilio REST API Integration.
     */
    private boolean sendTwilioSms(String phoneNumber, String otpCode) {
        String accountSid = AppConfig.get("notification.sms.twilio.account.sid", "");
        String authToken = AppConfig.get("notification.sms.twilio.auth.token", "");
        String fromNumber = AppConfig.get("notification.sms.twilio.from.number", "");

        if (accountSid.isEmpty() || authToken.isEmpty() || fromNumber.isEmpty()) {
            LOGGER.warning("Twilio credentials not configured.");
            return false;
        }

        HttpURLConnection conn = null;
        try {
            String twilioUrl = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
            URL url = new URL(twilioUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            String auth = accountSid + ":" + authToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            String body = "Your AnnPath verification code is: " + otpCode + ". Valid for 10 minutes.";
            String postData = "To=" + URLEncoder.encode(phoneNumber, "UTF-8") +
                              "&From=" + URLEncoder.encode(fromNumber, "UTF-8") +
                              "&Body=" + URLEncoder.encode(body, "UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                LOGGER.info("[TWILIO API DISPATCHED] Successfully sent SMS OTP to " + phoneNumber);
                return true;
            } else {
                LOGGER.warning("[TWILIO ERROR] Response code: " + responseCode);
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Twilio connection error: " + e.getMessage(), e);
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
