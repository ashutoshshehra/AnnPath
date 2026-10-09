package org.annpath.service;

import org.annpath.config.AppConfig;
import org.annpath.config.Constants;
import org.annpath.dao.BaseDao;
import org.annpath.dao.FoodListingDao;
import org.annpath.dao.PickupDao;
import org.annpath.dao.RescueBottleneckDao;
import org.annpath.model.FoodListing;
import org.annpath.model.Pickup;
import org.annpath.model.RescueBottleneck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Intelligent Rescue Bottleneck Detector.
 * Continuously analyzes live platform pipeline for volunteer shortages,
 * rescue request delays, transit stalls, and operational friction points.
 */
public class RescueBottleneckDetector extends BaseDao {

    private static final Logger LOGGER = Logger.getLogger(RescueBottleneckDetector.class.getName());

    private final RescueBottleneckDao bottleneckDao = new RescueBottleneckDao();
    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final PickupDao pickupDao = new PickupDao();

    private static volatile long lastScanTimestamp = 0L;
    private static volatile List<RescueBottleneck> cachedBottlenecks = new ArrayList<>();
    private static final Object SCAN_LOCK = new Object();

    /**
     * Run a comprehensive system-wide scan of all rescue stages,
     * detect any stalls, auto-resolve cleared bottlenecks, and return active items.
     */
    public List<RescueBottleneck> scanAllBottlenecks() {
        try {
            detectVolunteerShortages();
            detectCriticalUnassignedSurplus();
            detectRequestDelays();
            detectPickupAndDeliveryDelays();
            detectNgoCapacityShortage();
            autoResolveClearedBottlenecks();
            List<RescueBottleneck> fresh = bottleneckDao.findActiveBottlenecks();
            cachedBottlenecks = (fresh != null) ? fresh : new ArrayList<>();
            return cachedBottlenecks;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error scanning rescue bottlenecks: " + e.getMessage(), e);
            return cachedBottlenecks != null ? cachedBottlenecks : new ArrayList<>();
        }
    }

    /**
     * Throttled scan to prevent full-database scans on every dashboard page load.
     * Only scans full pipeline if at least minIntervalSeconds have passed since the last run.
     * Returns in-memory cached results near-instantaneously if called within the throttle window.
     */
    public List<RescueBottleneck> scanAllBottlenecksThrottled(int minIntervalSeconds) {
        long now = System.currentTimeMillis();
        if (now - lastScanTimestamp > (minIntervalSeconds * 1000L)) {
            synchronized (SCAN_LOCK) {
                if (now - lastScanTimestamp > (minIntervalSeconds * 1000L)) {
                    lastScanTimestamp = now;
                    return scanAllBottlenecks();
                }
            }
        }
        return cachedBottlenecks != null ? cachedBottlenecks : new ArrayList<>();
    }

    /**
     * 1. Detect volunteer shortages where pickups lack an assigned volunteer beyond response timeout.
     */
    public void detectVolunteerShortages() throws SQLException {
        String sql = "SELECT p.id, p.food_listing_id, p.ngo_user_id, fl.title " +
                     "FROM pickups p " +
                     "JOIN food_listings fl ON p.food_listing_id = fl.id " +
                     "WHERE p.status IN ('ASSIGNED', 'REASSIGNING', 'VOLUNTEER_UNAVAILABLE') " +
                     "AND p.volunteer_user_id IS NULL " +
                     "AND p.created_at < DATE_SUB(NOW(), INTERVAL ? MINUTE)";

        int timeoutMin = AppConfig.getVolunteerResponseTimeoutMinutes();
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, timeoutMin);
            rs = ps.executeQuery();
            while (rs.next()) {
                long pickupId = rs.getLong("id");
                long listingId = rs.getLong("food_listing_id");
                long ngoId = rs.getLong("ngo_user_id");
                String title = rs.getString("title");

                if (!bottleneckDao.existsActive("VOLUNTEER_SHORTAGE", listingId, pickupId)) {
                    RescueBottleneck b = new RescueBottleneck();
                    b.setBottleneckType("VOLUNTEER_SHORTAGE");
                    b.setSeverity("HIGH");
                    b.setPickupId(pickupId);
                    b.setFoodListingId(listingId);
                    b.setNgoUserId(ngoId);
                    b.setDescription(String.format("No volunteer accepted for '%s' (Pickup #%d) after %d+ minutes.", title, pickupId, timeoutMin));
                    b.setStatus("ACTIVE");
                    bottleneckDao.create(b);
                    LOGGER.warning("Detected Bottleneck: VOLUNTEER_SHORTAGE on Pickup #" + pickupId);
                }
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    /**
     * 2. Detect unrequested critical surplus approaching shelf-life expiration.
     */
    public void detectCriticalUnassignedSurplus() throws SQLException {
        String sql = "SELECT fl.id, fl.title, fl.urgency_status, fl.remaining_quantity " +
                     "FROM food_listings fl " +
                     "WHERE fl.listing_status IN ('DONATED', 'MATCHED') " +
                     "AND fl.urgency_status IN ('CRITICAL', 'EXPIRING_SOON') " +
                     "AND fl.created_at < DATE_SUB(NOW(), INTERVAL 30 MINUTE)";

        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                long listingId = rs.getLong("id");
                String title = rs.getString("title");
                String urgency = rs.getString("urgency_status");

                if (!bottleneckDao.existsActive("CRITICAL_RESCUE_UNASSIGNED", listingId, null)) {
                    RescueBottleneck b = new RescueBottleneck();
                    b.setBottleneckType("CRITICAL_RESCUE_UNASSIGNED");
                    b.setSeverity("CRITICAL".equalsIgnoreCase(urgency) ? "CRITICAL" : "HIGH");
                    b.setFoodListingId(listingId);
                    b.setDescription(String.format("Urgent surplus '%s' (%s) remains unrequested by any NGO.", title, urgency));
                    b.setStatus("ACTIVE");
                    bottleneckDao.create(b);
                    LOGGER.warning("Detected Bottleneck: CRITICAL_RESCUE_UNASSIGNED on Listing #" + listingId);
                }
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    /**
     * 3. Detect donor listings that have stayed unrequested beyond delay threshold (e.g. > 2 hours).
     */
    public void detectRequestDelays() throws SQLException {
        int delayHours = AppConfig.getBottleneckRequestDelayHours();
        String sql = "SELECT fl.id, fl.title, fl.remaining_quantity " +
                     "FROM food_listings fl " +
                     "WHERE fl.listing_status = 'DONATED' " +
                     "AND fl.created_at < DATE_SUB(NOW(), INTERVAL ? HOUR)";

        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, delayHours);
            rs = ps.executeQuery();
            while (rs.next()) {
                long listingId = rs.getLong("id");
                String title = rs.getString("title");

                if (!bottleneckDao.existsActive("REQUEST_DELAY", listingId, null)) {
                    RescueBottleneck b = new RescueBottleneck();
                    b.setBottleneckType("REQUEST_DELAY");
                    b.setSeverity("MEDIUM");
                    b.setFoodListingId(listingId);
                    b.setDescription(String.format("Listing '%s' (ID #%d) unrequested for over %d hours.", title, listingId, delayHours));
                    b.setStatus("ACTIVE");
                    bottleneckDao.create(b);
                }
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    /**
     * 4. Detect pickup delays (not collected after window start) and transit delays (in-transit > 2h).
     */
    public void detectPickupAndDeliveryDelays() throws SQLException {
        int delayHours = AppConfig.getBottleneckPickupDelayHours();

        // 4a. Pickup delay
        String pickupSql = "SELECT p.id, p.food_listing_id, p.volunteer_user_id, p.ngo_user_id, fl.title " +
                           "FROM pickups p " +
                           "JOIN food_listings fl ON p.food_listing_id = fl.id " +
                           "WHERE p.status IN ('ASSIGNED', 'VOLUNTEER_ASSIGNED', 'NGO_SELF_PICKUP_ASSIGNED') " +
                           "AND p.pickup_window_start IS NOT NULL " +
                           "AND p.pickup_window_start < DATE_SUB(NOW(), INTERVAL ? HOUR)";

        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(pickupSql);
            ps.setInt(1, delayHours);
            rs = ps.executeQuery();
            while (rs.next()) {
                long pickupId = rs.getLong("id");
                long listingId = rs.getLong("food_listing_id");
                String title = rs.getString("title");

                if (!bottleneckDao.existsActive("PICKUP_DELAY", listingId, pickupId)) {
                    RescueBottleneck b = new RescueBottleneck();
                    b.setBottleneckType("PICKUP_DELAY");
                    b.setSeverity("HIGH");
                    b.setPickupId(pickupId);
                    b.setFoodListingId(listingId);
                    b.setNgoUserId(rs.getLong("ngo_user_id"));
                    b.setVolunteerUserId(rs.getObject("volunteer_user_id") != null ? rs.getLong("volunteer_user_id") : null);
                    b.setDescription(String.format("Pickup for '%s' is delayed by over %d hours past pickup window start.", title, delayHours));
                    b.setStatus("ACTIVE");
                    bottleneckDao.create(b);
                }
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }

        // 4b. Delivery delay (picked up but not delivered after 2 hours)
        int deliveryDelayHours = AppConfig.getBottleneckDeliveryDelayHours();
        String delivSql = "SELECT p.id, p.food_listing_id, p.volunteer_user_id, p.ngo_user_id, fl.title " +
                          "FROM pickups p " +
                          "JOIN food_listings fl ON p.food_listing_id = fl.id " +
                          "WHERE p.status = 'PICKED_UP' " +
                          "AND p.actual_picked_up_at < DATE_SUB(NOW(), INTERVAL ? HOUR)";

        conn = getConnection();
        try {
            ps = conn.prepareStatement(delivSql);
            ps.setInt(1, deliveryDelayHours);
            rs = ps.executeQuery();
            while (rs.next()) {
                long pickupId = rs.getLong("id");
                long listingId = rs.getLong("food_listing_id");
                String title = rs.getString("title");

                if (!bottleneckDao.existsActive("DELIVERY_DELAY", listingId, pickupId)) {
                    RescueBottleneck b = new RescueBottleneck();
                    b.setBottleneckType("DELIVERY_DELAY");
                    b.setSeverity("CRITICAL");
                    b.setPickupId(pickupId);
                    b.setFoodListingId(listingId);
                    b.setNgoUserId(rs.getLong("ngo_user_id"));
                    b.setVolunteerUserId(rs.getObject("volunteer_user_id") != null ? rs.getLong("volunteer_user_id") : null);
                    b.setDescription(String.format("Urgent: Food '%s' is in transit for over %d hours without delivery confirmation.", title, deliveryDelayHours));
                    b.setStatus("ACTIVE");
                    bottleneckDao.create(b);
                    LOGGER.severe("Detected Critical Bottleneck: DELIVERY_DELAY on Pickup #" + pickupId);
                }
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    /**
     * 5. Detect NGO capacity shortage where claimed food volume reaches or exceeds 90% of daily capacity.
     */
    public void detectNgoCapacityShortage() throws SQLException {
        String sql = "SELECT np.user_id, np.organization_name, np.max_daily_capacity_kg, " +
                     "COALESCE(SUM(fr.requested_quantity), 0) AS active_volume " +
                     "FROM ngo_profiles np " +
                     "JOIN pickups p ON np.user_id = p.ngo_user_id " +
                     "JOIN food_requests fr ON p.food_request_id = fr.id " +
                     "WHERE p.status IN ('ASSIGNED', 'VOLUNTEER_ASSIGNED', 'NGO_SELF_PICKUP_ASSIGNED', 'PICKED_UP', 'DELIVERED') " +
                     "AND np.max_daily_capacity_kg IS NOT NULL AND np.max_daily_capacity_kg > 0 " +
                     "GROUP BY np.user_id, np.organization_name, np.max_daily_capacity_kg " +
                     "HAVING active_volume >= (np.max_daily_capacity_kg * 0.90)";

        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                long ngoId = rs.getLong("user_id");
                String orgName = rs.getString("organization_name");
                double capacity = rs.getDouble("max_daily_capacity_kg");
                double activeVol = rs.getDouble("active_volume");

                if (!bottleneckDao.existsActive("NGO_CAPACITY_SHORTAGE", null, null, ngoId)) {
                    RescueBottleneck b = new RescueBottleneck();
                    b.setBottleneckType("NGO_CAPACITY_SHORTAGE");
                    b.setSeverity(activeVol >= capacity ? "CRITICAL" : "HIGH");
                    b.setNgoUserId(ngoId);
                    b.setDescription(String.format("NGO '%s' is operating near peak intake (%.0f / %.0f kg, %.0f%% capacity).",
                            orgName != null ? orgName : "NGO #" + ngoId, activeVol, capacity, (activeVol / capacity) * 100.0));
                    b.setStatus("ACTIVE");
                    bottleneckDao.create(b);
                    LOGGER.warning("Detected Bottleneck: NGO_CAPACITY_SHORTAGE on NGO #" + ngoId);
                }
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    /**
     * 6. Automatically resolve bottlenecks when progress occurs.
     */
    public void autoResolveClearedBottlenecks() throws SQLException {
        List<RescueBottleneck> active = bottleneckDao.findActiveBottlenecks();
        for (RescueBottleneck b : active) {
            boolean shouldResolve = false;

            if (b.getPickupId() != null) {
                Pickup p = pickupDao.findById(b.getPickupId());
                if (p == null) {
                    shouldResolve = true;
                } else {
                    String status = p.getStatus();
                    if ("VOLUNTEER_SHORTAGE".equals(b.getBottleneckType())) {
                        if (p.getVolunteerUserId() != null || "NGO_SELF_PICKUP_ASSIGNED".equals(status) ||
                            "PICKED_UP".equals(status) || "DELIVERED".equals(status) || "RECEIVED".equals(status) ||
                            "CANCELLED".equals(status) || "EXPIRED".equals(status)) {
                            shouldResolve = true;
                        }
                    } else if ("PICKUP_DELAY".equals(b.getBottleneckType())) {
                        if ("PICKED_UP".equals(status) || "DELIVERED".equals(status) || "RECEIVED".equals(status) || "DISTRIBUTED".equals(status) ||
                            "CANCELLED".equals(status) || "EXPIRED".equals(status)) {
                            shouldResolve = true;
                        }
                    } else if ("DELIVERY_DELAY".equals(b.getBottleneckType())) {
                        if ("DELIVERED".equals(status) || "RECEIVED".equals(status) || "DISTRIBUTED".equals(status) ||
                            "CANCELLED".equals(status) || "EXPIRED".equals(status)) {
                            shouldResolve = true;
                        }
                    }
                }
            } else if (b.getFoodListingId() != null) {
                FoodListing fl = foodListingDao.findById(b.getFoodListingId());
                if (fl == null) {
                    shouldResolve = true;
                } else {
                    String status = fl.getListingStatus();
                    if (!"DONATED".equals(status) && !"MATCHED".equals(status)) {
                        shouldResolve = true; // Claimed, approved, distributed, cancelled, or expired
                    }
                }
            } else if ("NGO_CAPACITY_SHORTAGE".equals(b.getBottleneckType()) && b.getNgoUserId() != null) {
                // Check if NGO volume has subsided
                double activeVol = getActiveNgoVolume(b.getNgoUserId());
                double capacity = getNgoCapacity(b.getNgoUserId());
                if (capacity <= 0 || activeVol < (capacity * 0.90)) {
                    shouldResolve = true;
                }
            }

            if (shouldResolve) {
                bottleneckDao.resolveBottlenecks(b.getFoodListingId(), b.getPickupId(), b.getBottleneckType());
                LOGGER.info("Auto-resolved bottleneck #" + b.getId() + " (" + b.getBottleneckType() + ")");
            }
        }
    }

    private double getActiveNgoVolume(Long ngoUserId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(fr.requested_quantity), 0) " +
                     "FROM pickups p " +
                     "JOIN food_requests fr ON p.food_request_id = fr.id " +
                     "WHERE p.ngo_user_id = ? " +
                     "AND p.status IN ('ASSIGNED', 'VOLUNTEER_ASSIGNED', 'NGO_SELF_PICKUP_ASSIGNED', 'PICKED_UP', 'DELIVERED')";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, ngoUserId);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
            return 0.0;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    private double getNgoCapacity(Long ngoUserId) throws SQLException {
        String sql = "SELECT max_daily_capacity_kg FROM ngo_profiles WHERE user_id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, ngoUserId);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
            return 0.0;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }
}
