package org.annpath.dao;

import org.annpath.model.FoodStatusHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FoodStatusHistoryDao extends BaseDao {

    public void recordTransition(FoodStatusHistory history, Connection conn) throws SQLException {
        String sql = "INSERT INTO food_status_history (food_listing_id, pickup_id, previous_status, " +
                     "new_status, actor_user_id, actor_role, transition_reason, transition_metadata_json) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, history.getFoodListingId());
            if (history.getPickupId() != null) {
                ps.setLong(2, history.getPickupId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setString(3, history.getPreviousStatus() != null ? history.getPreviousStatus() : "UNKNOWN");
            ps.setString(4, history.getNewStatus());
            if (history.getActorUserId() != null) {
                ps.setLong(5, history.getActorUserId());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            ps.setString(6, history.getActorRole() != null ? history.getActorRole() : "SYSTEM");
            ps.setString(7, history.getTransitionReason() != null ? history.getTransitionReason() : "Status update");
            ps.setString(8, history.getTransitionMetadataJson());
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public void recordTransition(FoodStatusHistory history) throws SQLException {
        Connection conn = getConnection();
        try {
            recordTransition(history, conn);
        } finally {
            releaseConnection(conn);
        }
    }

    public List<FoodStatusHistory> findByFoodListingId(Long listingId) throws SQLException {
        List<FoodStatusHistory> list = new ArrayList<>();
        String sql = "SELECT fsh.*, COALESCE(u.email, 'System') AS actor_email, " +
                     "COALESCE(dp.organization_name, np.organization_name, CONCAT(vp.first_name, ' ', vp.last_name), u.email, 'System Engine') AS actor_name " +
                     "FROM food_status_history fsh " +
                     "LEFT JOIN users u ON fsh.actor_user_id = u.id " +
                     "LEFT JOIN donor_profiles dp ON u.id = dp.user_id " +
                     "LEFT JOIN ngo_profiles np ON u.id = np.user_id " +
                     "LEFT JOIN volunteer_profiles vp ON u.id = vp.user_id " +
                     "WHERE fsh.food_listing_id = ? " +
                     "ORDER BY fsh.created_at ASC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, listingId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapHistory(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public List<FoodStatusHistory> findByPickupId(Long pickupId) throws SQLException {
        List<FoodStatusHistory> list = new ArrayList<>();
        String sql = "SELECT fsh.*, COALESCE(u.email, 'System') AS actor_email, " +
                     "COALESCE(dp.organization_name, np.organization_name, CONCAT(vp.first_name, ' ', vp.last_name), u.email, 'System Engine') AS actor_name " +
                     "FROM food_status_history fsh " +
                     "LEFT JOIN users u ON fsh.actor_user_id = u.id " +
                     "LEFT JOIN donor_profiles dp ON u.id = dp.user_id " +
                     "LEFT JOIN ngo_profiles np ON u.id = np.user_id " +
                     "LEFT JOIN volunteer_profiles vp ON u.id = vp.user_id " +
                     "WHERE fsh.pickup_id = ? " +
                     "ORDER BY fsh.created_at ASC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, pickupId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapHistory(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    private FoodStatusHistory mapHistory(ResultSet rs) throws SQLException {
        FoodStatusHistory h = new FoodStatusHistory();
        h.setId(rs.getLong("id"));
        h.setFoodListingId(rs.getLong("food_listing_id"));
        h.setPickupId(rs.getObject("pickup_id") != null ? rs.getLong("pickup_id") : null);
        h.setPreviousStatus(rs.getString("previous_status"));
        h.setNewStatus(rs.getString("new_status"));
        h.setActorUserId(rs.getObject("actor_user_id") != null ? rs.getLong("actor_user_id") : null);
        h.setActorRole(rs.getString("actor_role"));
        h.setTransitionReason(rs.getString("transition_reason"));
        h.setTransitionMetadataJson(rs.getString("transition_metadata_json"));
        h.setCreatedAt(rs.getTimestamp("created_at"));
        h.setActorName(rs.getString("actor_name"));
        return h;
    }
}
