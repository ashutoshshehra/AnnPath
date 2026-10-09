package org.annpath.service;

import org.annpath.dao.FoodListingDao;
import org.annpath.dao.NgoDao;
import org.annpath.dao.NgoRequirementDao;
import org.annpath.location.GeoUtils;
import org.annpath.model.FoodListing;
import org.annpath.model.MatchExplanation;
import org.annpath.model.NgoProfile;
import org.annpath.model.NgoRequirement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SmartMatchingEngine {

    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final NgoRequirementDao requirementDao = new NgoRequirementDao();
    private final NgoDao ngoDao = new NgoDao();
    private final RescuePriorityEngine priorityEngine = new RescuePriorityEngine();
    private final MatchExplanationService explanationService = new MatchExplanationService();

    public static class MatchedListing {
        private final FoodListing listing;
        private final double distanceKm;
        private final double matchScore;
        private final String matchReason;
        private final int foodCompatScore;
        private final int quantityFitScore;
        private final int distanceScore;
        private final int timeCompatScore;
        private final int operationalScore;
        private MatchExplanation.Summary explanationSummary;

        public MatchedListing(FoodListing listing, double distanceKm, double matchScore, String matchReason) {
            this(listing, distanceKm, matchScore, matchReason,
                 calculateDefaultFoodScore(listing),
                 calculateDefaultQuantityScore(listing),
                 calculateDefaultDistanceScore(distanceKm),
                 calculateDefaultTimeScore(listing),
                 calculateDefaultOperationalScore(listing));
        }

        public MatchedListing(FoodListing listing, double distanceKm, double matchScore, String matchReason,
                              int foodCompatScore, int quantityFitScore, int distanceScore, int timeCompatScore, int operationalScore) {
            this.listing = listing;
            this.distanceKm = Math.round(distanceKm * 100.0) / 100.0;
            this.matchScore = Math.round(matchScore * 100.0) / 100.0;
            this.matchReason = matchReason;
            this.foodCompatScore = Math.max(0, Math.min(100, foodCompatScore));
            this.quantityFitScore = Math.max(0, Math.min(100, quantityFitScore));
            this.distanceScore = Math.max(0, Math.min(100, distanceScore));
            this.timeCompatScore = Math.max(0, Math.min(100, timeCompatScore));
            this.operationalScore = Math.max(0, Math.min(100, operationalScore));
            this.listing.setDistanceKm(this.distanceKm);
        }

        private static int calculateDefaultFoodScore(FoodListing fl) {
            return fl != null && fl.getFoodCategoryId() != null ? 50 : 0;
        }

        private static int calculateDefaultQuantityScore(FoodListing fl) {
            if (fl == null || fl.getRemainingQuantity() == null) return 0;
            return fl.getRemainingQuantity().doubleValue() > 0 ? 50 : 0;
        }

        private static int calculateDefaultDistanceScore(double distanceKm) {
            if (distanceKm <= 0) return 0;
            return (int) Math.max(0, Math.min(100, Math.round((1.0 - (distanceKm / 15.0)) * 100.0)));
        }

        private static int calculateDefaultTimeScore(FoodListing fl) {
            if (fl == null) return 0;
            if (fl.getAvailableUntil() != null) {
                long remainingMs = fl.getAvailableUntil().getTime() - System.currentTimeMillis();
                double hours = remainingMs / (1000.0 * 3600.0);
                if (hours <= 0) return 0;
                if (hours < 2.0) return 100;
                if (hours < 6.0) return 85;
                if (hours < 24.0) return 70;
                return 50;
            }
            if ("CRITICAL".equalsIgnoreCase(fl.getUrgencyStatus())) return 100;
            if ("EXPIRING_SOON".equalsIgnoreCase(fl.getUrgencyStatus())) return 85;
            return 50;
        }

        private static int calculateDefaultOperationalScore(FoodListing fl) {
            if (fl == null) return 0;
            int score = 0;
            if (fl.getContactPhone() != null && !fl.getContactPhone().trim().isEmpty()) score += 30;
            if (fl.getPickupLatitude() != null && fl.getPickupLongitude() != null) score += 40;
            if (fl.getStorageCondition() != null) score += 15;
            if (fl.getPickupAddress() != null && !fl.getPickupAddress().trim().isEmpty()) score += 15;
            return score;
        }

        public FoodListing getListing() { return listing; }
        public double getDistanceKm() { return distanceKm; }
        public double getMatchScore() { return matchScore; }
        public String getMatchReason() { return matchReason; }
        public int getFoodCompatScore() { return foodCompatScore; }
        public int getQuantityFitScore() { return quantityFitScore; }
        public int getDistanceScore() { return distanceScore; }
        public int getTimeCompatScore() { return timeCompatScore; }
        public int getOperationalScore() { return operationalScore; }
        public MatchExplanation.Summary getExplanationSummary() { return explanationSummary; }
        public void setExplanationSummary(MatchExplanation.Summary explanationSummary) { this.explanationSummary = explanationSummary; }
    }

    /**
     * Compute matching food surplus for a verified NGO.
     */
    public List<MatchedListing> findMatchesForNgo(Long ngoUserId) throws SQLException {
        NgoProfile ngo = ngoDao.findByUserId(ngoUserId);
        if (ngo == null) return Collections.emptyList();

        double radiusKm = (ngo.getServiceRadiusKm() != null) ? ngo.getServiceRadiusKm().doubleValue() : 15.0;
        double ngoLat = ngo.getLatitude().doubleValue();
        double ngoLng = ngo.getLongitude().doubleValue();

        double[] bbox = GeoUtils.calculateBoundingBox(ngoLat, ngoLng, radiusKm);
        List<FoodListing> candidateListings = foodListingDao.findAvailableListings(
                bbox[0], bbox[1], bbox[2], bbox[3], null, null
        );

        List<NgoRequirement> requirements = requirementDao.findByNgoId(ngoUserId);
        List<MatchedListing> matches = new ArrayList<>();

        for (FoodListing fl : candidateListings) {
            double distance = GeoUtils.calculateDistanceKm(
                    ngoLat, ngoLng,
                    fl.getPickupLatitude().doubleValue(), fl.getPickupLongitude().doubleValue()
            );

            if (distance > radiusKm) {
                continue;
            }

            // Calculate match affinity
            List<String> reasons = new ArrayList<>();
            reasons.add(String.format("%.1f km away", distance));

            // 1. Proximity component (Weight: 25%)
            double proximityFactor = Math.max(0.0, 1.0 - (distance / radiusKm));
            int distScore = (int) Math.round(proximityFactor * 100.0);

            // 2. Food Category & Dietary Compatibility (Weight: 25%)
            int foodScore = 0;
            boolean requirementMatched = false;
            NgoRequirement matchedReq = null;
            if (requirements == null || requirements.isEmpty()) {
                foodScore = 50; // No specific category filter configured
            } else {
                for (NgoRequirement req : requirements) {
                    if (req.getFoodCategoryId() != null && req.getFoodCategoryId().equals(fl.getFoodCategoryId())) {
                        requirementMatched = true;
                        matchedReq = req;
                        int catScore = 100;
                        int dietScore = 25;
                        if ("ANY".equalsIgnoreCase(req.getDietaryType()) ||
                            (fl.getDietaryType() != null && req.getDietaryType().equalsIgnoreCase(fl.getDietaryType()))) {
                            dietScore = 100;
                            reasons.add("Dietary requirement match (" + fl.getDietaryType() + ")");
                        }
                        foodScore = (int) Math.round(catScore * 0.7 + dietScore * 0.3);
                        break;
                    }
                }
                if (!requirementMatched) {
                    foodScore = 0;
                }
            }

            // 3. Quantity suitability (Weight: 20%)
            int quantScore = 50;
            double avail = (fl.getRemainingQuantity() != null) ? fl.getRemainingQuantity().doubleValue() : 0.0;
            if (matchedReq != null) {
                double min = (matchedReq.getMinQuantity() != null) ? matchedReq.getMinQuantity().doubleValue() : 0.0;
                double max = (matchedReq.getMaxQuantity() != null) ? matchedReq.getMaxQuantity().doubleValue() : 0.0;
                if (avail >= min && (max <= 0 || avail <= max)) {
                    quantScore = 100;
                    reasons.add("Exact quantity requirement match");
                } else if (max > 0 && avail > max) {
                    quantScore = (int) Math.max(20, Math.min(90, Math.round((max / avail) * 100.0)));
                    reasons.add("Partial rescue suitable");
                } else if (min > 0 && avail < min) {
                    quantScore = (int) Math.max(10, Math.min(80, Math.round((avail / min) * 100.0)));
                    reasons.add("Available quantity below standard batch");
                }
            }

            if (!requirementMatched && requirements != null && !requirements.isEmpty()) {
                reasons.add("Compatible category available in service area");
            }

            // 4. Time / Freshness component (Weight: 15%)
            int timeScore = MatchedListing.calculateDefaultTimeScore(fl);
            if ("CRITICAL".equalsIgnoreCase(fl.getUrgencyStatus())) {
                reasons.add("Critical: Rescue needed immediately");
            } else if ("EXPIRING_SOON".equalsIgnoreCase(fl.getUrgencyStatus())) {
                reasons.add("Expiring soon: 2-6 hours remaining");
            }

            // 5. Operational reliability (Weight: 15%)
            int opScore = MatchedListing.calculateDefaultOperationalScore(fl);

            // Compute composite match score (0 - 100)
            double score = (distScore * 0.25) + (foodScore * 0.25) + (quantScore * 0.20) + (timeScore * 0.15) + (opScore * 0.15);

            MatchedListing ml = new MatchedListing(fl, distance, Math.min(100.0, score), String.join(" • ", reasons),
                    foodScore, quantScore, distScore, timeScore, opScore);
            ml.setExplanationSummary(explanationService.explainSurplusToNgoMatch(fl, ngo, requirements));
            matches.add(ml);
        }

        // If no matches within strict radius, provide regional recommendations
        if (matches.isEmpty()) {
            List<FoodListing> allAvailable = foodListingDao.findAllAvailableListings(null, null);
            for (FoodListing fl : allAvailable) {
                double distance = 0.0;
                if (fl.getPickupLatitude() != null && fl.getPickupLongitude() != null) {
                    distance = GeoUtils.calculateDistanceKm(
                            ngoLat, ngoLng,
                            fl.getPickupLatitude().doubleValue(), fl.getPickupLongitude().doubleValue()
                    );
                }

                List<String> reasons = new ArrayList<>();
                if (distance > 0) {
                    reasons.add(String.format("%.1f km away", distance));
                }

                int distScore = (distance > 0) ? (int) Math.max(0, Math.min(100, Math.round((1.0 - (distance / (radiusKm * 3.0))) * 100.0))) : 0;
                int foodScore = MatchedListing.calculateDefaultFoodScore(fl);
                int quantScore = MatchedListing.calculateDefaultQuantityScore(fl);
                int timeScore = MatchedListing.calculateDefaultTimeScore(fl);
                int opScore = MatchedListing.calculateDefaultOperationalScore(fl);

                if ("CRITICAL".equalsIgnoreCase(fl.getUrgencyStatus())) {
                    reasons.add("Critical: Immediate rescue needed");
                } else {
                    reasons.add("Fresh surplus available for rescue");
                }

                if (fl.getPickupCity() != null && !fl.getPickupCity().trim().isEmpty()) {
                    reasons.add("Location: " + fl.getPickupCity());
                }

                double score = (distScore * 0.25) + (foodScore * 0.25) + (quantScore * 0.20) + (timeScore * 0.15) + (opScore * 0.15);

                MatchedListing ml = new MatchedListing(fl, distance, Math.min(100.0, score), String.join(" • ", reasons),
                        foodScore, quantScore, distScore, timeScore, opScore);
                ml.setExplanationSummary(explanationService.explainSurplusToNgoMatch(fl, ngo, requirements));
                matches.add(ml);
            }
        }

        // Sort descending by match score
        matches.sort(Comparator.comparingDouble(MatchedListing::getMatchScore).reversed());
        return matches;
    }
}
