package org.annpath.dao;

import org.annpath.model.FoodRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FoodRequestDao extends BaseDao {

    public Long create(FoodRequest req, Connection conn) throws SQLException {
        String sql = "INSERT INTO food_requests (food_listing_id, ngo_user_id, requested_quantity, " +
                     "unit_id, request_notes, status) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, req.getFoodListingId());
            ps.setLong(2, req.getNgoUserId());
            ps.setBigDecimal(3, req.getRequestedQuantity());
            ps.setInt(4, req.getUnitId());
            ps.setString(5, req.getRequestNotes());
            ps.setString(6, req.getStatus() != null ? req.getStatus() : "REQUESTED");

            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                req.setId(id);
                return id;
            }
            throw new SQLException("Failed to retrieve generated request ID.");
        } finally {
            closeQuietly(rs, ps);
        }
    }

    public FoodRequest findById(Long id) throws SQLException {
        String sql = "SELECT fr.*, fl.title AS food_title, fc.category_name AS food_category, mu.unit_name, " +
                     "COALESCE(np.organization_name, u_ngo.email) AS ngo_name, u_ngo.phone_number AS ngo_phone, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u_donor.email) AS donor_name, " +
                     "fl.remaining_quantity, fl.urgency_status " +
                     "FROM food_requests fr " +
                     "JOIN food_listings fl ON fr.food_listing_id = fl.id " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN measurement_units mu ON fr.unit_id = mu.id " +
                     "JOIN users u_ngo ON fr.ngo_user_id = u_ngo.id " +
                     "LEFT JOIN ngo_profiles np ON u_ngo.id = np.user_id " +
                     "JOIN users u_donor ON fl.donor_user_id = u_donor.id " +
                     "LEFT JOIN donor_profiles dp ON u_donor.id = dp.user_id " +
                     "WHERE fr.id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapFoodRequest(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public List<FoodRequest> findPendingByDonor(Long donorUserId) throws SQLException {
        List<FoodRequest> list = new ArrayList<>();
        String sql = "SELECT fr.*, fl.title AS food_title, fc.category_name AS food_category, mu.unit_name, " +
                     "COALESCE(np.organization_name, u_ngo.email) AS ngo_name, u_ngo.phone_number AS ngo_phone, " +
                     "'' AS donor_name, fl.remaining_quantity, fl.urgency_status " +
                     "FROM food_requests fr " +
                     "JOIN food_listings fl ON fr.food_listing_id = fl.id " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN measurement_units mu ON fr.unit_id = mu.id " +
                     "JOIN users u_ngo ON fr.ngo_user_id = u_ngo.id " +
                     "LEFT JOIN ngo_profiles np ON u_ngo.id = np.user_id " +
                     "WHERE fl.donor_user_id = ? AND fr.status = 'REQUESTED' " +
                     "ORDER BY fr.created_at ASC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, donorUserId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFoodRequest(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public List<FoodRequest> findByNgo(Long ngoUserId) throws SQLException {
        List<FoodRequest> list = new ArrayList<>();
        String sql = "SELECT fr.*, fl.title AS food_title, fc.category_name AS food_category, mu.unit_name, " +
                     "'' AS ngo_name, '' AS ngo_phone, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u_donor.email) AS donor_name, " +
                     "fl.remaining_quantity, fl.urgency_status " +
                     "FROM food_requests fr " +
                     "JOIN food_listings fl ON fr.food_listing_id = fl.id " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN measurement_units mu ON fr.unit_id = mu.id " +
                     "JOIN users u_donor ON fl.donor_user_id = u_donor.id " +
                     "LEFT JOIN donor_profiles dp ON u_donor.id = dp.user_id " +
                     "WHERE fr.ngo_user_id = ? ORDER BY fr.created_at DESC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, ngoUserId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFoodRequest(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public void updateStatus(Long requestId, String status, String donorResponseNotes, Connection conn) throws SQLException {
        String sql = "UPDATE food_requests SET status = ?, donor_response_notes = ?, responded_at = CURRENT_TIMESTAMP WHERE id = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, donorResponseNotes);
            ps.setLong(3, requestId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    private FoodRequest mapFoodRequest(ResultSet rs) throws SQLException {
        FoodRequest fr = new FoodRequest();
        fr.setId(rs.getLong("id"));
        fr.setFoodListingId(rs.getLong("food_listing_id"));
        fr.setNgoUserId(rs.getLong("ngo_user_id"));
        fr.setRequestedQuantity(rs.getBigDecimal("requested_quantity"));
        fr.setUnitId(rs.getInt("unit_id"));
        fr.setRequestNotes(rs.getString("request_notes"));
        fr.setStatus(rs.getString("status"));
        fr.setDonorResponseNotes(rs.getString("donor_response_notes"));
        fr.setRespondedAt(rs.getTimestamp("responded_at"));
        fr.setCreatedAt(rs.getTimestamp("created_at"));
        fr.setUpdatedAt(rs.getTimestamp("updated_at"));

        fr.setFoodTitle(rs.getString("food_title"));
        fr.setFoodCategory(rs.getString("food_category"));
        fr.setUnitName(rs.getString("unit_name"));
        fr.setNgoName(rs.getString("ngo_name"));
        fr.setNgoPhone(rs.getString("ngo_phone"));
        fr.setDonorName(rs.getString("donor_name"));
        fr.setRemainingQuantity(rs.getBigDecimal("remaining_quantity"));
        fr.setUrgencyStatus(rs.getString("urgency_status"));
        return fr;
    }
}
