package org.annpath.controller;

import org.annpath.dao.ReferenceDataDao;
import org.annpath.dao.VolunteerDao;
import org.annpath.model.Pickup;
import org.annpath.model.User;
import org.annpath.model.VolunteerProfile;
import org.annpath.service.AuditService;
import org.annpath.service.VolunteerService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet(name = "VolunteerServlet", urlPatterns = {"/volunteer/*"})
public class VolunteerServlet extends BaseServlet {

    private final VolunteerService volunteerService = new VolunteerService();
    private final VolunteerDao volunteerDao = new VolunteerDao();
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
            case "/availability":
                handleToggleAvailability(currentUser, request, response);
                break;
            case "/claim":
                handleClaim(currentUser, request, response);
                break;
            case "/accept":
                handleAccept(currentUser, request, response);
                break;
            case "/reject":
                handleReject(currentUser, request, response);
                break;
            case "/pickup":
                handleConfirmPickup(currentUser, request, response);
                break;
            case "/deliver":
                handleConfirmDelivery(currentUser, request, response);
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
        if (user == null) {
            redirect("/auth/login", request, response);
            return;
        }
        try {
            VolunteerProfile profile = volunteerDao.findByUserId(user.getId());
            List<Pickup> allTasks = volunteerService.getVolunteerTasks(user.getId());

            List<Pickup> newDeliveries = new java.util.ArrayList<>();
            List<Pickup> completedDeliveries = new java.util.ArrayList<>();

            for (Pickup p : allTasks) {
                if ("DELIVERED".equals(p.getStatus()) || "RECEIVED".equals(p.getStatus()) || "DISTRIBUTED".equals(p.getStatus())) {
                    completedDeliveries.add(p);
                } else {
                    newDeliveries.add(p);
                }
            }

            List<Pickup> openMissions = volunteerService.getOpenPickups(user.getId());

            request.setAttribute("volunteerProfile", profile);
            request.setAttribute("tasks", allTasks);
            request.setAttribute("newDeliveries", newDeliveries);
            request.setAttribute("completedDeliveries", completedDeliveries);
            request.setAttribute("openMissions", openMissions);
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
        }
        renderJsp("volunteer/dashboard.jsp", request, response);
    }

    private void handleClaim(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pickupIdStr = request.getParameter("pickupId");
        try {
            Long pickupId = Long.parseLong(pickupIdStr);
            volunteerService.claimPickup(pickupId, user.getId());
            auditService.log(user, "CLAIM_MISSION", "Claimed rescue mission #" + pickupId, "Pickup", pickupId, "SUCCESS", null, request);
            flashSuccess(request, "Mission claimed successfully! You are now assigned to this rescue.");
        } catch (Exception e) {
            flashError(request, "Failed to claim mission: " + e.getMessage());
        }
        redirect("/volunteer/dashboard", request, response);
    }

    private void handleToggleAvailability(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String status = request.getParameter("availabilityStatus");
        try {
            volunteerService.updateAvailability(user.getId(), status);
            auditService.log(user, "TOGGLE_AVAILABILITY", "Updated status to " + status, "VolunteerProfile", user.getId(), "SUCCESS", null, request);
            flashSuccess(request, "Availability updated to: " + status);
        } catch (Exception e) {
            flashError(request, e.getMessage());
        }
        redirect("/volunteer/dashboard", request, response);
    }

    private void handleAccept(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long pickupId = Long.parseLong(request.getParameter("pickupId"));
            volunteerService.acceptAssignment(pickupId, user.getId());
            auditService.log(user, "ACCEPT_ASSIGNMENT", "Accepted pickup task #" + pickupId, "Pickup", pickupId, "SUCCESS", null, request);
            flashSuccess(request, "Pickup task accepted! Please proceed to pickup location.");
        } catch (Exception e) {
            flashError(request, e.getMessage());
        }
        redirect("/volunteer/dashboard", request, response);
    }

    private void handleReject(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long pickupId = Long.parseLong(request.getParameter("pickupId"));
            String reason = request.getParameter("rejectionReason");
            volunteerService.rejectAssignment(pickupId, user.getId(), reason);
            auditService.log(user, "REJECT_ASSIGNMENT", "Rejected task #" + pickupId + ": " + reason, "Pickup", pickupId, "SUCCESS", null, request);
            flashSuccess(request, "Task declined. Reassignment initiated.");
        } catch (Exception e) {
            flashError(request, e.getMessage());
        }
        redirect("/volunteer/dashboard", request, response);
    }

    private void handleConfirmPickup(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long pickupId = Long.parseLong(request.getParameter("pickupId"));
            String otp = request.getParameter("pickupOtp");
            volunteerService.confirmPickupWithOtp(pickupId, user.getId(), otp);
            auditService.log(user, "CONFIRM_PICKUP", "Completed pickup with OTP for task #" + pickupId, "Pickup", pickupId, "SUCCESS", null, request);
            flashSuccess(request, "Food pickup verified successfully! Now in transit.");
        } catch (Exception e) {
            flashError(request, e.getMessage());
        }
        redirect("/volunteer/dashboard", request, response);
    }

    private void handleConfirmDelivery(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long pickupId = Long.parseLong(request.getParameter("pickupId"));
            String otp = request.getParameter("deliveryOtp");
            volunteerService.confirmDeliveryWithOtp(pickupId, user.getId(), otp);
            auditService.log(user, "CONFIRM_DELIVERY", "Completed delivery with OTP for task #" + pickupId, "Pickup", pickupId, "SUCCESS", null, request);
            flashSuccess(request, "Food delivery verified successfully! Mission accomplished.");
        } catch (Exception e) {
            flashError(request, e.getMessage());
        }
        redirect("/volunteer/dashboard", request, response);
    }

    private void showProfile(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (user == null) {
            redirect("/auth/login", request, response);
            return;
        }
        try {
            VolunteerProfile profile = volunteerDao.findByUserId(user.getId());
            if (profile == null) {
                profile = new VolunteerProfile();
                profile.setUserId(user.getId());
                profile.setFirstName(user.getEmail());
            }
            request.setAttribute("profile", profile);
            request.setAttribute("user", user);
            request.setAttribute("countries", refDao.getAllCountries());
            renderJsp("volunteer/profile.jsp", request, response);
        } catch (Exception e) {
            flashError(request, "Failed to load profile: " + e.getMessage());
            redirect("/volunteer/dashboard", request, response);
        }
    }

    private void handleUpdateProfile(User user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (user == null) {
            redirect("/auth/login", request, response);
            return;
        }
        try {
            VolunteerProfile profile = volunteerDao.findByUserId(user.getId());
            if (profile == null) {
                profile = new VolunteerProfile();
                profile.setUserId(user.getId());
            }

            profile.setFirstName(request.getParameter("firstName"));
            profile.setLastName(request.getParameter("lastName"));
            profile.setAddressLine1(request.getParameter("addressLine1"));
            profile.setCity(request.getParameter("city"));
            profile.setStateRegion(request.getParameter("stateRegion"));
            profile.setPostalCode(request.getParameter("postalCode"));
            profile.setCountryCode(request.getParameter("countryCode"));
            profile.setVehicleType(request.getParameter("vehicleType"));
            profile.setEmergencyContactPhone(request.getParameter("emergencyContactPhone"));

            String distStr = request.getParameter("maxTravelDistanceKm");
            if (distStr != null && !distStr.trim().isEmpty()) {
                try {
                    profile.setMaxTravelDistanceKm(new BigDecimal(distStr.trim()));
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

            volunteerDao.update(profile);
            auditService.log(user, "PROFILE_UPDATED", "Updated volunteer profile", "VolunteerProfile", user.getId(), "SUCCESS", null, request);
            flashSuccess(request, "Volunteer profile updated successfully!");
        } catch (Exception e) {
            flashError(request, "Failed to update profile: " + e.getMessage());
        }
        redirect("/volunteer/profile", request, response);
    }
}
