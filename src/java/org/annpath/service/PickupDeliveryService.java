package org.annpath.service;

import org.annpath.config.Constants;
import org.annpath.dao.ConnectionPool;
import org.annpath.dao.FoodListingDao;
import org.annpath.dao.FoodRequestDao;
import org.annpath.dao.ImpactDao;
import org.annpath.dao.PickupDao;
import org.annpath.dao.ReceiptDao;
import org.annpath.dao.VolunteerDao;
import org.annpath.exception.InvalidStateTransitionException;
import org.annpath.exception.ValidationException;
import org.annpath.location.GeoUtils;
import org.annpath.model.DigitalRescueReceipt;
import org.annpath.model.FoodListing;
import org.annpath.model.ImpactRecord;
import org.annpath.model.Pickup;
import org.annpath.security.PasswordHasher;
import org.annpath.util.HashGenerator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PickupDeliveryService {

    private final PickupDao pickupDao = new PickupDao();
    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final FoodRequestDao foodRequestDao = new FoodRequestDao();
    private final VolunteerDao volunteerDao = new VolunteerDao();
    private final ReceiptDao receiptDao = new ReceiptDao();
    private final ImpactDao impactDao = new ImpactDao();
    private final NotificationService notificationService = new NotificationService();
    private final RescueJourneyService journeyService = new RescueJourneyService();

    /**
     * Create a new Pickup and assign OTPs when a Donor approves a food request.
     */
    public Pickup createPickup(Long foodListingId, Long foodRequestId, Long donorUserId, Long ngoUserId,
                               Timestamp windowStart, Timestamp windowEnd, Connection conn) throws SQLException {
        String rawPickupOtp = HashGenerator.generate6DigitOtp();
        String rawDeliveryOtp = HashGenerator.generate6DigitOtp();

        String pickupSalt = PasswordHasher.generateSalt();
        String deliverySalt = PasswordHasher.generateSalt();

        String pickupHash = PasswordHasher.hashPassword(rawPickupOtp, pickupSalt) + ":" + pickupSalt;
        String deliveryHash = PasswordHasher.hashPassword(rawDeliveryOtp, deliverySalt) + ":" + deliverySalt;

        Pickup p = new Pickup();
        p.setFoodListingId(foodListingId);
        p.setFoodRequestId(foodRequestId);
        p.setDonorUserId(donorUserId);
        p.setNgoUserId(ngoUserId);
        p.setPickupMethod(Constants.PICKUP_METHOD_VOLUNTEER);
        p.setPickupWindowStart(windowStart);
        p.setPickupWindowEnd(windowEnd);
        p.setPickupOtpHash(pickupHash);
        p.setDeliveryOtpHash(deliveryHash);

        Long pickupId = pickupDao.create(p, conn);
        p.setId(pickupId);
        return p;
    }

    public Pickup createPickup(Long foodListingId, Long foodRequestId, Long donorUserId, Long ngoUserId,
                               Timestamp windowStart, Timestamp windowEnd) throws SQLException {
        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            Pickup p = createPickup(foodListingId, foodRequestId, donorUserId, ngoUserId, windowStart, windowEnd, conn);
            conn.commit();
            return p;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Verify pickup OTP and transition status to PICKED_UP.
     */
    public void confirmPickup(Long pickupId, Long actorUserId, String enteredOtp) throws ValidationException, InvalidStateTransitionException, SQLException {
        Pickup pickup = pickupDao.findById(pickupId);
        if (pickup == null) throw new ValidationException("Pickup task not found.");

        if (!Constants.PICKUP_STATUS_ASSIGNED.equals(pickup.getStatus()) &&
            !Constants.PICKUP_STATUS_SELF_ASSIGNED.equals(pickup.getStatus())) {
            throw new InvalidStateTransitionException(pickup.getStatus(), Constants.STATE_PICKED_UP, "Pickup cannot be confirmed in current status.");
        }

        // Verify OTP
        if (!verifyOtpWithSalt(enteredOtp, pickup.getPickupOtpHash())) {
            throw new ValidationException("Invalid Pickup OTP provided. Please obtain the correct code from the donor.");
        }

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            pickupDao.recordPickedUp(pickupId, conn);
            foodListingDao.updateStatus(pickup.getFoodListingId(), Constants.STATE_PICKED_UP, conn);

            try {
                journeyService.recordTransition(pickup.getFoodListingId(), pickupId, pickup.getStatus(),
                        Constants.STATE_PICKED_UP, actorUserId, Constants.ROLE_VOLUNTEER, "Food pickup verified via OTP", null, conn);
            } catch (Exception ignored) {}

            conn.commit();

            notificationService.notifyUser(
                    pickup.getDonorUserId(),
                    "FOOD_PICKED_UP",
                    "Food Handover Confirmed!",
                    "Food rescue (" + pickup.getFoodTitle() + ") has been picked up successfully.",
                    "PICKUP",
                    pickupId
            );
            notificationService.notifyUser(
                    pickup.getNgoUserId(),
                    "FOOD_IN_TRANSIT",
                    "Food In Transit",
                    "Food rescue (" + pickup.getFoodTitle() + ") is now in transit to your distribution center.",
                    "PICKUP",
                    pickupId
            );
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Verify delivery OTP and transition status to DELIVERED.
     */
    public void confirmDelivery(Long pickupId, Long actorUserId, String enteredOtp) throws ValidationException, InvalidStateTransitionException, SQLException {
        Pickup pickup = pickupDao.findById(pickupId);
        if (pickup == null) throw new ValidationException("Pickup task not found.");

        if (!Constants.PICKUP_STATUS_PICKED_UP.equals(pickup.getStatus())) {
            throw new InvalidStateTransitionException(pickup.getStatus(), Constants.STATE_DELIVERED, "Delivery can only be confirmed after food is picked up.");
        }

        // Verify Delivery OTP
        if (!verifyOtpWithSalt(enteredOtp, pickup.getDeliveryOtpHash())) {
            throw new ValidationException("Invalid Delivery OTP provided. Please obtain the correct code from the receiving NGO.");
        }

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            pickupDao.recordDelivered(pickupId, conn);
            foodListingDao.updateStatus(pickup.getFoodListingId(), Constants.STATE_DELIVERED, conn);

            try {
                journeyService.recordTransition(pickup.getFoodListingId(), pickupId, pickup.getStatus(),
                        Constants.STATE_DELIVERED, actorUserId, Constants.ROLE_VOLUNTEER, "Food delivery verified via OTP", null, conn);
            } catch (Exception ignored) {}

            // If volunteer delivered, decrement volunteer's active count
            if (pickup.getVolunteerUserId() != null) {
                volunteerDao.adjustActiveAssignments(pickup.getVolunteerUserId(), -1, conn);
            }

            conn.commit();

            notificationService.notifyUser(
                    pickup.getNgoUserId(),
                    "DELIVERY_ARRIVED",
                    "Food Arrived at Destination",
                    "Food rescue (" + pickup.getFoodTitle() + ") has arrived. Please inspect and confirm receipt.",
                    "PICKUP",
                    pickupId
            );
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * NGO confirms inspection and receipt of food (RECEIVED).
     */
    public void confirmReceipt(Long pickupId, Long ngoUserId) throws ValidationException, InvalidStateTransitionException, SQLException {
        Pickup pickup = pickupDao.findById(pickupId);
        if (pickup == null || !pickup.getNgoUserId().equals(ngoUserId)) {
            throw new ValidationException("Unauthorized or pickup task not found.");
        }

        if (!Constants.PICKUP_STATUS_DELIVERED.equals(pickup.getStatus())) {
            throw new InvalidStateTransitionException(pickup.getStatus(), Constants.STATE_RECEIVED, "Cannot confirm receipt before delivery.");
        }

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            pickupDao.recordReceived(pickupId, conn);
            foodListingDao.updateStatus(pickup.getFoodListingId(), Constants.STATE_RECEIVED, conn);

            try {
                journeyService.recordTransition(pickup.getFoodListingId(), pickupId, pickup.getStatus(),
                        Constants.STATE_RECEIVED, ngoUserId, Constants.ROLE_NGO, "NGO verified and received food into center", null, conn);
            } catch (Exception ignored) {}

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * NGO records distribution to community (DISTRIBUTED).
     * Atomically produces Digital Rescue Receipt and Impact Record.
     */
    public DigitalRescueReceipt completeDistribution(Long pickupId, Long ngoUserId) throws ValidationException, InvalidStateTransitionException, SQLException {
        Pickup pickup = pickupDao.findById(pickupId);
        if (pickup == null || !pickup.getNgoUserId().equals(ngoUserId)) {
            throw new ValidationException("Unauthorized or pickup task not found.");
        }

        if (!Constants.PICKUP_STATUS_RECEIVED.equals(pickup.getStatus())) {
            throw new InvalidStateTransitionException(pickup.getStatus(), Constants.STATE_DISTRIBUTED, "Cannot distribute before receipt confirmation.");
        }

        FoodListing listing = foodListingDao.findById(pickup.getFoodListingId());
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // Compute metrics
        BigDecimal rescuedQty = pickup.getQuantity();
        int estimatedMeals = (int) Math.round(rescuedQty.doubleValue() / Constants.MEAL_PORTION_KG);
        BigDecimal co2Prevented = rescuedQty.multiply(BigDecimal.valueOf(Constants.CO2_KG_PER_KG_FOOD));

        // Generate Digital Receipt Number & Cryptographic verification signature
        String receiptNumber = HashGenerator.generateReceiptNumber();
        String hashPayload = receiptNumber + "|" + listing.getId() + "|" + pickup.getDonorUserId() + "|" +
                             pickup.getNgoUserId() + "|" + rescuedQty.toPlainString() + "|" + now.getTime();
        String verificationHash = HashGenerator.sha256(hashPayload);

        DigitalRescueReceipt receipt = new DigitalRescueReceipt();
        receipt.setReceiptNumber(receiptNumber);
        receipt.setFoodListingId(listing.getId());
        receipt.setPickupId(pickupId);
        receipt.setDonorUserId(pickup.getDonorUserId());
        receipt.setNgoUserId(pickup.getNgoUserId());
        receipt.setVolunteerUserId(pickup.getVolunteerUserId());
        receipt.setRescuedQuantity(rescuedQty);
        receipt.setUnitName(pickup.getUnitName());
        receipt.setFoodCategoryName(pickup.getFoodCategory());
        receipt.setPickupTimestamp(pickup.getActualPickedUpAt() != null ? pickup.getActualPickedUpAt() : now);
        receipt.setDeliveryTimestamp(pickup.getActualDeliveredAt() != null ? pickup.getActualDeliveredAt() : now);
        receipt.setDistributionTimestamp(now);
        receipt.setDistanceCoveredKm(BigDecimal.valueOf(5.0)); // Proximity estimate
        receipt.setVerificationHashSha256(verificationHash);

        ImpactRecord impact = new ImpactRecord();
        impact.setFoodListingId(listing.getId());
        impact.setPickupId(pickupId);
        impact.setNgoUserId(pickup.getNgoUserId());
        impact.setDonorUserId(pickup.getDonorUserId());
        impact.setVolunteerUserId(pickup.getVolunteerUserId());
        impact.setRescuedQuantityKg(rescuedQty);
        impact.setEstimatedPeopleFed(estimatedMeals);
        impact.setCo2EmissionsPreventedKg(co2Prevented);

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            pickupDao.recordDistributed(pickupId, conn);
            foodListingDao.updateStatus(listing.getId(), Constants.STATE_DISTRIBUTED, conn);
            receiptDao.save(receipt, conn);
            impactDao.save(impact, conn);

            try {
                journeyService.recordTransition(listing.getId(), pickupId, pickup.getStatus(),
                        Constants.STATE_DISTRIBUTED, ngoUserId, Constants.ROLE_NGO, "Surplus food distributed to community beneficiaries",
                        "{\"receiptNumber\":\"" + receiptNumber + "\"}", conn);
            } catch (Exception ignored) {}

            conn.commit();

            notificationService.notifyUser(
                    pickup.getDonorUserId(),
                    "RESCUE_COMPLETED",
                    "Impact Achieved! Food Distributed",
                    "Your surplus food (" + listing.getTitle() + ") has fed an estimated " + estimatedMeals + " people! Your Digital Rescue Receipt #" + receiptNumber + " is now ready.",
                    "RECEIPT",
                    pickupId
            );

            return receipt;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    private boolean verifyOtpWithSalt(String enteredOtp, String storedHashAndSalt) {
        if (enteredOtp == null || storedHashAndSalt == null || !storedHashAndSalt.contains(":")) {
            return false;
        }
        String[] parts = storedHashAndSalt.split(":");
        String hash = parts[0];
        String salt = parts[1];
        return PasswordHasher.verifyPassword(enteredOtp.trim(), salt, hash);
    }
}
