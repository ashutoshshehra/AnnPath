package org.annpath.service;

import org.annpath.config.Constants;
import org.annpath.dao.ConnectionPool;
import org.annpath.dao.PickupDao;
import org.annpath.exception.ValidationException;
import org.annpath.model.Pickup;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class NoVolunteerFallbackService {

    private static final Logger LOGGER = Logger.getLogger(NoVolunteerFallbackService.class.getName());
    private final PickupDao pickupDao = new PickupDao();
    private final NotificationService notificationService = new NotificationService();

    /**
     * Trigger VOLUNTEER_UNAVAILABLE status when all volunteer attempts are exhausted.
     */
    public void triggerVolunteerUnavailable(Long pickupId) throws SQLException {
        Pickup pickup = pickupDao.findById(pickupId);
        if (pickup == null) return;

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            pickupDao.updateStatus(pickupId, Constants.PICKUP_STATUS_VOLUNTEER_UNAVAILABLE, conn);
            conn.commit();

            notificationService.notifyUser(
                    pickup.getNgoUserId(),
                    "VOLUNTEER_UNAVAILABLE",
                    "No Volunteers Currently Available for Pickup",
                    "We could not find an available volunteer nearby for food rescue (" + pickup.getFoodTitle() + "). You may arrange Self-Pickup or continue waiting.",
                    "PICKUP",
                    pickupId
            );

            LOGGER.info("Pickup #" + pickupId + " transitioned to VOLUNTEER_UNAVAILABLE. NGO notified.");
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * NGO elects to arrange Self-Pickup.
     */
    public void configureSelfPickup(Long pickupId, Long ngoUserId, String personName, String phone,
                                   String idType, String idNumber) throws ValidationException, SQLException {
        if (personName == null || personName.trim().isEmpty()) {
            throw new ValidationException("Authorized pickup person name is required.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Authorized pickup person contact number is required.");
        }

        Pickup pickup = pickupDao.findById(pickupId);
        if (pickup == null || !pickup.getNgoUserId().equals(ngoUserId)) {
            throw new ValidationException("Pickup task not found or unauthorized.");
        }

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            pickupDao.assignSelfPickup(pickupId, personName.trim(), phone.trim(), idType, idNumber, conn);
            conn.commit();

            notificationService.notifyUser(
                    pickup.getDonorUserId(),
                    "SELF_PICKUP_ASSIGNED",
                    "NGO Will Perform Self-Pickup",
                    "The NGO representative " + personName + " (" + phone + ") will pick up the food rescue (" + pickup.getFoodTitle() + ") directly.",
                    "PICKUP",
                    pickupId
            );

            LOGGER.info("Pickup #" + pickupId + " switched to NGO_SELF_PICKUP by person: " + personName);
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }
}
