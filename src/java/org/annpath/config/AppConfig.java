package org.annpath.config;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe configuration manager for AnnPath platform.
 * Loads defaults from application.properties and supports environment variable overrides.
 */
public class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final Properties PROPS = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                PROPS.load(in);
                LOGGER.info("AnnPath application.properties loaded successfully.");
            } else {
                LOGGER.warning("application.properties not found on classpath, using system defaults.");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load application.properties", ex);
        }
    }

    public static String get(String key, String defaultValue) {
        // 1. Check direct system property
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }

        // 2. Check uppercase underscore system property
        String envKey = key.toUpperCase().replace('.', '_');
        String sysPropEnv = System.getProperty(envKey);
        if (sysPropEnv != null && !sysPropEnv.trim().isEmpty()) {
            return sysPropEnv.trim();
        }

        // 3. Check environment variable (both uppercase-underscore and exact)
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        String envExact = System.getenv(key);
        if (envExact != null && !envExact.trim().isEmpty()) {
            return envExact.trim();
        }

        // 4. Retrieve from loaded properties with recursive placeholder resolution
        String rawVal = PROPS.getProperty(key);
        if (rawVal != null) {
            String resolved = resolvePlaceholders(rawVal);
            if (resolved != null && !resolved.trim().isEmpty()) {
                return resolved.trim();
            }
        }

        return defaultValue;
    }

    public static String resolvePlaceholders(String text) {
        if (text == null || !text.contains("${")) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        int cursor = 0;
        while (cursor < text.length()) {
            int start = text.indexOf("${", cursor);
            if (start == -1) {
                sb.append(text.substring(cursor));
                break;
            }
            sb.append(text, cursor, start);
            int end = text.indexOf("}", start + 2);
            if (end == -1) {
                sb.append(text.substring(start));
                break;
            }
            String expression = text.substring(start + 2, end).trim();
            String varName = expression;
            String defaultPart = null;
            int colonIdx = expression.indexOf(':');
            if (colonIdx != -1) {
                varName = expression.substring(0, colonIdx).trim();
                defaultPart = expression.substring(colonIdx + 1).trim();
            }

            String val = System.getenv(varName);
            if (val == null || val.trim().isEmpty()) {
                val = System.getProperty(varName);
            }
            if (val == null || val.trim().isEmpty()) {
                val = System.getenv(varName.toUpperCase().replace('.', '_'));
            }
            if (val == null || val.trim().isEmpty()) {
                val = System.getProperty(varName.toUpperCase().replace('.', '_'));
            }
            if (val == null || val.trim().isEmpty()) {
                val = defaultPart;
            }

            if (val != null) {
                sb.append(val);
            }
            cursor = end + 1;
        }
        return sb.toString();
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double getDouble(String key, double defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(val.trim());
    }

    // Database Configuration Getters
    public static String getDbUrl() {
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl.trim();
        }
        String sysUrl = System.getProperty("DB_URL");
        if (sysUrl != null && !sysUrl.trim().isEmpty()) {
            return sysUrl.trim();
        }
        String host = get("db.host", "localhost");
        String port = get("db.port", "3306");
        String dbName = get("db.name", "annpath");
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    }

    public static String getDbUsername() {
        String envUser = System.getenv("DB_USERNAME");
        if (envUser != null && !envUser.trim().isEmpty()) {
            return envUser.trim();
        }
        String sysUser = System.getProperty("DB_USERNAME");
        if (sysUser != null && !sysUser.trim().isEmpty()) {
            return sysUser.trim();
        }
        String user = get("db.username", "");
        return user != null ? user.trim() : "";
    }

    public static String getDbPassword() {
        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null) {
            return envPass;
        }
        String sysPass = System.getProperty("DB_PASSWORD");
        if (sysPass != null) {
            return sysPass;
        }
        String pass = get("db.password", "");
        return pass != null ? pass : "";
    }

    public static String getDbDriver() {
        return get("db.driver", "com.mysql.cj.jdbc.Driver");
    }

    public static int getDbPoolMinIdle() {
        return getInt("db.pool.minIdle", 5);
    }

    public static int getDbPoolMaxTotal() {
        return getInt("db.pool.maxTotal", 25);
    }

    public static int getUrgencyCriticalHours() {
        return getInt("urgency.critical.hours", 2);
    }

    public static int getUrgencyExpiringSoonHours() {
        return getInt("urgency.expiringSoon.hours", 6);
    }

    public static int getVolunteerResponseTimeoutMinutes() {
        return getInt("volunteer.responseTimeoutMinutes", 15);
    }

    public static double getVolunteerMaxSearchRadiusKm() {
        return getDouble("volunteer.maxSearchRadiusKm", 20.0);
    }

    public static String getUploadDirectory() {
        String envUpload = System.getenv("ANNPATH_UPLOAD_DIR");
        if (envUpload != null && !envUpload.trim().isEmpty()) {
            return envUpload.trim();
        }
        String propUpload = get("upload.directory", null);
        if (propUpload != null && !propUpload.trim().isEmpty() && !propUpload.contains("${user.home}")) {
            return propUpload.trim();
        }
        String userHome = System.getProperty("user.home", ".");
        return userHome + java.io.File.separator + ".annpath" + java.io.File.separator + "uploads";
    }

    public static long getMaxFileSizeBytes() {
        return getInt("upload.maxFileSizeBytes", 5242880);
    }

    // Email SMTP Configuration Getters
    public static String getEmailHost() {
        return get("notification.email.smtp.host", "smtp.gmail.com");
    }

    public static String getEmailPort() {
        return get("notification.email.smtp.port", "587");
    }

    public static String getEmailUsername() {
        return get("notification.email.smtp.user", "annpath.redistributionplatform@gmail.com");
    }

    public static String getEmailPassword() {
        return get("notification.email.smtp.password", "nejlpjxruzdvnixo");
    }

    public static String getEmailFrom() {
        return get("notification.email.from", "annpath.redistributionplatform@gmail.com");
    }

    public static boolean getEmailUseTls() {
        return getBoolean("notification.email.smtp.tls", true);
    }

    public static boolean isEmailEnabled() {
        return getBoolean("notification.email.enabled", true);
    }

    // Security & Environment Getters
    public static String getAllowedCorsOrigins() {
        return get("api.allowed.origins", "http://localhost:8080,http://127.0.0.1:8080");
    }

    public static boolean isProduction() {
        String env = get("app.environment", "development");
        return "production".equalsIgnoreCase(env) || "prod".equalsIgnoreCase(env);
    }

    public static boolean isCookieSecure() {
        return getBoolean("security.cookie.secure", isProduction());
    }

    // Failure Recovery & Rescue Intelligence Getters
    public static int getFailureRecoveryMaxAttempts() {
        return getInt("failureRecovery.maxAttempts", 3);
    }

    public static int getFailureRecoveryCooldownMinutes() {
        return getInt("failureRecovery.cooldownMinutes", 10);
    }

    public static int getBottleneckRequestDelayHours() {
        return getInt("bottleneck.requestDelayHours", 2);
    }

    public static int getBottleneckPickupDelayHours() {
        return getInt("bottleneck.pickupDelayHours", 2);
    }

    public static int getBottleneckDeliveryDelayHours() {
        return getInt("bottleneck.deliveryDelayHours", 2);
    }

    public static double getBottleneckNgoCapacityThresholdRatio() {
        return getDouble("bottleneck.ngoCapacityThresholdRatio", 0.90);
    }
}
