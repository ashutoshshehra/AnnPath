package org.annpath.service;

import org.annpath.config.Constants;
import org.annpath.dao.ConnectionPool;
import org.annpath.dao.FoodListingDao;
import org.annpath.dao.PickupDao;
import org.annpath.dao.RescueBottleneckDao;
import org.annpath.dao.VolunteerDao;
import org.annpath.exception.InvalidStateTransitionException;
import org.annpath.exception.ValidationException;
import org.annpath.model.FoodListing;
import org.annpath.model.Pickup;
import org.annpath.model.RescueBottleneck;
import org.annpath.model.User;
import org.annpath.model.VolunteerProfile;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Rescue Autopilot Engine for AnnPath.
 * Autonomous coordination engine that evaluates food lifecycle state, volunteer bandwidth,
 * NGO availability, and risk metrics to recommend or trigger the next optimal operational action.
 */
public class RescueAutopilotEngine {

    private static final Logger LOGGER = Logger.getLogger(RescueAutopilotEngine.class.getName());

    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final PickupDao pickupDao = new PickupDao();
    private final RescueBottleneckDao bottleneckDao = new RescueBottleneckDao();
    private final VolunteerDao volunteerDao = new VolunteerDao();
    private final UrgencyEngine urgencyEngine = new UrgencyEngine();
    private final RescueJourneyService journeyService = new RescueJourneyService();
    private final VolunteerAssignmentEngine assignmentEngine = new VolunteerAssignmentEngine();
    private final NoVolunteerFallbackService fallbackService = new NoVolunteerFallbackService();

    /**
     * Data Transfer Object containing Autopilot's calculated state, risk assessment, and recommended next action.
     */
    public static class AutopilotDecision implements Serializable {
        private static final long serialVersionUID = 1L;

        private String nextAction;         // FIND_NGO, WAITING_FOR_DONOR_APPROVAL, ASSIGN_VOLUNTEER, START_PICKUP, DELIVER_FOOD, CONFIRM_RECEIPT, NGO_SELF_PICKUP, ESCALATE_CRITICAL, COMPLETED
        private String actionLabel;        // Human-friendly title
        private String currentStatus;
        private int riskScore;             // 0 - 100
        private String riskLevel;          // LOW, MEDIUM, HIGH, CRITICAL
        private String rationale;          // Detailed explanation of decision
        private Long recommendedNgoId;
        private String recommendedNgoName;
        private Long recommendedVolunteerId;
        private String recommendedVolunteerName;
        private List<String> actionItems = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public AutopilotDecision() {}

        public String getNextAction() { return nextAction; }
        public void setNextAction(String nextAction) { this.nextAction = nextAction; }

        public String getActionLabel() { return actionLabel; }
        public void setActionLabel(String actionLabel) { this.actionLabel = actionLabel; }

        public String getCurrentStatus() { return currentStatus; }
        public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = Math.max(0, Math.min(100, riskScore)); }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public String getRationale() { return rationale; }
        public void setRationale(String rationale) { this.rationale = rationale; }

        public Long getRecommendedNgoId() { return recommendedNgoId; }
        public void setRecommendedNgoId(Long recommendedNgoId) { this.recommendedNgoId = recommendedNgoId; }

        public String getRecommendedNgoName() { return recommendedNgoName; }
        public void setRecommendedNgoName(String recommendedNgoName) { this.recommendedNgoName = recommendedNgoName; }

        public Long getRecommendedVolunteerId() { return recommendedVolunteerId; }
        public void setRecommendedVolunteerId(Long recommendedVolunteerId) { this.recommendedVolunteerId = recommendedVolunteerId; }

        public String getRecommendedVolunteerName() { return recommendedVolunteerName; }
        public void setRecommendedVolunteerName(String recommendedVolunteerName) { this.recommendedVolunteerName = recommendedVolunteerName; }

        public List<String> getActionItems() { return actionItems; }
        public void setActionItems(List<String> actionItems) { this.actionItems = actionItems; }
        public void addActionItem(String item) { this.actionItems.add(item); }

        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        public void addWarning(String warning) { this.warnings.add(warning); }
    }

    /**
     * Fully evaluate a rescue mission and generate autonomous next-step recommendation.
     */
    public AutopilotDecision evaluateRescue(Long listingId, Long pickupId) {
        AutopilotDecision decision = new AutopilotDecision();

        try {
            FoodListing listing = null;
            Pickup pickup = null;

            if (pickupId != null) {
                pickup = pickupDao.findById(pickupId);
                if (pickup != null && listingId == null) {
                    listingId = pickup.getFoodListingId();
                }
            }

            if (listingId != null) {
                listing = foodListingDao.findById(listingId);
            }

            if (listing == null && pickup == null) {
                decision.setNextAction("UNKNOWN");
                decision.setActionLabel("No Mission Found");
                decision.setRationale("Unable to locate food listing or pickup records.");
                decision.setRiskLevel("LOW");
                return decision;
            }

            // Determine status
            String status = (pickup != null) ? pickup.getStatus() : listing.getListingStatus();
            decision.setCurrentStatus(status);

            // 1. Calculate Operational Risk Score (0 - 100)
            int risk = 10;
            String urgency = (listing != null && listing.getUrgencyStatus() != null) ? listing.getUrgencyStatus() : Constants.URGENCY_FRESH;

            if (Constants.URGENCY_CRITICAL.equalsIgnoreCase(urgency)) {
                risk += 45;
                decision.addWarning("Critical: Under 2 hours of shelf-life remaining before expiration.");
            } else if (Constants.URGENCY_EXPIRING_SOON.equalsIgnoreCase(urgency)) {
                risk += 25;
                decision.addWarning("Shelf-life window expiring soon (under 6 hours).");
            } else if (Constants.URGENCY_EXPIRED.equalsIgnoreCase(urgency)) {
                risk += 60;
                decision.addWarning("Surplus food is past safe distribution window.");
            }

            // Storage condition sensitivity
            if (listing != null && listing.getStorageCondition() != null) {
                String sc = listing.getStorageCondition().toUpperCase();
                if ("WARM_HEATED".equals(sc) || "REFRIGERATED".equals(sc) || "FROZEN".equals(sc)) {
                    risk += 15;
                }
            }

            // Check for active bottlenecks
            boolean hasBottleneck = false;
            if (listing != null) {
                hasBottleneck = bottleneckDao.existsActive("CRITICAL_RESCUE_UNASSIGNED", listing.getId(), null) ||
                                bottleneckDao.existsActive("REQUEST_DELAY", listing.getId(), null);
            }
            if (pickup != null) {
                hasBottleneck = hasBottleneck ||
                                bottleneckDao.existsActive("VOLUNTEER_SHORTAGE", null, pickup.getId()) ||
                                bottleneckDao.existsActive("PICKUP_DELAY", null, pickup.getId()) ||
                                bottleneckDao.existsActive("DELIVERY_DELAY", null, pickup.getId());
            }
            if (hasBottleneck) {
                risk += 20;
                decision.addWarning("Operational bottleneck flagged in live rescue pipeline.");
            }

            decision.setRiskScore(risk);
            if (risk >= 75) decision.setRiskLevel("CRITICAL");
            else if (risk >= 50) decision.setRiskLevel("HIGH");
            else if (risk >= 25) decision.setRiskLevel("MEDIUM");
            else decision.setRiskLevel("LOW");

            // 2. Determine State-Driven Autonomous Decision
            evaluateNextAction(decision, status, listing, pickup);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error in Rescue Autopilot evaluation: " + e.getMessage(), e);
            decision.setNextAction("ERROR");
            decision.setActionLabel("Evaluation Error");
            decision.setRationale("Database connectivity issue during autopilot calculation.");
            decision.setRiskLevel("HIGH");
        }

        return decision;
    }

    private void evaluateNextAction(AutopilotDecision decision, String status, FoodListing listing, Pickup pickup) throws SQLException {
        if (status == null) status = Constants.STATE_DONATED;

        switch (status) {
            case Constants.STATE_DONATED:
                decision.setNextAction("FIND_NGO");
                decision.setActionLabel("Match Verified NGO");
                decision.setRationale("Surplus published. Autopilot is scanning verified NGOs with matching food requirements.");
                decision.addActionItem("Notify matched NGOs within service radius");
                decision.addActionItem("Check category and dietary affinity");
                break;

            case Constants.STATE_MATCHED:
                decision.setNextAction("WAITING_FOR_NGO_REQUEST");
                decision.setActionLabel("Awaiting NGO Request");
                decision.setRationale("Compatible NGOs notified. Awaiting official request submission.");
                decision.addActionItem("Follow up with candidate NGOs if critical window approaches");
                break;

            case Constants.STATE_REQUESTED:
                decision.setNextAction("WAITING_FOR_DONOR_APPROVAL");
                decision.setActionLabel("Awaiting Donor Approval");
                decision.setRationale("NGO requested surplus. Waiting for donor confirmation to finalize pickup schedule.");
                decision.addActionItem("Donor must review and accept or reject request");
                break;

            case Constants.STATE_APPROVED:
            case Constants.PICKUP_STATUS_ASSIGNED:
                if (pickup != null && pickup.getVolunteerUserId() != null) {
                    decision.setNextAction("WAITING_FOR_VOLUNTEER");
                    decision.setActionLabel("Waiting for Volunteer Acceptance");
                    decision.setRationale("Volunteer assigned. Awaiting confirmation from volunteer.");
                    decision.addActionItem("Volunteer acceptance required within 15 minutes");
                } else {
                    decision.setNextAction("ASSIGN_VOLUNTEER");
                    decision.setActionLabel("Dispatch Volunteer");
                    decision.setRationale("Rescue confirmed. Autopilot recommends broadcasting mission to available local volunteers.");
                    decision.addActionItem("Broadcast rescue alert to on-duty volunteers");
                    decision.addActionItem("Activate fallback to NGO Self-Pickup if no volunteer responds");
                }
                break;

            case Constants.STATE_VOLUNTEER_ASSIGNED:
                decision.setNextAction("START_PICKUP");
                decision.setActionLabel("Initiate Pickup");
                decision.setRationale("Volunteer accepted mission. Volunteer should travel to donor premises.");
                decision.addActionItem("Navigate to pickup location");
                decision.addActionItem("Verify food packaging and exchange Pickup OTP");
                if (pickup != null && pickup.getVolunteerUserId() != null) {
                    decision.setRecommendedVolunteerId(pickup.getVolunteerUserId());
                    decision.setRecommendedVolunteerName(pickup.getVolunteerName());
                }
                break;

            case Constants.STATE_VOLUNTEER_UNAVAILABLE:
                decision.setNextAction("NGO_SELF_PICKUP");
                decision.setActionLabel("Arrange NGO Self-Pickup");
                decision.setRationale("No volunteers currently reachable. Autopilot recommends NGO arrange direct self-pickup.");
                decision.addActionItem("NGO assigns internal staff/vehicle for direct collection");
                decision.addActionItem("Alternatively, extend volunteer search radius");
                break;

            case Constants.STATE_NGO_SELF_PICKUP_ASSIGNED:
                decision.setNextAction("START_PICKUP");
                decision.setActionLabel("NGO Self-Pickup In Progress");
                decision.setRationale("NGO designated pickup representative. Authorized person should collect surplus using Pickup OTP.");
                decision.addActionItem("Proceed to donor premises");
                decision.addActionItem("Exchange Pickup OTP with donor");
                break;

            case Constants.STATE_PICKED_UP:
                decision.setNextAction("DELIVER_FOOD");
                decision.setActionLabel("Deliver Food to NGO");
                decision.setRationale("Food surplus secured and in transit. Safely transport to NGO facility.");
                decision.addActionItem("Deliver directly to NGO recipient location");
                decision.addActionItem("Provide Delivery OTP to NGO coordinator upon arrival");
                break;

            case Constants.STATE_DELIVERED:
                decision.setNextAction("CONFIRM_RECEIPT");
                decision.setActionLabel("Confirm NGO Receipt");
                decision.setRationale("Food arrived at destination. NGO must verify contents and complete confirmation.");
                decision.addActionItem("Inspect temperature and food quality");
                decision.addActionItem("Verify Delivery OTP and issue Digital Rescue Receipt");
                break;

            case Constants.STATE_RECEIVED:
                decision.setNextAction("DISTRIBUTE_FOOD");
                decision.setActionLabel("Community Distribution");
                decision.setRationale("Food verified and stored at NGO facility. Proceed to distribute meals to beneficiaries.");
                decision.addActionItem("Distribute meals to targeted beneficiaries");
                decision.addActionItem("Record final distribution timestamp to complete Rescue Passport");
                break;

            case Constants.STATE_DISTRIBUTED:
                decision.setNextAction("COMPLETED");
                decision.setActionLabel("Mission Accomplished");
                decision.setRationale("Food successfully reached beneficiaries. Full rescue lifecycle complete with zero food waste.");
                decision.addActionItem("View verified Food Journey Passport");
                decision.addActionItem("Download Digital Rescue Receipt & Verification Summary");
                break;

            case Constants.PICKUP_STATUS_PICKUP_FAILED:
            case Constants.PICKUP_STATUS_DELIVERY_FAILED:
                decision.setNextAction("ESCALATE_CRITICAL");
                decision.setActionLabel("Operational Incident Recovery");
                decision.setRationale("Rescue interrupted during transit. Failure recovery protocols active.");
                decision.addActionItem("Review failure documentation and notes");
                decision.addActionItem("Re-evaluate surplus suitability or safe disposal");
                break;

            case Constants.STATE_CANCELLED:
            case Constants.STATE_EXPIRED:
                decision.setNextAction("ARCHIVED");
                decision.setActionLabel("Mission Closed");
                decision.setRationale("Rescue mission cancelled or expired.");
                break;

            default:
                decision.setNextAction("MONITOR");
                decision.setActionLabel("Monitor Status");
                decision.setRationale("Current status: " + status + ". Tracking operational milestones.");
                break;
        }
    }

    /**
     * DTO for Autopilot Action execution results.
     */
    public static class AutopilotExecutionResult implements Serializable {
        private static final long serialVersionUID = 1L;
        private final boolean success;
        private final String action;
        private final String message;
        private final String resultingStatus;

        public AutopilotExecutionResult(boolean success, String action, String message, String resultingStatus) {
            this.success = success;
            this.action = action;
            this.message = message;
            this.resultingStatus = resultingStatus;
        }

        public boolean isSuccess() { return success; }
        public String getAction() { return action; }
        public String getMessage() { return message; }
        public String getResultingStatus() { return resultingStatus; }
    }

    /**
     * Execute recommended operational Autopilot action with strict validation and transaction controls.
     */
    public AutopilotExecutionResult executeAction(Long listingId, Long pickupId, String action, User actor, String notes)
            throws ValidationException, InvalidStateTransitionException, SQLException {
        if (actor == null) {
            throw new ValidationException("Authentication required: Actor cannot be null.");
        }
        if (action == null || action.trim().isEmpty()) {
            throw new ValidationException("Action is required.");
        }

        Pickup pickup = null;
        if (pickupId != null) {
            pickup = pickupDao.findById(pickupId);
            if (pickup != null && listingId == null) {
                listingId = pickup.getFoodListingId();
            }
        }
        FoodListing listing = null;
        if (listingId != null) {
            listing = foodListingDao.findById(listingId);
        }

        if (listing == null && pickup == null) {
            throw new ValidationException("Rescue mission record not found.");
        }

        String currentStatus = (pickup != null) ? pickup.getStatus() : listing.getListingStatus();

        // Check expiration
        boolean isExpired = (listing != null && (
                Constants.URGENCY_EXPIRED.equalsIgnoreCase(listing.getUrgencyStatus()) ||
                (listing.getAvailableUntil() != null && listing.getAvailableUntil().before(new java.util.Date()))
        ));

        String targetAction = action.trim().toUpperCase();

        if (isExpired && !"EXPIRE_RESCUE".equals(targetAction)) {
            throw new ValidationException("Cannot execute '" + action + "': Food surplus has already expired.");
        }

        String role = actor.getRole();

        switch (targetAction) {
            case "ASSIGN_VOLUNTEER":
            case "TRY_ALTERNATIVE":
                if (pickup == null) {
                    throw new ValidationException("Cannot assign volunteer without an active pickup mission.");
                }
                boolean broadcastSuccess = assignmentEngine.broadcastPickupToAllVolunteers(pickup.getId());
                if (broadcastSuccess) {
                    return new AutopilotExecutionResult(true, targetAction, "Rescue mission broadcast to eligible volunteers.", currentStatus);
                } else {
                    return new AutopilotExecutionResult(false, targetAction, "No available volunteers reachable at this moment.", currentStatus);
                }

            case "NGO_SELF_PICKUP":
                if (!Constants.ROLE_NGO.equalsIgnoreCase(role)) {
                    throw new ValidationException("Authorization failed: Only registered NGOs can arrange self-pickup.");
                }
                if (pickup == null) {
                    throw new ValidationException("Cannot configure self-pickup without an active pickup mission.");
                }
                fallbackService.configureSelfPickup(
                        pickup.getId(),
                        actor.getId(),
                        actor.getEmail(),
                        actor.getPhoneNumber() != null ? actor.getPhoneNumber() : "Contact NGO",
                        "NGO_AUTH",
                        "NGO-" + actor.getId()
                );
                return new AutopilotExecutionResult(true, targetAction, "NGO Self-Pickup successfully registered.", Constants.STATE_NGO_SELF_PICKUP_ASSIGNED);

            case "START_PICKUP":
                if (pickup == null) {
                    throw new ValidationException("Cannot start pickup: Missing pickup mission.");
                }
                if (!Constants.ROLE_VOLUNTEER.equalsIgnoreCase(role) && !Constants.ROLE_NGO.equalsIgnoreCase(role)) {
                    throw new ValidationException("Authorization failed: Only assigned volunteers or NGO representatives can start pickup.");
                }
                journeyService.recordTransition(listingId, pickup.getId(), currentStatus, Constants.STATE_PICKED_UP,
                        actor.getId(), role, notes != null ? notes : "Autopilot: Pickup started and food surplus in transit",
                        "{\"actor\":\"" + actor.getEmail() + "\",\"action\":\"START_PICKUP\"}");
                return new AutopilotExecutionResult(true, targetAction, "Food successfully marked in transit.", Constants.STATE_PICKED_UP);

            case "DELIVER_FOOD":
                if (pickup == null) {
                    throw new ValidationException("Cannot deliver food: Missing pickup mission.");
                }
                if (!Constants.ROLE_VOLUNTEER.equalsIgnoreCase(role) && !Constants.ROLE_NGO.equalsIgnoreCase(role)) {
                    throw new ValidationException("Authorization failed: Only transit agents can mark food delivered.");
                }
                journeyService.recordTransition(listingId, pickup.getId(), currentStatus, Constants.STATE_DELIVERED,
                        actor.getId(), role, notes != null ? notes : "Autopilot: Food safely delivered to NGO facility",
                        "{\"actor\":\"" + actor.getEmail() + "\",\"action\":\"DELIVER_FOOD\"}");
                return new AutopilotExecutionResult(true, targetAction, "Food delivered to destination facility.", Constants.STATE_DELIVERED);

            case "CONFIRM_RECEIPT":
                if (pickup == null) {
                    throw new ValidationException("Cannot confirm receipt: Missing pickup mission.");
                }
                if (!Constants.ROLE_NGO.equalsIgnoreCase(role)) {
                    throw new ValidationException("Authorization failed: Only receiving NGOs can confirm receipt.");
                }
                journeyService.recordTransition(listingId, pickup.getId(), currentStatus, Constants.STATE_RECEIVED,
                        actor.getId(), role, notes != null ? notes : "Autopilot: NGO verified receipt and quality",
                        "{\"actor\":\"" + actor.getEmail() + "\",\"action\":\"CONFIRM_RECEIPT\"}");
                return new AutopilotExecutionResult(true, targetAction, "Food receipt successfully confirmed by NGO.", Constants.STATE_RECEIVED);

            case "ESCALATE_CRITICAL":
                bottleneckDao.createBottleneck(
                        "ESCALATED_CRITICAL",
                        "HIGH",
                        (notes != null && !notes.isEmpty()) ? notes : "Autopilot escalated critical bottleneck for immediate operational attention.",
                        listingId,
                        pickupId
                );
                return new AutopilotExecutionResult(true, targetAction, "Critical bottleneck escalated to operations queue.", currentStatus);

            case "EXPIRE_RESCUE":
                journeyService.recordTransition(listingId, (pickup != null ? pickup.getId() : null), currentStatus, Constants.STATE_EXPIRED,
                        actor.getId(), role, notes != null ? notes : "Autopilot: Food surplus passed safe distribution window",
                        "{\"actor\":\"" + actor.getEmail() + "\",\"action\":\"EXPIRE_RESCUE\"}");
                return new AutopilotExecutionResult(true, targetAction, "Food surplus safely marked as expired.", Constants.STATE_EXPIRED);

            default:
                throw new ValidationException("Unsupported or unrecognized Autopilot action: " + action);
        }
    }
}
