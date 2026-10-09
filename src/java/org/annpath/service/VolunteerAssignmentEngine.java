package org.annpath.service;

import org.annpath.config.AppConfig;
import org.annpath.config.Constants;
import org.annpath.dao.ConnectionPool;
import org.annpath.dao.FoodListingDao;
import org.annpath.dao.PickupDao;
import org.annpath.dao.VolunteerDao;
import org.annpath.location.GeoUtils;
import org.annpath.model.FoodListing;
import org.annpath.model.Pickup;
import org.annpath.model.PickupAssignmentHistory;
import org.annpath.model.VolunteerProfile;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VolunteerAssignmentEngine {

    private static final Logger LOGGER = Logger.getLogger(VolunteerAssignmentEngine.class.getName());
    private final PickupDao pickupDao = new PickupDao();
    private final VolunteerDao volunteerDao = new VolunteerDao();
    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final NotificationService notificationService = new NotificationService();

    public boolean broadcastPickupToAllVolunteers(Long pickupId) throws SQLException {
        Pickup pickup = pickupDao.findById(pickupId);
        if (pickup == null) return false;

        FoodListing listing = foodListingDao.findById(pickup.getFoodListingId());
        if (listing == null) return false;

        List<VolunteerProfile> allVolunteers = volunteerDao.findAllAvailableVolunteers();
        Set<Long> pastCandidates = pickupDao.findPastAssignedVolunteerIds(pickupId);
        List<VolunteerProfile> eligibleVolunteers = allVolunteers.stream()
                .filter(v -> pastCandidates == null || !pastCandidates.contains(v.getUserId()))
                .collect(Collectors.toList());

        if (eligibleVolunteers.isEmpty()) {
            LOGGER.warning("No eligible unattempted volunteers found in the system for Pickup #" + pickupId);
            return false;
        }

        for (VolunteerProfile v : eligibleVolunteers) {
            notificationService.notifyUser(
                    v.getUserId(),
                    "NEW_ASSIGNMENT",
                    "New Food Rescue Task Available!",
                    "New pickup available from " + listing.getPickupAddress() + " (" + pickup.getFoodTitle() + "). Tap to accept and claim this mission!",
                    "PICKUP",
                    pickupId
            );
        }

        LOGGER.info("Pickup #" + pickupId + " successfully broadcast to " + eligibleVolunteers.size() + " volunteers.");
        return true;
    }

    /**
     * Dispatch the pickup by broadcasting to all available volunteers.
     */
    public boolean assignBestVolunteer(Long pickupId, NoVolunteerFallbackService fallbackService) throws SQLException {
        return broadcastPickupToAllVolunteers(pickupId);
    }

    public boolean assignBestVolunteerLegacy(Long pickupId, NoVolunteerFallbackService fallbackService) throws SQLException {
        Pickup pickup = pickupDao.findById(pickupId);
        if (pickup == null) return false;

        FoodListing listing = foodListingDao.findById(pickup.getFoodListingId());
        if (listing == null) return false;

        boolean hasPickupCoords = listing.getPickupLatitude() != null && listing.getPickupLongitude() != null;
        List<PickupAssignmentHistory> history = pickupDao.getAssignmentHistory(pickupId);
        Set<Long> pastCandidates = history.stream().map(PickupAssignmentHistory::getVolunteerUserId).collect(Collectors.toSet());
        List<VolunteerProfile> eligible;

        if (hasPickupCoords) {
            double pickupLat = listing.getPickupLatitude().doubleValue();
            double pickupLng = listing.getPickupLongitude().doubleValue();
            double maxRadiusKm = AppConfig.getVolunteerMaxSearchRadiusKm();

            double[] bbox = GeoUtils.calculateBoundingBox(pickupLat, pickupLng, maxRadiusKm);
            List<VolunteerProfile> candidates = volunteerDao.findAvailableVolunteersInBoundingBox(
                    bbox[0], bbox[1], bbox[2], bbox[3]
            );

            eligible = candidates.stream()
                    .filter(v -> !pastCandidates.contains(v.getUserId()))
                    .filter(v -> {
                        if (v.getLatitude() == null || v.getLongitude() == null) return false;
                        double dist = GeoUtils.calculateDistanceKm(
                                pickupLat, pickupLng,
                                v.getLatitude().doubleValue(), v.getLongitude().doubleValue()
                        );
                        double maxTravel = v.getMaxTravelDistanceKm() != null ? v.getMaxTravelDistanceKm().doubleValue() : 10.0;
                        return dist <= maxTravel;
                    })
                    .sorted(Comparator.comparingInt(VolunteerProfile::getActiveAssignmentsCount)
                            .thenComparingDouble(v -> GeoUtils.calculateDistanceKm(
                                    pickupLat, pickupLng,
                                    v.getLatitude().doubleValue(), v.getLongitude().doubleValue()
                            )))
                    .collect(Collectors.toList());

            if (eligible.isEmpty()) {
                // Fallback: Check all available volunteers across regions
                List<VolunteerProfile> allVolunteers = volunteerDao.findAllAvailableVolunteers();
                eligible = allVolunteers.stream()
                        .filter(v -> !pastCandidates.contains(v.getUserId()))
                        .sorted(Comparator.comparingInt(VolunteerProfile::getActiveAssignmentsCount)
                                .thenComparingDouble(v -> (v.getLatitude() != null && v.getLongitude() != null)
                                        ? GeoUtils.calculateDistanceKm(pickupLat, pickupLng, v.getLatitude().doubleValue(), v.getLongitude().doubleValue())
                                        : 9999.0))
                        .collect(Collectors.toList());
            }
        } else {
            // If listing has no GPS coordinates, sort by active workload
            List<VolunteerProfile> allVolunteers = volunteerDao.findAllAvailableVolunteers();
            eligible = allVolunteers.stream()
                    .filter(v -> !pastCandidates.contains(v.getUserId()))
                    .sorted(Comparator.comparingInt(VolunteerProfile::getActiveAssignmentsCount))
                    .collect(Collectors.toList());
        }

        if (eligible.isEmpty()) {
            LOGGER.warning("No eligible volunteer found for Pickup #" + pickupId + ". Triggering fallback.");
            fallbackService.triggerVolunteerUnavailable(pickupId);
            return false;
        }

        VolunteerProfile chosen = eligible.get(0);

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            pickupDao.assignVolunteer(pickupId, chosen.getUserId(), conn);
            pickupDao.logAssignmentAttempt(pickupId, chosen.getUserId(), conn);
            volunteerDao.adjustActiveAssignments(chosen.getUserId(), 1, conn);
            conn.commit();

            notificationService.notifyUser(
                    chosen.getUserId(),
                    "NEW_ASSIGNMENT",
                    "New Food Rescue Task Available!",
                    "You have been assigned to pick up food from " + listing.getPickupAddress() + " (" + pickup.getFoodTitle() + "). Please accept or reject within 15 minutes.",
                    "PICKUP",
                    pickupId
            );

            LOGGER.info("Pickup #" + pickupId + " assigned to Volunteer #" + chosen.getUserId());
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Handles volunteer rejection or timeout, delegating to the Failure Recovery Engine.
     */
    public void handleVolunteerRejection(Long pickupId, Long volunteerUserId, String reason, NoVolunteerFallbackService fallbackService) throws SQLException {
        FailureRecoveryEngine recoveryEngine = new FailureRecoveryEngine();
        recoveryEngine.handleVolunteerRejection(pickupId, volunteerUserId, reason);
    }
}
