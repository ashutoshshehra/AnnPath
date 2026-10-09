package org.annpath.dao;

import org.annpath.model.AuditLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditLogDao extends BaseDao {

    public void log(AuditLog entry) {
        String sql = "INSERT INTO audit_logs (user_id, user_role, event_type, ip_address, user_agent, " +
                     "action_description, affected_entity, entity_id, status, details_json) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            if (entry.getUserId() != null) {
                ps.setLong(1, entry.getUserId());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }
            ps.setString(2, entry.getUserRole());
            ps.setString(3, entry.getEventType());
            ps.setString(4, entry.getIpAddress() != null ? entry.getIpAddress() : "0.0.0.0");
            ps.setString(5, entry.getUserAgent());
            ps.setString(6, entry.getActionDescription());
            ps.setString(7, entry.getAffectedEntity());
            if (entry.getEntityId() != null) {
                ps.setLong(8, entry.getEntityId());
            } else {
                ps.setNull(8, java.sql.Types.BIGINT);
            }
            ps.setString(9, entry.getStatus() != null ? entry.getStatus() : "SUCCESS");
            ps.setString(10, entry.getDetailsJson());
            ps.executeUpdate();
        } catch (Exception ex) {
            logger.warning("Failed to record audit log: " + ex.getMessage());
        } finally {
            closeQuietly(ps);
            if (conn != null) releaseConnection(conn);
        }
    }
}
