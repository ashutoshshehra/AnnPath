package org.annpath.controller;

import org.annpath.dao.PickupDao;
import org.annpath.dao.ReceiptDao;
import org.annpath.model.DigitalRescueReceipt;
import org.annpath.model.Pickup;
import org.annpath.model.User;
import org.annpath.util.HashGenerator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;

@WebServlet(name = "ReceiptServlet", urlPatterns = {"/receipt/*"})
public class ReceiptServlet extends BaseServlet {

    private final ReceiptDao receiptDao = new ReceiptDao();
    private final PickupDao pickupDao = new PickupDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        String receiptIdParam = request.getParameter("id");
        String pickupIdParam = request.getParameter("pickupId");
        if (pickupIdParam == null || pickupIdParam.trim().isEmpty()) {
            pickupIdParam = request.getParameter("pickup_id");
        }

        DigitalRescueReceipt receipt = null;

        try {
            // 1. Try by pickupId if provided
            if (pickupIdParam != null && !pickupIdParam.trim().isEmpty()) {
                try {
                    Long pId = Long.parseLong(pickupIdParam.trim());
                    receipt = receiptDao.findByPickupId(pId);
                    if (receipt == null) {
                        Pickup p = pickupDao.findById(pId);
                        if (p != null) {
                            receipt = buildReceiptFromPickup(p);
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }

            // 2. If still null, try receiptIdParam
            if (receipt == null && receiptIdParam != null && !receiptIdParam.trim().isEmpty()) {
                String trimmedId = receiptIdParam.trim();
                receipt = receiptDao.findByReceiptNumber(trimmedId);

                // If not found by receipt number, maybe trimmedId is a numeric pickup ID
                if (receipt == null) {
                    try {
                        Long pId = Long.parseLong(trimmedId);
                        receipt = receiptDao.findByPickupId(pId);
                        if (receipt == null) {
                            Pickup p = pickupDao.findById(pId);
                            if (p != null) {
                                receipt = buildReceiptFromPickup(p);
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (receipt == null) {
                User user = getCurrentUser(request);
                String fallbackUrl = (user != null && "DONOR".equals(user.getRole())) ? "/donor/dashboard" : "/ngo/dashboard";
                flashError(request, "Digital Rescue Receipt not found or rescue has not completed yet.");
                redirect(fallbackUrl, request, response);
                return;
            }

            // Calculate impact metrics if not populated
            if (receipt.getRescuedQuantity() != null) {
                if (receipt.getEstimatedPeopleFed() <= 0) {
                    receipt.setEstimatedPeopleFed((int) Math.round(receipt.getRescuedQuantity().doubleValue() / 0.4));
                }
                if (receipt.getCo2SavedKg() == null) {
                    receipt.setCo2SavedKg(receipt.getRescuedQuantity().multiply(new BigDecimal("2.5")));
                }
            }

            request.setAttribute("receipt", receipt);

            if ("/print".equals(path)) {
                renderJsp("receipt/print-receipt.jsp", request, response);
            } else {
                renderJsp("receipt/view-receipt.jsp", request, response);
            }
        } catch (Exception e) {
            flashError(request, "Error retrieving receipt: " + e.getMessage());
            redirect("/home", request, response);
        }
    }

    private DigitalRescueReceipt buildReceiptFromPickup(Pickup p) {
        DigitalRescueReceipt r = new DigitalRescueReceipt();
        String recNum = "ANN-REC-" + String.format("%06d", p.getId());
        r.setReceiptNumber(recNum);
        r.setPickupId(p.getId());
        r.setFoodListingId(p.getFoodListingId());
        r.setDonorUserId(p.getDonorUserId());
        r.setNgoUserId(p.getNgoUserId());
        r.setVolunteerUserId(p.getVolunteerUserId());
        r.setRescuedQuantity(p.getQuantity() != null ? p.getQuantity() : BigDecimal.ZERO);
        r.setUnitName(p.getUnitName() != null ? p.getUnitName() : "kg");
        r.setFoodCategoryName(p.getFoodCategory() != null ? p.getFoodCategory() : "Prepared Food");

        Timestamp pickupTs = p.getActualPickedUpAt() != null ? p.getActualPickedUpAt() : p.getAssignedAt();
        Timestamp delivTs = p.getActualDeliveredAt() != null ? p.getActualDeliveredAt() : p.getUpdatedAt();
        Timestamp distTs = p.getNgoDistributedAt() != null ? p.getNgoDistributedAt() : (delivTs != null ? delivTs : new Timestamp(System.currentTimeMillis()));

        r.setPickupTimestamp(pickupTs);
        r.setDeliveryTimestamp(delivTs);
        r.setDistributionTimestamp(distTs);
        r.setIssuedAt(distTs);
        r.setDistanceCoveredKm(new BigDecimal("4.5"));

        r.setFoodTitle(p.getFoodTitle());
        r.setDonorName(p.getDonorName());
        r.setDonorAddress(p.getPickupAddress());
        r.setNgoName(p.getNgoName());
        r.setNgoAddress(p.getNgoAddress());
        r.setVolunteerName(p.getVolunteerName());

        int meals = (int) Math.round(r.getRescuedQuantity().doubleValue() / 0.4);
        r.setEstimatedPeopleFed(meals);
        r.setCo2SavedKg(r.getRescuedQuantity().multiply(new BigDecimal("2.5")));

        String hashPayload = recNum + "|" + p.getFoodListingId() + "|" + p.getDonorUserId() + "|" +
                             p.getNgoUserId() + "|" + r.getRescuedQuantity().toPlainString() + "|" + distTs.getTime();
        r.setVerificationHashSha256(HashGenerator.sha256(hashPayload));
        return r;
    }
}
