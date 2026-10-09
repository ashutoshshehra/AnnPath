package org.annpath.dao;

import org.annpath.model.FoodListing;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FoodListingDao extends BaseDao {

    public Long createListing(FoodListing listing, Connection conn) throws SQLException {
        String sql = "INSERT INTO food_listings (donor_user_id, title, food_category_id, dietary_type, total_quantity, " +
                     "remaining_quantity, reserved_quantity, unit_id, storage_condition, packaging_type, description, " +
                     "ingredients, allergens, preparation_time, available_from, available_until, urgency_status, " +
                     "listing_status, pickup_address, pickup_city, pickup_region, pickup_postal_code, pickup_country_code, " +
                     "pickup_latitude, pickup_longitude, pickup_instructions, contact_name, contact_phone, food_image_path, " +
                     "priority_score, priority_explanation, version) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, listing.getDonorUserId());
            ps.setString(2, listing.getTitle());
            ps.setInt(3, listing.getFoodCategoryId());
            ps.setString(4, listing.getDietaryType());
            ps.setBigDecimal(5, listing.getTotalQuantity());
            ps.setBigDecimal(6, listing.getRemainingQuantity());
            ps.setBigDecimal(7, listing.getReservedQuantity() != null ? listing.getReservedQuantity() : BigDecimal.ZERO);
            ps.setInt(8, listing.getUnitId());
            ps.setString(9, listing.getStorageCondition());
            ps.setString(10, listing.getPackagingType());
            ps.setString(11, listing.getDescription());
            ps.setString(12, listing.getIngredients());
            ps.setString(13, listing.getAllergens());
            ps.setTimestamp(14, listing.getPreparationTime());
            ps.setTimestamp(15, listing.getAvailableFrom());
            ps.setTimestamp(16, listing.getAvailableUntil());
            ps.setString(17, listing.getUrgencyStatus() != null ? listing.getUrgencyStatus() : "FRESH");
            ps.setString(18, listing.getListingStatus() != null ? listing.getListingStatus() : "DONATED");
            ps.setString(19, listing.getPickupAddress());
            ps.setString(20, listing.getPickupCity());
            ps.setString(21, listing.getPickupRegion());
            ps.setString(22, listing.getPickupPostalCode());
            ps.setString(23, listing.getPickupCountryCode());
            ps.setBigDecimal(24, listing.getPickupLatitude());
            ps.setBigDecimal(25, listing.getPickupLongitude());
            ps.setString(26, listing.getPickupInstructions());
            ps.setString(27, listing.getContactName());
            ps.setString(28, listing.getContactPhone());
            ps.setString(29, listing.getFoodImagePath());
            ps.setBigDecimal(30, listing.getPriorityScore() != null ? listing.getPriorityScore() : BigDecimal.ZERO);
            ps.setString(31, listing.getPriorityExplanation());

            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                listing.setId(id);
                return id;
            }
            throw new SQLException("Failed to retrieve generated food listing ID.");
        } finally {
            closeQuietly(rs, ps);
        }
    }

    public FoodListing findById(Long id) throws SQLException {
        String sql = "SELECT fl.*, fc.category_name, mu.unit_name, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u.email) AS donor_name, dp.donor_type " +
                     "FROM food_listings fl " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN measurement_units mu ON fl.unit_id = mu.id " +
                     "JOIN users u ON fl.donor_user_id = u.id " +
                     "LEFT JOIN donor_profiles dp ON u.id = dp.user_id " +
                     "WHERE fl.id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapFoodListing(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    /**
     * Row-level locking for atomic partial rescue and race condition prevention.
     */
    public FoodListing findByIdForUpdate(Long id, Connection conn) throws SQLException {
        String sql = "SELECT fl.*, fc.category_name, mu.unit_name, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u.email) AS donor_name, dp.donor_type " +
                     "FROM food_listings fl " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN measurement_units mu ON fl.unit_id = mu.id " +
                     "JOIN users u ON fl.donor_user_id = u.id " +
                     "LEFT JOIN donor_profiles dp ON u.id = dp.user_id " +
                     "WHERE fl.id = ? FOR UPDATE";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapFoodListing(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
        }
    }

    public FoodListing findByRescueCode(String rescueCode) throws SQLException {
        String sql = "SELECT fl.*, fc.category_name, mu.unit_name, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u.email) AS donor_name, " +
                     "dp.donor_type " +
                     "FROM food_listings fl " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN measurement_units mu ON fl.unit_id = mu.id " +
                     "JOIN users u ON fl.donor_user_id = u.id " +
                     "LEFT JOIN donor_profiles dp ON u.id = dp.user_id " +
                     "WHERE fl.rescue_code = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, rescueCode);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapFoodListing(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    /**
     * Atomically reserve quantity and increment optimistic version.
     * Returns true if update succeeded, false if version mismatch or insufficient quantity.
     */
    public boolean reserveQuantity(Long id, BigDecimal quantity, int expectedVersion, Connection conn) throws SQLException {
        String sql = "UPDATE food_listings SET remaining_quantity = remaining_quantity - ?, " +
                     "reserved_quantity = reserved_quantity + ?, version = version + 1 " +
                     "WHERE id = ? AND version = ? AND remaining_quantity >= ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setBigDecimal(1, quantity);
            ps.setBigDecimal(2, quantity);
            ps.setLong(3, id);
            ps.setInt(4, expectedVersion);
            ps.setBigDecimal(5, quantity);
            int rows = ps.executeUpdate();
            return rows > 0;
        } finally {
            closeQuietly(ps);
        }
    }

    public void releaseReservedQuantity(Long id, BigDecimal quantity, Connection conn) throws SQLException {
        String sql = "UPDATE food_listings SET remaining_quantity = remaining_quantity + ?, " +
                     "reserved_quantity = GREATEST(0, reserved_quantity - ?), version = version + 1 " +
                     "WHERE id = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setBigDecimal(1, quantity);
            ps.setBigDecimal(2, quantity);
            ps.setLong(3, id);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public void updateStatus(Long id, String status) throws SQLException {
        updateStatus(id, status, null);
    }

    public void updateStatus(Long id, String status, Connection conn) throws SQLException {
        boolean ownConn = (conn == null);
        Connection activeConn = ownConn ? getConnection() : conn;
        String sql = "UPDATE food_listings SET listing_status = ?, version = version + 1 WHERE id = ?";
        PreparedStatement ps = null;
        try {
            ps = activeConn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            if (ownConn) {
                releaseConnection(activeConn);
            }
        }
    }

    public void updateUrgencyAndPriority(Long id, String urgencyStatus, BigDecimal priorityScore, String explanation) throws SQLException {
        String sql = "UPDATE food_listings SET urgency_status = ?, priority_score = ?, priority_explanation = ? WHERE id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, urgencyStatus);
            ps.setBigDecimal(2, priorityScore);
            ps.setString(3, explanation);
            ps.setLong(4, id);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }

    public List<FoodListing> findByDonor(Long donorUserId) throws SQLException {
        List<FoodListing> list = new ArrayList<>();
        String sql = "SELECT fl.*, fc.category_name, mu.unit_name, '' AS donor_name, '' AS donor_type " +
                     "FROM food_listings fl " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN measurement_units mu ON fl.unit_id = mu.id " +
                     "WHERE fl.donor_user_id = ? ORDER BY fl.created_at DESC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, donorUserId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFoodListing(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public List<FoodListing> findAvailableListings(double minLat, double maxLat, double minLng, double maxLng,
                                                  Integer categoryId, String dietaryType) throws SQLException {
        List<FoodListing> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT fl.*, fc.category_name, mu.unit_name, " +
                "COALESCE(dp.organization_name, dp.contact_person, u.email) AS donor_name, dp.donor_type " +
                "FROM food_listings fl " +
                "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                "JOIN measurement_units mu ON fl.unit_id = mu.id " +
                "JOIN users u ON fl.donor_user_id = u.id " +
                "LEFT JOIN donor_profiles dp ON u.id = dp.user_id " +
                "WHERE fl.listing_status NOT IN ('EXPIRED', 'CANCELLED', 'DISTRIBUTED') " +
                "AND fl.urgency_status != 'EXPIRED' " +
                "AND fl.remaining_quantity > 0 " +
                "AND fl.available_until > CURRENT_TIMESTAMP " +
                "AND fl.pickup_latitude BETWEEN ? AND ? " +
                "AND fl.pickup_longitude BETWEEN ? AND ? "
        );

        if (categoryId != null && categoryId > 0) {
            sql.append("AND fl.food_category_id = ? ");
        }
        if (dietaryType != null && !dietaryType.trim().isEmpty() && !"ANY".equalsIgnoreCase(dietaryType)) {
            sql.append("AND fl.dietary_type = ? ");
        }

        sql.append("ORDER BY fl.priority_score DESC, fl.available_until ASC");

        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql.toString());
            int idx = 1;
            ps.setDouble(idx++, minLat);
            ps.setDouble(idx++, maxLat);
            ps.setDouble(idx++, minLng);
            ps.setDouble(idx++, maxLng);
            if (categoryId != null && categoryId > 0) {
                ps.setInt(idx++, categoryId);
            }
            if (dietaryType != null && !dietaryType.trim().isEmpty() && !"ANY".equalsIgnoreCase(dietaryType)) {
                ps.setString(idx++, dietaryType);
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFoodListing(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public List<FoodListing> findAllAvailableListings(Integer categoryId, String dietaryType) throws SQLException {
        List<FoodListing> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT fl.*, fc.category_name, mu.unit_name, " +
                "COALESCE(dp.organization_name, dp.contact_person, u.email) AS donor_name, dp.donor_type " +
                "FROM food_listings fl " +
                "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                "JOIN measurement_units mu ON fl.unit_id = mu.id " +
                "JOIN users u ON fl.donor_user_id = u.id " +
                "LEFT JOIN donor_profiles dp ON u.id = dp.user_id " +
                "WHERE fl.listing_status NOT IN ('EXPIRED', 'CANCELLED', 'DISTRIBUTED') " +
                "AND fl.urgency_status != 'EXPIRED' " +
                "AND fl.remaining_quantity > 0 " +
                "AND fl.available_until > CURRENT_TIMESTAMP "
        );

        if (categoryId != null && categoryId > 0) {
            sql.append("AND fl.food_category_id = ? ");
        }
        if (dietaryType != null && !dietaryType.trim().isEmpty() && !"ANY".equalsIgnoreCase(dietaryType)) {
            sql.append("AND fl.dietary_type = ? ");
        }

        sql.append("ORDER BY fl.priority_score DESC, fl.available_until ASC");

        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (categoryId != null && categoryId > 0) {
                ps.setInt(idx++, categoryId);
            }
            if (dietaryType != null && !dietaryType.trim().isEmpty() && !"ANY".equalsIgnoreCase(dietaryType)) {
                ps.setString(idx++, dietaryType);
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFoodListing(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public List<FoodListing> findUrgentListings(int limit) throws SQLException {
        List<FoodListing> list = new ArrayList<>();
        String sql = "SELECT fl.*, fc.category_name, mu.unit_name, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u.email) AS donor_name, dp.donor_type " +
                     "FROM food_listings fl " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN measurement_units mu ON fl.unit_id = mu.id " +
                     "JOIN users u ON fl.donor_user_id = u.id " +
                     "LEFT JOIN donor_profiles dp ON u.id = dp.user_id " +
                     "WHERE fl.listing_status NOT IN ('EXPIRED', 'CANCELLED', 'DISTRIBUTED') " +
                     "AND fl.urgency_status != 'EXPIRED' " +
                     "AND fl.remaining_quantity > 0 " +
                     "AND fl.available_until > CURRENT_TIMESTAMP " +
                     "ORDER BY fl.priority_score DESC, fl.available_until ASC LIMIT ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, limit > 0 ? limit : 6);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFoodListing(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public List<FoodListing> findActiveListingsForUrgencyScan() throws SQLException {
        List<FoodListing> list = new ArrayList<>();
        String sql = "SELECT fl.*, fc.category_name, mu.unit_name, '' AS donor_name, '' AS donor_type " +
                     "FROM food_listings fl " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN measurement_units mu ON fl.unit_id = mu.id " +
                     "WHERE fl.listing_status NOT IN ('DISTRIBUTED', 'CANCELLED', 'EXPIRED')";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFoodListing(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    private FoodListing mapFoodListing(ResultSet rs) throws SQLException {
        FoodListing fl = new FoodListing();
        fl.setId(rs.getLong("id"));
        try {
            fl.setRescueCode(rs.getString("rescue_code"));
        } catch (SQLException ignored) {}
        fl.setDonorUserId(rs.getLong("donor_user_id"));
        fl.setTitle(rs.getString("title"));
        fl.setFoodCategoryId(rs.getInt("food_category_id"));
        fl.setDietaryType(rs.getString("dietary_type"));
        fl.setTotalQuantity(rs.getBigDecimal("total_quantity"));
        fl.setRemainingQuantity(rs.getBigDecimal("remaining_quantity"));
        fl.setReservedQuantity(rs.getBigDecimal("reserved_quantity"));
        fl.setUnitId(rs.getInt("unit_id"));
        fl.setStorageCondition(rs.getString("storage_condition"));
        fl.setPackagingType(rs.getString("packaging_type"));
        fl.setDescription(rs.getString("description"));
        fl.setIngredients(rs.getString("ingredients"));
        fl.setAllergens(rs.getString("allergens"));
        fl.setPreparationTime(rs.getTimestamp("preparation_time"));
        fl.setAvailableFrom(rs.getTimestamp("available_from"));
        fl.setAvailableUntil(rs.getTimestamp("available_until"));
        fl.setUrgencyStatus(rs.getString("urgency_status"));
        fl.setListingStatus(rs.getString("listing_status"));
        fl.setPickupAddress(rs.getString("pickup_address"));
        fl.setPickupCity(rs.getString("pickup_city"));
        fl.setPickupRegion(rs.getString("pickup_region"));
        fl.setPickupPostalCode(rs.getString("pickup_postal_code"));
        fl.setPickupCountryCode(rs.getString("pickup_country_code"));
        fl.setPickupLatitude(rs.getBigDecimal("pickup_latitude"));
        fl.setPickupLongitude(rs.getBigDecimal("pickup_longitude"));
        fl.setPickupInstructions(rs.getString("pickup_instructions"));
        fl.setContactName(rs.getString("contact_name"));
        fl.setContactPhone(rs.getString("contact_phone"));
        fl.setFoodImagePath(rs.getString("food_image_path"));
        fl.setPriorityScore(rs.getBigDecimal("priority_score"));
        fl.setPriorityExplanation(rs.getString("priority_explanation"));
        fl.setVersion(rs.getInt("version"));
        fl.setCreatedAt(rs.getTimestamp("created_at"));
        fl.setUpdatedAt(rs.getTimestamp("updated_at"));

        fl.setCategoryName(rs.getString("category_name"));
        fl.setUnitName(rs.getString("unit_name"));
        fl.setDonorName(rs.getString("donor_name"));
        fl.setDonorType(rs.getString("donor_type"));
        return fl;
    }
}
