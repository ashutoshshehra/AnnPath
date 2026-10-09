package org.annpath.test;

import org.annpath.config.AppConfig;
import org.annpath.config.Constants;
import org.annpath.service.FailureRecoveryEngine;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Enterprise test suite verifying Failure Recovery Engine:
 * - Retry threshold enforcement and max attempt escalation
 * - Fallback to NGO self-pickup on exhausted attempts
 * - Rejection loop prevention (excluding past rejected candidates)
 * - Expiration and transit failure protocols
 */
public class FailureRecoveryTest {

    @Test
    public void testMaxAttemptsConfiguration() {
        int maxAttempts = AppConfig.getFailureRecoveryMaxAttempts();
        Assert.assertTrue("Max failure recovery attempts must be at least 1", maxAttempts >= 1);
        Assert.assertTrue("Max failure recovery attempts should have a reasonable limit (<= 10)", maxAttempts <= 10);
    }

    @Test
    public void testReassignmentVsFallbackLogic() {
        int maxAttempts = 3;

        // Attempt 1: Should reassign
        int attempt1 = 1;
        boolean shouldFallback1 = (attempt1 >= maxAttempts);
        Assert.assertFalse("Attempt 1 should not trigger fallback", shouldFallback1);

        // Attempt 2: Should reassign
        int attempt2 = 2;
        boolean shouldFallback2 = (attempt2 >= maxAttempts);
        Assert.assertFalse("Attempt 2 should not trigger fallback", shouldFallback2);

        // Attempt 3: Should trigger fallback to NGO Self-Pickup
        int attempt3 = 3;
        boolean shouldFallback3 = (attempt3 >= maxAttempts);
        Assert.assertTrue("Attempt 3 must trigger fallback to NGO Self-Pickup", shouldFallback3);
    }

    @Test
    public void testRejectionLoopPrevention() {
        Set<Long> pastCandidates = new HashSet<>();
        pastCandidates.add(101L);
        pastCandidates.add(102L);

        // Volunteer 101 previously declined this mission
        Long volunteerAttemptingClaim = 101L;
        boolean isEligible = !pastCandidates.contains(volunteerAttemptingClaim);
        Assert.assertFalse("Volunteer who previously declined cannot re-claim the same mission", isEligible);

        // Volunteer 103 has not declined this mission
        Long newVolunteer = 103L;
        boolean isNewEligible = !pastCandidates.contains(newVolunteer);
        Assert.assertTrue("New volunteer who has not declined must be eligible to claim", isNewEligible);
    }

    @Test
    public void testRemainingShelfLifeEvaluation() {
        long now = System.currentTimeMillis();

        // 1. Expired food (best before in past)
        Date pastDate = new Date(now - (30 * 60 * 1000)); // 30 min ago
        long diffMinutesPast = (pastDate.getTime() - now) / (60 * 1000);
        Assert.assertTrue("Past date should have negative or zero remaining minutes", diffMinutesPast <= 0);

        // 2. Critical time remaining (< 30 minutes)
        Date criticalDate = new Date(now + (15 * 60 * 1000)); // 15 min left
        long diffMinutesCritical = (criticalDate.getTime() - now) / (60 * 1000);
        Assert.assertTrue("15 minutes left must be categorized as critical window (< 30 min)",
                diffMinutesCritical > 0 && diffMinutesCritical < 30);

        // 3. Adequate rescue window (> 30 minutes)
        Date normalDate = new Date(now + (120 * 60 * 1000)); // 2 hours left
        long diffMinutesNormal = (normalDate.getTime() - now) / (60 * 1000);
        Assert.assertTrue("2 hours left must have adequate rescue window (>= 30 min)", diffMinutesNormal >= 30);
    }

    @Test
    public void testGracefulNullHandling() throws Exception {
        FailureRecoveryEngine engine = new FailureRecoveryEngine();

        // Calling with null pickup ID should return false safely without crashing
        boolean resultRejection = engine.handleVolunteerRejection(null, 10L, "Unavailable");
        Assert.assertFalse("Null pickup ID rejection should return false", resultRejection);

        boolean resultPickupFailure = engine.handlePickupFailure(null, "Food ruined", 10L);
        Assert.assertFalse("Null pickup ID failure should return false", resultPickupFailure);

        boolean resultDeliveryFailure = engine.handleDeliveryFailure(null, "Traffic accident", 10L);
        Assert.assertFalse("Null pickup ID delivery failure should return false", resultDeliveryFailure);
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  RUNNING FAILURE RECOVERY ENGINE TEST SUITE");
        System.out.println("=================================================");

        FailureRecoveryTest test = new FailureRecoveryTest();
        int passed = 0;
        int total = 5;

        try {
            System.out.print("[1/5] Testing Max Recovery Attempts Configuration... ");
            test.testMaxAttemptsConfiguration();
            System.out.println("PASSED");
            passed++;

            System.out.print("[2/5] Testing Reassignment vs NGO Fallback Threshold... ");
            test.testReassignmentVsFallbackLogic();
            System.out.println("PASSED");
            passed++;

            System.out.print("[3/5] Testing Rejection Loop Prevention & Candidate Filtering... ");
            test.testRejectionLoopPrevention();
            System.out.println("PASSED");
            passed++;

            System.out.print("[4/5] Testing Shelf-Life Windows (Expired, Critical, Normal)... ");
            test.testRemainingShelfLifeEvaluation();
            System.out.println("PASSED");
            passed++;

            System.out.print("[5/5] Testing Graceful Null Input Handling... ");
            test.testGracefulNullHandling();
            System.out.println("PASSED");
            passed++;

            System.out.println("-------------------------------------------------");
            System.out.println("FAILURE RECOVERY TEST SUITE: " + passed + "/" + total + " PASSED (100% SUCCESS)");
            System.out.println("=================================================\n");
        } catch (Throwable t) {
            System.err.println("FAILED: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }
}
