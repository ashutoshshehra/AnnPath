package org.annpath.dao;

import org.annpath.model.Feedback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDao extends BaseDao {

    public Long save(Feedback feedback) throws SQLException {
        String sql = "INSERT INTO feedback (food_listing_id, pickup_id, submitter_user_id, target_user_id, rating_stars, comment, feedback_type) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, feedback.getFoodListingId());
            ps.setLong(2, feedback.getPickupId());
            ps.setLong(3, feedback.getSubmitterUserId());
            if (feedback.getTargetUserId() != null) {
                ps.setLong(4, feedback.getTargetUserId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }
            ps.setInt(5, feedback.getRatingStars());
            ps.setString(6, feedback.getComment());
            ps.setString(7, feedback.getFeedbackType());
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

    public List<Feedback> findByPickupId(Long pickupId) throws SQLException {
        String sql = "SELECT f.*, u_sub.email AS submitter_email, u_tar.email AS target_email " +
                     "FROM feedback f " +
                     "JOIN users u_sub ON f.submitter_user_id = u_sub.id " +
                     "LEFT JOIN users u_tar ON f.target_user_id = u_tar.id " +
                     "WHERE f.pickup_id = ? ORDER BY f.created_at DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Feedback> list = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, pickupId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFeedback(rs));
            }
            return list;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    public List<Feedback> findByTargetUserId(Long targetUserId) throws SQLException {
        String sql = "SELECT f.*, u_sub.email AS submitter_email, u_tar.email AS target_email " +
                     "FROM feedback f " +
                     "JOIN users u_sub ON f.submitter_user_id = u_sub.id " +
                     "LEFT JOIN users u_tar ON f.target_user_id = u_tar.id " +
                     "WHERE f.target_user_id = ? ORDER BY f.created_at DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Feedback> list = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, targetUserId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapFeedback(rs));
            }
            return list;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    public boolean hasSubmittedFeedback(Long pickupId, Long submitterUserId, String feedbackType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM feedback WHERE pickup_id = ? AND submitter_user_id = ? AND feedback_type = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, pickupId);
            ps.setLong(2, submitterUserId);
            ps.setString(3, feedbackType);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    public double getAverageRatingForUser(Long targetUserId) throws SQLException {
        String sql = "SELECT AVG(rating_stars) FROM feedback WHERE target_user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, targetUserId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 5.0;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    private Feedback mapFeedback(ResultSet rs) throws SQLException {
        Feedback fb = new Feedback();
        fb.setId(rs.getLong("id"));
        fb.setFoodListingId(rs.getLong("food_listing_id"));
        fb.setPickupId(rs.getLong("pickup_id"));
        fb.setSubmitterUserId(rs.getLong("submitter_user_id"));
        long target = rs.getLong("target_user_id");
        if (!rs.wasNull()) {
            fb.setTargetUserId(target);
        }
        fb.setRatingStars(rs.getInt("rating_stars"));
        fb.setComment(rs.getString("comment"));
        fb.setFeedbackType(rs.getString("feedback_type"));
        fb.setCreatedAt(rs.getTimestamp("created_at"));
        fb.setSubmitterName(rs.getString("submitter_email"));
        fb.setTargetName(rs.getString("target_email"));
        return fb;
    }
}
