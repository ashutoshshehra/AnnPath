package org.annpath.service;

import org.annpath.config.AppConfig;
import org.annpath.config.Constants;
import org.annpath.dao.ConnectionPool;
import org.annpath.dao.FoodListingDao;
import org.annpath.dao.PickupDao;
import org.annpath.dao.RescueBottleneckDao;
import org.annpath.dao.VolunteerDao;
import org.annpath.model.FoodListing;
import org.annpath.model.Pickup;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Failure Recovery Engine for AnnPath.
 * Automatically recovers from real-world volunteer rejections, timeouts, and operational transit failures.
 * Enforces retry limits, row locks, fallback mechanisms, and audit logging.
 */
public class FailureRecoveryEngine {

    private static final Logger LOGGER = Logger.getLogger(FailureRecoveryEngine.class.getName());

    private final PickupDao pickupDao = new PickupDao();
    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final VolunteerDao volunteerDao = new VolunteerDao();
    private final RescueBottleneckDao bottleneckDao = new RescueBottleneckDao();
    private final NotificationService notificationService = new NotificationService();
    private final RescueJourneyService journeyService = new RescueJourneyService();
    private final NoVolunteerFallbackService fallbackService = new NoVolunteerFallbackService();

    /**
     * Handle volunteer rejection or response timeout during assignment phase.
     * Evaluates retry counts against platform limit and reassigns or falls back.
     *
     * @param pickupId the pickup ID being recovered
     * @param volunteerUserId the volunteer who rejected or timed out
     * @param reason the documented failure reason
     * @return true if reassignment attempt initiated; false if max attempts reached and fallback triggered
     */
    public boolean handleVolunteerRejection(Long pickupId, Long volunteerUserId, String reason) throws SQLException {
        if (pickupId == null) return false;

        Connection conn = ConnectionPool.getInstance().getConnection();
        boolean fallbackTriggered = false;
        Pickup pickup = null;

        try {
            conn.setAutoCommit(false);

            // 1. Lock pickup row to prevent race conditions
            pickup = pickupDao.findByIdForUpdate(pickupId, conn);
            if (pickup == null) {
                conn.rollback();
                LOGGER.warning("Failure recovery aborted: Pickup #" + pickupId + " not found.");
                return false;
            }

            // 2. Record rejection in assignment history & release volunteer
            if (volunteerUserId != null) {
                pickupDao.recordAssignmentResponse(pickupId, volunteerUserId, Constants.VOL_ASSIGN_REJECTED,
                        reason != null ? reason : "Volunteer unavailable", conn);
                volunteerDao.adjustActiveAssignments(volunteerUserId, -1, conn);
                pickupDao.unclaimPickup(pickupId, volunteerUserId, conn);
            }

            // 3. Count total assignment attempts
            int totalAttempts = pickupDao.countAssignmentAttempts(pickupId);
            int maxAttempts = AppConfig.getFailureRecoveryMaxAttempts();

            LOGGER.info(String.format("Pickup #%d recovery check: Attempt %d of max %d", pickupId, totalAttempts, maxAttempts));

            if (totalAttempts < maxAttempts) {
                // Set state to REASSIGNING
                pickupDao.updateStatus(pickupId, Constants.PICKUP_STATUS_REASSIGNING, conn);

                // Audit log the transition
                try {
                    journeyService.recordTransition(
                            pickup.getFoodListingId(), pickupId,
                            pickup.getStatus(), Constants.PICKUP_STATUS_REASSIGNING,
                            volunteerUserId, Constants.ROLE_VOLUNTEER,
                            "Volunteer rejection (Attempt " + totalAttempts + "): " + reason,
                            "{\"totalAttempts\":" + totalAttempts + "}",
                            conn
                    );
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Error recording transition to REASSIGNING: " + ex.getMessage(), ex);
                }

                conn.commit();
            } else {
                // Max attempts reached: fallback to VOLUNTEER_UNAVAILABLE
                fallbackTriggered = true;
                pickupDao.updateStatus(pickupId, Constants.PICKUP_STATUS_VOLUNTEER_UNAVAILABLE, conn);

                try {
                    journeyService.recordTransition(
                            pickup.getFoodListingId(), pickupId,
                            pickup.getStatus(), Constants.STATE_VOLUNTEER_UNAVAILABLE,
                            volunteerUserId, Constants.ROLE_VOLUNTEER,
                            "Max volunteer attempts (" + maxAttempts + ") exhausted. Triggering NGO fallback.",
                            "{\"totalAttempts\":" + totalAttempts + ",\"fallback\":\"NGO_SELF_PICKUP\"}",
                            conn
                    );
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Error recording transition to VOLUNTEER_UNAVAILABLE: " + ex.getMessage(), ex);
                }

                conn.commit();
            }
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignore) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
                ConnectionPool.getInstance().releaseConnection(conn);
            }
        }

        // Post-transaction actions
        if (fallbackTriggered) {
            fallbackService.triggerVolunteerUnavailable(pickupId);
            return false;
        } else {
            // Rebroadcast pickup to other available volunteers
            VolunteerAssignmentEngine assignmentEngine = new VolunteerAssignmentEngine();
            assignmentEngine.broadcastPickupToAllVolunteers(pickupId);
            return true;
        }
    }

    /**
     * Handle pickup operational failure (e.g., food spoiled, donor unreachable, packaging broken).
     */
    public boolean handlePickupFailure(Long pickupId, String reason, Long actorUserId) throws SQLException {
        if (pickupId == null) return false;

        Connection conn = ConnectionPool.getInstance().getConnection();
        FoodListing listing = null;
        Pickup pickup = null;
        boolean expired = false;
        boolean criticalTime = false;
        int totalAttempts = 0;
        int maxAttempts = AppConfig.getFailureRecoveryMaxAttempts();

        try {
            conn.setAutoCommit(false);

            pickup = pickupDao.findByIdForUpdate(pickupId, conn);
            if (pickup == null) {
                conn.rollback();
                return false;
            }

            listing = foodListingDao.findById(pickup.getFoodListingId());

            // Check remaining shelf-life
            if (listing != null && listing.getAvailableUntil() != null) {
                long diffMs = listing.getAvailableUntil().getTime() - System.currentTimeMillis();
                long diffMinutes = diffMs / (60 * 1000);
                if (diffMinutes <= 0) {
                    expired = true;
                } else if (diffMinutes < 30) {
                    criticalTime = true;
                }
            }

            // Free volunteer workload if one was assigned
            if (pickup.getVolunteerUserId() != null) {
                volunteerDao.adjustActiveAssignments(pickup.getVolunteerUserId(), -1, conn);
                pickupDao.unclaimPickup(pickupId, pickup.getVolunteerUserId(), conn);
            }

            String targetStatus = expired ? Constants.STATE_EXPIRED : Constants.PICKUP_STATUS_PICKUP_FAILED;
            pickupDao.updateStatus(pickupId, targetStatus, conn);

            // Record verified audit transition
            try {
                journeyService.recordTransition(
                        pickup.getFoodListingId(), pickupId,
                        pickup.getStatus(), targetStatus,
                        actorUserId, "OPERATIONAL",
                        expired ? "Food surplus expired during pickup attempt: " + reason : "Pickup failed: " + reason,
                        "{\"failureType\":\"PICKUP_STAGE\",\"expired\":" + expired + ",\"criticalTime\":" + criticalTime + ",\"reason\":\"" + (reason != null ? reason.replace("\"", "\\\"") : "") + "\"}",
                        conn
                );
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error recording transition to " + targetStatus + ": " + ex.getMessage(), ex);
            }

            conn.commit();

            // Total attempts so far
            totalAttempts = pickupDao.countAssignmentAttempts(pickupId);

            // Notify Stakeholders of outcome
            notificationService.notifyUser(
                    pickup.getDonorUserId(),
                    expired ? "RESCUE_EXPIRED" : "PICKUP_FAILED",
                    expired ? "Food Rescue Expired" : "Food Rescue Pickup Interrupted",
                    "Pickup for '" + pickup.getFoodTitle() + "' was interrupted: " + reason,
                    "PICKUP",
                    pickupId
            );
            notificationService.notifyUser(
                    pickup.getNgoUserId(),
                    expired ? "RESCUE_EXPIRED" : "PICKUP_FAILED",
                    expired ? "Food Rescue Expired" : "Food Rescue Pickup Interrupted",
                    "Pickup for '" + pickup.getFoodTitle() + "' was interrupted: " + reason,
                    "PICKUP",
                    pickupId
            );

            LOGGER.warning("Pickup #" + pickupId + " handled failure. Result: " + targetStatus + " Reason: " + reason);
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignore) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
                ConnectionPool.getInstance().releaseConnection(conn);
            }
        }

        // Post-transaction operational decisions
        if (expired) {
            return true;
        }

        if (criticalTime || totalAttempts >= maxAttempts) {
            // Escalate critical bottleneck and trigger NGO self-pickup
            bottleneckDao.createBottleneck(
                    criticalTime ? "CRITICAL_TIME_SHORTAGE" : "VOLUNTEER_SHORTAGE",
                    "CRITICAL",
                    "Pickup failed (" + reason + "). Remaining shelf life is critical or volunteer attempts exhausted (" + totalAttempts + "/" + maxAttempts + "). NGO Self-Pickup required.",
                    listing != null ? listing.getId() : null,
                    pickupId
            );
            fallbackService.triggerVolunteerUnavailable(pickupId);
        } else {
            // Attempt alternative volunteer broadcast
            VolunteerAssignmentEngine assignmentEngine = new VolunteerAssignmentEngine();
            assignmentEngine.broadcastPickupToAllVolunteers(pickupId);
        }

        return true;
    }

    /**
     * Handle delivery operational failure (e.g., NGO closed, transit accident, access blocked).
     */
    public boolean handleDeliveryFailure(Long pickupId, String reason, Long actorUserId) throws SQLException {
        if (pickupId == null) return false;

        Connection conn = ConnectionPool.getInstance().getConnection();
        FoodListing listing = null;
        Pickup pickup = null;
        boolean expired = false;

        try {
            conn.setAutoCommit(false);

            pickup = pickupDao.findByIdForUpdate(pickupId, conn);
            if (pickup == null) {
                conn.rollback();
                return false;
            }

            listing = foodListingDao.findById(pickup.getFoodListingId());
            if (listing != null && listing.getAvailableUntil() != null) {
                long diffMs = listing.getAvailableUntil().getTime() - System.currentTimeMillis();
                if (diffMs <= 0) {
                    expired = true;
                }
            }

            String targetStatus = expired ? Constants.STATE_EXPIRED : Constants.PICKUP_STATUS_DELIVERY_FAILED;
            pickupDao.updateStatus(pickupId, targetStatus, conn);

            // Free volunteer workload
            if (pickup.getVolunteerUserId() != null) {
                volunteerDao.adjustActiveAssignments(pickup.getVolunteerUserId(), -1, conn);
            }

            try {
                journeyService.recordTransition(
                        pickup.getFoodListingId(), pickupId,
                        pickup.getStatus(), targetStatus,
                        actorUserId, "OPERATIONAL",
                        expired ? "Surplus expired during delivery transit: " + reason : "Delivery failed: " + reason,
                        "{\"failureType\":\"DELIVERY_STAGE\",\"expired\":" + expired + ",\"reason\":\"" + (reason != null ? reason.replace("\"", "\\\"") : "") + "\"}",
                        conn
                );
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error recording transition to " + targetStatus + ": " + ex.getMessage(), ex);
            }

            conn.commit();

            notificationService.notifyUser(
                    pickup.getNgoUserId(),
                    expired ? "RESCUE_EXPIRED" : "DELIVERY_FAILED",
                    expired ? "Food Rescue Expired" : "Food Delivery Interrupted",
                    "Delivery for '" + pickup.getFoodTitle() + "' could not be finalized: " + reason,
                    "PICKUP",
                    pickupId
            );
            notificationService.notifyUser(
                    pickup.getDonorUserId(),
                    expired ? "RESCUE_EXPIRED" : "DELIVERY_FAILED",
                    expired ? "Food Rescue Expired" : "Food Delivery Interrupted",
                    "Delivery for '" + pickup.getFoodTitle() + "' could not be finalized: " + reason,
                    "PICKUP",
                    pickupId
            );

            LOGGER.warning("Pickup #" + pickupId + " handled delivery failure. Result: " + targetStatus + " Reason: " + reason);
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignore) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
                ConnectionPool.getInstance().releaseConnection(conn);
            }
        }

        if (!expired) {
            bottleneckDao.createBottleneck(
                    "DELIVERY_FAILURE",
                    "CRITICAL",
                    "Delivery transit failed (" + reason + ") for Food Rescue #" + pickupId + ". Immediate operations review required.",
                    listing != null ? listing.getId() : null,
                    pickupId
            );
        }

        return true;
    }
}
