package org.annpath.controller;

import org.annpath.dao.FoodListingDao;
import org.annpath.dao.NgoDao;
import org.annpath.dao.ReferenceDataDao;
import org.annpath.model.DigitalRescueReceipt;
import org.annpath.model.FoodListing;
import org.annpath.model.NgoProfile;
import org.annpath.model.NgoRequirement;
import org.annpath.model.User;
import org.annpath.model.VerificationDocument;
import org.annpath.security.FileUploadValidator;
import org.annpath.service.AuditService;
import org.annpath.service.NgoService;
import org.annpath.service.NoVolunteerFallbackService;
import org.annpath.service.PickupDeliveryService;
import org.annpath.service.SmartMatchingEngine;
import org.annpath.util.HashGenerator;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "NgoServlet", urlPatterns = {"/ngo/*"})
@MultipartConfig(maxFileSize = 5242880, maxRequestSize = 10485760)
public class NgoServlet extends BaseServlet {

    private final NgoService ngoService = new NgoService();
    private final NgoDao ngoDao = new NgoDao();
    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final SmartMatchingEngine matchingEngine = new SmartMatchingEngine();
    private final PickupDeliveryService pickupDeliveryService = new PickupDeliveryService();
    private final NoVolunteerFallbackService fallbackService = new NoVolunteerFallbackService();
    private final ReferenceDataDao refDao = new ReferenceDataDao();
    private final AuditService auditService = new AuditService();
    private final org.annpath.service.RescueBottleneckDetector bottleneckDetector = new org.annpath.service.RescueBottleneckDetector();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = getCurrentUser(request);
        String path = request.getPathInfo();
        if (path == null) path = "/dashboard";

        switch (path) {
            case "/dashboard":
                showDashboard(currentUser, request, response);
                break;
            case "/browse":
                showBrowse(currentUser, request, response);
                break;
            case "/requirements":
                showRequirements(currentUser, request, response);
                break;
            case "/verify":
                showVerification(currentUser, request, response);
                break;
            case "/profile":
                showProfile(currentUser, request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = getCurrentUser(request);
        String path = request.getPathInfo();
        if (path == null) path = "/dashboard";

        switch (path) {
            case "/request":
                handleRequestFood(currentUser, request, response);
                break;
            case "/requirements/add":
                handleAddRequirement(currentUser, request, response);
                break;
            case "/verify/upload":
                handleUploadDocument(currentUser, request, response);
                break;
            case "/confirm-receipt":
                handleConfirmReceipt(currentUser, request, response);
                break;
            case "/distribute":
                handleCompleteDistribution(currentUser, request, response);
                break;
            case "/self-pickup":
                handleConfigureSelfPickup(currentUser, request, response);
                break;
            case "/profile":
                handleUpdateProfile(currentUser, request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showDashboard(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            NgoProfile profile = ngoDao.findByUserId(user.getId());
            request.setAttribute("ngoProfile", profile);
            request.setAttribute("recommendedMatches", matchingEngine.findMatchesForNgo(user.getId()));
            request.setAttribute("myRequests", ngoService.getNgoRequests(user.getId()));
            request.setAttribute("activeDeliveries", ngoService.getActiveDeliveries(user.getId()));
            request.setAttribute("activeBottlenecks", bottleneckDetector.scanAllBottlenecksThrottled(60));
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
        }
        renderJsp("ngo/dashboard.jsp", request, response);
    }

    private void showBrowse(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            NgoProfile profile = ngoDao.findByUserId(user.getId());
            request.setAttribute("ngoProfile", profile);
            request.setAttribute("categories", refDao.getAllFoodCategories());

            boolean hasCoords = (profile != null && profile.getLatitude() != null && profile.getLongitude() != null);
            double radius = (profile != null && profile.getServiceRadiusKm() != null) ? profile.getServiceRadiusKm().doubleValue() : 25.0;

            String scope = request.getParameter("scope");
            boolean forceAll = "all".equalsIgnoreCase(scope);

            List<FoodListing> available;
            boolean isFallback = false;

            if (hasCoords && !forceAll) {
                double lat = profile.getLatitude().doubleValue();
                double lng = profile.getLongitude().doubleValue();
                double[] bbox = org.annpath.location.GeoUtils.calculateBoundingBox(lat, lng, radius);
                available = foodListingDao.findAvailableListings(bbox[0], bbox[1], bbox[2], bbox[3], null, null);
                if (available.isEmpty()) {
                    available = foodListingDao.findAllAvailableListings(null, null);
                    isFallback = true;
                }
                for (FoodListing fl : available) {
                    if (fl.getPickupLatitude() != null && fl.getPickupLongitude() != null) {
                        double dist = org.annpath.location.GeoUtils.calculateDistanceKm(
                                lat, lng,
                                fl.getPickupLatitude().doubleValue(),
                                fl.getPickupLongitude().doubleValue()
                        );
                        fl.setDistanceKm(Math.round(dist * 10.0) / 10.0);
                    }
                }
            } else {
                available = foodListingDao.findAllAvailableListings(null, null);
            }

            request.setAttribute("availableListings", available);
            request.setAttribute("isRegionalFallback", isFallback);
            request.setAttribute("ngoRadius", radius);
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
        }
        renderJsp("ngo/browse-food.jsp", request, response);
    }

    private void showRequirements(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("requirements", ngoService.getRequirements(user.getId()));
            request.setAttribute("categories", refDao.getAllFoodCategories());
            request.setAttribute("units", refDao.getAllMeasurementUnits());
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
        }
        renderJsp("ngo/requirements.jsp", request, response);
    }

    private void showVerification(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            NgoProfile profile = ngoDao.findByUserId(user.getId());
            request.setAttribute("ngoProfile", profile);
            request.setAttribute("documents", ngoDao.getDocumentsByNgoId(user.getId()));
            String countryCode = (profile != null && profile.getCountryCode() != null) ? profile.getCountryCode() : "IN";
            request.setAttribute("rule", refDao.getVerificationRuleForCountry(countryCode));
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
        }
        renderJsp("ngo/verification-submit.jsp", request, response);
    }

    private void handleRequestFood(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long listingId = Long.parseLong(request.getParameter("listingId"));
            BigDecimal requestedQty = new BigDecimal(request.getParameter("requestedQuantity"));
            String notes = request.getParameter("requestNotes");

            Long reqId = ngoService.requestFood(listingId, user.getId(), requestedQty, notes);
            auditService.log(user, "REQUEST_FOOD", "Submitted request #" + reqId + " for listing #" + listingId, "FoodRequest", reqId, "SUCCESS", null, request);

            flashSuccess(request, "Food request placed successfully! Awaiting Donor approval.");
        } catch (Exception e) {
            flashError(request, e.getMessage());
        }
        redirect("/ngo/dashboard", request, response);
    }

    private void handleAddRequirement(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String title = request.getParameter("title");
            int catId = Integer.parseInt(request.getParameter("foodCategoryId"));
            String dietary = request.getParameter("dietaryType");
            BigDecimal minQty = new BigDecimal(request.getParameter("minQuantity"));
            BigDecimal maxQty = new BigDecimal(request.getParameter("maxQuantity"));
            int unitId = Integer.parseInt(request.getParameter("unitId"));
            String priority = request.getParameter("priorityLevel");

            NgoRequirement r = new NgoRequirement();
            r.setNgoUserId(user.getId());
            r.setTitle(title);
            r.setFoodCategoryId(catId);
            r.setDietaryType(dietary);
            r.setMinQuantity(minQty);
            r.setMaxQuantity(maxQty);
            r.setUnitId(unitId);
            r.setPriorityLevel(priority);
            r.setActive(true);

            ngoService.addRequirement(r);
            flashSuccess(request, "Requirement profile added successfully.");
        } catch (Exception e) {
            flashError(request, e.getMessage());
        }
        redirect("/ngo/requirements", request, response);
    }

    private void handleUploadDocument(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String docType = request.getParameter("documentType");
            Part filePart = request.getPart("documentFile");
            FileUploadValidator.validateFile(filePart);

            String originalName = filePart.getSubmittedFileName();
            String storedName = FileUploadValidator.generateStoredFileName(originalName);
            File uploadDir = FileUploadValidator.getOrCreateUploadDir();
            File targetFile = new File(uploadDir, storedName);

            try (InputStream in = filePart.getInputStream()) {
                Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Compute file hash
            byte[] fileBytes = Files.readAllBytes(targetFile.toPath());
            String hash = HashGenerator.sha256(fileBytes);

            VerificationDocument doc = new VerificationDocument();
            doc.setNgoUserId(user.getId());
            doc.setDocumentType(docType);
            doc.setOriginalFileName(originalName);
            doc.setStoredFileName(storedName);
            doc.setFilePath(targetFile.getAbsolutePath());
            doc.setFileSizeBytes(filePart.getSize());
            doc.setMimeType(filePart.getContentType());
            doc.setFileHashSha256(hash);

            ngoService.submitDocument(doc);
            auditService.log(user, "UPLOAD_DOC", "Uploaded verification document: " + docType, "VerificationDocument", null, "SUCCESS", null, request);

            flashSuccess(request, "Document uploaded. System verification evaluated.");
        } catch (Exception e) {
            flashError(request, e.getMessage());
        }
        redirect("/ngo/verify", request, response);
    }

    private void handleConfirmReceipt(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long pickupId = Long.parseLong(request.getParameter("pickupId"));
            pickupDeliveryService.confirmReceipt(pickupId, user.getId());
            auditService.log(user, "CONFIRM_RECEIPT", "Confirmed receipt of food for pickup #" + pickupId, "Pickup", pickupId, "SUCCESS", null, request);
            flashSuccess(request, "Food receipt confirmed. Ready for community distribution.");
        } catch (Exception e) {
            flashError(request, e.getMessage());
        }
        redirect("/ngo/dashboard", request, response);
    }

    private void handleCompleteDistribution(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long pickupId = Long.parseLong(request.getParameter("pickupId"));
            DigitalRescueReceipt receipt = pickupDeliveryService.completeDistribution(pickupId, user.getId());
            auditService.log(user, "DISTRIBUTION_COMPLETED", "Food distributed. Generated receipt #" + receipt.getReceiptNumber(), "Pickup", pickupId, "SUCCESS", null, request);
            flashSuccess(request, "Distribution recorded! Digital Rescue Receipt #" + receipt.getReceiptNumber() + " generated.");
            redirect("/receipt/view?id=" + receipt.getReceiptNumber(), request, response);
            return;
        } catch (Exception e) {
            flashError(request, e.getMessage());
        }
        redirect("/ngo/dashboard", request, response);
    }

    private void handleConfigureSelfPickup(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long pickupId = Long.parseLong(request.getParameter("pickupId"));
            String personName = request.getParameter("selfPickupPersonName");
            String phone = request.getParameter("selfPickupPersonPhone");
            String idType = request.getParameter("selfPickupIdType");
            String idNumber = request.getParameter("selfPickupIdNumber");

            fallbackService.configureSelfPickup(pickupId, user.getId(), personName, phone, idType, idNumber);
            auditService.log(user, "SELF_PICKUP_CHOSEN", "Configured self-pickup by " + personName, "Pickup", pickupId, "SUCCESS", null, request);
            flashSuccess(request, "Self-pickup confirmed! Please proceed to pickup with the authorized OTP.");
        } catch (Exception e) {
            flashError(request, e.getMessage());
        }
        redirect("/ngo/dashboard", request, response);
    }

    private void showProfile(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (user == null) {
            redirect("/auth/login", request, response);
            return;
        }
        try {
            NgoProfile profile = ngoDao.findByUserId(user.getId());
            if (profile == null) {
                profile = new NgoProfile();
                profile.setUserId(user.getId());
                profile.setRepresentativeName(user.getEmail());
            }
            request.setAttribute("profile", profile);
            request.setAttribute("user", user);
            request.setAttribute("countries", refDao.getAllCountries());
            renderJsp("ngo/profile.jsp", request, response);
        } catch (Exception e) {
            flashError(request, "Failed to load NGO profile: " + e.getMessage());
            redirect("/ngo/dashboard", request, response);
        }
    }

    private void handleUpdateProfile(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (user == null) {
            redirect("/auth/login", request, response);
            return;
        }
        try {
            NgoProfile profile = ngoDao.findByUserId(user.getId());
            if (profile == null) {
                profile = new NgoProfile();
                profile.setUserId(user.getId());
            }

            profile.setOrganizationName(request.getParameter("organizationName"));
            profile.setRegistrationNumber(request.getParameter("registrationNumber"));
            profile.setRegistrationAuthority(request.getParameter("registrationAuthority"));

            String estYearStr = request.getParameter("establishedYear");
            if (estYearStr != null && !estYearStr.trim().isEmpty()) {
                try {
                    profile.setEstablishedYear(Integer.parseInt(estYearStr.trim()));
                } catch (NumberFormatException ignored) {}
            }

            profile.setRepresentativeName(request.getParameter("representativeName"));
            profile.setRepresentativeDesignation(request.getParameter("representativeDesignation"));
            profile.setWebsite(request.getParameter("website"));
            profile.setAddressLine1(request.getParameter("addressLine1"));
            profile.setAddressLine2(request.getParameter("addressLine2"));
            profile.setCity(request.getParameter("city"));
            profile.setStateRegion(request.getParameter("stateRegion"));
            profile.setPostalCode(request.getParameter("postalCode"));
            profile.setCountryCode(request.getParameter("countryCode"));

            String radiusStr = request.getParameter("serviceRadiusKm");
            if (radiusStr != null && !radiusStr.trim().isEmpty()) {
                try {
                    profile.setServiceRadiusKm(new BigDecimal(radiusStr.trim()));
                } catch (Exception ignored) {}
            }

            String capStr = request.getParameter("maxDailyCapacityKg");
            if (capStr != null && !capStr.trim().isEmpty()) {
                try {
                    profile.setMaxDailyCapacityKg(new BigDecimal(capStr.trim()));
                } catch (Exception ignored) {}
            }

            String latStr = request.getParameter("latitude");
            String lngStr = request.getParameter("longitude");
            if (latStr != null && !latStr.trim().isEmpty()) {
                profile.setLatitude(new BigDecimal(latStr.trim()));
            }
            if (lngStr != null && !lngStr.trim().isEmpty()) {
                profile.setLongitude(new BigDecimal(lngStr.trim()));
            }

            ngoDao.update(profile);
            auditService.log(user, "PROFILE_UPDATED", "Updated NGO organization profile", "NgoProfile", user.getId(), "SUCCESS", null, request);
            flashSuccess(request, "NGO organization profile updated successfully!");
        } catch (Exception e) {
            flashError(request, "Failed to update profile: " + e.getMessage());
        }
        redirect("/ngo/profile", request, response);
    }
}
