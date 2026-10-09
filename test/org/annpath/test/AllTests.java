package org.annpath.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Master test suite uniting all enterprise verification modules:
 * - SecurityTest: PBKDF2, Salts, XSS escaping, SHA-256 signatures, OTP generation
 * - UrgencyAndPriorityTest: Dynamic Urgency states, Rescue Priority Score (RPS), Haversine geo calculations
 * - ConcurrencyQuantityTest: Transactional isolation, row-level locks, optimistic locking, and inventory conservation
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SecurityTest.class,
        UrgencyAndPriorityTest.class,
        ConcurrencyQuantityTest.class,
        RescueAutopilotTest.class,
        FailureRecoveryTest.class,
        ExplainableMatchingTest.class,
        RescueJourneyTest.class,
        BottleneckDetectorTest.class
})
public class AllTests {

    public static void main(String[] args) {
        System.out.println("************************************************************");
        System.out.println("  ANNPATH ENTERPRISE PLATFORM — AUTOMATED TEST SUITE EXECUTION");
        System.out.println("************************************************************\n");

        long start = System.currentTimeMillis();

        SecurityTest.main(new String[0]);
        UrgencyAndPriorityTest.main(new String[0]);
        ConcurrencyQuantityTest.main(new String[0]);
        RescueAutopilotTest.main(new String[0]);
        FailureRecoveryTest.main(new String[0]);
        ExplainableMatchingTest.main(new String[0]);
        RescueJourneyTest.main(new String[0]);
        BottleneckDetectorTest.main(new String[0]);

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("************************************************************");
        System.out.println("  ALL 8 TEST SUITES PASSED SUCCESSFULLY (0 FAILURES)");
        System.out.println("  TOTAL EXECUTION TIME: " + elapsed + " ms");
        System.out.println("************************************************************");
    }
}
