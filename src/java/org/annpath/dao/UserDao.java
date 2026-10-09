package org.annpath.dao;

import org.annpath.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Level;

public class UserDao extends BaseDao {

    public User findById(Long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public User findByEmail(String email) throws SQLException {
        if (email == null) return null;
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, email.trim());
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public Long createUser(User user, Connection conn) throws SQLException {
        String sql = "INSERT INTO users (email, phone_number, phone_country_code, password_hash, password_salt, " +
                     "role, status, preferred_language, preferred_timezone, email_verified, phone_verified) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail().trim().toLowerCase());
            ps.setString(2, user.getPhoneNumber());
            ps.setString(3, user.getPhoneCountryCode());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getPasswordSalt());
            ps.setString(6, user.getRole());
            ps.setString(7, user.getStatus() != null ? user.getStatus() : "PENDING_VERIFICATION");
            ps.setString(8, user.getPreferredLanguage() != null ? user.getPreferredLanguage() : "en");
            ps.setString(9, user.getPreferredTimezone() != null ? user.getPreferredTimezone() : "UTC");
            ps.setBoolean(10, user.isEmailVerified());
            ps.setBoolean(11, user.isPhoneVerified());

            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long generatedId = rs.getLong(1);
                user.setId(generatedId);
                return generatedId;
            }
            throw new SQLException("Failed to retrieve generated user ID.");
        } finally {
            closeQuietly(rs, ps);
        }
    }

    public void updateVerification(Long userId, boolean emailVerified, boolean phoneVerified) throws SQLException {
        String sql = "UPDATE users SET email_verified = ?, phone_verified = ?, status = 'ACTIVE' WHERE id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setBoolean(1, emailVerified);
            ps.setBoolean(2, phoneVerified);
            ps.setLong(3, userId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }

    public void recordFailedLogin(Long userId, int currentAttempts, int lockoutMinutes) throws SQLException {
        String sql = "UPDATE users SET failed_login_attempts = ?, locked_until = ? WHERE id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            int newAttempts = currentAttempts + 1;
            ps.setInt(1, newAttempts);
            if (newAttempts >= 5) {
                Timestamp lockUntil = new Timestamp(System.currentTimeMillis() + (lockoutMinutes * 60L * 1000L));
                ps.setTimestamp(2, lockUntil);
            } else {
                ps.setNull(2, java.sql.Types.TIMESTAMP);
            }
            ps.setLong(3, userId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }

    public void resetFailedLogin(Long userId) throws SQLException {
        String sql = "UPDATE users SET failed_login_attempts = 0, locked_until = NULL WHERE id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }

    public void updatePassword(Long userId, String passwordHash, String passwordSalt) throws SQLException {
        String sql = "UPDATE users SET password_hash = ?, password_salt = ?, failed_login_attempts = 0, locked_until = NULL WHERE id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, passwordHash);
            ps.setString(2, passwordSalt);
            ps.setLong(3, userId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }

    public void updatePreferences(Long userId, String language, String timezone) throws SQLException {
        String sql = "UPDATE users SET preferred_language = ?, preferred_timezone = ? WHERE id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, language);
            ps.setString(2, timezone);
            ps.setLong(3, userId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setPhoneNumber(rs.getString("phone_number"));
        u.setPhoneCountryCode(rs.getString("phone_country_code"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setPasswordSalt(rs.getString("password_salt"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getString("status"));
        u.setPreferredLanguage(rs.getString("preferred_language"));
        u.setPreferredTimezone(rs.getString("preferred_timezone"));
        u.setEmailVerified(rs.getBoolean("email_verified"));
        u.setPhoneVerified(rs.getBoolean("phone_verified"));
        u.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        u.setLockedUntil(rs.getTimestamp("locked_until"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setUpdatedAt(rs.getTimestamp("updated_at"));
        return u;
    }
}
