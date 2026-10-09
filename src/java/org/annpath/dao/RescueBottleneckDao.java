package org.annpath.dao;

import org.annpath.model.RescueBottleneck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Rescue Bottlenecks.
 */
public class RescueBottleneckDao extends BaseDao {

    public Long create(RescueBottleneck bottleneck, Connection conn) throws SQLException {
        String sql = "INSERT INTO rescue_bottlenecks (bottleneck_type, severity, food_listing_id, pickup_id, " +
                     "ngo_user_id, volunteer_user_id, description, status, detected_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, bottleneck.getBottleneckType());
            ps.setString(2, bottleneck.getSeverity() != null ? bottleneck.getSeverity() : "MEDIUM");
            if (bottleneck.getFoodListingId() != null) ps.setLong(3, bottleneck.getFoodListingId());
            else ps.setNull(3, java.sql.Types.BIGINT);
            if (bottleneck.getPickupId() != null) ps.setLong(4, bottleneck.getPickupId());
            else ps.setNull(4, java.sql.Types.BIGINT);
            if (bottleneck.getNgoUserId() != null) ps.setLong(5, bottleneck.getNgoUserId());
            else ps.setNull(5, java.sql.Types.BIGINT);
            if (bottleneck.getVolunteerUserId() != null) ps.setLong(6, bottleneck.getVolunteerUserId());
            else ps.setNull(6, java.sql.Types.BIGINT);
            ps.setString(7, bottleneck.getDescription());
            ps.setString(8, bottleneck.getStatus() != null ? bottleneck.getStatus() : "ACTIVE");

            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                bottleneck.setId(id);
                return id;
            }
            throw new SQLException("Failed to retrieve generated bottleneck ID.");
        } finally {
            closeQuietly(rs, ps);
        }
    }

    public Long create(RescueBottleneck bottleneck) throws SQLException {
        Connection conn = getConnection();
        try {
            return create(bottleneck, conn);
        } finally {
            releaseConnection(conn);
        }
    }

    public Long createBottleneck(String type, String severity, String description, Long listingId, Long pickupId) throws SQLException {
        RescueBottleneck b = new RescueBottleneck();
        b.setBottleneckType(type);
        b.setSeverity(severity != null ? severity : "MEDIUM");
        b.setDescription(description);
        b.setFoodListingId(listingId);
        b.setPickupId(pickupId);
        b.setStatus("ACTIVE");
        return create(b);
    }

    public boolean existsActive(String bottleneckType, Long listingId, Long pickupId) throws SQLException {
        return existsActive(bottleneckType, listingId, pickupId, null);
    }

    public boolean existsActive(String bottleneckType, Long listingId, Long pickupId, Long ngoUserId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM rescue_bottlenecks WHERE status IN ('DETECTED', 'ACTIVE') AND bottleneck_type = ?");
        if (listingId != null) sql.append(" AND food_listing_id = ?");
        if (pickupId != null) sql.append(" AND pickup_id = ?");
        if (ngoUserId != null) sql.append(" AND ngo_user_id = ?");

        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql.toString());
            int idx = 1;
            ps.setString(idx++, bottleneckType);
            if (listingId != null) ps.setLong(idx++, listingId);
            if (pickupId != null) ps.setLong(idx++, pickupId);
            if (ngoUserId != null) ps.setLong(idx++, ngoUserId);

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public List<RescueBottleneck> findActiveBottlenecks() throws SQLException {
        String sql = "SELECT rb.*, fl.title AS food_title, " +
                     "COALESCE(np.organization_name, u_n.email) AS ngo_name, " +
                     "CONCAT(COALESCE(vp.first_name,''), ' ', COALESCE(vp.last_name,'')) AS volunteer_name, " +
                     "fl.pickup_city AS location " +
                     "FROM rescue_bottlenecks rb " +
                     "LEFT JOIN food_listings fl ON rb.food_listing_id = fl.id " +
                     "LEFT JOIN users u_n ON rb.ngo_user_id = u_n.id " +
                     "LEFT JOIN ngo_profiles np ON u_n.id = np.user_id " +
                     "LEFT JOIN users u_v ON rb.volunteer_user_id = u_v.id " +
                     "LEFT JOIN volunteer_profiles vp ON u_v.id = vp.user_id " +
                     "WHERE rb.status IN ('DETECTED', 'ACTIVE') " +
                     "ORDER BY CASE rb.severity " +
                     "  WHEN 'CRITICAL' THEN 1 " +
                     "  WHEN 'HIGH' THEN 2 " +
                     "  WHEN 'MEDIUM' THEN 3 " +
                     "  WHEN 'LOW' THEN 4 " +
                     "  ELSE 5 END ASC, rb.detected_at DESC";
        List<RescueBottleneck> list = new ArrayList<>();
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapBottleneck(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public void resolveBottlenecks(Long listingId, Long pickupId, String bottleneckType) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE rescue_bottlenecks SET status = 'RESOLVED', resolved_at = CURRENT_TIMESTAMP WHERE status IN ('DETECTED', 'ACTIVE')");
        if (listingId != null) sql.append(" AND food_listing_id = ?");
        if (pickupId != null) sql.append(" AND pickup_id = ?");
        if (bottleneckType != null) sql.append(" AND bottleneck_type = ?");

        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (listingId != null) ps.setLong(idx++, listingId);
            if (pickupId != null) ps.setLong(idx++, pickupId);
            if (bottleneckType != null) ps.setString(idx++, bottleneckType);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }

    public void resolveAllForPickup(Long pickupId) throws SQLException {
        if (pickupId == null) return;
        resolveBottlenecks(null, pickupId, null);
    }

    public void resolveAllForListing(Long listingId) throws SQLException {
        if (listingId == null) return;
        resolveBottlenecks(listingId, null, null);
    }

    private RescueBottleneck mapBottleneck(ResultSet rs) throws SQLException {
        RescueBottleneck b = new RescueBottleneck();
        b.setId(rs.getLong("id"));
        b.setBottleneckType(rs.getString("bottleneck_type"));
        b.setSeverity(rs.getString("severity"));
        b.setFoodListingId(rs.getObject("food_listing_id") != null ? rs.getLong("food_listing_id") : null);
        b.setPickupId(rs.getObject("pickup_id") != null ? rs.getLong("pickup_id") : null);
        b.setNgoUserId(rs.getObject("ngo_user_id") != null ? rs.getLong("ngo_user_id") : null);
        b.setVolunteerUserId(rs.getObject("volunteer_user_id") != null ? rs.getLong("volunteer_user_id") : null);
        b.setDescription(rs.getString("description"));
        b.setStatus(rs.getString("status"));
        b.setDetectedAt(rs.getTimestamp("detected_at"));
        b.setResolvedAt(rs.getTimestamp("resolved_at"));

        b.setFoodTitle(rs.getString("food_title"));
        b.setNgoName(rs.getString("ngo_name"));
        b.setVolunteerName(rs.getString("volunteer_name"));
        b.setLocation(rs.getString("location"));
        return b;
    }
}
