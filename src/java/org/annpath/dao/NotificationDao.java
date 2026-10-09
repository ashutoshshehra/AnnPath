package org.annpath.dao;

import org.annpath.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao extends BaseDao {

    public Long send(Notification notif) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, notification_type, title, message, " +
                     "reference_type, reference_id, channel, is_read) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, notif.getUserId());
            ps.setString(2, notif.getNotificationType());
            ps.setString(3, notif.getTitle());
            ps.setString(4, notif.getMessage());
            ps.setString(5, notif.getReferenceType());
            if (notif.getReferenceId() != null) {
                ps.setLong(6, notif.getReferenceId());
            } else {
                ps.setNull(6, java.sql.Types.BIGINT);
            }
            ps.setString(7, notif.getChannel() != null ? notif.getChannel() : "IN_APP");
            ps.setBoolean(8, notif.isRead());

            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                notif.setId(id);
                return id;
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public List<Notification> findByUser(Long userId, int limit) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY sent_at DESC LIMIT ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            ps.setInt(2, limit > 0 ? limit : 20);
            rs = ps.executeQuery();
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getLong("id"));
                n.setUserId(rs.getLong("user_id"));
                n.setNotificationType(rs.getString("notification_type"));
                n.setTitle(rs.getString("title"));
                n.setMessage(rs.getString("message"));
                n.setReferenceType(rs.getString("reference_type"));
                n.setReferenceId(rs.getObject("reference_id") != null ? rs.getLong("reference_id") : null);
                n.setChannel(rs.getString("channel"));
                n.setRead(rs.getBoolean("is_read"));
                n.setSentAt(rs.getTimestamp("sent_at"));
                n.setReadAt(rs.getTimestamp("read_at"));
                list.add(n);
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public int getUnreadCount(Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
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

    public void markAllRead(Long userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE, read_at = CURRENT_TIMESTAMP WHERE user_id = ? AND is_read = FALSE";
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
}
