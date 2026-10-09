package org.annpath.config;

/**
 * Platform-wide constants and state values for AnnPath.
 */
public final class Constants {

    private Constants() {}

    // Roles (Exactly three roles, no Admin)
    public static final String ROLE_DONOR = "DONOR";
    public static final String ROLE_NGO = "NGO";
    public static final String ROLE_VOLUNTEER = "VOLUNTEER";

    // User Statuses
    public static final String USER_STATUS_ACTIVE = "ACTIVE";
    public static final String USER_STATUS_SUSPENDED = "SUSPENDED";
    public static final String USER_STATUS_PENDING = "PENDING_VERIFICATION";

    // Food Journey / Listing States
    public static final String STATE_DONATED = "DONATED";
    public static final String STATE_MATCHED = "MATCHED";
    public static final String STATE_REQUESTED = "REQUESTED";
    public static final String STATE_APPROVED = "APPROVED";
    public static final String STATE_VOLUNTEER_ASSIGNED = "VOLUNTEER_ASSIGNED";
    public static final String STATE_VOLUNTEER_UNAVAILABLE = "VOLUNTEER_UNAVAILABLE";
    public static final String STATE_NGO_SELF_PICKUP_ASSIGNED = "NGO_SELF_PICKUP_ASSIGNED";
    public static final String STATE_PICKED_UP = "PICKED_UP";
    public static final String STATE_DELIVERED = "DELIVERED";
    public static final String STATE_RECEIVED = "RECEIVED";
    public static final String STATE_DISTRIBUTED = "DISTRIBUTED";
    public static final String STATE_REJECTED = "REJECTED";
    public static final String STATE_CANCELLED = "CANCELLED";
    public static final String STATE_EXPIRED = "EXPIRED";

    // Urgency Statuses
    public static final String URGENCY_FRESH = "FRESH";
    public static final String URGENCY_EXPIRING_SOON = "EXPIRING_SOON";
    public static final String URGENCY_CRITICAL = "CRITICAL";
    public static final String URGENCY_EXPIRED = "EXPIRED";

    // NGO Verification Statuses
    public static final String NGO_VERIFY_PENDING = "PENDING_VERIFICATION";
    public static final String NGO_VERIFY_VERIFIED = "VERIFIED";
    public static final String NGO_VERIFY_REJECTED = "REJECTED";
    public static final String NGO_VERIFY_SUSPENDED = "SUSPENDED";

    // Pickup Methods
    public static final String PICKUP_METHOD_VOLUNTEER = "VOLUNTEER";
    public static final String PICKUP_METHOD_SELF = "NGO_SELF_PICKUP";

    // Pickup Operational Statuses
    public static final String PICKUP_STATUS_ASSIGNED = "ASSIGNED";
    public static final String PICKUP_STATUS_REASSIGNING = "REASSIGNING";
    public static final String PICKUP_STATUS_VOLUNTEER_UNAVAILABLE = "VOLUNTEER_UNAVAILABLE";
    public static final String PICKUP_STATUS_SELF_ASSIGNED = "NGO_SELF_PICKUP_ASSIGNED";
    public static final String PICKUP_STATUS_PICKED_UP = "PICKED_UP";
    public static final String PICKUP_STATUS_DELIVERED = "DELIVERED";
    public static final String PICKUP_STATUS_RECEIVED = "RECEIVED";
    public static final String PICKUP_STATUS_DISTRIBUTED = "DISTRIBUTED";
    public static final String PICKUP_STATUS_PICKUP_FAILED = "PICKUP_FAILED";
    public static final String PICKUP_STATUS_DELIVERY_FAILED = "DELIVERY_FAILED";
    public static final String PICKUP_STATUS_CANCELLED = "CANCELLED";

    // Volunteer Assignment Response Statuses
    public static final String VOL_ASSIGN_PENDING = "PENDING";
    public static final String VOL_ASSIGN_ACCEPTED = "ACCEPTED";
    public static final String VOL_ASSIGN_REJECTED = "REJECTED";
    public static final String VOL_ASSIGN_TIMEOUT = "TIMEOUT_EXPIRED";

    // Session Attribute Keys
    public static final String SESSION_USER = "currentUser";
    public static final String SESSION_USER_ROLE = "userRole";
    public static final String SESSION_CSRF_TOKEN = "csrfToken";
    public static final String SESSION_LOCALE = "userLocale";
    public static final String SESSION_TIMEZONE = "userTimezone";

    // Impact Calculation Coefficients
    public static final double MEAL_PORTION_KG = 0.42; // 1 standard meal = 0.42 kg
    public static final double CO2_KG_PER_KG_FOOD = 2.50; // EPA standard: 2.5 kg CO2e saved per kg food rescued
}
