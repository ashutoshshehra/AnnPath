package org.annpath.dao;

import org.annpath.model.Complaint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDao extends BaseDao {

    public Long save(Complaint complaint) throws SQLException {
        String sql = "INSERT INTO complaints (reporter_user_id, reported_user_id, food_listing_id, pickup_id, category, description, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, complaint.getReporterUserId());
            if (complaint.getReportedUserId() != null) {
                ps.setLong(2, complaint.getReportedUserId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            if (complaint.getFoodListingId() != null) {
                ps.setLong(3, complaint.getFoodListingId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            if (complaint.getPickupId() != null) {
                ps.setLong(4, complaint.getPickupId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }
            ps.setString(5, complaint.getCategory());
            ps.setString(6, complaint.getDescription());
            ps.setString(7, complaint.getStatus() != null ? complaint.getStatus() : "OPEN");
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return null;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    public Complaint findById(Long id) throws SQLException {
        String sql = "SELECT c.*, u_rep.email AS reporter_email, u_tg.email AS reported_email " +
                     "FROM complaints c " +
                     "JOIN users u_rep ON c.reporter_user_id = u_rep.id " +
                     "LEFT JOIN users u_tg ON c.reported_user_id = u_tg.id " +
                     "WHERE c.id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapComplaint(rs);
            }
            return null;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    public List<Complaint> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT c.*, u_rep.email AS reporter_email, u_tg.email AS reported_email " +
                     "FROM complaints c " +
                     "JOIN users u_rep ON c.reporter_user_id = u_rep.id " +
                     "LEFT JOIN users u_tg ON c.reported_user_id = u_tg.id " +
                     "WHERE c.reporter_user_id = ? OR c.reported_user_id = ? " +
                     "ORDER BY c.created_at DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Complaint> list = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            ps.setLong(2, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapComplaint(rs));
            }
            return list;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    public boolean updateStatus(Long id, String status, String resolutionNotes) throws SQLException {
        String sql = "UPDATE complaints SET status = ?, resolution_notes = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, resolutionNotes);
            ps.setLong(3, id);
            return ps.executeUpdate() > 0;
        } finally {
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    private Complaint mapComplaint(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setId(rs.getLong("id"));
        c.setReporterUserId(rs.getLong("reporter_user_id"));
        long repTarget = rs.getLong("reported_user_id");
        if (!rs.wasNull()) {
            c.setReportedUserId(repTarget);
        }
        long flId = rs.getLong("food_listing_id");
        if (!rs.wasNull()) {
            c.setFoodListingId(flId);
        }
        long pId = rs.getLong("pickup_id");
        if (!rs.wasNull()) {
            c.setPickupId(pId);
        }
        c.setCategory(rs.getString("category"));
        c.setDescription(rs.getString("description"));
        c.setStatus(rs.getString("status"));
        c.setResolutionNotes(rs.getString("resolution_notes"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setUpdatedAt(rs.getTimestamp("updated_at"));
        c.setReporterName(rs.getString("reporter_email"));
        c.setReportedName(rs.getString("reported_email"));
        return c;
    }
}
