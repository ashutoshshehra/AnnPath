package org.annpath.service;

import org.annpath.config.AppConfig;
import org.annpath.config.Constants;
import org.annpath.dao.FoodListingDao;
import org.annpath.model.FoodListing;
import org.annpath.util.DateTimeUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

public class UrgencyEngine {

    private static final Logger LOGGER = Logger.getLogger(UrgencyEngine.class.getName());
    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final RescuePriorityEngine priorityEngine = new RescuePriorityEngine();

    /**
     * Compute the urgency tier for a food listing.
     */
    public String calculateUrgency(Timestamp availableUntil, Timestamp availableFrom) {
        if (availableUntil == null) {
            return Constants.URGENCY_FRESH;
        }

        long now = System.currentTimeMillis();
        long diffMillis = availableUntil.getTime() - now;

        if (diffMillis <= 0) {
            return Constants.URGENCY_EXPIRED;
        }

        double hoursRemaining = (double) diffMillis / (1000.0 * 60.0 * 60.0);
        int criticalThresholdHours = AppConfig.getUrgencyCriticalHours();
        int expiringSoonThresholdHours = AppConfig.getUrgencyExpiringSoonHours();

        if (hoursRemaining <= criticalThresholdHours) {
            return Constants.URGENCY_CRITICAL;
        } else if (hoursRemaining <= expiringSoonThresholdHours) {
            return Constants.URGENCY_EXPIRING_SOON;
        } else {
            return Constants.URGENCY_FRESH;
        }
    }

    /**
     * Evaluate and update urgency status and priority score for a listing.
     */
    public void evaluateListingUrgency(FoodListing listing) {
        if (listing == null || listing.getId() == null) return;
        try {
            String newUrgency = calculateUrgency(listing.getAvailableUntil(), listing.getAvailableFrom());
            listing.setUrgencyStatus(newUrgency);

            // Compute explainable priority score
            RescuePriorityEngine.PriorityResult res = priorityEngine.computeScore(listing, 0.0, 1);
            listing.setPriorityScore(BigDecimal.valueOf(res.getScore()));
            listing.setPriorityExplanation(res.getExplanation());

            foodListingDao.updateUrgencyAndPriority(
                    listing.getId(),
                    newUrgency,
                    listing.getPriorityScore(),
                    listing.getPriorityExplanation()
            );

            if (Constants.URGENCY_EXPIRED.equals(newUrgency) && !Constants.STATE_EXPIRED.equals(listing.getListingStatus())) {
                foodListingDao.updateStatus(listing.getId(), Constants.STATE_EXPIRED);
                LOGGER.info("FoodListing #" + listing.getId() + " marked as EXPIRED.");
            }
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.WARNING, "Failed to update listing urgency for #" + listing.getId(), e);
        }
    }

    /**
     * Batch job to evaluate all active listings.
     */
    public void runUrgencyScan() {
        try {
            List<FoodListing> activeListings = foodListingDao.findActiveListingsForUrgencyScan();
            if (activeListings != null) {
                for (FoodListing fl : activeListings) {
                    evaluateListingUrgency(fl);
                }
            }
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.WARNING, "Error running background urgency scan", e);
        }
    }
}
