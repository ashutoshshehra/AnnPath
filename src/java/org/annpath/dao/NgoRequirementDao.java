package org.annpath.dao;

import org.annpath.model.NgoRequirement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NgoRequirementDao extends BaseDao {

    public Long create(NgoRequirement req) throws SQLException {
        String sql = "INSERT INTO ngo_requirements (ngo_user_id, title, food_category_id, dietary_type, " +
                     "min_quantity, max_quantity, unit_id, is_recurring, priority_level, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, req.getNgoUserId());
            ps.setString(2, req.getTitle());
            ps.setInt(3, req.getFoodCategoryId());
            ps.setString(4, req.getDietaryType());
            ps.setBigDecimal(5, req.getMinQuantity());
            ps.setBigDecimal(6, req.getMaxQuantity());
            ps.setInt(7, req.getUnitId());
            ps.setBoolean(8, req.isRecurring());
            ps.setString(9, req.getPriorityLevel());
            ps.setBoolean(10, req.isActive());

            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                req.setId(id);
                return id;
            }
            throw new SQLException("Failed to retrieve generated requirement ID.");
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }

    public List<NgoRequirement> findByNgoId(Long ngoUserId) throws SQLException {
        List<NgoRequirement> list = new ArrayList<>();
        String sql = "SELECT nr.*, fc.category_name, mu.unit_name, '' AS ngo_name " +
                     "FROM ngo_requirements nr " +
                     "JOIN food_categories fc ON nr.food_category_id = fc.id " +
                     "JOIN measurement_units mu ON nr.unit_id = mu.id " +
                     "WHERE nr.ngo_user_id = ? AND nr.is_active = TRUE ORDER BY nr.created_at DESC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, ngoUserId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRequirement(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public List<NgoRequirement> findAllActive() throws SQLException {
        List<NgoRequirement> list = new ArrayList<>();
        String sql = "SELECT nr.*, fc.category_name, mu.unit_name, " +
                     "COALESCE(np.organization_name, u.email) AS ngo_name " +
                     "FROM ngo_requirements nr " +
                     "JOIN food_categories fc ON nr.food_category_id = fc.id " +
                     "JOIN measurement_units mu ON nr.unit_id = mu.id " +
                     "JOIN users u ON nr.ngo_user_id = u.id " +
                     "LEFT JOIN ngo_profiles np ON u.id = np.user_id " +
                     "WHERE nr.is_active = TRUE";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRequirement(rs));
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public void deactivate(Long id, Long ngoUserId) throws SQLException {
        String sql = "UPDATE ngo_requirements SET is_active = FALSE WHERE id = ? AND ngo_user_id = ?";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ps.setLong(2, ngoUserId);
            ps.executeUpdate();
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }

    private NgoRequirement mapRequirement(ResultSet rs) throws SQLException {
        NgoRequirement r = new NgoRequirement();
        r.setId(rs.getLong("id"));
        r.setNgoUserId(rs.getLong("ngo_user_id"));
        r.setTitle(rs.getString("title"));
        r.setFoodCategoryId(rs.getInt("food_category_id"));
        r.setDietaryType(rs.getString("dietary_type"));
        r.setMinQuantity(rs.getBigDecimal("min_quantity"));
        r.setMaxQuantity(rs.getBigDecimal("max_quantity"));
        r.setUnitId(rs.getInt("unit_id"));
        r.setRecurring(rs.getBoolean("is_recurring"));
        r.setPriorityLevel(rs.getString("priority_level"));
        r.setActive(rs.getBoolean("is_active"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setCategoryName(rs.getString("category_name"));
        r.setUnitName(rs.getString("unit_name"));
        r.setNgoName(rs.getString("ngo_name"));
        return r;
    }
}
