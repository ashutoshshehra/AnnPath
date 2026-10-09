package org.annpath.test;

import org.annpath.config.Constants;
import org.annpath.exception.InvalidStateTransitionException;
import org.annpath.service.RescueJourneyService;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Timestamp;

/**
 * Enterprise test suite verifying Food Journey Passport & State Safety:
 * - Rescue ID code format (ANP-YYYY-XXXXXX)
 * - Strict forward state transitions
 * - Failure recovery transitions (PICKUP_FAILED, DELIVERY_FAILED)
 * - Illegal state leap rejection
 * - Idempotent state transitions
 */
public class RescueJourneyTest {

    @Test
    public void testRescueIdCodeGeneration() {
        Timestamp ts2026 = Timestamp.valueOf("2026-05-18 10:30:00");
        String code1 = RescueJourneyService.generateRescueCode(42L, ts2026);
        Assert.assertEquals("Rescue code must follow ANP-YYYY-XXXXXX format", "ANP-2026-000042", code1);

        String code2 = RescueJourneyService.generateRescueCode(10594L, ts2026);
        Assert.assertEquals("ANP-2026-010594", code2);

        // Null timestamp fallback to current year
        String codeCurrent = RescueJourneyService.generateRescueCode(1L, null);
        Assert.assertNotNull(codeCurrent);
        Assert.assertTrue("Rescue code must start with ANP-", codeCurrent.startsWith("ANP-"));
        Assert.assertTrue("Rescue code must end with 6-digit padded listing ID", codeCurrent.endsWith("-000001"));
    }

    @Test
    public void testValidLinearTransitions() {
        RescueJourneyService journeyService = new RescueJourneyService();

        try {
            // Full happy path lifecycle
            journeyService.validateTransition("NONE", Constants.STATE_DONATED);
            journeyService.validateTransition(Constants.STATE_DONATED, Constants.STATE_MATCHED);
            journeyService.validateTransition(Constants.STATE_MATCHED, Constants.STATE_REQUESTED);
            journeyService.validateTransition(Constants.STATE_REQUESTED, Constants.STATE_APPROVED);
            journeyService.validateTransition(Constants.STATE_APPROVED, Constants.STATE_VOLUNTEER_ASSIGNED);
            journeyService.validateTransition(Constants.STATE_VOLUNTEER_ASSIGNED, Constants.STATE_PICKED_UP);
            journeyService.validateTransition(Constants.STATE_PICKED_UP, Constants.STATE_DELIVERED);
            journeyService.validateTransition(Constants.STATE_DELIVERED, Constants.STATE_RECEIVED);
            journeyService.validateTransition(Constants.STATE_RECEIVED, Constants.STATE_DISTRIBUTED);
        } catch (InvalidStateTransitionException e) {
            Assert.fail("Valid linear transitions must not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testValidFailureRecoveryTransitions() {
        RescueJourneyService journeyService = new RescueJourneyService();

        try {
            // Volunteer transit failure -> PICKUP_FAILED
            journeyService.validateTransition(Constants.STATE_VOLUNTEER_ASSIGNED, Constants.PICKUP_STATUS_PICKUP_FAILED);

            // PICKUP_FAILED -> Reassignment
            journeyService.validateTransition(Constants.PICKUP_STATUS_PICKUP_FAILED, Constants.PICKUP_STATUS_REASSIGNING);

            // PICKUP_FAILED -> NGO Self-Pickup
            journeyService.validateTransition(Constants.PICKUP_STATUS_PICKUP_FAILED, Constants.STATE_NGO_SELF_PICKUP_ASSIGNED);

            // PICKUP_FAILED -> Expired
            journeyService.validateTransition(Constants.PICKUP_STATUS_PICKUP_FAILED, Constants.STATE_EXPIRED);

            // Transit failure -> DELIVERY_FAILED
            journeyService.validateTransition(Constants.STATE_PICKED_UP, Constants.PICKUP_STATUS_DELIVERY_FAILED);

            // DELIVERY_FAILED -> Delivered or Expired
            journeyService.validateTransition(Constants.PICKUP_STATUS_DELIVERY_FAILED, Constants.STATE_DELIVERED);
            journeyService.validateTransition(Constants.PICKUP_STATUS_DELIVERY_FAILED, Constants.STATE_EXPIRED);
        } catch (InvalidStateTransitionException e) {
            Assert.fail("Failure recovery transitions must be legally registered: " + e.getMessage());
        }
    }

    @Test
    public void testIllegalStateLeapsRejected() {
        RescueJourneyService journeyService = new RescueJourneyService();

        // 1. Skip from DONATED to DELIVERED
        try {
            journeyService.validateTransition(Constants.STATE_DONATED, Constants.STATE_DELIVERED);
            Assert.fail("Direct leap from DONATED to DELIVERED must be rejected");
        } catch (InvalidStateTransitionException e) {
            Assert.assertEquals(Constants.STATE_DONATED, e.getFromState());
            Assert.assertEquals(Constants.STATE_DELIVERED, e.getToState());
        }

        // 2. Backward step from PICKED_UP to APPROVED
        try {
            journeyService.validateTransition(Constants.STATE_PICKED_UP, Constants.STATE_APPROVED);
            Assert.fail("Backward leap from PICKED_UP to APPROVED must be rejected");
        } catch (InvalidStateTransitionException e) {
            Assert.assertEquals(Constants.STATE_PICKED_UP, e.getFromState());
            Assert.assertEquals(Constants.STATE_APPROVED, e.getToState());
        }

        // 3. Post-terminal transition from DISTRIBUTED to PICKED_UP
        try {
            journeyService.validateTransition(Constants.STATE_DISTRIBUTED, Constants.STATE_PICKED_UP);
            Assert.fail("Transition out of terminal DISTRIBUTED must be rejected");
        } catch (InvalidStateTransitionException e) {
            Assert.assertEquals(Constants.STATE_DISTRIBUTED, e.getFromState());
        }
    }

    @Test
    public void testIdempotentStateTransitions() {
        RescueJourneyService journeyService = new RescueJourneyService();

        try {
            // Transition to identical state should be a safe no-op
            journeyService.validateTransition(Constants.STATE_DONATED, Constants.STATE_DONATED);
            journeyService.validateTransition(Constants.STATE_PICKED_UP, Constants.STATE_PICKED_UP);
            journeyService.validateTransition(Constants.STATE_DELIVERED, Constants.STATE_DELIVERED);
            journeyService.validateTransition(Constants.STATE_DISTRIBUTED, Constants.STATE_DISTRIBUTED);
        } catch (InvalidStateTransitionException e) {
            Assert.fail("Idempotent state transitions must succeed safely without throwing: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  RUNNING RESCUE JOURNEY & PASSPORT TEST SUITE");
        System.out.println("=================================================");

        RescueJourneyTest test = new RescueJourneyTest();
        int passed = 0;
        int total = 5;

        try {
            System.out.print("[1/5] Testing Rescue ID Code Formatting (ANP-YYYY-XXXXXX)... ");
            test.testRescueIdCodeGeneration();
            System.out.println("PASSED");
            passed++;

            System.out.print("[2/5] Testing Valid Linear Lifecycle Transitions... ");
            test.testValidLinearTransitions();
            System.out.println("PASSED");
            passed++;

            System.out.print("[3/5] Testing Failure Recovery State Transitions... ");
            test.testValidFailureRecoveryTransitions();
            System.out.println("PASSED");
            passed++;

            System.out.print("[4/5] Testing Illegal State Leaps & Reversals Rejection... ");
            test.testIllegalStateLeapsRejected();
            System.out.println("PASSED");
            passed++;

            System.out.print("[5/5] Testing Idempotent Transition Safety... ");
            test.testIdempotentStateTransitions();
            System.out.println("PASSED");
            passed++;

            System.out.println("-------------------------------------------------");
            System.out.println("RESCUE JOURNEY TEST SUITE: " + passed + "/" + total + " PASSED (100% SUCCESS)");
            System.out.println("=================================================\n");
        } catch (Throwable t) {
            System.err.println("FAILED: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }
}
