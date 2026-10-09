package org.annpath.service;

import org.annpath.location.GeoUtils;
import org.annpath.model.FoodListing;
import org.annpath.model.MatchExplanation;
import org.annpath.model.NgoProfile;
import org.annpath.model.NgoRequirement;
import org.annpath.model.Pickup;
import org.annpath.model.VolunteerProfile;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Explainable Matching Engine for AnnPath.
 * Transforms opaque scoring algorithms into transparent, audit-ready breakdowns for Donors, NGOs, and Volunteers.
 */
public class MatchExplanationService {

    /**
     * Generate comprehensive factor-by-factor explanation for an NGO surplus recommendation.
     */
    public MatchExplanation.Summary explainSurplusToNgoMatch(FoodListing listing, NgoProfile ngo, List<NgoRequirement> requirements) {
        MatchExplanation.Summary summary = new MatchExplanation.Summary();

        if (listing == null || ngo == null) {
            summary.setHeadline("Insufficient data to generate match explanation.");
            return summary;
        }

        double totalWeightedScore = 0.0;

        // 1. Distance & Proximity (Weight: 30%)
        double distanceKm = 0.0;
        double radiusKm = (ngo.getServiceRadiusKm() != null) ? ngo.getServiceRadiusKm().doubleValue() : 15.0;
        int distanceScore;
        String distanceReason;
        boolean distanceMatched;

        if (ngo.getLatitude() != null && ngo.getLongitude() != null &&
            listing.getPickupLatitude() != null && listing.getPickupLongitude() != null) {
            distanceKm = GeoUtils.calculateDistanceKm(
                    ngo.getLatitude().doubleValue(), ngo.getLongitude().doubleValue(),
                    listing.getPickupLatitude().doubleValue(), listing.getPickupLongitude().doubleValue()
            );
            distanceKm = Math.round(distanceKm * 10.0) / 10.0;

            if (distanceKm <= radiusKm) {
                double proximityRatio = Math.max(0.0, 1.0 - (distanceKm / radiusKm));
                distanceScore = (int) Math.round(50.0 + (proximityRatio * 50.0));
                distanceReason = String.format("%.1f km away from your base — well inside your %.0f km service radius.", distanceKm, radiusKm);
                distanceMatched = true;
                summary.addPositiveFactor(String.format("Proximity: Located %.1f km away (within %.0f km radius)", distanceKm, radiusKm));
            } else {
                distanceScore = Math.max(15, (int) Math.round(100.0 - (distanceKm * 3.0)));
                distanceReason = String.format("%.1f km away — extends slightly beyond primary %.0f km radius but reachable.", distanceKm, radiusKm);
                distanceMatched = false;
                summary.addPotentialRisk(String.format("Distance: %.1f km exceeds primary service boundary (%.0f km)", distanceKm, radiusKm));
            }
        } else {
            if (ngo.getCity() != null && listing.getPickupCity() != null &&
                ngo.getCity().trim().equalsIgnoreCase(listing.getPickupCity().trim())) {
                distanceScore = 50;
                distanceReason = "Geographic coordinates not set; matched by city municipality (" + ngo.getCity().trim() + ").";
                distanceMatched = true;
            } else {
                distanceScore = 0;
                distanceReason = "Geographic coordinates not configured for proximity calculation.";
                distanceMatched = false;
                summary.addPotentialRisk("Location: Precise GPS coordinates unavailable for distance scoring");
            }
        }
        totalWeightedScore += distanceScore * 0.30;
        summary.addFactor(new MatchExplanation("Proximity & Transit Distance", distanceScore, 0.30, distanceReason, distanceMatched, "map-pin"));

        // 2. Food Category & Capacity (Weight: 25%)
        int categoryScore = 0;
        String categoryReason = "Surplus food does not match any of your registered requirement categories.";
        boolean categoryMatched = false;

        NgoRequirement matchedReq = null;
        if (requirements != null && !requirements.isEmpty()) {
            for (NgoRequirement req : requirements) {
                if (req.getFoodCategoryId() != null && req.getFoodCategoryId().equals(listing.getFoodCategoryId())) {
                    matchedReq = req;
                    categoryScore = 100;
                    categoryReason = "Matches an active food category requirement registered by your NGO.";
                    categoryMatched = true;
                    summary.addPositiveFactor("Category Need: Exactly matches registered requirement category");
                    break;
                }
            }
            if (!categoryMatched) {
                categoryScore = 0;
                categoryReason = "Surplus food does not match any of your registered requirement categories.";
            }
        } else {
            categoryScore = 50;
            categoryReason = "General surplus category compatible with standard community distribution (no category filter set).";
        }
        totalWeightedScore += categoryScore * 0.25;
        summary.addFactor(new MatchExplanation("Food Category Suitability", categoryScore, 0.25, categoryReason, categoryMatched, "utensils"));

        // 3. Dietary Compatibility (Weight: 15%)
        int dietaryScore = 50;
        String dietaryReason = "Standard dietary fit for community distribution.";
        boolean dietaryMatched = true;

        if (matchedReq != null && matchedReq.getDietaryType() != null) {
            String reqDiet = matchedReq.getDietaryType().trim();
            String listDiet = listing.getDietaryType() != null ? listing.getDietaryType().trim() : "VEG";

            if ("ANY".equalsIgnoreCase(reqDiet) || reqDiet.equalsIgnoreCase(listDiet)) {
                dietaryScore = 100;
                dietaryReason = String.format("Exact dietary alignment: Donor listing is %s, matching NGO requirement (%s).", listDiet, reqDiet);
                summary.addPositiveFactor(String.format("Dietary Fit: Verified %s match", listDiet));
            } else {
                dietaryScore = 25;
                dietaryReason = String.format("Dietary preference differs: Listing is %s, requested preference is %s.", listDiet, reqDiet);
                dietaryMatched = false;
                summary.addPotentialRisk("Dietary Divergence: Verify beneficiary preferences for " + listDiet);
            }
        } else {
            dietaryScore = 50;
            dietaryReason = "Listing dietary profile: " + (listing.getDietaryType() != null ? listing.getDietaryType() : "VEG");
        }
        totalWeightedScore += dietaryScore * 0.15;
        summary.addFactor(new MatchExplanation("Dietary Compatibility", dietaryScore, 0.15, dietaryReason, dietaryMatched, "check-circle"));

        // 4. Quantity Fit (Weight: 15%)
        double remainingQty = (listing.getRemainingQuantity() != null) ? listing.getRemainingQuantity().doubleValue() : 0.0;
        int quantityScore = remainingQty > 0 ? 50 : 0;
        String quantityReason = String.format("Available surplus: %.0f units ready for distribution.", remainingQty);
        boolean quantityMatched = true;

        if (matchedReq != null) {
            double minQty = (matchedReq.getMinQuantity() != null) ? matchedReq.getMinQuantity().doubleValue() : 0.0;
            double maxQty = (matchedReq.getMaxQuantity() != null) ? matchedReq.getMaxQuantity().doubleValue() : 0.0;

            if (remainingQty >= minQty && (maxQty <= 0 || remainingQty <= maxQty)) {
                quantityScore = 100;
                quantityReason = String.format("Surplus volume (%.0f units) perfectly fits NGO target batch size (%.0f - %.0f).", remainingQty, minQty, maxQty);
                summary.addPositiveFactor("Quantity Fit: Volume matches desired intake batch size");
            } else if (maxQty > 0 && remainingQty > maxQty) {
                quantityScore = (int) Math.max(20, Math.min(90, Math.round((maxQty / remainingQty) * 100.0)));
                quantityReason = String.format("Surplus volume (%.0f units) exceeds target batch (%.0f). Can be partially rescued.", remainingQty, maxQty);
            } else if (minQty > 0 && remainingQty < minQty) {
                quantityScore = (int) Math.max(10, Math.min(80, Math.round((remainingQty / minQty) * 100.0)));
                quantityReason = String.format("Surplus batch (%.0f units) is below standard quota (%.0f) but ready for rescue.", remainingQty, minQty);
            } else {
                quantityScore = 50;
                quantityReason = String.format("Surplus volume: %.0f units available.", remainingQty);
            }
        } else {
            quantityScore = remainingQty > 0 ? 50 : 0;
            quantityReason = String.format("Available surplus: %.0f units ready for distribution.", remainingQty);
        }
        totalWeightedScore += quantityScore * 0.15;
        summary.addFactor(new MatchExplanation("Batch Volume & Quantity Fit", quantityScore, 0.15, quantityReason, quantityMatched, "box"));

        // 5. Freshness & Rescue Window (Weight: 15%)
        int freshnessScore = 60;
        String freshnessReason = "Fresh surplus with comfortable distribution window.";
        boolean freshnessMatched = true;

        if (listing.getAvailableUntil() != null) {
            long remainingMs = listing.getAvailableUntil().getTime() - System.currentTimeMillis();
            double hours = remainingMs / (1000.0 * 3600.0);
            if (hours <= 0) {
                freshnessScore = 0;
                freshnessReason = "Food surplus has expired past safe distribution window.";
                freshnessMatched = false;
                summary.addPotentialRisk("Expired: Food surplus is past safe consumption window");
            } else if (hours < 2.0) {
                freshnessScore = 100;
                freshnessReason = String.format("Immediate rescue priority! Only %.1f hours remaining before expiration.", hours);
                summary.addPositiveFactor("High Priority: Expedited rescue dispatch recommended");
                summary.addPotentialRisk("Shelf Life: Immediate pickup required within 2 hours");
            } else if (hours < 6.0) {
                freshnessScore = 85;
                freshnessReason = String.format("Expiring soon: %.1f hours remaining for optimal consumption.", hours);
                summary.addPositiveFactor("Active Window: 2-6 hours remaining");
            } else if (hours < 24.0) {
                freshnessScore = 70;
                freshnessReason = String.format("Fresh surplus: %.1f hours remaining in distribution window.", hours);
                summary.addPositiveFactor("Fresh Quality: Distribution window available today");
            } else {
                freshnessScore = 50;
                freshnessReason = String.format("Extended shelf-life: %.0f hours remaining.", hours);
            }
        } else if ("CRITICAL".equalsIgnoreCase(listing.getUrgencyStatus())) {
            freshnessScore = 100;
            freshnessReason = "Immediate rescue priority! Under 2 hours remaining before expiration.";
            summary.addPositiveFactor("High Priority: Expedited rescue dispatch recommended");
            summary.addPotentialRisk("Shelf Life: Immediate pickup required within 2 hours");
        } else if ("EXPIRING_SOON".equalsIgnoreCase(listing.getUrgencyStatus())) {
            freshnessScore = 85;
            freshnessReason = "Expiring soon: 2 to 6 hours remaining for optimal consumption.";
            summary.addPositiveFactor("Active Window: 2-6 hours remaining");
        } else {
            freshnessScore = 60;
            freshnessReason = "Fresh surplus with comfortable distribution window (> 6 hours).";
            summary.addPositiveFactor("Fresh Quality: Generous distribution window available");
        }
        totalWeightedScore += freshnessScore * 0.15;
        summary.addFactor(new MatchExplanation("Freshness & Shelf-Life Window", freshnessScore, 0.15, freshnessReason, freshnessMatched, "clock"));

        // Set summary
        summary.setTotalScore(totalWeightedScore);
        summary.setHeadline(String.format("Compatibility Score: %.1f%% — %s Match",
                summary.getTotalScore(), summary.getMatchGrade()));

        return summary;
    }

    /**
     * Generate factor-by-factor explanation for Volunteer assignment.
     */
    public MatchExplanation.Summary explainVolunteerPickupMatch(Pickup pickup, FoodListing listing, VolunteerProfile volunteer) {
        MatchExplanation.Summary summary = new MatchExplanation.Summary();

        if (volunteer == null) {
            summary.setHeadline("No volunteer candidate profile available.");
            return summary;
        }

        double totalWeightedScore = 0.0;

        // 1. Proximity to Donor (Weight: 35%)
        double distanceKm = 0.0;
        int distanceScore;
        String distanceReason;
        boolean distanceMatched = true;

        if (listing != null && listing.getPickupLatitude() != null && listing.getPickupLongitude() != null &&
            volunteer.getLatitude() != null && volunteer.getLongitude() != null) {
            distanceKm = GeoUtils.calculateDistanceKm(
                    volunteer.getLatitude().doubleValue(), volunteer.getLongitude().doubleValue(),
                    listing.getPickupLatitude().doubleValue(), listing.getPickupLongitude().doubleValue()
            );
            distanceKm = Math.round(distanceKm * 10.0) / 10.0;
            double maxTravel = (volunteer.getMaxTravelDistanceKm() != null) ? volunteer.getMaxTravelDistanceKm().doubleValue() : 10.0;

            if (distanceKm <= maxTravel) {
                double factor = Math.max(0.0, 1.0 - (distanceKm / maxTravel));
                distanceScore = (int) Math.round(60.0 + (factor * 40.0));
                distanceReason = String.format("%.1f km from pickup point (comfortably within %.0f km travel radius).", distanceKm, maxTravel);
                summary.addPositiveFactor(String.format("Quick Dispatch: Volunteer is %.1f km from donor", distanceKm));
            } else {
                distanceScore = Math.max(0, (int) Math.round(100.0 - (distanceKm * 4.0)));
                distanceReason = String.format("%.1f km from pickup point (exceeds preferred %.0f km limit).", distanceKm, maxTravel);
                distanceMatched = false;
                summary.addPotentialRisk("Travel Distance: Volunteer travel may take longer than usual");
            }
        } else {
            distanceScore = 0;
            distanceReason = "Geographic coordinates not set for volunteer or donor pickup point.";
            distanceMatched = false;
            summary.addPotentialRisk("Transit: Coordinates missing for exact proximity routing");
        }
        totalWeightedScore += distanceScore * 0.35;
        summary.addFactor(new MatchExplanation("Proximity & Transit Time", distanceScore, 0.35, distanceReason, distanceMatched, "map-pin"));

        // 2. Active Workload & Capacity (Weight: 25%)
        int workload = volunteer.getActiveAssignmentsCount();
        int workloadScore;
        String workloadReason;
        boolean workloadMatched;

        if (workload == 0) {
            workloadScore = 100;
            workloadReason = "Zero active assignments — volunteer is completely free for immediate dispatch.";
            workloadMatched = true;
            summary.addPositiveFactor("Availability: 100% capacity free (0 concurrent tasks)");
        } else if (workload == 1) {
            workloadScore = 80;
            workloadReason = "1 active task in progress — capable of taking an adjoining rescue route.";
            workloadMatched = true;
        } else if (workload == 2) {
            workloadScore = 55;
            workloadReason = "2 active tasks in progress — near maximum recommended concurrent capacity.";
            workloadMatched = false;
            summary.addPotentialRisk("Workload: Volunteer already has 2 active assignments");
        } else {
            workloadScore = 30;
            workloadReason = "Volunteer is overloaded with 3+ active deliveries.";
            workloadMatched = false;
            summary.addPotentialRisk("High Load: Multiple pending deliveries may delay pickup");
        }
        totalWeightedScore += workloadScore * 0.25;
        summary.addFactor(new MatchExplanation("Active Workload & Bandwidth", workloadScore, 0.25, workloadReason, workloadMatched, "activity"));

        // 3. Status & Readiness (Weight: 20%)
        int readyScore = volunteer.isAvailable() ? 95 : 20;
        String readyReason = volunteer.isAvailable()
                ? "Volunteer marked active and on-duty for food rescues."
                : "Volunteer is currently set to off-duty status.";
        boolean readyMatched = volunteer.isAvailable();
        if (readyMatched) {
            summary.addPositiveFactor("Status: Verified on-duty and active");
        } else {
            summary.addPotentialRisk("Status: Currently marked off-duty");
        }
        totalWeightedScore += readyScore * 0.20;
        summary.addFactor(new MatchExplanation("Availability & Shift Status", readyScore, 0.20, readyReason, readyMatched, "user-check"));

        // 4. Vehicle & Transport Capability (Weight: 20%)
        String vehicle = volunteer.getVehicleType() != null ? volunteer.getVehicleType().toUpperCase() : "BIKE";
        int vehicleScore = 85;
        String vehicleReason = "Vehicle type (" + vehicle + ") suitable for transit.";
        boolean vehicleMatched = true;

        if ("CAR".equals(vehicle) || "VAN".equals(vehicle) || "TRUCK".equals(vehicle)) {
            vehicleScore = 100;
            vehicleReason = "Covered four-wheeler / vehicle ensures temperature protection and large capacity.";
            summary.addPositiveFactor("Transport: Four-wheeler suitable for large and bulk deliveries");
        } else {
            vehicleReason = "Two-wheeler / bike suitable for rapid urban food rescue transit.";
            summary.addPositiveFactor("Mobility: Rapid urban mobility");
        }
        totalWeightedScore += vehicleScore * 0.20;
        summary.addFactor(new MatchExplanation("Transit & Vehicle Compatibility", vehicleScore, 0.20, vehicleReason, vehicleMatched, "truck"));

        // Set summary
        summary.setTotalScore(totalWeightedScore);
        summary.setHeadline(String.format("Assignment Suitability: %.1f%% — %s Candidate",
                summary.getTotalScore(), summary.getMatchGrade()));

        return summary;
    }
}
