package org.annpath.security;

import java.util.regex.Pattern;

/**
 * Defensive input sanitization, validation, and XSS defense utilities.
 */
public final class InputSanitizer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9\\s-]{7,20}$"
    );

    private InputSanitizer() {}

    /**
     * Escape special HTML characters to prevent XSS attacks.
     */
    public static String escapeHtml(String input) {
        if (input == null) return "";
        StringBuilder out = new StringBuilder(Math.max(16, input.length()));
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '<': out.append("&lt;"); break;
                case '>': out.append("&gt;"); break;
                case '&': out.append("&amp;"); break;
                case '"': out.append("&quot;"); break;
                case '\'': out.append("&#x27;"); break;
                case '/': out.append("&#x2F;"); break;
                default: out.append(c); break;
            }
        }
        return out.toString();
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String trimmed = email.trim();
        return !trimmed.isEmpty() && trimmed.length() <= 191 && EMAIL_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String trimmed = phone.trim();
        return !trimmed.isEmpty() && trimmed.length() <= 30 && PHONE_PATTERN.matcher(trimmed).matches();
    }

    public static String clean(String input) {
        if (input == null) return null;
        return input.trim();
    }

    /**
     * Escape SQL LIKE wildcards (% and _) to avoid pattern injection.
     */
    public static String escapeLikeWildcards(String input) {
        if (input == null) return "";
        return input.replace("!", "!!")
                    .replace("%", "!%")
                    .replace("_", "!_");
    }
}
