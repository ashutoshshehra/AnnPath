package org.annpath.dao;

import org.annpath.model.ImpactRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImpactDao extends BaseDao {

    public void save(ImpactRecord record, Connection conn) throws SQLException {
        String sql = "INSERT INTO impact_records (food_listing_id, pickup_id, ngo_user_id, " +
                     "donor_user_id, volunteer_user_id, rescued_quantity_kg, estimated_people_fed, co2_emissions_prevented_kg) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, record.getFoodListingId());
            ps.setLong(2, record.getPickupId());
            ps.setLong(3, record.getNgoUserId());
            ps.setLong(4, record.getDonorUserId());
            if (record.getVolunteerUserId() != null) {
                ps.setLong(5, record.getVolunteerUserId());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            ps.setBigDecimal(6, record.getRescuedQuantityKg());
            ps.setInt(7, record.getEstimatedPeopleFed());
            ps.setBigDecimal(8, record.getCo2EmissionsPreventedKg());
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    /**
     * Compute actual aggregated impact metrics directly from immutable database records.
     * Never generates synthetic/fake data.
     */
    public Map<String, Object> getGlobalImpactSummary() throws SQLException {
        Map<String, Object> map = new HashMap<>();
        String sql = "SELECT " +
                     "COALESCE(SUM(rescued_quantity_kg), 0) AS total_kg, " +
                     "COALESCE(SUM(estimated_people_fed), 0) AS total_meals, " +
                     "COALESCE(SUM(co2_emissions_prevented_kg), 0) AS total_co2, " +
                     "COUNT(*) AS total_rescues " +
                     "FROM impact_records";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                map.put("totalRescuedKg", rs.getBigDecimal("total_kg"));
                map.put("totalMealsFed", rs.getLong("total_meals"));
                map.put("totalCo2PreventedKg", rs.getBigDecimal("total_co2"));
                map.put("totalRescuesCount", rs.getLong("total_rescues"));
            } else {
                map.put("totalRescuedKg", BigDecimal.ZERO);
                map.put("totalMealsFed", 0L);
                map.put("totalCo2PreventedKg", BigDecimal.ZERO);
                map.put("totalRescuesCount", 0L);
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }

        // Active network stats
        String netSql = "SELECT " +
                        "(SELECT COUNT(*) FROM users WHERE role = 'DONOR' AND status = 'ACTIVE') AS active_donors, " +
                        "(SELECT COUNT(*) FROM ngo_profiles WHERE verification_status = 'VERIFIED') AS verified_ngos, " +
                        "(SELECT COUNT(*) FROM volunteer_profiles WHERE availability_status = 'AVAILABLE') AS active_volunteers";
        conn = getConnection();
        try {
            ps = conn.prepareStatement(netSql);
            rs = ps.executeQuery();
            if (rs.next()) {
                map.put("activeDonors", rs.getInt("active_donors"));
                map.put("verifiedNgos", rs.getInt("verified_ngos"));
                map.put("activeVolunteers", rs.getInt("active_volunteers"));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }

        return map;
    }

    /**
     * Compute live operational network bottleneck counts directly from active records.
     */
    public Map<String, Object> getNetworkBottleneckMetrics() throws SQLException {
        Map<String, Object> map = new HashMap<>();
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM food_listings WHERE listing_status NOT IN ('EXPIRED', 'CANCELLED', 'DISTRIBUTED') AND urgency_status != 'EXPIRED' AND remaining_quantity > 0 AND available_until > CURRENT_TIMESTAMP) AS unmatched_surplus, " +
                     "(SELECT COUNT(*) FROM pickups WHERE status = 'ASSIGNED' AND volunteer_user_id IS NULL) AS awaiting_transit, " +
                     "(SELECT COUNT(*) FROM pickups WHERE status IN ('ASSIGNED', 'PICKED_UP') AND volunteer_user_id IS NOT NULL) AS active_rescues, " +
                     "(SELECT COUNT(*) FROM volunteer_profiles WHERE availability_status = 'AVAILABLE') AS available_fleet";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                map.put("unmatchedSurplus", rs.getInt("unmatched_surplus"));
                map.put("awaitingTransit", rs.getInt("awaiting_transit"));
                map.put("activeRescues", rs.getInt("active_rescues"));
                map.put("availableFleet", rs.getInt("available_fleet"));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return map;
    }

    public List<Map<String, Object>> getRecentImpactActivities(int limit) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT ir.*, fl.title AS food_title, fc.category_name, " +
                     "COALESCE(dp.organization_name, dp.contact_person, 'Anonymous Donor') AS donor_name, " +
                     "COALESCE(np.organization_name, 'Relief Partner') AS ngo_name, " +
                     "p.status AS pickup_status " +
                     "FROM impact_records ir " +
                     "JOIN food_listings fl ON ir.food_listing_id = fl.id " +
                     "JOIN food_categories fc ON fl.food_category_id = fc.id " +
                     "JOIN pickups p ON ir.pickup_id = p.id " +
                     "LEFT JOIN donor_profiles dp ON ir.donor_user_id = dp.user_id " +
                     "LEFT JOIN ngo_profiles np ON ir.ngo_user_id = np.user_id " +
                     "ORDER BY ir.recorded_at DESC LIMIT ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, limit > 0 ? limit : 10);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getLong("id"));
                row.put("pickupId", rs.getLong("pickup_id"));
                row.put("foodTitle", rs.getString("food_title"));
                row.put("categoryName", rs.getString("category_name"));
                row.put("donorName", rs.getString("donor_name"));
                row.put("ngoName", rs.getString("ngo_name"));
                row.put("rescuedQuantityKg", rs.getBigDecimal("rescued_quantity_kg"));
                row.put("estimatedPeopleFed", rs.getInt("estimated_people_fed"));
                row.put("co2SavedKg", rs.getBigDecimal("co2_emissions_prevented_kg"));
                row.put("createdAt", rs.getTimestamp("recorded_at"));
                list.add(row);
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }
}
