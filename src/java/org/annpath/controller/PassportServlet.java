package org.annpath.controller;

import org.annpath.dao.FoodListingDao;
import org.annpath.dao.PickupDao;
import org.annpath.dao.ReceiptDao;
import org.annpath.model.DigitalRescueReceipt;
import org.annpath.model.FoodListing;
import org.annpath.model.FoodStatusHistory;
import org.annpath.model.Pickup;
import org.annpath.service.RescueAutopilotEngine;
import org.annpath.service.RescueJourneyService;
import org.annpath.util.HashGenerator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@WebServlet(name = "PassportServlet", urlPatterns = {"/rescue/passport", "/passport"})
public class PassportServlet extends BaseServlet {

    private final PickupDao pickupDao = new PickupDao();
    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final ReceiptDao receiptDao = new ReceiptDao();
    private final RescueJourneyService journeyService = new RescueJourneyService();
    private final RescueAutopilotEngine autopilotEngine = new RescueAutopilotEngine();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pickupIdParam = request.getParameter("pickupId");
        if (pickupIdParam == null || pickupIdParam.trim().isEmpty()) {
            pickupIdParam = request.getParameter("id");
        }
        String listingIdParam = request.getParameter("listingId");
        String rescueCodeParam = request.getParameter("code");
        if (rescueCodeParam == null || rescueCodeParam.trim().isEmpty()) {
            rescueCodeParam = request.getParameter("rescueCode");
        }

        try {
            FoodListing listing = null;
            Pickup pickup = null;

            // 1. Resolve by rescue code
            if (rescueCodeParam != null && !rescueCodeParam.trim().isEmpty()) {
                String code = rescueCodeParam.trim();
                listing = foodListingDao.findByRescueCode(code);
                if (listing == null && code.toUpperCase().startsWith("ANP-")) {
                    try {
                        String[] parts = code.split("-");
                        if (parts.length >= 3) {
                            Long parsedId = Long.parseLong(parts[2]);
                            listing = foodListingDao.findById(parsedId);
                        }
                    } catch (Exception ignored) {}
                }
                if (listing != null) {
                    pickup = pickupDao.findByFoodListingId(listing.getId());
                }
            }

            // 2. Resolve by pickupId
            if (pickup == null && pickupIdParam != null && !pickupIdParam.trim().isEmpty()) {
                try {
                    Long pid = Long.parseLong(pickupIdParam.trim());
                    pickup = pickupDao.findById(pid);
                    if (pickup != null && listing == null) {
                        listing = foodListingDao.findById(pickup.getFoodListingId());
                    }
                } catch (NumberFormatException ignored) {}
            }

            // 3. Resolve by listingId
            if (listing == null && listingIdParam != null && !listingIdParam.trim().isEmpty()) {
                try {
                    Long lid = Long.parseLong(listingIdParam.trim());
                    listing = foodListingDao.findById(lid);
                    if (listing != null && pickup == null) {
                        pickup = pickupDao.findByFoodListingId(listing.getId());
                    }
                } catch (NumberFormatException ignored) {}
            }

            // If no valid identifier provided or resolved, show Not Found state (do not auto-select latest rescue)
            if (listing == null && pickup == null) {
                request.setAttribute("rescueNotFound", true);
                renderJsp("rescue/passport.jsp", request, response);
                return;
            }

            // Generate/Retrieve Digital Receipt
            DigitalRescueReceipt receipt = null;
            if (pickup != null) {
                receipt = receiptDao.findByPickupId(pickup.getId());
                if (receipt == null) {
                    receipt = buildReceiptFromPickup(pickup, listing);
                }
            } else if (listing != null) {
                receipt = buildPreliminaryReceipt(listing);
            }

            // Retrieve verified state transition journey events
            Long lid = (listing != null) ? listing.getId() : null;
            Long pid = (pickup != null) ? pickup.getId() : null;
            List<FoodStatusHistory> journeyEvents = journeyService.getJourneyEvents(lid, pid);

            // Compute Autopilot operational assessment
            RescueAutopilotEngine.AutopilotDecision autopilotDecision = autopilotEngine.evaluateRescue(lid, pid);

            String formattedRescueId = (listing != null) ? listing.getFormattedRescueId()
                    : (pickup != null ? "ANP-2026-" + String.format("%06d", pickup.getId()) : "ANP-RESCUE");

            request.setAttribute("pickup", pickup);
            request.setAttribute("listing", listing);
            request.setAttribute("receipt", receipt);
            request.setAttribute("journeyEvents", journeyEvents);
            request.setAttribute("autopilotDecision", autopilotDecision);
            request.setAttribute("rescueCode", formattedRescueId);

            renderJsp("rescue/passport.jsp", request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error loading food journey passport: " + e.getMessage());
            renderJsp("error/500.jsp", request, response);
        }
    }

    private DigitalRescueReceipt buildReceiptFromPickup(Pickup p, FoodListing fl) {
        DigitalRescueReceipt r = new DigitalRescueReceipt();
        r.setReceiptNumber("REC-" + p.getId() + "-" + System.currentTimeMillis() % 10000);
        r.setPickupId(p.getId());
        r.setIssuedAt(p.getActualDeliveredAt() != null ? p.getActualDeliveredAt() : new Timestamp(System.currentTimeMillis()));
        r.setDonorName(p.getDonorName());
        r.setDonorAddress(p.getPickupAddress());
        r.setNgoName(p.getNgoName());
        r.setNgoAddress(p.getNgoAddress());
        r.setVolunteerName(p.getVolunteerName());
        r.setFoodTitle(p.getFoodTitle());
        r.setFoodCategoryName(p.getFoodCategory());
        r.setRescuedQuantity(p.getQuantity());
        r.setUnitName(p.getUnitName());
        r.setPickupTimestamp(p.getActualPickedUpAt());
        r.setDeliveryTimestamp(p.getActualDeliveredAt());
        r.setDistributionTimestamp(p.getNgoDistributedAt() != null ? p.getNgoDistributedAt() : p.getActualDeliveredAt());

        BigDecimal qty = p.getQuantity() != null ? p.getQuantity() : BigDecimal.ONE;
        int meals = (int) Math.round(qty.doubleValue() * 2.5);
        if (meals < 1) meals = 1;
        BigDecimal co2 = BigDecimal.valueOf(Math.round(qty.doubleValue() * 1.9 * 100.0) / 100.0);

        r.setEstimatedPeopleFed(meals);
        r.setCo2SavedKg(co2);

        String payload = r.getReceiptNumber() + "|" + p.getId() + "|" + p.getDonorUserId() + "|" +
                p.getNgoUserId() + "|" + r.getRescuedQuantity() + "|" + r.getIssuedAt();
        r.setVerificationHashSha256(HashGenerator.sha256(payload));

        return r;
    }

    private DigitalRescueReceipt buildPreliminaryReceipt(FoodListing fl) {
        DigitalRescueReceipt r = new DigitalRescueReceipt();
        r.setReceiptNumber("PRE-" + fl.getId() + "-" + System.currentTimeMillis() % 10000);
        r.setIssuedAt(fl.getCreatedAt());
        r.setDonorName(fl.getDonorName() != null ? fl.getDonorName() : "Verified Donor");
        r.setDonorAddress(fl.getPickupAddress());
        r.setFoodTitle(fl.getTitle());
        r.setFoodCategoryName(fl.getCategoryName());
        r.setRescuedQuantity(fl.getTotalQuantity());
        r.setUnitName(fl.getUnitName());

        BigDecimal qty = fl.getTotalQuantity() != null ? fl.getTotalQuantity() : BigDecimal.ONE;
        int meals = (int) Math.round(qty.doubleValue() * 2.5);
        if (meals < 1) meals = 1;
        BigDecimal co2 = BigDecimal.valueOf(Math.round(qty.doubleValue() * 1.9 * 100.0) / 100.0);

        r.setEstimatedPeopleFed(meals);
        r.setCo2SavedKg(co2);

        String payload = r.getReceiptNumber() + "|" + fl.getId() + "|" + fl.getDonorUserId() + "|" +
                r.getRescuedQuantity() + "|" + r.getIssuedAt();
        r.setVerificationHashSha256(HashGenerator.sha256(payload));

        return r;
    }
}
