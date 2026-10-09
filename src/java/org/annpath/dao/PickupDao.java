package org.annpath.dao;

import org.annpath.model.Pickup;
import org.annpath.model.PickupAssignmentHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PickupDao extends BaseDao {

    public Long create(Pickup pickup, Connection conn) throws SQLException {
        String sql = "INSERT INTO pickups (food_listing_id, food_request_id, donor_user_id, ngo_user_id, " +
                     "pickup_method, volunteer_user_id, pickup_window_start, pickup_window_end, " +
                     "pickup_otp_hash, delivery_otp_hash, status, version) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, pickup.getFoodListingId());
            ps.setLong(2, pickup.getFoodRequestId());
            ps.setLong(3, pickup.getDonorUserId());
            ps.setLong(4, pickup.getNgoUserId());
            ps.setString(5, pickup.getPickupMethod() != null ? pickup.getPickupMethod() : "VOLUNTEER");
            if (pickup.getVolunteerUserId() != null) {
                ps.setLong(6, pickup.getVolunteerUserId());
            } else {
                ps.setNull(6, java.sql.Types.BIGINT);
            }
            ps.setTimestamp(7, pickup.getPickupWindowStart());
            ps.setTimestamp(8, pickup.getPickupWindowEnd());
            ps.setString(9, pickup.getPickupOtpHash());
            ps.setString(10, pickup.getDeliveryOtpHash());
            ps.setString(11, pickup.getStatus() != null ? pickup.getStatus() : "ASSIGNED");

            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                pickup.setId(id);
                return id;
            }
            throw new SQLException("Failed to retrieve generated pickup ID.");
        } finally {
            closeQuietly(rs, ps);
        }
    }

    public Pickup findById(Long id) throws SQLException {
        String sql = "SELECT p.*, fl.title AS food_title, fc.category_name AS food_category, " +
                     "fr.requested_quantity AS quantity, mu.unit_name, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u_d.email) AS donor_name, " +
                     "fl.contact_phone AS donor_phone, fl.pickup_address, " +
                     "COALESCE(np.organization_name, u_n.email) AS ngo_name, " +
                     "u_n.phone_number AS ngo_phone, np.address_line1 AS ngo_address, " +
                     "CONCAT(COALESCE(vp.first_name,''), ' ', COALESCE(vp.last_name,'')) AS volunteer_name, " +
                     "u_v.phone_number AS volunteer_phone " +
                     "FROM pickups p " +
                     "JOIN food_listings fl ON p.food_listing_id = fl.id " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN food_requests fr ON p.food_request_id = fr.id " +
                     "JOIN measurement_units mu ON fr.unit_id = mu.id " +
                     "JOIN users u_d ON p.donor_user_id = u_d.id " +
                     "LEFT JOIN donor_profiles dp ON u_d.id = dp.user_id " +
                     "JOIN users u_n ON p.ngo_user_id = u_n.id " +
                     "LEFT JOIN ngo_profiles np ON u_n.id = np.user_id " +
                     "LEFT JOIN users u_v ON p.volunteer_user_id = u_v.id " +
                     "LEFT JOIN volunteer_profiles vp ON u_v.id = vp.user_id " +
                     "WHERE p.id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapPickup(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public Pickup findLatestActiveOrCompleted() throws SQLException {
        String sql = "SELECT p.*, fl.title AS food_title, fc.category_name AS food_category, " +
                     "fr.requested_quantity AS quantity, mu.unit_name, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u_d.email) AS donor_name, " +
                     "fl.contact_phone AS donor_phone, fl.pickup_address, " +
                     "COALESCE(np.organization_name, u_n.email) AS ngo_name, " +
                     "u_n.phone_number AS ngo_phone, np.address_line1 AS ngo_address, " +
                     "CONCAT(COALESCE(vp.first_name,''), ' ', COALESCE(vp.last_name,'')) AS volunteer_name, " +
                     "u_v.phone_number AS volunteer_phone " +
                     "FROM pickups p " +
                     "JOIN food_listings fl ON p.food_listing_id = fl.id " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN food_requests fr ON p.food_request_id = fr.id " +
                     "JOIN measurement_units mu ON fr.unit_id = mu.id " +
                     "JOIN users u_d ON p.donor_user_id = u_d.id " +
                     "LEFT JOIN donor_profiles dp ON u_d.id = dp.user_id " +
                     "JOIN users u_n ON p.ngo_user_id = u_n.id " +
                     "LEFT JOIN ngo_profiles np ON u_n.id = np.user_id " +
                     "LEFT JOIN users u_v ON p.volunteer_user_id = u_v.id " +
                     "LEFT JOIN volunteer_profiles vp ON u_v.id = vp.user_id " +
                     "ORDER BY p.id DESC LIMIT 1";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapPickup(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public Pickup findByIdForUpdate(Long id, Connection conn) throws SQLException {
        String sql = "SELECT p.*, fl.title AS food_title, fc.category_name AS food_category, " +
                     "fr.requested_quantity AS quantity, mu.unit_name, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u_d.email) AS donor_name, " +
                     "fl.contact_phone AS donor_phone, fl.pickup_address, " +
                     "COALESCE(np.organization_name, u_n.email) AS ngo_name, " +
                     "u_n.phone_number AS ngo_phone, np.address_line1 AS ngo_address, " +
                     "CONCAT(COALESCE(vp.first_name,''), ' ', COALESCE(vp.last_name,'')) AS volunteer_name, " +
                     "u_v.phone_number AS volunteer_phone " +
                     "FROM pickups p " +
                     "JOIN food_listings fl ON p.food_listing_id = fl.id " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN food_requests fr ON p.food_request_id = fr.id " +
                     "JOIN measurement_units mu ON fr.unit_id = mu.id " +
                     "JOIN users u_d ON p.donor_user_id = u_d.id " +
                     "LEFT JOIN donor_profiles dp ON u_d.id = dp.user_id " +
                     "JOIN users u_n ON p.ngo_user_id = u_n.id " +
                     "LEFT JOIN ngo_profiles np ON u_n.id = np.user_id " +
                     "LEFT JOIN users u_v ON p.volunteer_user_id = u_v.id " +
                     "LEFT JOIN volunteer_profiles vp ON u_v.id = vp.user_id " +
                     "WHERE p.id = ? FOR UPDATE";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapPickup(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
        }
    }

    public Pickup findByFoodListingId(Long foodListingId) throws SQLException {
        String sql = "SELECT p.*, fl.title AS food_title, fc.category_name AS food_category, " +
                     "fr.requested_quantity AS quantity, mu.unit_name, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u_d.email) AS donor_name, " +
                     "fl.contact_phone AS donor_phone, fl.pickup_address, " +
                     "COALESCE(np.organization_name, u_n.email) AS ngo_name, " +
                     "u_n.phone_number AS ngo_phone, np.address_line1 AS ngo_address, " +
                     "CONCAT(COALESCE(vp.first_name,''), ' ', COALESCE(vp.last_name,'')) AS volunteer_name, " +
                     "u_v.phone_number AS volunteer_phone " +
                     "FROM pickups p " +
                     "JOIN food_listings fl ON p.food_listing_id = fl.id " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN food_requests fr ON p.food_request_id = fr.id " +
                     "JOIN measurement_units mu ON fr.unit_id = mu.id " +
                     "JOIN users u_d ON p.donor_user_id = u_d.id " +
                     "LEFT JOIN donor_profiles dp ON u_d.id = dp.user_id " +
                     "JOIN users u_n ON p.ngo_user_id = u_n.id " +
                     "LEFT JOIN ngo_profiles np ON u_n.id = np.user_id " +
                     "LEFT JOIN users u_v ON p.volunteer_user_id = u_v.id " +
                     "LEFT JOIN volunteer_profiles vp ON u_v.id = vp.user_id " +
                     "WHERE p.food_listing_id = ? ORDER BY p.created_at DESC LIMIT 1";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, foodListingId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapPickup(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public List<Pickup> findByVolunteer(Long volunteerUserId) throws SQLException {
        List<Pickup> list = new ArrayList<>();
        String sql = "SELECT p.*, fl.title AS food_title, fc.category_name AS food_category, " +
                     "fr.requested_quantity AS quantity, mu.unit_name, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u_d.email) AS donor_name, " +
                     "fl.contact_phone AS donor_phone, fl.pickup_address, " +
                     "COALESCE(np.organization_name, u_n.email) AS ngo_name, " +
                     "u_n.phone_number AS ngo_phone, np.address_line1 AS ngo_address, " +
                     "'' AS volunteer_name, '' AS volunteer_phone " +
                     "FROM pickups p " +
                     "JOIN food_listings fl ON p.food_listing_id = fl.id " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN food_requests fr ON p.food_request_id = fr.id " +
                     "JOIN measurement_units mu ON fr.unit_id = mu.id " +
                     "JOIN users u_d ON p.donor_user_id = u_d.id " +
                     "LEFT JOIN donor_profiles dp ON u_d.id = dp.user_id " +
                     "JOIN users u_n ON p.ngo_user_id = u_n.id " +
                     "LEFT JOIN ngo_profiles np ON u_n.id = np.user_id " +
                     "WHERE p.volunteer_user_id = ? ORDER BY p.created_at DESC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, volunteerUserId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPickup(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public List<Pickup> findBroadcastPickups() throws SQLException {
        List<Pickup> list = new ArrayList<>();
        String sql = "SELECT p.*, fl.title AS food_title, fc.category_name AS food_category, " +
                     "fr.requested_quantity AS quantity, mu.unit_name, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u_d.email) AS donor_name, " +
                     "fl.contact_phone AS donor_phone, fl.pickup_address, " +
                     "COALESCE(np.organization_name, u_n.email) AS ngo_name, " +
                     "u_n.phone_number AS ngo_phone, np.address_line1 AS ngo_address, " +
                     "'' AS volunteer_name, '' AS volunteer_phone " +
                     "FROM pickups p " +
                     "JOIN food_listings fl ON p.food_listing_id = fl.id " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN food_requests fr ON p.food_request_id = fr.id " +
                     "JOIN measurement_units mu ON fr.unit_id = mu.id " +
                     "JOIN users u_d ON p.donor_user_id = u_d.id " +
                     "LEFT JOIN donor_profiles dp ON u_d.id = dp.user_id " +
                     "JOIN users u_n ON p.ngo_user_id = u_n.id " +
                     "LEFT JOIN ngo_profiles np ON u_n.id = np.user_id " +
                     "WHERE p.status = 'ASSIGNED' AND p.volunteer_user_id IS NULL " +
                     "ORDER BY p.created_at DESC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPickup(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public boolean claimBroadcastPickup(Long pickupId, Long volunteerUserId, Connection conn) throws SQLException {
        String sql = "UPDATE pickups SET volunteer_user_id = ?, status = 'ASSIGNED', assigned_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ? AND (volunteer_user_id IS NULL OR volunteer_user_id = ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, volunteerUserId);
            ps.setLong(2, pickupId);
            ps.setLong(3, volunteerUserId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } finally {
            closeQuietly(ps);
        }
    }

    public void unclaimPickup(Long pickupId, Long volunteerUserId, Connection conn) throws SQLException {
        String sql = "UPDATE pickups SET volunteer_user_id = NULL, status = 'ASSIGNED', updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ? AND volunteer_user_id = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, pickupId);
            ps.setLong(2, volunteerUserId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public List<Pickup> findOpenPickupsForVolunteers(Long excludeVolunteerUserId) throws SQLException {
        return findBroadcastPickups();
    }

    public void claimPickup(Long pickupId, Long volunteerUserId, Connection conn) throws SQLException {
        claimBroadcastPickup(pickupId, volunteerUserId, conn);
    }

    public List<Pickup> findByNgo(Long ngoUserId) throws SQLException {
        List<Pickup> list = new ArrayList<>();
        String sql = "SELECT p.*, fl.title AS food_title, fc.category_name AS food_category, " +
                     "fr.requested_quantity AS quantity, mu.unit_name, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u_d.email) AS donor_name, " +
                     "fl.contact_phone AS donor_phone, fl.pickup_address, " +
                     "'' AS ngo_name, '' AS ngo_phone, '' AS ngo_address, " +
                     "CONCAT(COALESCE(vp.first_name,''), ' ', COALESCE(vp.last_name,'')) AS volunteer_name, " +
                     "u_v.phone_number AS volunteer_phone " +
                     "FROM pickups p " +
                     "JOIN food_listings fl ON p.food_listing_id = fl.id " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN food_requests fr ON p.food_request_id = fr.id " +
                     "JOIN measurement_units mu ON fr.unit_id = mu.id " +
                     "JOIN users u_d ON p.donor_user_id = u_d.id " +
                     "LEFT JOIN donor_profiles dp ON u_d.id = dp.user_id " +
                     "LEFT JOIN users u_v ON p.volunteer_user_id = u_v.id " +
                     "LEFT JOIN volunteer_profiles vp ON u_v.id = vp.user_id " +
                     "WHERE p.ngo_user_id = ? ORDER BY p.created_at DESC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, ngoUserId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPickup(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public List<Pickup> findByDonor(Long donorUserId) throws SQLException {
        List<Pickup> list = new ArrayList<>();
        String sql = "SELECT p.*, fl.title AS food_title, fc.category_name AS food_category, " +
                     "fr.requested_quantity AS quantity, mu.unit_name, " +
                     "'' AS donor_name, '' AS donor_phone, fl.pickup_address, " +
                     "COALESCE(np.organization_name, u_n.email) AS ngo_name, " +
                     "u_n.phone_number AS ngo_phone, np.address_line1 AS ngo_address, " +
                     "CONCAT(COALESCE(vp.first_name,''), ' ', COALESCE(vp.last_name,'')) AS volunteer_name, " +
                     "u_v.phone_number AS volunteer_phone " +
                     "FROM pickups p " +
                     "JOIN food_listings fl ON p.food_listing_id = fl.id " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN food_requests fr ON p.food_request_id = fr.id " +
                     "JOIN measurement_units mu ON fr.unit_id = mu.id " +
                     "JOIN users u_n ON p.ngo_user_id = u_n.id " +
                     "LEFT JOIN ngo_profiles np ON u_n.id = np.user_id " +
                     "LEFT JOIN users u_v ON p.volunteer_user_id = u_v.id " +
                     "LEFT JOIN volunteer_profiles vp ON u_v.id = vp.user_id " +
                     "WHERE p.donor_user_id = ? ORDER BY p.created_at DESC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, donorUserId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPickup(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public void assignVolunteer(Long pickupId, Long volunteerUserId, Connection conn) throws SQLException {
        String sql = "UPDATE pickups SET volunteer_user_id = ?, status = 'ASSIGNED', assigned_at = CURRENT_TIMESTAMP, version = version + 1 WHERE id = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, volunteerUserId);
            ps.setLong(2, pickupId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public void assignSelfPickup(Long pickupId, String personName, String phone, String idType, String idNumber, Connection conn) throws SQLException {
        String sql = "UPDATE pickups SET pickup_method = 'NGO_SELF_PICKUP', volunteer_user_id = NULL, " +
                     "self_pickup_person_name = ?, self_pickup_person_phone = ?, " +
                     "self_pickup_id_type = ?, self_pickup_id_number = ?, " +
                     "status = 'NGO_SELF_PICKUP_ASSIGNED', version = version + 1 WHERE id = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, personName);
            ps.setString(2, phone);
            ps.setString(3, idType);
            ps.setString(4, idNumber);
            ps.setLong(5, pickupId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public void updateStatus(Long pickupId, String status) throws SQLException {
        updateStatus(pickupId, status, null);
    }

    public void updateStatus(Long pickupId, String status, Connection conn) throws SQLException {
        boolean ownConn = (conn == null);
        Connection activeConn = ownConn ? getConnection() : conn;
        String sql = "UPDATE pickups SET status = ?, version = version + 1 WHERE id = ?";
        PreparedStatement ps = null;
        try {
            ps = activeConn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setLong(2, pickupId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            if (ownConn) {
                releaseConnection(activeConn);
            }
        }
    }

    public void recordPickedUp(Long pickupId, Connection conn) throws SQLException {
        String sql = "UPDATE pickups SET status = 'PICKED_UP', actual_picked_up_at = CURRENT_TIMESTAMP, version = version + 1 WHERE id = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, pickupId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public void recordDelivered(Long pickupId, Connection conn) throws SQLException {
        String sql = "UPDATE pickups SET status = 'DELIVERED', actual_delivered_at = CURRENT_TIMESTAMP, version = version + 1 WHERE id = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, pickupId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public void recordReceived(Long pickupId, Connection conn) throws SQLException {
        String sql = "UPDATE pickups SET status = 'RECEIVED', ngo_received_at = CURRENT_TIMESTAMP, version = version + 1 WHERE id = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, pickupId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public void recordDistributed(Long pickupId, Connection conn) throws SQLException {
        String sql = "UPDATE pickups SET status = 'DISTRIBUTED', ngo_distributed_at = CURRENT_TIMESTAMP, version = version + 1 WHERE id = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, pickupId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public void logAssignmentAttempt(Long pickupId, Long volunteerUserId, Connection conn) throws SQLException {
        String sql = "INSERT INTO pickup_assignment_history (pickup_id, volunteer_user_id, assigned_at, response_status) " +
                     "VALUES (?, ?, CURRENT_TIMESTAMP, 'PENDING')";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, pickupId);
            ps.setLong(2, volunteerUserId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public void recordAssignmentResponse(Long pickupId, Long volunteerUserId, String responseStatus, String rejectionReason, Connection conn) throws SQLException {
        String sql = "UPDATE pickup_assignment_history SET response_status = ?, rejection_reason = ?, responded_at = CURRENT_TIMESTAMP " +
                     "WHERE pickup_id = ? AND volunteer_user_id = ? AND response_status = 'PENDING'";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, responseStatus);
            ps.setString(2, rejectionReason);
            ps.setLong(3, pickupId);
            ps.setLong(4, volunteerUserId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public List<PickupAssignmentHistory> getAssignmentHistory(Long pickupId) throws SQLException {
        List<PickupAssignmentHistory> list = new ArrayList<>();
        String sql = "SELECT pah.*, CONCAT(COALESCE(vp.first_name,''), ' ', COALESCE(vp.last_name,'')) AS volunteer_name, " +
                     "u.phone_number AS volunteer_phone " +
                     "FROM pickup_assignment_history pah " +
                     "JOIN users u ON pah.volunteer_user_id = u.id " +
                     "LEFT JOIN volunteer_profiles vp ON u.id = vp.user_id " +
                     "WHERE pah.pickup_id = ? ORDER BY pah.assigned_at ASC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, pickupId);
            rs = ps.executeQuery();
            while (rs.next()) {
                PickupAssignmentHistory h = new PickupAssignmentHistory();
                h.setId(rs.getLong("id"));
                h.setPickupId(rs.getLong("pickup_id"));
                h.setVolunteerUserId(rs.getLong("volunteer_user_id"));
                h.setAssignedAt(rs.getTimestamp("assigned_at"));
                h.setRespondedAt(rs.getTimestamp("responded_at"));
                h.setResponseStatus(rs.getString("response_status"));
                h.setRejectionReason(rs.getString("rejection_reason"));
                h.setVolunteerName(rs.getString("volunteer_name"));
                h.setVolunteerPhone(rs.getString("volunteer_phone"));
                list.add(h);
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public int countAssignmentAttempts(Long pickupId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pickup_assignment_history WHERE pickup_id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, pickupId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public Set<Long> findPastAssignedVolunteerIds(Long pickupId) throws SQLException {
        Set<Long> set = new HashSet<>();
        String sql = "SELECT DISTINCT volunteer_user_id FROM pickup_assignment_history WHERE pickup_id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, pickupId);
            rs = ps.executeQuery();
            while (rs.next()) {
                set.add(rs.getLong("volunteer_user_id"));
            }
            return set;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    private Pickup mapPickup(ResultSet rs) throws SQLException {
        Pickup p = new Pickup();
        p.setId(rs.getLong("id"));
        p.setFoodListingId(rs.getLong("food_listing_id"));
        p.setFoodRequestId(rs.getLong("food_request_id"));
        p.setDonorUserId(rs.getLong("donor_user_id"));
        p.setNgoUserId(rs.getLong("ngo_user_id"));
        p.setPickupMethod(rs.getString("pickup_method"));
        p.setVolunteerUserId(rs.getObject("volunteer_user_id") != null ? rs.getLong("volunteer_user_id") : null);
        p.setSelfPickupPersonName(rs.getString("self_pickup_person_name"));
        p.setSelfPickupPersonPhone(rs.getString("self_pickup_person_phone"));
        p.setSelfPickupIdType(rs.getString("self_pickup_id_type"));
        p.setSelfPickupIdNumber(rs.getString("self_pickup_id_number"));
        p.setPickupWindowStart(rs.getTimestamp("pickup_window_start"));
        p.setPickupWindowEnd(rs.getTimestamp("pickup_window_end"));
        p.setPickupOtpHash(rs.getString("pickup_otp_hash"));
        p.setDeliveryOtpHash(rs.getString("delivery_otp_hash"));
        p.setPickupNotes(rs.getString("pickup_notes"));
        p.setDeliveryNotes(rs.getString("delivery_notes"));
        p.setProofImagePath(rs.getString("proof_image_path"));
        p.setStatus(rs.getString("status"));
        p.setAssignedAt(rs.getTimestamp("assigned_at"));
        p.setActualPickedUpAt(rs.getTimestamp("actual_picked_up_at"));
        p.setActualDeliveredAt(rs.getTimestamp("actual_delivered_at"));
        p.setNgoReceivedAt(rs.getTimestamp("ngo_received_at"));
        p.setNgoDistributedAt(rs.getTimestamp("ngo_distributed_at"));
        p.setVersion(rs.getInt("version"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));

        p.setFoodTitle(rs.getString("food_title"));
        p.setFoodCategory(rs.getString("food_category"));
        p.setQuantity(rs.getBigDecimal("quantity"));
        p.setUnitName(rs.getString("unit_name"));
        p.setDonorName(rs.getString("donor_name"));
        p.setDonorPhone(rs.getString("donor_phone"));
        p.setPickupAddress(rs.getString("pickup_address"));
        p.setNgoName(rs.getString("ngo_name"));
        p.setNgoPhone(rs.getString("ngo_phone"));
        p.setNgoAddress(rs.getString("ngo_address"));
        p.setVolunteerName(rs.getString("volunteer_name"));
        p.setVolunteerPhone(rs.getString("volunteer_phone"));
        return p;
    }
}
