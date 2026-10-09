package org.annpath.service;

import org.annpath.config.Constants;
import org.annpath.model.FoodListing;
import org.annpath.util.DateTimeUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RescuePriorityEngine {

    public static class PriorityResult {
        private final double score;
        private final String badge;
        private final String explanation;

        public PriorityResult(double score, String badge, String explanation) {
            this.score = Math.round(score * 100.0) / 100.0;
            this.badge = badge;
            this.explanation = explanation;
        }

        public double getScore() { return score; }
        public String getBadge() { return badge; }
        public String getExplanation() { return explanation; }
    }

    public PriorityResult computeScore(FoodListing listing, double distanceKm, int localDemandMatches) {
        if (listing == null) {
            return new PriorityResult(0.0, "Standard", "Standard priority");
        }

        double timeScore = 20.0;
        List<String> reasons = new ArrayList<>();

        if (listing.getAvailableUntil() == null) {
            timeScore = 25.0;
            reasons.add("Available for collection");
        } else {
            double hoursLeft = DateTimeUtil.getHoursRemaining(listing.getAvailableUntil());
            if (hoursLeft <= 0) {
                return new PriorityResult(0.0, "Expired", "Food listing has passed available-until window");
            } else if (hoursLeft <= 2.0) {
                timeScore = 100.0;
                reasons.add("Critical: expires in " + String.format("%.1f", hoursLeft) + " hours");
            } else if (hoursLeft <= 6.0) {
                timeScore = 75.0;
                reasons.add("Expiring soon (" + String.format("%.1f", hoursLeft) + "h remaining)");
            } else if (hoursLeft <= 12.0) {
                timeScore = 50.0;
                reasons.add("Available for " + String.format("%.1f", hoursLeft) + "h");
            } else {
                timeScore = 25.0;
            }
        }

        // Perishability factor (25%)
        double perishabilityScore = 40.0;
        String storage = listing.getStorageCondition() != null ? listing.getStorageCondition() : "ROOM_TEMP";
        if ("WARM_HEATED".equalsIgnoreCase(storage)) {
            perishabilityScore = 100.0;
            reasons.add("Cooked hot food requires immediate rescue");
        } else if ("REFRIGERATED".equalsIgnoreCase(storage) || "FROZEN".equalsIgnoreCase(storage)) {
            perishabilityScore = 80.0;
            reasons.add("Temperature-controlled storage requirement");
        }

        // Quantity factor (20%)
        double qty = listing.getRemainingQuantity() != null ? listing.getRemainingQuantity().doubleValue() : 0.0;
        double quantityScore = Math.min(100.0, Math.log10(Math.max(10.0, qty)) * 35.0);
        if (qty >= 50.0) {
            reasons.add("High volume rescue (" + qty + " units available)");
        }

        // Demand factor (15%)
        double demandScore = Math.min(100.0, localDemandMatches * 30.0);
        if (localDemandMatches > 0) {
            reasons.add("Matches active requirements of " + localDemandMatches + " verified NGO(s)");
        }

        // Proximity bonus
        if (distanceKm > 0 && distanceKm <= 5.0) {
            reasons.add("Nearby pickup (" + String.format("%.1f", distanceKm) + " km away)");
        }

        double totalScore = (timeScore * 0.40) + (perishabilityScore * 0.25) + (quantityScore * 0.20) + (demandScore * 0.15);
        totalScore = Math.min(100.0, Math.max(0.0, totalScore));

        String badge = "Standard";
        if (totalScore >= 80.0 || Constants.URGENCY_CRITICAL.equalsIgnoreCase(listing.getUrgencyStatus())) {
            badge = "Rescue Now";
        } else if (totalScore >= 60.0) {
            badge = "High Priority";
        } else if (totalScore >= 40.0) {
            badge = "Medium Priority";
        }

        String explanation = String.join(" • ", reasons);
        return new PriorityResult(totalScore, badge, explanation);
    }
}
