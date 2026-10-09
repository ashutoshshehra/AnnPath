package org.annpath.dao;

import org.annpath.model.VolunteerProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VolunteerDao extends BaseDao {

    public VolunteerProfile findByUserId(Long userId) throws SQLException {
        String sql = "SELECT * FROM volunteer_profiles WHERE user_id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapVolunteerProfile(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public void save(VolunteerProfile profile, Connection conn) throws SQLException {
        String sql = "INSERT INTO volunteer_profiles (user_id, first_name, last_name, address_line1, city, state_region, " +
                     "postal_code, country_code, latitude, longitude, max_travel_distance_km, vehicle_type, availability_status, " +
                     "max_concurrent_assignments, emergency_contact_phone) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), " +
                     "address_line1 = VALUES(address_line1), city = VALUES(city), state_region = VALUES(state_region), " +
                     "postal_code = VALUES(postal_code), country_code = VALUES(country_code), latitude = VALUES(latitude), " +
                     "longitude = VALUES(longitude), max_travel_distance_km = VALUES(max_travel_distance_km), " +
                     "vehicle_type = VALUES(vehicle_type), availability_status = VALUES(availability_status), " +
                     "max_concurrent_assignments = VALUES(max_concurrent_assignments), emergency_contact_phone = VALUES(emergency_contact_phone)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, profile.getUserId());
            ps.setString(2, profile.getFirstName() != null ? profile.getFirstName() : "Volunteer");
            ps.setString(3, profile.getLastName() != null ? profile.getLastName() : "User");
            ps.setString(4, profile.getAddressLine1() != null ? profile.getAddressLine1() : "Pending Address");
            ps.setString(5, profile.getCity() != null ? profile.getCity() : "City");
            ps.setString(6, profile.getStateRegion() != null ? profile.getStateRegion() : "State");
            ps.setString(7, profile.getPostalCode() != null ? profile.getPostalCode() : "000000");
            ps.setString(8, profile.getCountryCode() != null ? profile.getCountryCode() : "IN");
            ps.setBigDecimal(9, profile.getLatitude());
            ps.setBigDecimal(10, profile.getLongitude());
            ps.setBigDecimal(11, profile.getMaxTravelDistanceKm() != null ? profile.getMaxTravelDistanceKm() : new java.math.BigDecimal("10.00"));
            ps.setString(12, profile.getVehicleType() != null ? profile.getVehicleType() : "MOTORCYCLE");
            ps.setString(13, profile.getAvailabilityStatus() != null ? profile.getAvailabilityStatus() : "AVAILABLE");
            ps.setInt(14, profile.getMaxConcurrentAssignments() > 0 ? profile.getMaxConcurrentAssignments() : 2);
            ps.setString(15, profile.getEmergencyContactPhone());
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public void update(VolunteerProfile profile) throws SQLException {
        Connection conn = getConnection();
        try {
            save(profile, conn);
        } finally {
            releaseConnection(conn);
        }
    }

    public void updateAvailability(Long userId, String status) throws SQLException {
        String sql = "UPDATE volunteer_profiles SET availability_status = ? WHERE user_id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }

    public void adjustActiveAssignments(Long userId, int delta, Connection conn) throws SQLException {
        String sql = "UPDATE volunteer_profiles SET active_assignments_count = GREATEST(0, active_assignments_count + ?) WHERE user_id = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, delta);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public List<VolunteerProfile> findAvailableVolunteersInBoundingBox(double minLat, double maxLat, double minLng, double maxLng) throws SQLException {
        List<VolunteerProfile> list = new ArrayList<>();
        String sql = "SELECT * FROM volunteer_profiles WHERE availability_status = 'AVAILABLE' " +
                     "AND active_assignments_count < max_concurrent_assignments " +
                     "AND latitude BETWEEN ? AND ? AND longitude BETWEEN ? AND ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setDouble(1, minLat);
            ps.setDouble(2, maxLat);
            ps.setDouble(3, minLng);
            ps.setDouble(4, maxLng);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapVolunteerProfile(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public List<VolunteerProfile> findAllAvailableVolunteers() throws SQLException {
        List<VolunteerProfile> list = new ArrayList<>();
        String sql = "SELECT * FROM volunteer_profiles WHERE availability_status = 'AVAILABLE' " +
                     "AND active_assignments_count < max_concurrent_assignments";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapVolunteerProfile(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    private VolunteerProfile mapVolunteerProfile(ResultSet rs) throws SQLException {
        VolunteerProfile p = new VolunteerProfile();
        p.setUserId(rs.getLong("user_id"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        p.setAddressLine1(rs.getString("address_line1"));
        p.setCity(rs.getString("city"));
        p.setStateRegion(rs.getString("state_region"));
        p.setPostalCode(rs.getString("postal_code"));
        p.setCountryCode(rs.getString("country_code"));
        p.setLatitude(rs.getBigDecimal("latitude"));
        p.setLongitude(rs.getBigDecimal("longitude"));
        p.setMaxTravelDistanceKm(rs.getBigDecimal("max_travel_distance_km"));
        p.setVehicleType(rs.getString("vehicle_type"));
        p.setAvailabilityStatus(rs.getString("availability_status"));
        p.setActiveAssignmentsCount(rs.getInt("active_assignments_count"));
        p.setMaxConcurrentAssignments(rs.getInt("max_concurrent_assignments"));
        p.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
        return p;
    }
}
