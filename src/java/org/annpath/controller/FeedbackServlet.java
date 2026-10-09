package org.annpath.controller;

import org.annpath.config.Constants;
import org.annpath.dao.FeedbackDao;
import org.annpath.dao.PickupDao;
import org.annpath.model.Feedback;
import org.annpath.model.Pickup;
import org.annpath.model.User;
import org.annpath.service.AuditService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "FeedbackServlet", urlPatterns = {"/feedback", "/feedback/*"})
public class FeedbackServlet extends BaseServlet {

    private static final Logger LOGGER = Logger.getLogger(FeedbackServlet.class.getName());
    private final FeedbackDao feedbackDao = new FeedbackDao();
    private final PickupDao pickupDao = new PickupDao();
    private final AuditService auditService = new AuditService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getCurrentUser(request);
        if (user == null) {
            redirect("/auth/login", request, response);
            return;
        }

        String path = request.getPathInfo();
        if ("/submit".equals(path) || path == null) {
            handleSubmitFeedback(request, response, user);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleSubmitFeedback(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        try {
            Long pickupId = getLongParam(request, "pickupId");
            int rating = getIntParam(request, "rating", 5);
            String comment = request.getParameter("comment");
            String feedbackType = request.getParameter("feedbackType");

            if (pickupId == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing pickup ID.");
                return;
            }

            Pickup pickup = pickupDao.findById(pickupId);
            if (pickup == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Pickup record not found.");
                return;
            }

            // Determine role-based target
            Long targetUserId = null;
            if (Constants.ROLE_DONOR.equalsIgnoreCase(user.getRole())) {
                targetUserId = (pickup.getVolunteerUserId() != null) ? pickup.getVolunteerUserId() : pickup.getNgoUserId();
                if (feedbackType == null || feedbackType.isEmpty()) feedbackType = "NGO_RATING";
            } else if (Constants.ROLE_NGO.equalsIgnoreCase(user.getRole())) {
                targetUserId = pickup.getDonorUserId();
                if (feedbackType == null || feedbackType.isEmpty()) feedbackType = "FOOD_QUALITY";
            } else if (Constants.ROLE_VOLUNTEER.equalsIgnoreCase(user.getRole())) {
                targetUserId = pickup.getDonorUserId();
                if (feedbackType == null || feedbackType.isEmpty()) feedbackType = "DONOR_RATING";
            }

            // Check if already submitted
            if (feedbackDao.hasSubmittedFeedback(pickupId, user.getId(), feedbackType)) {
                redirectBasedOnRole(user.getRole(), request, response, "?feedbackMsg=Already+submitted");
                return;
            }

            Feedback fb = new Feedback();
            fb.setFoodListingId(pickup.getFoodListingId());
            fb.setPickupId(pickupId);
            fb.setSubmitterUserId(user.getId());
            fb.setTargetUserId(targetUserId);
            fb.setRatingStars(Math.max(1, Math.min(5, rating)));
            fb.setComment(comment != null ? comment.trim() : "");
            fb.setFeedbackType(feedbackType);

            feedbackDao.save(fb);

            auditService.log(user, "FEEDBACK_SUBMITTED", "Submitted feedback for pickup #" + pickupId,
                    "Pickup", pickupId, "SUCCESS", null, request);

            redirectBasedOnRole(user.getRole(), request, response, "?feedbackMsg=Thank+you+for+your+feedback!");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to record feedback: " + e.getMessage(), e);
            redirectBasedOnRole(user.getRole(), request, response, "?error=Could+not+record+feedback");
        }
    }

    private void redirectBasedOnRole(String role, HttpServletRequest request, HttpServletResponse response, String query)
            throws IOException {
        if (Constants.ROLE_DONOR.equalsIgnoreCase(role)) {
            redirect("/donor/dashboard" + query, request, response);
        } else if (Constants.ROLE_NGO.equalsIgnoreCase(role)) {
            redirect("/ngo/dashboard" + query, request, response);
        } else if (Constants.ROLE_VOLUNTEER.equalsIgnoreCase(role)) {
            redirect("/volunteer/dashboard" + query, request, response);
        } else {
            redirect("/home", request, response);
        }
    }
}
