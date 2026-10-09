<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="journey-timeline" role="region" aria-label="Rescue Journey Lifecycle Tracker">
    <div class="timeline-step ${statusOrder >= 1 ? (statusOrder == 1 ? 'active' : 'completed') : ''}" onclick="inspectJourneyStep(1)" title="Click to view Stage 1 details">
        <span class="step-emoji">&#x1F372;</span>
        <div class="step-circle">${statusOrder > 1 ? '✓' : '1'}</div>
        <div class="step-label">1. Donated</div>
    </div>
    <div class="timeline-step ${statusOrder >= 2 ? (statusOrder == 2 ? 'active' : 'completed') : ''}" onclick="inspectJourneyStep(2)" title="Click to view Stage 2 details">
        <span class="step-emoji">&#x1F4E9;</span>
        <div class="step-circle">${statusOrder > 2 ? '✓' : '2'}</div>
        <div class="step-label">2. Requested</div>
    </div>
    <div class="timeline-step ${statusOrder >= 3 ? (statusOrder == 3 ? 'active' : 'completed') : ''}" onclick="inspectJourneyStep(3)" title="Click to view Stage 3 details">
        <span class="step-emoji">&#x1F91D;</span>
        <div class="step-circle">${statusOrder > 3 ? '✓' : '3'}</div>
        <div class="step-label">3. Approved</div>
    </div>
    <div class="timeline-step ${statusOrder >= 4 ? (statusOrder == 4 ? 'active' : 'completed') : ''}" onclick="inspectJourneyStep(4)" title="Click to view Stage 4 details">
        <span class="step-emoji">&#x1F6B4;</span>
        <div class="step-circle">${statusOrder > 4 ? '✓' : '4'}</div>
        <div class="step-label">4. Assigned</div>
    </div>
    <div class="timeline-step ${statusOrder >= 5 ? (statusOrder == 5 ? 'active' : 'completed') : ''}" onclick="inspectJourneyStep(5)" title="Click to view Stage 5 details">
        <span class="step-emoji">&#x1F4E6;</span>
        <div class="step-circle">${statusOrder > 5 ? '✓' : '5'}</div>
        <div class="step-label">5. Picked Up</div>
    </div>
    <div class="timeline-step ${statusOrder >= 6 ? (statusOrder == 6 ? 'active' : 'completed') : ''}" onclick="inspectJourneyStep(6)" title="Click to view Stage 6 details">
        <span class="step-emoji">&#x1F69A;</span>
        <div class="step-circle">${statusOrder > 6 ? '✓' : '6'}</div>
        <div class="step-label">6. In Transit</div>
    </div>
    <div class="timeline-step ${statusOrder >= 7 ? (statusOrder == 7 ? 'active' : 'completed') : ''}" onclick="inspectJourneyStep(7)" title="Click to view Stage 7 details">
        <span class="step-emoji">&#x1F3C1;</span>
        <div class="step-circle">${statusOrder > 7 ? '✓' : '7'}</div>
        <div class="step-label">7. Delivered</div>
    </div>
    <div class="timeline-step ${statusOrder >= 8 ? (statusOrder == 8 ? 'active' : 'completed') : ''}" onclick="inspectJourneyStep(8)" title="Click to view Stage 8 details">
        <span class="step-emoji">&#x1F37D;&#xFE0F;</span>
        <div class="step-circle">${statusOrder > 8 ? '✓' : '8'}</div>
        <div class="step-label">8. Distributed</div>
    </div>
</div>

<!-- Interactive Step Inspector Card -->
<div id="journeyStepInspector" class="journey-inspector-panel">
    <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.25rem; border-bottom: 1px solid #e2e8f0; padding-bottom: 1.15rem;">
        <div style="display: flex; align-items: center; gap: 0.85rem;">
            <div id="inspStepIcon" style="width: 48px; height: 48px; border-radius: 14px; background: rgba(23, 107, 77, 0.1); color: var(--primary); display: flex; align-items: center; justify-content: center; font-size: 1.6rem;">
                📦
            </div>
            <div>
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <span id="inspStepNumber" style="font-size: 0.78rem; font-weight: 800; text-transform: uppercase; color: var(--text-muted); letter-spacing: 0.5px;">Phase 5 of 8</span>
                    <span id="inspStatusBadge" class="badge badge-fresh">✓ Verified Milestone</span>
                </div>
                <h3 id="inspStepTitle" style="margin: 0.2rem 0 0 0; font-size: 1.25rem; color: var(--text-main);">5. Physical Pickup &amp; Custody Transfer</h3>
            </div>
        </div>
        <div style="display: flex; gap: 0.5rem;">
            <button type="button" class="btn btn-outline btn-sm" onclick="navJourneyStep(-1)">&larr; Previous Step</button>
            <button type="button" class="btn btn-outline btn-sm" onclick="navJourneyStep(1)">Next Step &rarr;</button>
        </div>
    </div>

    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1.25rem;">
        <div style="background: #f8fafc; padding: 1.15rem; border-radius: 12px; border: 1px solid #e2e8f0;">
            <span style="font-size: 0.75rem; font-weight: 800; text-transform: uppercase; color: var(--text-muted); letter-spacing: 0.5px; display: block; margin-bottom: 0.4rem;">
                Lifecycle Action
            </span>
            <p id="inspActionDesc" style="margin: 0; font-size: 0.88rem; color: #334155; line-height: 1.5;">
                Volunteer arrives at donor kitchen. Donor verifies courier and reveals secret 6-digit pickup OTP. Custody transfers atomically in system.
            </p>
        </div>
        <div style="background: #f8fafc; padding: 1.15rem; border-radius: 12px; border: 1px solid #e2e8f0;">
            <span style="font-size: 0.75rem; font-weight: 800; text-transform: uppercase; color: var(--text-muted); letter-spacing: 0.5px; display: block; margin-bottom: 0.4rem;">
                Security &amp; SLA Guarantee
            </span>
            <p id="inspSecurityDesc" style="margin: 0; font-size: 0.88rem; color: var(--primary); font-weight: 600; line-height: 1.5;">
                🔐 Cryptographic Dual-OTP Authentication with SHA-256 HMAC validation. Prevents unauthorized claims and guarantees zero physical loss.
            </p>
        </div>
        <div style="background: #f8fafc; padding: 1.15rem; border-radius: 12px; border: 1px solid #e2e8f0;">
            <span style="font-size: 0.75rem; font-weight: 800; text-transform: uppercase; color: var(--text-muted); letter-spacing: 0.5px; display: block; margin-bottom: 0.4rem;">
                Responsible Actors
            </span>
            <div id="inspActorDesc" style="font-size: 0.92rem; font-weight: 700; color: var(--text-main); margin-top: 0.2rem;">
                🏨 Donor Kitchen &bull; 🚴 Volunteer Courier
            </div>
            <div id="inspNextTransition" style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.4rem;">
                Next State: <strong>IN_TRANSIT</strong> &rarr; Transit Countdown
            </div>
        </div>
    </div>
</div>

<script>
var journeyPhases = [
    {
        num: "Phase 1 of 8", icon: "\uD83C\uDF72", title: "1. Food Surplus Donated",
        badge: "Created & Verified",
        action: "Commercial kitchen, restaurant, or hotel logs surplus quantity, dietary tags, storage condition, and pickup window.",
        security: "\u23F1 Dynamic urgency countdown initialized. Category and dietary parameters locked into immutable audit trail.",
        actors: "\uD83C\uDFE8 Food Donor Kitchen", nextState: "Next State: MATCHED / REQUESTED"
    },
    {
        num: "Phase 2 of 8", icon: "\uD83D\uDCE9", title: "2. Surplus Claimed / Requested",
        badge: "Demand Matched",
        action: "Verified NGO community shelter claims donation matching its registered dietary rules (Halal, Veg, Kosher) and capacity.",
        security: "\uD83D\uDCCD Geospatial radius validation ensures shelter is within viable thermal transit range (<15 km).",
        actors: "\uD83C\uDFE1 Verified Beneficiary NGO", nextState: "Next State: APPROVED"
    },
    {
        num: "Phase 3 of 8", icon: "\uD83E\uDD1D", title: "3. Reservation Approved",
        badge: "Confirmed & Locked",
        action: "Surplus reservation confirmed. Quantity atomically deducted from listing inventory with optimistic locking version check.",
        security: "\uD83D\uDD12 Atomic database decrement guarantees zero double-booking or inventory collisions.",
        actors: "\uD83E\uDD16 Automated Dispatch Engine", nextState: "Next State: ASSIGNED"
    },
    {
        num: "Phase 4 of 8", icon: "\uD83D\uDEB4", title: "4. Courier Mission Assigned",
        badge: "Courier Dispatched",
        action: "Proximity dispatch assigns nearest standby volunteer courier with vehicle capacity matching food volume.",
        security: "\u26A1 15-Minute SLA response window. Automatic self-pickup fallback activated if courier is delayed.",
        actors: "\uD83D\uDEB4 Standby Volunteer Courier", nextState: "Next State: PICKED_UP"
    },
    {
        num: "Phase 5 of 8", icon: "\uD83D\uDCE6", title: "5. Physical Pickup & Custody Handover",
        badge: "OTP Verified",
        action: "Volunteer arrives at donor kitchen. Donor verifies courier identity and reveals secret 6-digit pickup OTP.",
        security: "\uD83D\uDD10 Cryptographic Dual-OTP Authentication with SHA-256 HMAC validation. Prevents unauthorized claims.",
        actors: "\uD83C\uDFE8 Donor Kitchen \u2022 \uD83D\uDEB4 Volunteer Courier", nextState: "Next State: IN_TRANSIT"
    },
    {
        num: "Phase 6 of 8", icon: "\uD83D\uDE9A", title: "6. Transit & Real-Time Monitoring",
        badge: "Active In-Transit",
        action: "Volunteer transports food using insulated thermal containers along optimized geodesic route.",
        security: "\uD83D\uDEE1 Live state machine monitoring detects transit bottlenecks and notifies destination shelter.",
        actors: "\uD83D\uDEB4 Volunteer Courier in Transit", nextState: "Next State: DELIVERED"
    },
    {
        num: "Phase 7 of 8", icon: "\uD83C\uDFC1", title: "7. Delivery at Recipient Shelter",
        badge: "Arrival Confirmed",
        action: "Volunteer arrives at NGO shelter. NGO representative inspects freshness and enters 6-digit delivery confirmation OTP.",
        security: "\u2713 Dual-party cryptographic handshake seals chain of custody and records immutable timestamp.",
        actors: "\uD83C\uDFE1 NGO Shelter Staff \u2022 \uD83D\uDEB4 Volunteer Courier", nextState: "Next State: DISTRIBUTED"
    },
    {
        num: "Phase 8 of 8", icon: "\uD83C\uDF7D\uFE0F", title: "8. Distributed & Permanent Passport",
        badge: "Audit Complete",
        action: "Food safely distributed to hungry families. Tamper-evident Digital Rescue Receipt and Food Passport published.",
        security: "\uD83C\uDF0D Certified SHA-256 Digital Certificate. Meal equivalence and CO\u2082 landfill diversion verified.",
        actors: "\uD83C\uDFE1 Community Beneficiaries", nextState: "Final State: COMPLETED & ARCHIVED"
    }
];

var currentJourneyStep = 5;

function inspectJourneyStep(stepIdx) {
    if (stepIdx < 1 || stepIdx > 8) return;
    currentJourneyStep = stepIdx;
    
    var allSteps = document.querySelectorAll('.journey-timeline .timeline-step');
    allSteps.forEach(function(el, i) {
        if (i + 1 === stepIdx) {
            el.classList.add('selected');
        } else {
            el.classList.remove('selected');
        }
    });

    var p = journeyPhases[stepIdx - 1];
    var iconEl = document.getElementById('inspStepIcon');
    if (iconEl) iconEl.textContent = p.icon;
    var numEl = document.getElementById('inspStepNumber');
    if (numEl) numEl.textContent = p.num;
    var badgeEl = document.getElementById('inspStatusBadge');
    if (badgeEl) badgeEl.textContent = "✓ " + p.badge;
    var titleEl = document.getElementById('inspStepTitle');
    if (titleEl) titleEl.textContent = p.title;
    var actEl = document.getElementById('inspActionDesc');
    if (actEl) actEl.textContent = p.action;
    var secEl = document.getElementById('inspSecurityDesc');
    if (secEl) secEl.textContent = p.security;
    var actorEl = document.getElementById('inspActorDesc');
    if (actorEl) actorEl.textContent = p.actors;
    var nextEl = document.getElementById('inspNextTransition');
    if (nextEl) nextEl.textContent = p.nextState;
}

function navJourneyStep(delta) {
    var next = currentJourneyStep + delta;
    if (next < 1) next = 8;
    if (next > 8) next = 1;
    inspectJourneyStep(next);
}

document.addEventListener('DOMContentLoaded', function() {
    inspectJourneyStep(5);
});
</script>
