package org.annpath.dao;

import org.annpath.model.OtpVerification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class OtpDao extends BaseDao {

    public Long save(OtpVerification otp) throws SQLException {
        // Invalidate old pending OTPs for the same identifier & purpose
        invalidatePreviousOtps(otp.getIdentifier(), otp.getPurpose());

        String sql = "INSERT INTO otp_verifications (user_id, identifier, channel, purpose, " +
                     "otp_hash, otp_salt, attempts_count, max_attempts, expires_at, cooldown_until, is_invalidated) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?, ?, FALSE)";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (otp.getUserId() != null) {
                ps.setLong(1, otp.getUserId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, otp.getIdentifier().trim().toLowerCase());
            ps.setString(3, otp.getChannel());
            ps.setString(4, otp.getPurpose());
            ps.setString(5, otp.getOtpHash());
            ps.setString(6, otp.getOtpSalt());
            ps.setInt(7, otp.getMaxAttempts() > 0 ? otp.getMaxAttempts() : 3);
            ps.setTimestamp(8, otp.getExpiresAt());
            ps.setTimestamp(9, otp.getCooldownUntil());

            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                otp.setId(id);
                return id;
            }
            throw new SQLException("Failed to retrieve generated OTP ID.");
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public OtpVerification findLatestValid(String identifier, String purpose) throws SQLException {
        String sql = "SELECT * FROM otp_verifications " +
                     "WHERE LOWER(identifier) = LOWER(?) AND purpose = ? AND is_invalidated = FALSE " +
                     "AND verified_at IS NULL " +
                     "ORDER BY id DESC LIMIT 1";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, identifier.trim());
            ps.setString(2, purpose);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapOtp(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public void incrementAttempts(Long otpId) throws SQLException {
        String sql = "UPDATE otp_verifications SET attempts_count = attempts_count + 1 WHERE id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, otpId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }

    public void markVerified(Long otpId) throws SQLException {
        String sql = "UPDATE otp_verifications SET verified_at = CURRENT_TIMESTAMP, is_invalidated = TRUE WHERE id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, otpId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }

    public void invalidatePreviousOtps(String identifier, String purpose) throws SQLException {
        String sql = "UPDATE otp_verifications SET is_invalidated = TRUE " +
                     "WHERE LOWER(identifier) = LOWER(?) AND purpose = ? AND verified_at IS NULL";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, identifier.trim());
            ps.setString(2, purpose);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }

    private OtpVerification mapOtp(ResultSet rs) throws SQLException {
        OtpVerification o = new OtpVerification();
        o.setId(rs.getLong("id"));
        long uid = rs.getLong("user_id");
        if (!rs.wasNull()) {
            o.setUserId(uid);
        } else {
            o.setUserId(null);
        }
        o.setIdentifier(rs.getString("identifier"));
        o.setChannel(rs.getString("channel"));
        o.setPurpose(rs.getString("purpose"));
        o.setOtpHash(rs.getString("otp_hash"));
        o.setOtpSalt(rs.getString("otp_salt"));
        o.setAttemptsCount(rs.getInt("attempts_count"));
        o.setMaxAttempts(rs.getInt("max_attempts"));
        o.setExpiresAt(rs.getTimestamp("expires_at"));
        o.setVerifiedAt(rs.getTimestamp("verified_at"));
        o.setCooldownUntil(rs.getTimestamp("cooldown_until"));
        o.setInvalidated(rs.getBoolean("is_invalidated"));
        return o;
    }
}
