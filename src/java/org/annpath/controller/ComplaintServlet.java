package org.annpath.controller;

import org.annpath.dao.ComplaintDao;
import org.annpath.dao.PickupDao;
import org.annpath.model.Complaint;
import org.annpath.model.Pickup;
import org.annpath.model.User;
import org.annpath.service.AuditService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "ComplaintServlet", urlPatterns = {"/complaint", "/complaint/*"})
public class ComplaintServlet extends BaseServlet {

    private static final Logger LOGGER = Logger.getLogger(ComplaintServlet.class.getName());
    private final ComplaintDao complaintDao = new ComplaintDao();
    private final PickupDao pickupDao = new PickupDao();
    private final AuditService auditService = new AuditService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getCurrentUser(request);
        if (user == null) {
            redirect("/auth/login", request, response);
            return;
        }

        String path = request.getPathInfo();
        if (path == null || "/".equals(path) || "/list".equals(path)) {
            handleList(request, response, user);
        } else if ("/file".equals(path)) {
            handleShowFilePage(request, response, user);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getCurrentUser(request);
        if (user == null) {
            redirect("/auth/login", request, response);
            return;
        }

        String path = request.getPathInfo();
        if ("/file".equals(path) || "/submit".equals(path)) {
            handleSubmitComplaint(request, response, user);
        } else if ("/resolve".equals(path)) {
            handleResolveComplaint(request, response, user);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        try {
            List<Complaint> list = complaintDao.findByUserId(user.getId());
            request.setAttribute("complaints", list);
            request.setAttribute("currentUser", user);
            renderJsp("complaint/complaint-list.jsp", request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading complaints: " + e.getMessage(), e);
            request.setAttribute("errorMessage", "Failed to load complaints.");
            renderJsp("complaint/complaint-list.jsp", request, response);
        }
    }

    private void handleShowFilePage(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        Long pickupId = getLongParam(request, "pickupId");
        if (pickupId != null) {
            try {
                Pickup p = pickupDao.findById(pickupId);
                request.setAttribute("pickup", p);
            } catch (Exception ignored) {}
        }
        renderJsp("complaint/file-complaint.jsp", request, response);
    }

    private void handleSubmitComplaint(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        try {
            Long pickupId = getLongParam(request, "pickupId");
            Long reportedUserId = getLongParam(request, "reportedUserId");
            Long foodListingId = getLongParam(request, "foodListingId");
            String category = request.getParameter("category");
            String description = request.getParameter("description");

            if (category == null || category.trim().isEmpty() || description == null || description.trim().isEmpty()) {
                redirect("/complaint/file?error=Category+and+description+are+required", request, response);
                return;
            }

            if (pickupId != null && (reportedUserId == null || foodListingId == null)) {
                Pickup p = pickupDao.findById(pickupId);
                if (p != null) {
                    if (foodListingId == null) foodListingId = p.getFoodListingId();
                    if (reportedUserId == null) {
                        if (user.getId().equals(p.getDonorUserId())) {
                            reportedUserId = (p.getVolunteerUserId() != null) ? p.getVolunteerUserId() : p.getNgoUserId();
                        } else if (user.getId().equals(p.getNgoUserId())) {
                            reportedUserId = p.getDonorUserId();
                        } else {
                            reportedUserId = p.getDonorUserId();
                        }
                    }
                }
            }

            Complaint c = new Complaint();
            c.setReporterUserId(user.getId());
            c.setReportedUserId(reportedUserId);
            c.setFoodListingId(foodListingId);
            c.setPickupId(pickupId);
            c.setCategory(category.trim().toUpperCase());
            c.setDescription(description.trim());
            c.setStatus("UNDER_REVIEW"); // Auto-acknowledged by system

            complaintDao.save(c);

            auditService.log(user, "COMPLAINT_FILED", "Logged issue category " + category,
                    "Complaint", c.getId(), "SUCCESS", null, request);

            redirect("/complaint/list?successMessage=Issue+logged+and+acknowledged.+Our+automated+peer-resolution+protocol+is+active.", request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to submit complaint: " + e.getMessage(), e);
            redirect("/complaint/list?errorMessage=Could+not+file+complaint", request, response);
        }
    }

    private void handleResolveComplaint(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        try {
            Long complaintId = getLongParam(request, "complaintId");
            String notes = request.getParameter("resolutionNotes");

            if (complaintId == null) {
                redirect("/complaint/list?errorMessage=Missing+complaint+ID", request, response);
                return;
            }

            Complaint c = complaintDao.findById(complaintId);
            if (c == null) {
                redirect("/complaint/list?errorMessage=Complaint+not+found", request, response);
                return;
            }

            // Only reporter or reported user can resolve peer complaint
            if (!user.getId().equals(c.getReporterUserId()) && !user.getId().equals(c.getReportedUserId())) {
                redirect("/complaint/list?errorMessage=Unauthorized", request, response);
                return;
            }

            String resolution = (notes != null && !notes.trim().isEmpty()) ?
                    "Resolved by " + user.getEmail() + ": " + notes.trim() :
                    "Mutually resolved between participants.";

            complaintDao.updateStatus(complaintId, "RESOLVED", resolution);

            auditService.log(user, "COMPLAINT_RESOLVED", "Complaint #" + complaintId + " resolved",
                    "Complaint", complaintId, "SUCCESS", null, request);

            redirect("/complaint/list?successMessage=Complaint+marked+as+resolved.", request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to resolve complaint: " + e.getMessage(), e);
            redirect("/complaint/list?errorMessage=Failed+to+update+complaint", request, response);
        }
    }
}
