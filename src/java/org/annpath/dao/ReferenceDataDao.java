package org.annpath.dao;

import org.annpath.model.Country;
import org.annpath.model.FoodCategory;
import org.annpath.model.MeasurementUnit;
import org.annpath.model.Region;
import org.annpath.model.VerificationRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReferenceDataDao extends BaseDao {

    public List<Country> getAllCountries() throws SQLException {
        List<Country> list = new ArrayList<>();
        String sql = "SELECT * FROM countries WHERE is_active = TRUE ORDER BY country_name ASC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Country c = new Country();
                c.setCountryCode(rs.getString("country_code"));
                c.setCountryName(rs.getString("country_name"));
                c.setPhoneCode(rs.getString("phone_code"));
                c.setDefaultCurrency(rs.getString("default_currency"));
                c.setDefaultTimezone(rs.getString("default_timezone"));
                c.setDistanceUnit(rs.getString("distance_unit"));
                c.setActive(rs.getBoolean("is_active"));
                list.add(c);
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public List<Region> getRegionsByCountry(String countryCode) throws SQLException {
        List<Region> list = new ArrayList<>();
        String sql = "SELECT * FROM regions WHERE country_code = ? ORDER BY region_name ASC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, countryCode);
            rs = ps.executeQuery();
            while (rs.next()) {
                Region r = new Region();
                r.setId(rs.getInt("id"));
                r.setCountryCode(rs.getString("country_code"));
                r.setRegionCode(rs.getString("region_code"));
                r.setRegionName(rs.getString("region_name"));
                list.add(r);
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public List<FoodCategory> getAllFoodCategories() throws SQLException {
        List<FoodCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM food_categories ORDER BY id ASC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                FoodCategory fc = new FoodCategory();
                fc.setId(rs.getInt("id"));
                fc.setCategoryCode(rs.getString("category_code"));
                fc.setCategoryName(rs.getString("category_name"));
                fc.setPerishable(rs.getBoolean("is_perishable"));
                fc.setDefaultShelfLifeHours(rs.getInt("default_shelf_life_hours"));
                fc.setDefaultStorageCondition(rs.getString("default_storage_condition"));
                fc.setUrgencyWeight(rs.getBigDecimal("urgency_weight"));
                list.add(fc);
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public List<MeasurementUnit> getAllMeasurementUnits() throws SQLException {
        List<MeasurementUnit> list = new ArrayList<>();
        String sql = "SELECT * FROM measurement_units ORDER BY id ASC";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                MeasurementUnit mu = new MeasurementUnit();
                mu.setId(rs.getInt("id"));
                mu.setUnitCode(rs.getString("unit_code"));
                mu.setUnitName(rs.getString("unit_name"));
                mu.setUnitType(rs.getString("unit_type"));
                mu.setConversionFactorToKg(rs.getBigDecimal("conversion_factor_to_kg"));
                list.add(mu);
            }
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
        return list;
    }

    public VerificationRule getVerificationRuleForCountry(String countryCode) throws SQLException {
        String sql = "SELECT * FROM verification_rules WHERE country_code = ? AND is_active = TRUE LIMIT 1";
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, countryCode);
            rs = ps.executeQuery();
            if (rs.next()) {
                VerificationRule vr = new VerificationRule();
                vr.setId(rs.getInt("id"));
                vr.setCountryCode(rs.getString("country_code"));
                vr.setRuleCode(rs.getString("rule_code"));
                vr.setRuleName(rs.getString("rule_name"));
                vr.setMinEstablishedYears(rs.getInt("min_established_years"));
                vr.setRequiresOrgDocument(rs.getBoolean("requires_org_document"));
                vr.setRequiresRegistrationDoc(rs.getBoolean("requires_registration_doc"));
                vr.setRequiresAddressProof(rs.getBoolean("requires_address_proof"));
                vr.setActive(rs.getBoolean("is_active"));
                return vr;
            }
            return null;
        } finally {
            closeQuietly(rs, ps);
            releaseConnection(conn);
        }
    }
}
