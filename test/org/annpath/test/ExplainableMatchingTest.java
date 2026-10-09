package org.annpath.test;

import org.annpath.model.FoodListing;
import org.annpath.model.MatchExplanation;
import org.annpath.model.NgoProfile;
import org.annpath.model.NgoRequirement;
import org.annpath.model.VolunteerProfile;
import org.annpath.service.MatchExplanationService;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise test suite verifying Explainable Matching Engine:
 * - Mathematical transparency and factor weight sum = 100%
 * - Real attribute derivation without magic numbers
 * - Deterministic dietary, category, quantity, and distance scoring
 * - Volunteer suitability factor breakdown
 */
public class ExplainableMatchingTest {

    @Test
    public void testSurplusToNgoFactorWeightsSumToUnity() {
        MatchExplanationService service = new MatchExplanationService();

        FoodListing listing = new FoodListing();
        listing.setFoodCategoryId(1);
        listing.setDietaryType("VEG");
        listing.setRemainingQuantity(new BigDecimal("50"));
        listing.setUrgencyStatus("FRESH");
        listing.setPickupLatitude(new BigDecimal("28.6139"));
        listing.setPickupLongitude(new BigDecimal("77.2090"));

        NgoProfile ngo = new NgoProfile();
        ngo.setLatitude(new BigDecimal("28.6200"));
        ngo.setLongitude(new BigDecimal("77.2100"));
        ngo.setServiceRadiusKm(new BigDecimal("15"));

        NgoRequirement req = new NgoRequirement();
        req.setFoodCategoryId(1);
        req.setDietaryType("VEG");
        req.setMinQuantity(new BigDecimal("10"));
        req.setMaxQuantity(new BigDecimal("100"));

        List<NgoRequirement> reqs = new ArrayList<>();
        reqs.add(req);

        MatchExplanation.Summary summary = service.explainSurplusToNgoMatch(listing, ngo, reqs);
        Assert.assertNotNull(summary);
        Assert.assertEquals("Summary must contain exactly 5 transparent factors", 5, summary.getBreakdown().size());

        double totalWeight = 0.0;
        for (MatchExplanation factor : summary.getBreakdown()) {
            totalWeight += factor.getWeight();
        }

        Assert.assertEquals("Sum of all 5 matching factor weights must equal 1.00 (100%)", 1.0, totalWeight, 0.001);
    }

    @Test
    public void testStrictCategoryAndDietaryDerivation() {
        MatchExplanationService service = new MatchExplanationService();

        FoodListing listing = new FoodListing();
        listing.setFoodCategoryId(5);
        listing.setDietaryType("NON_VEG");
        listing.setRemainingQuantity(new BigDecimal("20"));

        NgoProfile ngo = new NgoProfile();

        // 1. Exact Category & Exact Dietary match
        NgoRequirement matchingReq = new NgoRequirement();
        matchingReq.setFoodCategoryId(5);
        matchingReq.setDietaryType("NON_VEG");
        List<NgoRequirement> reqs1 = new ArrayList<>();
        reqs1.add(matchingReq);

        MatchExplanation.Summary s1 = service.explainSurplusToNgoMatch(listing, ngo, reqs1);
        MatchExplanation catFactor1 = s1.getBreakdown().get(1);
        MatchExplanation dietFactor1 = s1.getBreakdown().get(2);

        Assert.assertEquals("Exact category match must score 100", 100, catFactor1.getScore());
        Assert.assertTrue(catFactor1.isMatched());
        Assert.assertEquals("Exact dietary match must score 100", 100, dietFactor1.getScore());
        Assert.assertTrue(dietFactor1.isMatched());

        // 2. Mismatched Category
        NgoRequirement wrongCatReq = new NgoRequirement();
        wrongCatReq.setFoodCategoryId(99);
        wrongCatReq.setDietaryType("NON_VEG");
        List<NgoRequirement> reqs2 = new ArrayList<>();
        reqs2.add(wrongCatReq);

        MatchExplanation.Summary s2 = service.explainSurplusToNgoMatch(listing, ngo, reqs2);
        MatchExplanation catFactor2 = s2.getBreakdown().get(1);
        Assert.assertEquals("Category mismatch against registered requirements must score 0", 0, catFactor2.getScore());
        Assert.assertFalse(catFactor2.isMatched());

        // 3. Matched Category but Divergent Dietary Preference
        NgoRequirement wrongDietReq = new NgoRequirement();
        wrongDietReq.setFoodCategoryId(5);
        wrongDietReq.setDietaryType("VEG"); // NGO wants VEG, food is NON_VEG
        List<NgoRequirement> reqs3 = new ArrayList<>();
        reqs3.add(wrongDietReq);

        MatchExplanation.Summary s3 = service.explainSurplusToNgoMatch(listing, ngo, reqs3);
        MatchExplanation dietFactor3 = s3.getBreakdown().get(2);
        Assert.assertEquals("Dietary divergence must score exactly 25", 25, dietFactor3.getScore());
        Assert.assertFalse(dietFactor3.isMatched());
    }

    @Test
    public void testQuantityRatioCalculation() {
        MatchExplanationService service = new MatchExplanationService();
        NgoProfile ngo = new NgoProfile();

        // Target batch: 50 to 100
        NgoRequirement req = new NgoRequirement();
        req.setFoodCategoryId(1);
        req.setMinQuantity(new BigDecimal("50"));
        req.setMaxQuantity(new BigDecimal("100"));
        List<NgoRequirement> reqs = new ArrayList<>();
        reqs.add(req);

        // Case A: Exact fit (75 is between 50 and 100)
        FoodListing flA = new FoodListing();
        flA.setFoodCategoryId(1);
        flA.setRemainingQuantity(new BigDecimal("75"));
        MatchExplanation.Summary sA = service.explainSurplusToNgoMatch(flA, ngo, reqs);
        Assert.assertEquals("Quantity within [min, max] must score 100", 100, sA.getBreakdown().get(3).getScore());

        // Case B: Surplus excess (200 available, max is 100 -> ratio 100/200 = 0.50 -> 50 score)
        FoodListing flB = new FoodListing();
        flB.setFoodCategoryId(1);
        flB.setRemainingQuantity(new BigDecimal("200"));
        MatchExplanation.Summary sB = service.explainSurplusToNgoMatch(flB, ngo, reqs);
        Assert.assertEquals("Quantity exceeding max should scale proportionally by (max/avail)", 50, sB.getBreakdown().get(3).getScore());

        // Case C: Surplus deficit (25 available, min is 50 -> ratio 25/50 = 0.50 -> 50 score)
        FoodListing flC = new FoodListing();
        flC.setFoodCategoryId(1);
        flC.setRemainingQuantity(new BigDecimal("25"));
        MatchExplanation.Summary sC = service.explainSurplusToNgoMatch(flC, ngo, reqs);
        Assert.assertEquals("Quantity below min should scale proportionally by (avail/min)", 50, sC.getBreakdown().get(3).getScore());
    }

    @Test
    public void testVolunteerMatchingFactorBreakdown() {
        MatchExplanationService service = new MatchExplanationService();

        VolunteerProfile volunteer = new VolunteerProfile();
        volunteer.setUserId(201L);
        volunteer.setActiveAssignmentsCount(0);
        volunteer.setAvailabilityStatus("AVAILABLE");
        volunteer.setVehicleType("CAR");
        volunteer.setLatitude(new BigDecimal("28.6139"));
        volunteer.setLongitude(new BigDecimal("77.2090"));
        volunteer.setMaxTravelDistanceKm(new BigDecimal("10"));

        FoodListing listing = new FoodListing();
        listing.setPickupLatitude(new BigDecimal("28.6200"));
        listing.setPickupLongitude(new BigDecimal("77.2100"));

        MatchExplanation.Summary summary = service.explainVolunteerPickupMatch(null, listing, volunteer);
        Assert.assertNotNull(summary);
        Assert.assertEquals("Volunteer explanation must contain 4 distinct factors", 4, summary.getBreakdown().size());

        // Factor weights sum to 1.00
        double totalWeight = 0.0;
        for (MatchExplanation factor : summary.getBreakdown()) {
            totalWeight += factor.getWeight();
        }
        Assert.assertEquals("Sum of volunteer factor weights must equal 1.00 (100%)", 1.0, totalWeight, 0.001);

        // Zero active assignments must give 100 workload score
        MatchExplanation workloadFactor = summary.getBreakdown().get(1);
        Assert.assertEquals("0 active assignments must score 100", 100, workloadFactor.getScore());

        // Four-wheeler must give 100 vehicle score
        MatchExplanation vehicleFactor = summary.getBreakdown().get(3);
        Assert.assertEquals("Four-wheeler / CAR must score 100", 100, vehicleFactor.getScore());
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  RUNNING EXPLAINABLE MATCHING ENGINE TEST SUITE");
        System.out.println("=================================================");

        ExplainableMatchingTest test = new ExplainableMatchingTest();
        int passed = 0;
        int total = 4;

        try {
            System.out.print("[1/4] Testing Matching Factor Weights Sum to 100%... ");
            test.testSurplusToNgoFactorWeightsSumToUnity();
            System.out.println("PASSED");
            passed++;

            System.out.print("[2/4] Testing Strict Category & Dietary Derivations... ");
            test.testStrictCategoryAndDietaryDerivation();
            System.out.println("PASSED");
            passed++;

            System.out.print("[3/4] Testing Proportional Quantity Ratio Calculations... ");
            test.testQuantityRatioCalculation();
            System.out.println("PASSED");
            passed++;

            System.out.print("[4/4] Testing Volunteer Suitability Factor Breakdown... ");
            test.testVolunteerMatchingFactorBreakdown();
            System.out.println("PASSED");
            passed++;

            System.out.println("-------------------------------------------------");
            System.out.println("EXPLAINABLE MATCHING TEST SUITE: " + passed + "/" + total + " PASSED (100% SUCCESS)");
            System.out.println("=================================================\n");
        } catch (Throwable t) {
            System.err.println("FAILED: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }
}
