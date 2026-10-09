package org.annpath.service;

import org.annpath.config.Constants;
import org.annpath.dao.ConnectionPool;
import org.annpath.dao.PickupDao;
import org.annpath.dao.VolunteerDao;
import org.annpath.exception.InvalidStateTransitionException;
import org.annpath.exception.ValidationException;
import org.annpath.model.Pickup;
import org.annpath.model.VolunteerProfile;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class VolunteerService {

    private final VolunteerDao volunteerDao = new VolunteerDao();
    private final PickupDao pickupDao = new PickupDao();
    private final VolunteerAssignmentEngine assignmentEngine = new VolunteerAssignmentEngine();
    private final NoVolunteerFallbackService fallbackService = new NoVolunteerFallbackService();
    private final PickupDeliveryService pickupDeliveryService = new PickupDeliveryService();
    private final NotificationService notificationService = new NotificationService();
    private final RescueJourneyService journeyService = new RescueJourneyService();
    private final FailureRecoveryEngine failureRecoveryEngine = new FailureRecoveryEngine();

    public void updateAvailability(Long volunteerUserId, String status) throws SQLException {
        volunteerDao.updateAvailability(volunteerUserId, status);
    }

    public List<Pickup> getVolunteerTasks(Long volunteerUserId) throws SQLException {
        return pickupDao.findByVolunteer(volunteerUserId);
    }

    public List<Pickup> getOpenPickups(Long volunteerUserId) throws SQLException {
        return pickupDao.findOpenPickupsForVolunteers(volunteerUserId);
    }

    public void claimPickup(Long pickupId, Long volunteerUserId) throws ValidationException, SQLException {
        Pickup p = pickupDao.findById(pickupId);
        if (p == null) throw new ValidationException("Pickup task not found.");
        if (!"ASSIGNED".equals(p.getStatus()) && !"VOLUNTEER_UNAVAILABLE".equals(p.getStatus()) && !Constants.PICKUP_STATUS_REASSIGNING.equals(p.getStatus())) {
            throw new ValidationException("This mission is no longer available for claiming.");
        }

        java.util.Set<Long> pastCandidates = pickupDao.findPastAssignedVolunteerIds(pickupId);
        if (pastCandidates != null && pastCandidates.contains(volunteerUserId)) {
            throw new ValidationException("You have previously declined this rescue mission.");
        }

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            boolean claimed = pickupDao.claimBroadcastPickup(pickupId, volunteerUserId, conn);
            if (!claimed) {
                conn.rollback();
                throw new ValidationException("This mission was just claimed by another volunteer.");
            }
            pickupDao.logAssignmentAttempt(pickupId, volunteerUserId, conn);
            pickupDao.recordAssignmentResponse(pickupId, volunteerUserId, Constants.VOL_ASSIGN_ACCEPTED, null, conn);
            volunteerDao.adjustActiveAssignments(volunteerUserId, 1, conn);

            try {
                journeyService.recordTransition(p.getFoodListingId(), pickupId, p.getStatus(),
                        Constants.STATE_VOLUNTEER_ASSIGNED, volunteerUserId, Constants.ROLE_VOLUNTEER, "Volunteer claimed rescue mission", null, conn);
            } catch (Exception ignored) {}

            conn.commit();

            notificationService.notifyUser(
                    p.getDonorUserId(),
                    "VOLUNTEER_CLAIMED",
                    "Volunteer Claimed Rescue Mission",
                    "A volunteer has claimed the pickup for " + p.getFoodTitle() + " and is proceeding to your location.",
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

    public void acceptAssignment(Long pickupId, Long volunteerUserId) throws ValidationException, SQLException {
        Pickup p = pickupDao.findById(pickupId);
        if (p == null) {
            throw new ValidationException("Pickup task not found.");
        }
        if (p.getVolunteerUserId() == null) {
            claimPickup(pickupId, volunteerUserId);
            return;
        }
        if (!volunteerUserId.equals(p.getVolunteerUserId())) {
            throw new ValidationException("This delivery mission has already been claimed by another volunteer.");
        }

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            pickupDao.recordAssignmentResponse(pickupId, volunteerUserId, Constants.VOL_ASSIGN_ACCEPTED, null, conn);
            conn.commit();

            notificationService.notifyUser(
                    p.getDonorUserId(),
                    "VOLUNTEER_ACCEPTED",
                    "Volunteer Assigned for Pickup",
                    "A volunteer has accepted the pickup for " + p.getFoodTitle() + " and will arrive shortly.",
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

    public void rejectAssignment(Long pickupId, Long volunteerUserId, String reason) throws ValidationException, SQLException {
        Pickup p = pickupDao.findById(pickupId);
        if (p != null && volunteerUserId != null && volunteerUserId.equals(p.getVolunteerUserId())) {
            failureRecoveryEngine.handleVolunteerRejection(pickupId, volunteerUserId, reason);
        }
    }

    public void confirmPickupWithOtp(Long pickupId, Long volunteerUserId, String otp)
            throws ValidationException, InvalidStateTransitionException, SQLException {
        pickupDeliveryService.confirmPickup(pickupId, volunteerUserId, otp);
    }

    public void confirmDeliveryWithOtp(Long pickupId, Long volunteerUserId, String otp)
            throws ValidationException, InvalidStateTransitionException, SQLException {
        pickupDeliveryService.confirmDelivery(pickupId, volunteerUserId, otp);
    }
}
