package org.annpath.controller;

import org.annpath.dao.DonorDao;
import org.annpath.dao.FoodListingDao;
import org.annpath.dao.PickupDao;
import org.annpath.dao.ReferenceDataDao;
import org.annpath.model.DonorProfile;
import org.annpath.model.FoodListing;
import org.annpath.model.User;
import org.annpath.service.AuditService;
import org.annpath.service.DonorService;
import org.annpath.util.DateTimeUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;

@WebServlet(name = "DonorServlet", urlPatterns = {"/donor/*"})
@MultipartConfig(maxFileSize = 5242880, maxRequestSize = 10485760)
public class DonorServlet extends BaseServlet {

    private final DonorService donorService = new DonorService();
    private final DonorDao donorDao = new DonorDao();
    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final PickupDao pickupDao = new PickupDao();
    private final ReferenceDataDao refDao = new ReferenceDataDao();
    private final AuditService auditService = new AuditService();

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
            case "/create":
                showCreateForm(currentUser, request, response);
                break;
            case "/view":
                showDonationDetail(currentUser, request, response);
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
            case "/create":
                handleCreateDonation(currentUser, request, response);
                break;
            case "/approve":
                handleApproveRequest(currentUser, request, response);
                break;
            case "/reject":
                handleRejectRequest(currentUser, request, response);
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
            request.setAttribute("donations", donorService.getDonorDonations(user.getId()));
            request.setAttribute("pendingRequests", donorService.getPendingRequests(user.getId()));
            request.setAttribute("activePickups", pickupDao.findByDonor(user.getId()));
            request.setAttribute("donorProfile", donorDao.findByUserId(user.getId()));
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
        }
        renderJsp("donor/dashboard.jsp", request, response);
    }

    private void showCreateForm(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("categories", refDao.getAllFoodCategories());
            request.setAttribute("units", refDao.getAllMeasurementUnits());
            request.setAttribute("countries", refDao.getAllCountries());
            request.setAttribute("donorProfile", donorDao.findByUserId(user.getId()));
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
        }
        renderJsp("donor/create-donation.jsp", request, response);
    }

    private void showDonationDetail(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            redirect("/donor/dashboard", request, response);
            return;
        }
        try {
            Long listingId = Long.parseLong(idStr);
            FoodListing fl = foodListingDao.findById(listingId);
            if (fl == null || !fl.getDonorUserId().equals(user.getId())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("listing", fl);
            renderJsp("donor/view-donation.jsp", request, response);
        } catch (Exception e) {
            redirect("/donor/dashboard", request, response);
        }
    }

    private void handleCreateDonation(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String title = request.getParameter("title");
            int categoryId = Integer.parseInt(request.getParameter("foodCategoryId"));
            String dietaryType = request.getParameter("dietaryType");
            BigDecimal totalQty = new BigDecimal(request.getParameter("totalQuantity"));
            int unitId = Integer.parseInt(request.getParameter("unitId"));
            String storage = request.getParameter("storageCondition");
            String packaging = request.getParameter("packagingType");
            String desc = request.getParameter("description");
            String ingredients = request.getParameter("ingredients");
            String allergens = request.getParameter("allergens");

            String tz = getUserTimezone(request);
            Timestamp prepTime = DateTimeUtil.parseFromUserTimezone(request.getParameter("preparationTime"), tz);
            Timestamp availFrom = DateTimeUtil.parseFromUserTimezone(request.getParameter("availableFrom"), tz);
            Timestamp availUntil = DateTimeUtil.parseFromUserTimezone(request.getParameter("availableUntil"), tz);

            long nowMs = System.currentTimeMillis();
            if (availFrom == null) {
                availFrom = new Timestamp(nowMs);
            }
            if (availUntil == null || availUntil.before(new Timestamp(nowMs + 3600000L))) {
                availUntil = new Timestamp(Math.max(nowMs, availFrom.getTime()) + (48L * 3600L * 1000L));
            }

            String address = request.getParameter("pickupAddress");
            String city = request.getParameter("pickupCity");
            String region = request.getParameter("pickupRegion");
            String postal = request.getParameter("pickupPostalCode");
            String country = request.getParameter("pickupCountryCode");
            String latParam = request.getParameter("pickupLatitude");
            String lngParam = request.getParameter("pickupLongitude");
            DonorProfile donorProf = donorDao.findByUserId(user.getId());
            BigDecimal lat = null;
            try {
                if (latParam != null && !latParam.trim().isEmpty()) {
                    lat = new BigDecimal(latParam.trim());
                } else if (donorProf != null && donorProf.getLatitude() != null) {
                    lat = donorProf.getLatitude();
                }
            } catch (Exception ex) {
                lat = (donorProf != null) ? donorProf.getLatitude() : null;
            }

            BigDecimal lng = null;
            try {
                if (lngParam != null && !lngParam.trim().isEmpty()) {
                    lng = new BigDecimal(lngParam.trim());
                } else if (donorProf != null && donorProf.getLongitude() != null) {
                    lng = donorProf.getLongitude();
                }
            } catch (Exception ex) {
                lng = (donorProf != null) ? donorProf.getLongitude() : null;
            }
            String instructions = request.getParameter("pickupInstructions");
            String contactName = request.getParameter("contactName");
            String contactPhone = request.getParameter("contactPhone");

            FoodListing listing = new FoodListing();
            listing.setDonorUserId(user.getId());
            listing.setTitle(title);
            listing.setFoodCategoryId(categoryId);
            listing.setDietaryType(dietaryType);
            listing.setTotalQuantity(totalQty);
            listing.setUnitId(unitId);
            listing.setStorageCondition(storage);
            listing.setPackagingType(packaging);
            listing.setDescription(desc);
            listing.setIngredients(ingredients);
            listing.setAllergens(allergens);
            listing.setPreparationTime(prepTime);
            listing.setAvailableFrom(availFrom);
            listing.setAvailableUntil(availUntil);
            listing.setPickupAddress(address);
            listing.setPickupCity(city);
            listing.setPickupRegion(region);
            listing.setPickupPostalCode(postal);
            listing.setPickupCountryCode(country);
            listing.setPickupLatitude(lat);
            listing.setPickupLongitude(lng);
            listing.setPickupInstructions(instructions);
            listing.setContactName(contactName);
            listing.setContactPhone(contactPhone);

            Long listingId = donorService.createFoodDonation(listing);
            auditService.log(user, "CREATE_DONATION", "Created food donation #" + listingId + " (" + title + ")", "FoodListing", listingId, "SUCCESS", null, request);

            flashSuccess(request, "Food donation created successfully. Smart matching activated.");
            redirect("/donor/dashboard", request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
            showCreateForm(user, request, response);
        }
    }

    private void handleApproveRequest(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String reqIdStr = request.getParameter("requestId");
        String notes = request.getParameter("responseNotes");
        try {
            Long reqId = Long.parseLong(reqIdStr);
            donorService.approveFoodRequest(reqId, user.getId(), notes);
            auditService.log(user, "APPROVE_REQUEST", "Approved food request #" + reqId, "FoodRequest", reqId, "SUCCESS", null, request);
            flashSuccess(request, "Food request approved! Volunteer dispatching in progress.");
        } catch (Exception e) {
            flashError(request, e.getMessage());
        }
        redirect("/donor/dashboard", request, response);
    }

    private void handleRejectRequest(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String reqIdStr = request.getParameter("requestId");
        String reason = request.getParameter("rejectionReason");
        try {
            Long reqId = Long.parseLong(reqIdStr);
            donorService.rejectFoodRequest(reqId, user.getId(), reason);
            auditService.log(user, "REJECT_REQUEST", "Rejected food request #" + reqId, "FoodRequest", reqId, "SUCCESS", null, request);
            flashSuccess(request, "Food request rejected.");
        } catch (Exception e) {
            flashError(request, e.getMessage());
        }
        redirect("/donor/dashboard", request, response);
    }

    private void showProfile(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (user == null) {
            redirect("/auth/login", request, response);
            return;
        }
        try {
            DonorProfile profile = donorDao.findByUserId(user.getId());
            if (profile == null) {
                profile = new DonorProfile();
                profile.setUserId(user.getId());
                profile.setContactPerson(user.getEmail());
            }
            request.setAttribute("profile", profile);
            request.setAttribute("user", user);
            request.setAttribute("countries", refDao.getAllCountries());
            renderJsp("donor/profile.jsp", request, response);
        } catch (Exception e) {
            flashError(request, "Failed to load profile: " + e.getMessage());
            redirect("/donor/dashboard", request, response);
        }
    }

    private void handleUpdateProfile(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (user == null) {
            redirect("/auth/login", request, response);
            return;
        }
        try {
            DonorProfile profile = donorDao.findByUserId(user.getId());
            if (profile == null) {
                profile = new DonorProfile();
                profile.setUserId(user.getId());
            }

            profile.setOrganizationName(request.getParameter("organizationName"));
            profile.setContactPerson(request.getParameter("contactPerson"));
            profile.setDonorType(request.getParameter("donorType"));
            profile.setAddressLine1(request.getParameter("addressLine1"));
            profile.setAddressLine2(request.getParameter("addressLine2"));
            profile.setCity(request.getParameter("city"));
            profile.setStateRegion(request.getParameter("stateRegion"));
            profile.setPostalCode(request.getParameter("postalCode"));
            profile.setCountryCode(request.getParameter("countryCode"));
            profile.setDefaultPickupInstructions(request.getParameter("defaultPickupInstructions"));

            String latStr = request.getParameter("latitude");
            String lngStr = request.getParameter("longitude");
            if (latStr != null && !latStr.trim().isEmpty()) {
                profile.setLatitude(new BigDecimal(latStr.trim()));
            }
            if (lngStr != null && !lngStr.trim().isEmpty()) {
                profile.setLongitude(new BigDecimal(lngStr.trim()));
            }

            donorDao.update(profile);
            auditService.log(user, "PROFILE_UPDATED", "Updated donor organization profile", "DonorProfile", user.getId(), "SUCCESS", null, request);
            flashSuccess(request, "Donor profile updated successfully!");
        } catch (Exception e) {
            flashError(request, "Failed to update profile: " + e.getMessage());
        }
        redirect("/donor/profile", request, response);
    }
}
