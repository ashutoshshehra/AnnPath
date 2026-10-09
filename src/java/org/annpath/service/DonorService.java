package org.annpath.service;

import org.annpath.config.Constants;
import org.annpath.dao.ConnectionPool;
import org.annpath.dao.FoodListingDao;
import org.annpath.dao.FoodRequestDao;
import org.annpath.exception.ConcurrencyException;
import org.annpath.exception.InvalidStateTransitionException;
import org.annpath.exception.ValidationException;
import org.annpath.model.FoodListing;
import org.annpath.model.FoodRequest;
import org.annpath.model.Pickup;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class DonorService {

    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final FoodRequestDao foodRequestDao = new FoodRequestDao();
    private final UrgencyEngine urgencyEngine = new UrgencyEngine();
    private final PickupDeliveryService pickupDeliveryService = new PickupDeliveryService();
    private final VolunteerAssignmentEngine volunteerEngine = new VolunteerAssignmentEngine();
    private final NoVolunteerFallbackService fallbackService = new NoVolunteerFallbackService();
    private final NotificationService notificationService = new NotificationService();
    private final RescueJourneyService journeyService = new RescueJourneyService();

    /**
     * Create a new food surplus listing with validation and urgency scoring.
     */
    public Long createFoodDonation(FoodListing listing) throws ValidationException, SQLException {
        if (listing.getTitle() == null || listing.getTitle().trim().isEmpty()) {
            throw new ValidationException("Food title is required.");
        }
        if (listing.getTotalQuantity() == null || listing.getTotalQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Quantity must be greater than zero.");
        }
        if (listing.getAvailableUntil() == null) {
            throw new ValidationException("Available-until expiry date and time is required.");
        }
        if (listing.getAvailableUntil().before(new Timestamp(System.currentTimeMillis()))) {
            throw new ValidationException("Available-until time cannot be in the past.");
        }

        listing.setRemainingQuantity(listing.getTotalQuantity());
        listing.setReservedQuantity(BigDecimal.ZERO);
        listing.setListingStatus(Constants.STATE_DONATED);

        String urgency = urgencyEngine.calculateUrgency(listing.getAvailableUntil(), listing.getAvailableFrom());
        listing.setUrgencyStatus(urgency);

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            Long id = foodListingDao.createListing(listing, conn);
            listing.setId(id);

            try {
                journeyService.recordTransition(id, null, "NONE", Constants.STATE_DONATED,
                        listing.getDonorUserId(), Constants.ROLE_DONOR, "Food surplus listed for donation", null, conn);
            } catch (Exception ignored) {}

            conn.commit();

            // Evaluate explainable priority score
            urgencyEngine.evaluateListingUrgency(listing);

            return id;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Approve a food request with atomic row-level database locking and quantity deduction.
     */
    public Pickup approveFoodRequest(Long requestId, Long donorUserId, String responseNotes)
            throws ValidationException, ConcurrencyException, InvalidStateTransitionException, SQLException {
        FoodRequest req = foodRequestDao.findById(requestId);
        if (req == null) throw new ValidationException("Request not found.");

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);

            // Row-level lock on listing to prevent race conditions on inventory
            FoodListing listing = foodListingDao.findByIdForUpdate(req.getFoodListingId(), conn);
            if (listing == null) throw new ValidationException("Listing not found.");

            if (!listing.getDonorUserId().equals(donorUserId)) {
                throw new ValidationException("Unauthorized to approve this request.");
            }

            if (!req.getStatus().equalsIgnoreCase(Constants.STATE_REQUESTED)) {
                throw new InvalidStateTransitionException(req.getStatus(), Constants.STATE_APPROVED, "Request has already been processed.");
            }

            if ("EXPIRED".equalsIgnoreCase(listing.getUrgencyStatus()) || listing.getAvailableUntil().before(new Timestamp(System.currentTimeMillis()))) {
                throw new ValidationException("Cannot approve request: food donation has expired.");
            }

            if (listing.getRemainingQuantity().compareTo(req.getRequestedQuantity()) < 0) {
                throw new ValidationException("Requested quantity (" + req.getRequestedQuantity() +
                        ") exceeds available remaining quantity (" + listing.getRemainingQuantity() + ").");
            }

            // Atomic quantity reservation with optimistic version check
            boolean reserved = foodListingDao.reserveQuantity(listing.getId(), req.getRequestedQuantity(), listing.getVersion(), conn);
            if (!reserved) {
                throw new ConcurrencyException("Donation inventory was modified concurrently by another transaction. Please refresh and retry.");
            }

            // Update request status to APPROVED
            foodRequestDao.updateStatus(requestId, Constants.STATE_APPROVED, responseNotes, conn);

            // Create Pickup record
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Timestamp windowEnd = listing.getAvailableUntil();
            Pickup pickup = pickupDeliveryService.createPickup(
                    listing.getId(), req.getId(), donorUserId, req.getNgoUserId(), now, windowEnd, conn
            );

            // Update listing status if fully reserved
            BigDecimal updatedRemaining = listing.getRemainingQuantity().subtract(req.getRequestedQuantity());
            if (updatedRemaining.compareTo(BigDecimal.ZERO) == 0) {
                foodListingDao.updateStatus(listing.getId(), Constants.STATE_APPROVED, conn);
            }

            try {
                journeyService.recordTransition(listing.getId(), pickup.getId(), listing.getListingStatus(),
                        Constants.STATE_APPROVED, donorUserId, Constants.ROLE_DONOR, "Donor approved food request",
                        "{\"requestId\":" + requestId + ",\"requestedQty\":" + req.getRequestedQuantity() + "}", conn);
            } catch (Exception ignored) {}

            conn.commit();

            // Notify NGO with Delivery confirmation notification
            notificationService.notifyUser(
                    req.getNgoUserId(),
                    "REQUEST_APPROVED",
                    "Food Request Approved!",
                    "Donor approved your request for " + req.getRequestedQuantity() + " " + req.getUnitName() + " of " + listing.getTitle() + 
                    ". A secure delivery verification code has been generated. Share this code with the volunteer upon arrival to verify delivery handover.",
                    "PICKUP",
                    pickup.getId()
            );

            // Notify Donor with Pickup verification notification
            notificationService.notifyUser(
                    donorUserId,
                    "PICKUP_SCHEDULED",
                    "Food Rescue Pickup Dispatched",
                    "A rescue pickup has been scheduled for " + listing.getTitle() + 
                    ". A secure pickup verification handover code has been generated. Provide this code to the assigned volunteer upon physical handover to transfer custody.",
                    "PICKUP",
                    pickup.getId()
            );

            // Broadcast pickup request to all available volunteers
            volunteerEngine.broadcastPickupToAllVolunteers(pickup.getId());

            return pickup;
        } catch (SQLException | ValidationException | ConcurrencyException e) {
            conn.rollback();
            throw e;
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Reject a food request.
     */
    public void rejectFoodRequest(Long requestId, Long donorUserId, String rejectionReason) throws ValidationException, SQLException {
        FoodRequest req = foodRequestDao.findById(requestId);
        if (req == null) throw new ValidationException("Request not found.");

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            FoodListing listing = foodListingDao.findById(req.getFoodListingId());
            if (listing == null || !listing.getDonorUserId().equals(donorUserId)) {
                throw new ValidationException("Unauthorized.");
            }

            foodRequestDao.updateStatus(requestId, Constants.STATE_REJECTED, rejectionReason, conn);
            conn.commit();

            notificationService.notifyUser(
                    req.getNgoUserId(),
                    "REQUEST_REJECTED",
                    "Food Request Update",
                    "Your request for " + listing.getTitle() + " was not accepted: " + rejectionReason,
                    "FOOD_REQUEST",
                    requestId
            );
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public List<FoodListing> getDonorDonations(Long donorUserId) throws SQLException {
        return foodListingDao.findByDonor(donorUserId);
    }

    public List<FoodRequest> getPendingRequests(Long donorUserId) throws SQLException {
        return foodRequestDao.findPendingByDonor(donorUserId);
    }
}
