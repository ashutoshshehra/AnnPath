package org.annpath.dao;

import org.annpath.model.DonorProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DonorDao extends BaseDao {

    public DonorProfile findByUserId(Long userId) throws SQLException {
        String sql = "SELECT * FROM donor_profiles WHERE user_id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapDonorProfile(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public void save(DonorProfile profile, Connection conn) throws SQLException {
        String sql = "INSERT INTO donor_profiles (user_id, donor_type, organization_name, contact_person, " +
                     "address_line1, address_line2, city, state_region, postal_code, country_code, latitude, longitude, default_pickup_instructions) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE donor_type = VALUES(donor_type), organization_name = VALUES(organization_name), " +
                     "contact_person = VALUES(contact_person), address_line1 = VALUES(address_line1), address_line2 = VALUES(address_line2), " +
                     "city = VALUES(city), state_region = VALUES(state_region), postal_code = VALUES(postal_code), " +
                     "country_code = VALUES(country_code), latitude = VALUES(latitude), longitude = VALUES(longitude), " +
                     "default_pickup_instructions = VALUES(default_pickup_instructions)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, profile.getUserId());
            ps.setString(2, profile.getDonorType() != null ? profile.getDonorType() : "INDIVIDUAL");
            ps.setString(3, profile.getOrganizationName());
            ps.setString(4, profile.getContactPerson() != null ? profile.getContactPerson() : "Contact Person");
            ps.setString(5, profile.getAddressLine1() != null ? profile.getAddressLine1() : "Address Line 1");
            ps.setString(6, profile.getAddressLine2());
            ps.setString(7, profile.getCity() != null ? profile.getCity() : "City");
            ps.setString(8, profile.getStateRegion() != null ? profile.getStateRegion() : "State");
            ps.setString(9, profile.getPostalCode() != null ? profile.getPostalCode() : "000000");
            ps.setString(10, profile.getCountryCode() != null ? profile.getCountryCode() : "IN");
            ps.setBigDecimal(11, profile.getLatitude());
            ps.setBigDecimal(12, profile.getLongitude());
            ps.setString(13, profile.getDefaultPickupInstructions());
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public void update(DonorProfile profile) throws SQLException {
        Connection conn = getConnection();
        try {
            save(profile, conn);
        } finally {
            releaseConnection(conn);
        }
    }

    private DonorProfile mapDonorProfile(ResultSet rs) throws SQLException {
        DonorProfile dp = new DonorProfile();
        dp.setUserId(rs.getLong("user_id"));
        dp.setDonorType(rs.getString("donor_type"));
        dp.setOrganizationName(rs.getString("organization_name"));
        dp.setContactPerson(rs.getString("contact_person"));
        dp.setAddressLine1(rs.getString("address_line1"));
        dp.setAddressLine2(rs.getString("address_line2"));
        dp.setCity(rs.getString("city"));
        dp.setStateRegion(rs.getString("state_region"));
        dp.setPostalCode(rs.getString("postal_code"));
        dp.setCountryCode(rs.getString("country_code"));
        dp.setLatitude(rs.getBigDecimal("latitude"));
        dp.setLongitude(rs.getBigDecimal("longitude"));
        dp.setDefaultPickupInstructions(rs.getString("default_pickup_instructions"));
        return dp;
    }
}
