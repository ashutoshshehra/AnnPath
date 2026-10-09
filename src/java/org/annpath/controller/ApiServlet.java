package org.annpath.controller;

import org.annpath.config.AppConfig;
import org.annpath.dao.FoodListingDao;
import org.annpath.dao.ImpactDao;
import org.annpath.dao.ReceiptDao;
import org.annpath.dao.UserDao;
import org.annpath.location.GeoUtils;
import org.annpath.model.DigitalRescueReceipt;
import org.annpath.model.FoodListing;
import org.annpath.model.User;
import org.annpath.service.OtpService;
import org.annpath.service.RescuePriorityEngine;
import org.annpath.util.HashGenerator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise RESTful API Controller for AnnPath Platform.
 * Serves JSON endpoints for mobile clients, external NGO systems, and integrations:
 * - GET  /api/health
 * - GET  /api/impact
 * - GET  /api/food/listings
 * - GET  /api/food/listings/{id}
 * - GET  /api/receipts/{receiptNumber}
 * - POST /api/otp/send
 * - POST /api/otp/verify
 */
@WebServlet(name = "ApiServlet", urlPatterns = {"/api/*"})
public class ApiServlet extends BaseServlet {

    private static final Logger LOGGER = Logger.getLogger(ApiServlet.class.getName());

    private final ImpactDao impactDao = new ImpactDao();
    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final ReceiptDao receiptDao = new ReceiptDao();
    private final UserDao userDao = new UserDao();
    private final OtpService otpService = new OtpService();
    private final RescuePriorityEngine priorityEngine = new RescuePriorityEngine();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setupCorsAndJson(request, response);
        String path = request.getPathInfo();
        if (path == null) path = "";
        if (path.startsWith("/v1/")) {
            path = path.substring(3);
        } else if (path.equals("/v1")) {
            path = "";
        }

        try {
            if (path.equals("/health") || path.isEmpty()) {
                handleHealth(response);
            } else if (path.equals("/impact")) {
                handleImpact(response);
            } else if (path.equals("/food/listings") || path.equals("/surplus/nearby") || path.equals("/surplus")) {
                handleFoodListings(request, response);
            } else if (path.startsWith("/food/listings/")) {
                String idStr = path.substring("/food/listings/".length());
                handleFoodListingById(Long.parseLong(idStr), response);
            } else if (path.startsWith("/receipts/")) {
                String receiptNum = path.substring("/receipts/".length());
                handleReceiptVerification(receiptNum, response);
            } else {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + path);
            }
        } catch (NumberFormatException e) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid numeric identifier parameter.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "API Error processing GET " + path, e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setupCorsAndJson(request, response);
        String path = request.getPathInfo();
        if (path == null) path = "";
        if (path.startsWith("/v1/")) {
            path = path.substring(3);
        } else if (path.equals("/v1")) {
            path = "";
        }

        try {
            if (path.equals("/otp/send")) {
                handleOtpSend(request, response);
            } else if (path.equals("/otp/verify")) {
                handleOtpVerify(request, response);
            } else {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + path);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "API Error processing POST " + path, e);
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        setupCorsAndJson(request, response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setupCorsAndJson(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.trim().isEmpty()) {
            String allowedOrigins = AppConfig.getAllowedCorsOrigins();
            boolean exactMatched = false;
            for (String allowedOrigin : allowedOrigins.split(",")) {
                allowedOrigin = allowedOrigin.trim();
                if (allowedOrigin.equalsIgnoreCase(origin.trim())) {
                    exactMatched = true;
                    break;
                }
            }
            if (exactMatched) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Vary", "Origin");
                response.setHeader("Access-Control-Allow-Credentials", "true");
            }
            // Requests from non-whitelisted origins receive no CORS headers (browser blocks them)
        }
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
    }

    private void handleHealth(HttpServletResponse response) throws IOException {
        long freeMemMb = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long totalMemMb = Runtime.getRuntime().totalMemory() / (1024 * 1024);

        StringBuilder json = new StringBuilder();
        json.append("{")
            .append("\"status\":\"UP\",")
            .append("\"service\":\"AnnPath Enterprise Platform API\",")
            .append("\"version\":\"1.0.0\",")
            .append("\"jvmFreeMemoryMb\":").append(freeMemMb).append(",")
            .append("\"jvmTotalMemoryMb\":").append(totalMemMb).append(",")
            .append("\"timestamp\":").append(System.currentTimeMillis())
            .append("}");
        writeJson(response, json.toString(), HttpServletResponse.SC_OK);
    }

    private void handleImpact(HttpServletResponse response) throws SQLException, IOException {
        Map<String, Object> summary = impactDao.getGlobalImpactSummary();
        StringBuilder json = new StringBuilder();
        json.append("{")
            .append("\"totalRescuedKg\":").append(summary.getOrDefault("totalRescuedKg", 0)).append(",")
            .append("\"totalMealsFed\":").append(summary.getOrDefault("totalMealsFed", 0)).append(",")
            .append("\"co2AvertedKg\":").append(summary.getOrDefault("co2AvertedKg", 0)).append(",")
            .append("\"completedRescuesCount\":").append(summary.getOrDefault("completedRescuesCount", 0))
            .append("}");
        writeJson(response, json.toString(), HttpServletResponse.SC_OK);
    }

    private void handleFoodListings(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String latParam = request.getParameter("lat");
        String lngParam = request.getParameter("lng");
        double radiusKm = parseDouble(request.getParameter("radiusKm"), 50.0);

        Integer categoryId = null;
        String catParam = request.getParameter("category");
        if (catParam != null && !catParam.isEmpty()) {
            try {
                categoryId = Integer.parseInt(catParam);
            } catch (NumberFormatException ignored) {}
        }
        String dietaryType = request.getParameter("dietaryType");

        List<FoodListing> listings;
        if (latParam != null && !latParam.trim().isEmpty() && lngParam != null && !lngParam.trim().isEmpty()) {
            try {
                double lat = Double.parseDouble(latParam.trim());
                double lng = Double.parseDouble(lngParam.trim());
                double[] bbox = GeoUtils.calculateBoundingBox(lat, lng, radiusKm);
                listings = foodListingDao.findAvailableListings(bbox[0], bbox[1], bbox[2], bbox[3], categoryId, dietaryType);
            } catch (NumberFormatException e) {
                listings = foodListingDao.findAllAvailableListings(categoryId, dietaryType);
            }
        } else {
            listings = foodListingDao.findAllAvailableListings(categoryId, dietaryType);
        }

        StringBuilder json = new StringBuilder();
        json.append("{\"count\":").append(listings.size()).append(",\"listings\":[");
        for (int i = 0; i < listings.size(); i++) {
            FoodListing fl = listings.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"id\":").append(fl.getId()).append(",")
                .append("\"title\":\"").append(escapeJson(fl.getTitle())).append("\",")
                .append("\"category\":\"").append(escapeJson(fl.getCategoryName())).append("\",")
                .append("\"dietaryType\":\"").append(escapeJson(fl.getDietaryType())).append("\",")
                .append("\"remainingQuantity\":").append(fl.getRemainingQuantity()).append(",")
                .append("\"unit\":\"").append(escapeJson(fl.getUnitName())).append("\",")
                .append("\"urgencyStatus\":\"").append(escapeJson(fl.getUrgencyStatus())).append("\",")
                .append("\"priorityScore\":").append(fl.getPriorityScore() != null ? fl.getPriorityScore() : 0).append(",")
                .append("\"donorName\":\"").append(escapeJson(fl.getDonorName())).append("\",")
                .append("\"latitude\":").append(fl.getPickupLatitude()).append(",")
                .append("\"longitude\":").append(fl.getPickupLongitude())
                .append("}");
        }
        json.append("]}");
        writeJson(response, json.toString(), HttpServletResponse.SC_OK);
    }

    private void handleFoodListingById(Long id, HttpServletResponse response) throws SQLException, IOException {
        FoodListing fl = foodListingDao.findById(id);
        if (fl == null) {
            sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Food listing not found.");
            return;
        }

        RescuePriorityEngine.PriorityResult res = priorityEngine.computeScore(fl, 0.0, 1);

        StringBuilder json = new StringBuilder();
        json.append("{")
            .append("\"id\":").append(fl.getId()).append(",")
            .append("\"title\":\"").append(escapeJson(fl.getTitle())).append("\",")
            .append("\"description\":\"").append(escapeJson(fl.getDescription())).append("\",")
            .append("\"category\":\"").append(escapeJson(fl.getCategoryName())).append("\",")
            .append("\"dietaryType\":\"").append(escapeJson(fl.getDietaryType())).append("\",")
            .append("\"totalQuantity\":").append(fl.getTotalQuantity()).append(",")
            .append("\"remainingQuantity\":").append(fl.getRemainingQuantity()).append(",")
            .append("\"reservedQuantity\":").append(fl.getReservedQuantity()).append(",")
            .append("\"unit\":\"").append(escapeJson(fl.getUnitName())).append("\",")
            .append("\"storageCondition\":\"").append(escapeJson(fl.getStorageCondition())).append("\",")
            .append("\"urgencyStatus\":\"").append(escapeJson(fl.getUrgencyStatus())).append("\",")
            .append("\"priorityScore\":").append(res.getScore()).append(",")
            .append("\"priorityBadge\":\"").append(escapeJson(res.getBadge())).append("\",")
            .append("\"priorityExplanation\":\"").append(escapeJson(res.getExplanation())).append("\",")
            .append("\"pickupAddress\":\"").append(escapeJson(fl.getPickupAddress())).append("\",")
            .append("\"pickupCity\":\"").append(escapeJson(fl.getPickupCity())).append("\",")
            .append("\"contactName\":\"").append(escapeJson(fl.getContactName())).append("\",")
            .append("\"contactPhone\":\"").append(escapeJson(fl.getContactPhone())).append("\"")
            .append("}");
        writeJson(response, json.toString(), HttpServletResponse.SC_OK);
    }

    private void handleReceiptVerification(String receiptNumber, HttpServletResponse response) throws SQLException, IOException {
        DigitalRescueReceipt r = receiptDao.findByReceiptNumber(receiptNumber);
        if (r == null) {
            sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Receipt not found: " + receiptNumber);
            return;
        }

        StringBuilder json = new StringBuilder();
        json.append("{")
            .append("\"receiptNumber\":\"").append(escapeJson(r.getReceiptNumber())).append("\",")
            .append("\"donorName\":\"").append(escapeJson(r.getDonorName())).append("\",")
            .append("\"ngoName\":\"").append(escapeJson(r.getNgoName())).append("\",")
            .append("\"quantityKg\":").append(r.getRescuedQuantity() != null ? r.getRescuedQuantity() : 0).append(",")
            .append("\"mealsProvided\":").append(r.getEstimatedPeopleFed()).append(",")
            .append("\"digitalSignatureSha256\":\"").append(escapeJson(r.getVerificationHashSha256())).append("\",")
            .append("\"verificationUrl\":\"").append(escapeJson(r.getVerificationUrl())).append("\",")
            .append("\"isAuthentic\":true")
            .append("}");
        writeJson(response, json.toString(), HttpServletResponse.SC_OK);
    }

    private void handleOtpSend(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String body = readRequestBody(request);
        String identifier = extractJsonField(body, "identifier");
        String channel = extractJsonField(body, "channel");
        String purpose = extractJsonField(body, "purpose");

        if (channel == null || channel.isEmpty()) channel = "EMAIL";
        if (purpose == null || purpose.isEmpty()) purpose = "VERIFICATION";

        if (identifier == null || identifier.trim().isEmpty()) {
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Parameter 'identifier' (email or phone) is required.");
            return;
        }

        User sessionUser = getCurrentUser(request);
        Long targetUserId = (sessionUser != null) ? sessionUser.getId() : null;
        if (targetUserId == null && identifier.contains("@")) {
            User foundUser = userDao.findByEmail(identifier.trim());
            if (foundUser != null) {
                targetUserId = foundUser.getId();
            }
        }

        otpService.generateAndSendOtp(targetUserId, identifier, channel, purpose);

        StringBuilder json = new StringBuilder();
        json.append("{")
            .append("\"success\":true,")
            .append("\"message\":\"OTP dispatched successfully.\",")
            .append("\"identifier\":\"").append(escapeJson(identifier)).append("\",")
            .append("\"channel\":\"").append(escapeJson(channel)).append("\"")
            .append("}");
        writeJson(response, json.toString(), HttpServletResponse.SC_OK);
    }

    private void handleOtpVerify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String body = readRequestBody(request);
        String identifier = extractJsonField(body, "identifier");
        String purpose = extractJsonField(body, "purpose");
        String otpCode = extractJsonField(body, "otpCode");

        if (purpose == null || purpose.isEmpty()) purpose = "VERIFICATION";

        boolean valid = otpService.verifyOtp(identifier, purpose, otpCode);

        StringBuilder json = new StringBuilder();
        json.append("{")
            .append("\"success\":").append(valid).append(",")
            .append("\"message\":\"OTP verified successfully.\",")
            .append("\"identifier\":\"").append(escapeJson(identifier)).append("\"")
            .append("}");
        writeJson(response, json.toString(), HttpServletResponse.SC_OK);
    }

    private void sendJsonError(HttpServletResponse response, int statusCode, String message) throws IOException {
        String json = "{\"success\":false,\"error\":\"" + escapeJson(message) + "\"}";
        writeJson(response, json, statusCode);
    }

    private void writeJson(HttpServletResponse response, String json, int statusCode) throws IOException {
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        out.write(json);
        out.flush();
    }

    private double parseDouble(String val, double def) {
        if (val == null || val.trim().isEmpty()) return def;
        try { return Double.parseDouble(val.trim()); } catch (NumberFormatException e) { return def; }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private String extractJsonField(String json, String field) {
        if (json == null || json.isEmpty()) return null;
        String pattern = "\"" + field + "\"\\s*:\\s*(?:\"([^\"]*)\"|([0-9a-zA-Z._+-]+))";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            String val = m.group(1);
            return val != null ? val.trim() : m.group(2).trim();
        }
        return null;
    }
}
