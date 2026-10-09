package org.annpath.dao;

import org.annpath.model.DigitalRescueReceipt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReceiptDao extends BaseDao {

    public Long save(DigitalRescueReceipt receipt, Connection conn) throws SQLException {
        String sql = "INSERT INTO digital_rescue_receipts (receipt_number, food_listing_id, pickup_id, " +
                     "donor_user_id, ngo_user_id, volunteer_user_id, rescued_quantity, unit_name, " +
                     "food_category_name, pickup_timestamp, delivery_timestamp, distribution_timestamp, " +
                     "distance_covered_km, verification_hash_sha256) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, receipt.getReceiptNumber());
            ps.setLong(2, receipt.getFoodListingId());
            ps.setLong(3, receipt.getPickupId());
            ps.setLong(4, receipt.getDonorUserId());
            ps.setLong(5, receipt.getNgoUserId());
            if (receipt.getVolunteerUserId() != null) {
                ps.setLong(6, receipt.getVolunteerUserId());
            } else {
                ps.setNull(6, java.sql.Types.BIGINT);
            }
            ps.setBigDecimal(7, receipt.getRescuedQuantity());
            ps.setString(8, receipt.getUnitName());
            ps.setString(9, receipt.getFoodCategoryName());
            ps.setTimestamp(10, receipt.getPickupTimestamp());
            ps.setTimestamp(11, receipt.getDeliveryTimestamp());
            ps.setTimestamp(12, receipt.getDistributionTimestamp());
            ps.setBigDecimal(13, receipt.getDistanceCoveredKm());
            ps.setString(14, receipt.getVerificationHashSha256());
            ps.executeUpdate();
            return receipt.getPickupId();
        } finally {
            closeQuietly(ps);
        }
    }

    public DigitalRescueReceipt findByPickupId(Long pickupId) throws SQLException {
        String sql = "SELECT drr.*, fl.title AS food_title, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u_d.email) AS donor_name, " +
                     "dp.address_line1 AS donor_address, " +
                     "COALESCE(np.organization_name, u_n.email) AS ngo_name, " +
                     "np.address_line1 AS ngo_address, " +
                     "CONCAT(COALESCE(vp.first_name,''), ' ', COALESCE(vp.last_name,'')) AS volunteer_name " +
                     "FROM digital_rescue_receipts drr " +
                     "JOIN food_listings fl ON drr.food_listing_id = fl.id " +
                     "JOIN users u_d ON drr.donor_user_id = u_d.id " +
                     "LEFT JOIN donor_profiles dp ON u_d.id = dp.user_id " +
                     "JOIN users u_n ON drr.ngo_user_id = u_n.id " +
                     "LEFT JOIN ngo_profiles np ON u_n.id = np.user_id " +
                     "LEFT JOIN users u_v ON drr.volunteer_user_id = u_v.id " +
                     "LEFT JOIN volunteer_profiles vp ON u_v.id = vp.user_id " +
                     "WHERE drr.pickup_id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, pickupId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapReceipt(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public DigitalRescueReceipt findByReceiptNumber(String receiptNumber) throws SQLException {
        String sql = "SELECT drr.*, fl.title AS food_title, " +
                     "COALESCE(dp.organization_name, dp.contact_person, u_d.email) AS donor_name, " +
                     "dp.address_line1 AS donor_address, " +
                     "COALESCE(np.organization_name, u_n.email) AS ngo_name, " +
                     "np.address_line1 AS ngo_address, " +
                     "CONCAT(COALESCE(vp.first_name,''), ' ', COALESCE(vp.last_name,'')) AS volunteer_name " +
                     "FROM digital_rescue_receipts drr " +
                     "JOIN food_listings fl ON drr.food_listing_id = fl.id " +
                     "JOIN users u_d ON drr.donor_user_id = u_d.id " +
                     "LEFT JOIN donor_profiles dp ON u_d.id = dp.user_id " +
                     "JOIN users u_n ON drr.ngo_user_id = u_n.id " +
                     "LEFT JOIN ngo_profiles np ON u_n.id = np.user_id " +
                     "LEFT JOIN users u_v ON drr.volunteer_user_id = u_v.id " +
                     "LEFT JOIN volunteer_profiles vp ON u_v.id = vp.user_id " +
                     "WHERE drr.receipt_number = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, receiptNumber.trim());
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapReceipt(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    private DigitalRescueReceipt mapReceipt(ResultSet rs) throws SQLException {
        DigitalRescueReceipt r = new DigitalRescueReceipt();
        r.setId(rs.getLong("id"));
        r.setReceiptNumber(rs.getString("receipt_number"));
        r.setFoodListingId(rs.getLong("food_listing_id"));
        r.setPickupId(rs.getLong("pickup_id"));
        r.setDonorUserId(rs.getLong("donor_user_id"));
        r.setNgoUserId(rs.getLong("ngo_user_id"));
        r.setVolunteerUserId(rs.getObject("volunteer_user_id") != null ? rs.getLong("volunteer_user_id") : null);
        r.setRescuedQuantity(rs.getBigDecimal("rescued_quantity"));
        r.setUnitName(rs.getString("unit_name"));
        r.setFoodCategoryName(rs.getString("food_category_name"));
        r.setPickupTimestamp(rs.getTimestamp("pickup_timestamp"));
        r.setDeliveryTimestamp(rs.getTimestamp("delivery_timestamp"));
        r.setDistributionTimestamp(rs.getTimestamp("distribution_timestamp"));
        r.setDistanceCoveredKm(rs.getBigDecimal("distance_covered_km"));
        r.setVerificationHashSha256(rs.getString("verification_hash_sha256"));
        r.setIssuedAt(rs.getTimestamp("issued_at"));

        r.setFoodTitle(rs.getString("food_title"));
        r.setDonorName(rs.getString("donor_name"));
        r.setDonorAddress(rs.getString("donor_address"));
        r.setNgoName(rs.getString("ngo_name"));
        r.setNgoAddress(rs.getString("ngo_address"));
        r.setVolunteerName(rs.getString("volunteer_name"));

        if (r.getRescuedQuantity() != null) {
            int meals = (int) Math.round(r.getRescuedQuantity().doubleValue() / 0.4);
            r.setEstimatedPeopleFed(meals);
            r.setCo2SavedKg(r.getRescuedQuantity().multiply(new java.math.BigDecimal("2.5")));
        }
        return r;
    }
}
