package org.annpath.service;

import org.annpath.config.Constants;
import org.annpath.dao.FoodListingDao;
import org.annpath.dao.FoodStatusHistoryDao;
import org.annpath.dao.PickupDao;
import org.annpath.exception.InvalidStateTransitionException;
import org.annpath.model.FoodListing;
import org.annpath.model.FoodStatusHistory;
import org.annpath.model.Pickup;

import org.annpath.dao.ConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Year;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Validated State Engine & Lifecycle Manager for Food Rescue Passports.
 * Enforces strict non-linear transitions and records immutable audit events.
 */
public class RescueJourneyService {

    private static final Logger LOGGER = Logger.getLogger(RescueJourneyService.class.getName());

    private final FoodStatusHistoryDao historyDao = new FoodStatusHistoryDao();
    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final PickupDao pickupDao = new PickupDao();

    // Validated State Transition Matrix
    private static final Map<String, Set<String>> VALID_TRANSITIONS = new HashMap<>();

    static {
        VALID_TRANSITIONS.put("NONE", new HashSet<>(Arrays.asList(Constants.STATE_DONATED)));
        VALID_TRANSITIONS.put(Constants.STATE_DONATED, new HashSet<>(Arrays.asList(
                Constants.STATE_MATCHED, Constants.STATE_REQUESTED, Constants.STATE_CANCELLED, Constants.STATE_EXPIRED
        )));
        VALID_TRANSITIONS.put(Constants.STATE_MATCHED, new HashSet<>(Arrays.asList(
                Constants.STATE_REQUESTED, Constants.STATE_CANCELLED, Constants.STATE_EXPIRED
        )));
        VALID_TRANSITIONS.put(Constants.STATE_REQUESTED, new HashSet<>(Arrays.asList(
                Constants.STATE_APPROVED, Constants.STATE_REJECTED, Constants.STATE_CANCELLED, Constants.STATE_EXPIRED
        )));
        VALID_TRANSITIONS.put(Constants.STATE_APPROVED, new HashSet<>(Arrays.asList(
                Constants.STATE_VOLUNTEER_ASSIGNED, Constants.STATE_NGO_SELF_PICKUP_ASSIGNED,
                Constants.STATE_VOLUNTEER_UNAVAILABLE, Constants.STATE_CANCELLED, Constants.STATE_EXPIRED
        )));
        VALID_TRANSITIONS.put(Constants.STATE_VOLUNTEER_ASSIGNED, new HashSet<>(Arrays.asList(
                Constants.STATE_PICKED_UP, Constants.PICKUP_STATUS_REASSIGNING,
                Constants.STATE_VOLUNTEER_UNAVAILABLE, Constants.PICKUP_STATUS_PICKUP_FAILED, Constants.STATE_CANCELLED
        )));
        VALID_TRANSITIONS.put(Constants.PICKUP_STATUS_REASSIGNING, new HashSet<>(Arrays.asList(
                Constants.STATE_VOLUNTEER_ASSIGNED, Constants.STATE_VOLUNTEER_UNAVAILABLE,
                Constants.STATE_NGO_SELF_PICKUP_ASSIGNED, Constants.STATE_CANCELLED
        )));
        VALID_TRANSITIONS.put(Constants.STATE_VOLUNTEER_UNAVAILABLE, new HashSet<>(Arrays.asList(
                Constants.STATE_NGO_SELF_PICKUP_ASSIGNED, Constants.STATE_VOLUNTEER_ASSIGNED, Constants.STATE_CANCELLED
        )));
        VALID_TRANSITIONS.put(Constants.STATE_NGO_SELF_PICKUP_ASSIGNED, new HashSet<>(Arrays.asList(
                Constants.STATE_PICKED_UP, Constants.PICKUP_STATUS_PICKUP_FAILED, Constants.STATE_CANCELLED
        )));
        VALID_TRANSITIONS.put(Constants.STATE_PICKED_UP, new HashSet<>(Arrays.asList(
                Constants.STATE_DELIVERED, Constants.PICKUP_STATUS_DELIVERY_FAILED, Constants.STATE_CANCELLED
        )));
        VALID_TRANSITIONS.put(Constants.STATE_DELIVERED, new HashSet<>(Arrays.asList(
                Constants.STATE_RECEIVED, Constants.STATE_DISTRIBUTED, Constants.STATE_CANCELLED
        )));
        VALID_TRANSITIONS.put(Constants.STATE_RECEIVED, new HashSet<>(Arrays.asList(
                Constants.STATE_DISTRIBUTED
        )));
        VALID_TRANSITIONS.put(Constants.PICKUP_STATUS_PICKUP_FAILED, new HashSet<>(Arrays.asList(
                Constants.PICKUP_STATUS_REASSIGNING, Constants.STATE_VOLUNTEER_ASSIGNED,
                Constants.STATE_VOLUNTEER_UNAVAILABLE, Constants.STATE_NGO_SELF_PICKUP_ASSIGNED,
                Constants.STATE_EXPIRED, Constants.STATE_CANCELLED
        )));
        VALID_TRANSITIONS.put(Constants.PICKUP_STATUS_DELIVERY_FAILED, new HashSet<>(Arrays.asList(
                Constants.STATE_DELIVERED, Constants.STATE_RECEIVED,
                Constants.STATE_EXPIRED, Constants.STATE_CANCELLED
        )));
    }

    /**
     * Generate standard human-readable Rescue ID tracking code (e.g. ANP-2026-000184).
     */
    public static String generateRescueCode(Long listingId, Timestamp createdAt) {
        int year = (createdAt != null) ? createdAt.toLocalDateTime().getYear() : Year.now().getValue();
        return String.format("ANP-%d-%06d", year, listingId);
    }

    /**
     * Validate whether a state transition is permitted by the state machine.
     */
    public void validateTransition(String currentStatus, String targetStatus) throws InvalidStateTransitionException {
        if (targetStatus == null || targetStatus.trim().isEmpty()) {
            throw new InvalidStateTransitionException(currentStatus, targetStatus, "Target state cannot be null or empty.");
        }

        String from = (currentStatus == null || currentStatus.trim().isEmpty()) ? "NONE" : currentStatus.trim();
        String to = targetStatus.trim();

        // Idempotent state transition (target is same as current)
        if (from.equalsIgnoreCase(to)) {
            return;
        }

        Set<String> allowed = VALID_TRANSITIONS.get(from);

        if (allowed == null || !allowed.contains(to)) {
            throw new InvalidStateTransitionException(
                    from,
                    to,
                    String.format("Illegal state leap: Transition from '%s' to '%s' is strictly disallowed by the Rescue State Machine.", from, to)
            );
        }
    }

    /**
     * Record a verified transition in food_status_history and update the entity status within an active transaction.
     */
    public void recordTransition(Long foodListingId, Long pickupId, String previousStatus, String newStatus,
                                 Long actorUserId, String actorRole, String reason, String metadataJson,
                                 Connection conn) throws InvalidStateTransitionException, SQLException {
        // Enforce state transition safety
        validateTransition(previousStatus, newStatus);

        FoodStatusHistory history = new FoodStatusHistory();
        history.setFoodListingId(foodListingId);
        history.setPickupId(pickupId);
        history.setPreviousStatus(previousStatus != null ? previousStatus : "NONE");
        history.setNewStatus(newStatus);
        history.setActorUserId(actorUserId);
        history.setActorRole(actorRole != null ? actorRole : "SYSTEM");
        history.setTransitionReason(reason != null ? reason : "Operational status update");
        history.setTransitionMetadataJson(metadataJson);

        historyDao.recordTransition(history, conn);

        // Update listing status if applicable
        if (foodListingId != null) {
            foodListingDao.updateStatus(foodListingId, newStatus, conn);
        }
        if (pickupId != null) {
            pickupDao.updateStatus(pickupId, newStatus, conn);
        }

        LOGGER.info(String.format("Rescue Transition: Listing #%d [Pickup #%s]: %s -> %s by User #%s (%s)",
                foodListingId, String.valueOf(pickupId), previousStatus, newStatus, String.valueOf(actorUserId), actorRole));
    }

    /**
     * Record a verified transition with automatic connection and transaction management.
     */
    public void recordTransition(Long foodListingId, Long pickupId, String previousStatus, String newStatus,
                                 Long actorUserId, String actorRole, String reason, String metadataJson)
            throws InvalidStateTransitionException, SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.setAutoCommit(false);
            recordTransition(foodListingId, pickupId, previousStatus, newStatus, actorUserId, actorRole, reason, metadataJson, conn);
            conn.commit();
        } catch (SQLException | RuntimeException e) {
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
    }

    /**
     * Fetch complete audit milestones for a food rescue.
     */
    public List<FoodStatusHistory> getJourneyEvents(Long foodListingId, Long pickupId) throws SQLException {
        if (foodListingId != null) {
            List<FoodStatusHistory> events = historyDao.findByFoodListingId(foodListingId);
            if (!events.isEmpty()) return events;
        }
        if (pickupId != null) {
            return historyDao.findByPickupId(pickupId);
        }
        return Collections.emptyList();
    }
}
