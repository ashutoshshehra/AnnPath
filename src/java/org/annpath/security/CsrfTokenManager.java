package org.annpath.security;

import org.annpath.config.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Thread-safe Anti-CSRF Token Manager.
 * Generates and validates cryptographic tokens per user session.
 */
public final class CsrfTokenManager {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private CsrfTokenManager() {}

    /**
     * Retrieves existing CSRF token from session or generates a new one.
     */
    public static String getToken(HttpSession session) {
        if (session == null) return null;
        synchronized (session) {
            String token = (String) session.getAttribute(Constants.SESSION_CSRF_TOKEN);
            if (token == null || token.trim().isEmpty()) {
                byte[] bytes = new byte[TOKEN_BYTES];
                RANDOM.nextBytes(bytes);
                token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                session.setAttribute(Constants.SESSION_CSRF_TOKEN, token);
            }
            return token;
        }
    }

    /**
     * Validates that the request CSRF token matches the session token.
     */
    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;

        String sessionToken = (String) session.getAttribute(Constants.SESSION_CSRF_TOKEN);
        if (sessionToken == null || sessionToken.isEmpty()) return false;

        String requestToken = request.getParameter("csrfToken");
        if (requestToken == null || requestToken.isEmpty()) {
            requestToken = request.getHeader("X-CSRF-Token");
        }

        if (requestToken == null || requestToken.isEmpty()) return false;

        return slowEquals(sessionToken, requestToken);
    }

    private static boolean slowEquals(String a, String b) {
        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();
        int diff = aBytes.length ^ bBytes.length;
        for (int i = 0; i < aBytes.length && i < bBytes.length; i++) {
            diff |= aBytes[i] ^ bBytes[i];
        }
        return diff == 0;
    }
}
