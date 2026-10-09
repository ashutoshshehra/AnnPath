package org.annpath.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Internationalization utility for AnnPath backed by UTF-8 ResourceBundles.
 */
public final class I18nUtil {

    private static final String BUNDLE_BASE_NAME = "messages";

    private I18nUtil() {}

    public static String getMessage(String key, Locale locale, Object... args) {
        if (key == null) return "";
        Locale effectiveLocale = locale != null ? locale : Locale.ENGLISH;
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, effectiveLocale);
            String pattern = bundle.getString(key);
            if (args != null && args.length > 0) {
                return MessageFormat.format(pattern, args);
            }
            return pattern;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public static Locale parseLocale(String langCode) {
        if (langCode == null || langCode.trim().isEmpty()) {
            return Locale.ENGLISH;
        }
        String clean = langCode.trim().toLowerCase();
        switch (clean) {
            case "hi": return new Locale("hi");
            case "es": return new Locale("es");
            case "fr": return new Locale("fr");
            default: return Locale.ENGLISH;
        }
    }
}
