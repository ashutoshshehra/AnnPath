package org.annpath.location;

import java.math.BigDecimal;

/**
 * Geodesic spatial calculations using the Haversine formula.
 * Provides high-efficiency proximity checking and bounding box query filters.
 */
public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double KM_TO_MILES = 0.621371;

    private GeoUtils() {}

    /**
     * Compute great-circle distance between two points in kilometers.
     */
    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public static double calculateDistanceKm(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }
        return calculateDistanceKm(lat1.doubleValue(), lon1.doubleValue(), lat2.doubleValue(), lon2.doubleValue());
    }

    public static double toMiles(double km) {
        return km * KM_TO_MILES;
    }

    /**
     * Calculate bounding box coordinate limits for indexed SQL range queries.
     * @return double[] { minLat, maxLat, minLon, maxLon }
     */
    public static double[] calculateBoundingBox(double centerLat, double centerLon, double radiusKm) {
        double latChange = Math.toDegrees(radiusKm / EARTH_RADIUS_KM);
        double minLat = centerLat - latChange;
        double maxLat = centerLat + latChange;

        double lonChange = Math.toDegrees(radiusKm / (EARTH_RADIUS_KM * Math.cos(Math.toRadians(centerLat))));
        double minLon = centerLon - lonChange;
        double maxLon = centerLon + lonChange;

        return new double[] { minLat, maxLat, minLon, maxLon };
    }
}
