package org.annpath.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Enterprise date/time formatting with strict UTC storage and localized timezone projection.
 */
public final class DateTimeUtil {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private static final DateTimeFormatter ISO_LOCAL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private DateTimeUtil() {}

    public static Timestamp nowUtc() {
        return Timestamp.from(Instant.now());
    }

    /**
     * Format UTC timestamp into user's preferred timezone.
     */
    public static String formatInTimezone(Timestamp timestamp, String timezoneStr) {
        if (timestamp == null) return "—";
        try {
            ZoneId zoneId = ZoneId.of(timezoneStr != null ? timezoneStr : "UTC");
            ZonedDateTime zdt = timestamp.toInstant().atZone(zoneId);
            return zdt.format(DISPLAY_FORMATTER);
        } catch (Exception e) {
            return timestamp.toString();
        }
    }

    public static String formatInTimezoneWithLocale(Timestamp timestamp, String timezoneStr, Locale locale) {
        if (timestamp == null) return "—";
        try {
            ZoneId zoneId = ZoneId.of(timezoneStr != null ? timezoneStr : "UTC");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", locale != null ? locale : Locale.ENGLISH);
            return timestamp.toInstant().atZone(zoneId).format(formatter);
        } catch (Exception e) {
            return timestamp.toString();
        }
    }

    /**
     * Parse HTML5 datetime-local input string interpreted as user timezone, converted to UTC Timestamp.
     */
    public static Timestamp parseFromUserTimezone(String localDateTimeStr, String timezoneStr) {
        if (localDateTimeStr == null || localDateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDateTime ldt = LocalDateTime.parse(localDateTimeStr.trim(), ISO_LOCAL_FORMATTER);
            ZoneId zoneId = ZoneId.of(timezoneStr != null ? timezoneStr : "UTC");
            ZonedDateTime userZdt = ldt.atZone(zoneId);
            Instant utcInstant = userZdt.toInstant();
            return Timestamp.from(utcInstant);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date-time format. Expected yyyy-MM-ddTHH:mm", e);
        }
    }

    /**
     * Compute hours remaining until target timestamp (fractional double).
     */
    public static double getHoursRemaining(Timestamp targetTime) {
        if (targetTime == null) return -1.0;
        long diffMillis = targetTime.getTime() - System.currentTimeMillis();
        return (double) diffMillis / (1000.0 * 60.0 * 60.0);
    }
}
