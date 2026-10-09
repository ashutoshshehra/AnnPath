package org.annpath.dao;

import org.annpath.model.NgoProfile;
import org.annpath.model.VerificationDocument;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NgoDao extends BaseDao {

    public NgoProfile findByUserId(Long userId) throws SQLException {
        String sql = "SELECT * FROM ngo_profiles WHERE user_id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapNgoProfile(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public void save(NgoProfile profile, Connection conn) throws SQLException {
        String sql = "INSERT INTO ngo_profiles (user_id, organization_name, registration_number, registration_authority, " +
                     "established_year, representative_name, representative_designation, website, address_line1, address_line2, " +
                     "city, state_region, postal_code, country_code, latitude, longitude, service_radius_km, verification_status, max_daily_capacity_kg) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE organization_name = VALUES(organization_name), registration_number = VALUES(registration_number), " +
                     "registration_authority = VALUES(registration_authority), established_year = VALUES(established_year), " +
                     "representative_name = VALUES(representative_name), representative_designation = VALUES(representative_designation), " +
                     "website = VALUES(website), address_line1 = VALUES(address_line1), address_line2 = VALUES(address_line2), " +
                     "city = VALUES(city), state_region = VALUES(state_region), postal_code = VALUES(postal_code), " +
                     "country_code = VALUES(country_code), latitude = VALUES(latitude), longitude = VALUES(longitude), " +
                     "service_radius_km = VALUES(service_radius_km), max_daily_capacity_kg = VALUES(max_daily_capacity_kg)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, profile.getUserId());
            ps.setString(2, profile.getOrganizationName() != null ? profile.getOrganizationName() : "Pending Organization");
            ps.setString(3, profile.getRegistrationNumber() != null ? profile.getRegistrationNumber() : "PENDING");
            ps.setString(4, profile.getRegistrationAuthority() != null ? profile.getRegistrationAuthority() : "Pending Authority");
            ps.setInt(5, profile.getEstablishedYear() > 1900 ? profile.getEstablishedYear() : 2020);
            ps.setString(6, profile.getRepresentativeName() != null ? profile.getRepresentativeName() : "Representative");
            ps.setString(7, profile.getRepresentativeDesignation() != null ? profile.getRepresentativeDesignation() : "Officer");
            ps.setString(8, profile.getWebsite());
            ps.setString(9, profile.getAddressLine1() != null ? profile.getAddressLine1() : "Pending Address");
            ps.setString(10, profile.getAddressLine2());
            ps.setString(11, profile.getCity() != null ? profile.getCity() : "City");
            ps.setString(12, profile.getStateRegion() != null ? profile.getStateRegion() : "State");
            ps.setString(13, profile.getPostalCode() != null ? profile.getPostalCode() : "000000");
            ps.setString(14, profile.getCountryCode() != null ? profile.getCountryCode() : "IN");
            ps.setBigDecimal(15, profile.getLatitude());
            ps.setBigDecimal(16, profile.getLongitude());
            ps.setBigDecimal(17, profile.getServiceRadiusKm() != null ? profile.getServiceRadiusKm() : new java.math.BigDecimal("50.00"));
            ps.setString(18, profile.getVerificationStatus() != null ? profile.getVerificationStatus() : "VERIFIED");
            ps.setBigDecimal(19, profile.getMaxDailyCapacityKg() != null ? profile.getMaxDailyCapacityKg() : new java.math.BigDecimal("500.00"));
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
        }
    }

    public void update(NgoProfile profile) throws SQLException {
        Connection conn = getConnection();
        try {
            save(profile, conn);
        } finally {
            releaseConnection(conn);
        }
    }

    public void updateVerificationStatus(Long userId, String status, String rejectionReason) throws SQLException {
        String sql = "UPDATE ngo_profiles SET verification_status = ?, rejection_reason = ?, " +
                     "verified_at = CASE WHEN ? = 'VERIFIED' THEN CURRENT_TIMESTAMP ELSE verified_at END WHERE user_id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, rejectionReason);
            ps.setString(3, status);
            ps.setLong(4, userId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }

    public void saveVerificationDocument(VerificationDocument doc) throws SQLException {
        String sql = "INSERT INTO verification_documents (ngo_user_id, document_type, original_file_name, " +
                     "stored_file_name, file_path, file_size_bytes, mime_type, file_hash_sha256) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, doc.getNgoUserId());
            ps.setString(2, doc.getDocumentType());
            ps.setString(3, doc.getOriginalFileName());
            ps.setString(4, doc.getStoredFileName());
            ps.setString(5, doc.getFilePath());
            ps.setLong(6, doc.getFileSizeBytes());
            ps.setString(7, doc.getMimeType());
            ps.setString(8, doc.getFileHashSha256());
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }

    public List<VerificationDocument> getDocumentsByNgoId(Long ngoUserId) throws SQLException {
        List<VerificationDocument> list = new ArrayList<>();
        String sql = "SELECT * FROM verification_documents WHERE ngo_user_id = ? ORDER BY uploaded_at DESC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, ngoUserId);
            rs = ps.executeQuery();
            while (rs.next()) {
                VerificationDocument doc = new VerificationDocument();
                doc.setId(rs.getLong("id"));
                doc.setNgoUserId(rs.getLong("ngo_user_id"));
                doc.setDocumentType(rs.getString("document_type"));
                doc.setOriginalFileName(rs.getString("original_file_name"));
                doc.setStoredFileName(rs.getString("stored_file_name"));
                doc.setFilePath(rs.getString("file_path"));
                doc.setFileSizeBytes(rs.getLong("file_size_bytes"));
                doc.setMimeType(rs.getString("mime_type"));
                doc.setFileHashSha256(rs.getString("file_hash_sha256"));
                doc.setUploadedAt(rs.getTimestamp("uploaded_at"));
                list.add(doc);
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public List<NgoProfile> findVerifiedNgosInBoundingBox(double minLat, double maxLat, double minLng, double maxLng) throws SQLException {
        List<NgoProfile> list = new ArrayList<>();
        String sql = "SELECT * FROM ngo_profiles WHERE verification_status = 'VERIFIED' " +
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
                list.add(mapNgoProfile(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    private NgoProfile mapNgoProfile(ResultSet rs) throws SQLException {
        NgoProfile p = new NgoProfile();
        p.setUserId(rs.getLong("user_id"));
        p.setOrganizationName(rs.getString("organization_name"));
        p.setRegistrationNumber(rs.getString("registration_number"));
        p.setRegistrationAuthority(rs.getString("registration_authority"));
        p.setEstablishedYear(rs.getInt("established_year"));
        p.setRepresentativeName(rs.getString("representative_name"));
        p.setRepresentativeDesignation(rs.getString("representative_designation"));
        p.setWebsite(rs.getString("website"));
        p.setAddressLine1(rs.getString("address_line1"));
        p.setAddressLine2(rs.getString("address_line2"));
        p.setCity(rs.getString("city"));
        p.setStateRegion(rs.getString("state_region"));
        p.setPostalCode(rs.getString("postal_code"));
        p.setCountryCode(rs.getString("country_code"));
        p.setLatitude(rs.getBigDecimal("latitude"));
        p.setLongitude(rs.getBigDecimal("longitude"));
        p.setServiceRadiusKm(rs.getBigDecimal("service_radius_km"));
        p.setVerificationStatus(rs.getString("verification_status"));
        p.setVerifiedAt(rs.getTimestamp("verified_at"));
        p.setRejectionReason(rs.getString("rejection_reason"));
        p.setMaxDailyCapacityKg(rs.getBigDecimal("max_daily_capacity_kg"));
        return p;
    }
}
