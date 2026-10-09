package org.annpath.service;

import org.annpath.config.Constants;
import org.annpath.dao.ConnectionPool;
import org.annpath.dao.FoodListingDao;
import org.annpath.dao.FoodRequestDao;
import org.annpath.dao.NgoDao;
import org.annpath.dao.NgoRequirementDao;
import org.annpath.dao.PickupDao;
import org.annpath.exception.UnauthorizedException;
import org.annpath.exception.ValidationException;
import org.annpath.model.FoodListing;
import org.annpath.model.FoodRequest;
import org.annpath.model.NgoProfile;
import org.annpath.model.NgoRequirement;
import org.annpath.model.Pickup;
import org.annpath.model.VerificationDocument;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class NgoService {

    private final NgoDao ngoDao = new NgoDao();
    private final NgoRequirementDao requirementDao = new NgoRequirementDao();
    private final FoodRequestDao foodRequestDao = new FoodRequestDao();
    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final PickupDao pickupDao = new PickupDao();
    private final VerificationService verificationService = new VerificationService();
    private final NotificationService notificationService = new NotificationService();

    public void updateOrganizationProfile(NgoProfile profile) throws ValidationException, SQLException {
        if (profile.getOrganizationName() == null || profile.getOrganizationName().trim().isEmpty()) {
            throw new ValidationException("Organization name is required.");
        }
        if (profile.getRegistrationNumber() == null || profile.getRegistrationNumber().trim().isEmpty()) {
            throw new ValidationException("Registration number is required.");
        }

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            ngoDao.save(profile, conn);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public void submitDocument(VerificationDocument doc) throws SQLException {
        ngoDao.saveVerificationDocument(doc);
        // Automatically evaluate verification rules
        verificationService.evaluateNgoVerification(doc.getNgoUserId());
    }

    public Long addRequirement(NgoRequirement req) throws ValidationException, SQLException {
        if (req.getMinQuantity() == null || req.getMinQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Minimum quantity must be greater than zero.");
        }
        if (req.getMaxQuantity() == null || req.getMaxQuantity().compareTo(req.getMinQuantity()) < 0) {
            throw new ValidationException("Maximum quantity must be greater than or equal to minimum quantity.");
        }
        return requirementDao.create(req);
    }

    /**
     * Request food surplus from an active listing.
     * Enforces verification check, quantity check, and expiration check.
     */
    public Long requestFood(Long listingId, Long ngoUserId, BigDecimal requestedQuantity, String notes)
            throws UnauthorizedException, ValidationException, SQLException {
        NgoProfile ngo = ngoDao.findByUserId(ngoUserId);
        if (ngo == null || !ngo.isVerified()) {
            throw new UnauthorizedException("Only Verified NGOs may request food surplus. Please submit organization verification documents.");
        }

        FoodListing listing = foodListingDao.findById(listingId);
        if (listing == null) {
            throw new ValidationException("Food listing not found.");
        }

        if ("EXPIRED".equalsIgnoreCase(listing.getUrgencyStatus()) || listing.getAvailableUntil().before(new Timestamp(System.currentTimeMillis()))) {
            throw new ValidationException("This food listing has expired and cannot be requested.");
        }

        if (requestedQuantity == null || requestedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Requested quantity must be greater than zero.");
        }

        if (requestedQuantity.compareTo(listing.getRemainingQuantity()) > 0) {
            throw new ValidationException("Requested quantity (" + requestedQuantity +
                    ") exceeds remaining available quantity (" + listing.getRemainingQuantity() + ").");
        }

        FoodRequest req = new FoodRequest();
        req.setFoodListingId(listingId);
        req.setNgoUserId(ngoUserId);
        req.setRequestedQuantity(requestedQuantity);
        req.setUnitId(listing.getUnitId());
        req.setRequestNotes(notes);
        req.setStatus(Constants.STATE_REQUESTED);

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            Long id = foodRequestDao.create(req, conn);
            conn.commit();

            // Notify Donor
            notificationService.notifyUser(
                    listing.getDonorUserId(),
                    "NEW_FOOD_REQUEST",
                    "New Food Rescue Request!",
                    "NGO " + ngo.getOrganizationName() + " has requested " + requestedQuantity + " " + listing.getUnitName() + " of " + listing.getTitle() + ".",
                    "FOOD_REQUEST",
                    id
            );

            return id;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public List<FoodRequest> getNgoRequests(Long ngoUserId) throws SQLException {
        return foodRequestDao.findByNgo(ngoUserId);
    }

    public List<Pickup> getActiveDeliveries(Long ngoUserId) throws SQLException {
        return pickupDao.findByNgo(ngoUserId);
    }

    public List<NgoRequirement> getRequirements(Long ngoUserId) throws SQLException {
        return requirementDao.findByNgoId(ngoUserId);
    }
}
