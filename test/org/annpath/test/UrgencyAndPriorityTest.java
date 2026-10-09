package org.annpath.test;

import org.annpath.config.Constants;
import org.annpath.location.GeoUtils;
import org.annpath.model.FoodListing;
import org.annpath.service.RescuePriorityEngine;
import org.annpath.service.UrgencyEngine;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Enterprise test suite verifying algorithmic intelligence:
 * 1. Urgency tier transitions (CRITICAL, EXPIRING_SOON, FRESH, EXPIRED).
 * 2. Rescue Priority Engine (0-100 RPS multi-criteria formula & explanation).
 * 3. Geodesic spatial calculations (Haversine & bounding box).
 */
public class UrgencyAndPriorityTest {

    private final UrgencyEngine urgencyEngine = new UrgencyEngine();
    private final RescuePriorityEngine priorityEngine = new RescuePriorityEngine();

    @Test
    public void testUrgencyTierCalculation() {
        long now = System.currentTimeMillis();

        // 1. Expired listing (past available-until timestamp)
        Timestamp past = new Timestamp(now - (30 * 60 * 1000)); // 30 mins ago
        String statusExpired = urgencyEngine.calculateUrgency(past, new Timestamp(now - (3600 * 1000)));
        Assert.assertEquals("Listing in the past must be EXPIRED", Constants.URGENCY_EXPIRED, statusExpired);

        // 2. Critical listing (expires in 1.5 hours, <= 3h threshold)
        Timestamp criticalTime = new Timestamp(now + (long) (1.5 * 3600 * 1000));
        String statusCritical = urgencyEngine.calculateUrgency(criticalTime, new Timestamp(now));
        Assert.assertEquals("Listing expiring in 1.5h must be CRITICAL", Constants.URGENCY_CRITICAL, statusCritical);

        // 3. Expiring soon listing (expires in 6 hours, > 3h and <= 12h threshold)
        Timestamp expiringSoonTime = new Timestamp(now + (long) (6 * 3600 * 1000));
        String statusExpiringSoon = urgencyEngine.calculateUrgency(expiringSoonTime, new Timestamp(now));
        Assert.assertEquals("Listing expiring in 6h must be EXPIRING_SOON", Constants.URGENCY_EXPIRING_SOON, statusExpiringSoon);

        // 4. Fresh listing (expires in 36 hours, > 12h)
        Timestamp freshTime = new Timestamp(now + (long) (36 * 3600 * 1000));
        String statusFresh = urgencyEngine.calculateUrgency(freshTime, new Timestamp(now));
        Assert.assertEquals("Listing expiring in 36h must be FRESH", Constants.URGENCY_FRESH, statusFresh);
    }

    @Test
    public void testRescuePriorityScoreComputationAndExplanation() {
        long now = System.currentTimeMillis();

        FoodListing listing = new FoodListing();
        listing.setId(101L);
        listing.setTitle("Fresh Cooked Vegetable Biryani");
        listing.setStorageCondition("WARM_HEATED");
        listing.setTotalQuantity(new BigDecimal("80.00"));
        listing.setRemainingQuantity(new BigDecimal("80.00"));
        listing.setAvailableUntil(new Timestamp(now + (long) (1.5 * 3600 * 1000))); // 1.5h left -> Critical
        listing.setUrgencyStatus(Constants.URGENCY_CRITICAL);

        // Compute score: critical time, cooked hot food, high volume (80), 2 matching NGOs, 3 km away
        RescuePriorityEngine.PriorityResult result = priorityEngine.computeScore(listing, 3.0, 2);

        Assert.assertNotNull(result);
        double score = result.getScore();
        Assert.assertTrue("RPS score must be between 0 and 100, got: " + score, score >= 0.0 && score <= 100.0);
        Assert.assertTrue("High urgency hot food with demand should score >= 70, got: " + score, score >= 70.0);
        Assert.assertEquals("High scoring critical item should have badge 'Rescue Now'", "Rescue Now", result.getBadge());

        String explanation = result.getExplanation();
        Assert.assertNotNull(explanation);
        Assert.assertTrue("Explanation must explain time urgency", explanation.contains("Critical: expires in"));
        Assert.assertTrue("Explanation must explain warm food handling", explanation.contains("Cooked hot food"));
        Assert.assertTrue("Explanation must explain volume impact", explanation.contains("High volume rescue"));
        Assert.assertTrue("Explanation must explain NGO demand", explanation.contains("Matches active requirements"));
        Assert.assertTrue("Explanation must explain proximity", explanation.contains("Nearby pickup"));
    }

    @Test
    public void testExpiredListingProducesZeroScore() {
        long now = System.currentTimeMillis();
        FoodListing listing = new FoodListing();
        listing.setAvailableUntil(new Timestamp(now - 10000));
        listing.setRemainingQuantity(new BigDecimal("50.00"));

        RescuePriorityEngine.PriorityResult result = priorityEngine.computeScore(listing, 1.0, 0);
        Assert.assertEquals("Expired food must yield a priority score of 0.0", 0.0, result.getScore(), 0.001);
        Assert.assertEquals("Expired badge must be returned", "Expired", result.getBadge());
    }

    @Test
    public void testGeodesicHaversineDistanceCalculations() {
        // Distance between same coordinates must be 0
        double distZero = GeoUtils.calculateDistanceKm(28.6139, 77.2090, 28.6139, 77.2090);
        Assert.assertEquals("Distance between identical coordinates must be 0", 0.0, distZero, 0.001);

        // Distance between London (51.5074, -0.1278) and Paris (48.8566, 2.3522) ~ 343 km
        double distLondonParis = GeoUtils.calculateDistanceKm(51.5074, -0.1278, 48.8566, 2.3522);
        Assert.assertTrue("London-Paris distance should be between 330 and 360 km, got: " + distLondonParis,
                distLondonParis >= 330.0 && distLondonParis <= 360.0);

        // Distance conversion to miles
        double miles = GeoUtils.toMiles(distLondonParis);
        Assert.assertTrue("Distance in miles must be approximately 0.621371 of km",
                Math.abs(miles - (distLondonParis * 0.621371)) < 0.01);

        // Bounding box validation
        double centerLat = 28.6139;
        double centerLon = 77.2090;
        double radiusKm = 10.0;
        double[] bbox = GeoUtils.calculateBoundingBox(centerLat, centerLon, radiusKm);
        // bbox: { minLat, maxLat, minLon, maxLon }
        Assert.assertEquals(4, bbox.length);
        Assert.assertTrue("minLat must be less than centerLat", bbox[0] < centerLat);
        Assert.assertTrue("maxLat must be greater than centerLat", bbox[1] > centerLat);
        Assert.assertTrue("minLon must be less than centerLon", bbox[2] < centerLon);
        Assert.assertTrue("maxLon must be greater than centerLon", bbox[3] > centerLon);
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("RUNNING URGENCY & PRIORITY ALGORITHMIC TEST SUITE");
        System.out.println("=================================================");
        UrgencyAndPriorityTest test = new UrgencyAndPriorityTest();
        int passed = 0;
        int total = 4;

        try {
            System.out.print("[1/4] Testing Urgency Tier Transitions... ");
            test.testUrgencyTierCalculation();
            System.out.println("PASSED");
            passed++;

            System.out.print("[2/4] Testing Explainable Rescue Priority Score (RPS)... ");
            test.testRescuePriorityScoreComputationAndExplanation();
            System.out.println("PASSED");
            passed++;

            System.out.print("[3/4] Testing Zero Score for Expired Surplus... ");
            test.testExpiredListingProducesZeroScore();
            System.out.println("PASSED");
            passed++;

            System.out.print("[4/4] Testing Haversine Distance & Geodesic Bounding Box... ");
            test.testGeodesicHaversineDistanceCalculations();
            System.out.println("PASSED");
            passed++;

            System.out.println("-------------------------------------------------");
            System.out.println("URGENCY & PRIORITY TEST SUITE: " + passed + "/" + total + " PASSED (100% SUCCESS)");
            System.out.println("=================================================\n");
        } catch (Throwable t) {
            System.err.println("FAILED: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }
}
