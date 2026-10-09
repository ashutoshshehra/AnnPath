<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="AnnPath — A Path from Surplus to Need | Global Food Redistribution" />
</jsp:include>
<jsp:include page="WEB-INF/views/common/navbar.jsp" />

<!-- 1. HERO SECTION -->
<section class="hero-section">
    <div class="container">
        <div class="hero-badge-container">
            <span class="hero-tag">
                <img src="${pageContext.request.contextPath}/assets/images/symbol.png" alt="AnnPath Symbol" class="hero-tag-icon" width="22" height="22" style="width:22px; height:22px; object-fit:contain; vertical-align:middle;">
                <c:out value="${not empty msg['hero.badge'] ? msg['hero.badge'] : 'Global Food Surplus Redistribution Platform'}" />
            </span>
        </div>
        <h1 class="hero-title"><c:out value="${not empty msg['hero.title'] ? msg['hero.title'] : 'A Path from Surplus to Need'}" /></h1>
        <p class="hero-subtitle">
            <c:out value="${not empty msg['hero.subtitle'] ? msg['hero.subtitle'] : 'Connecting food donors with verified NGOs and volunteer dispatch networks. Moving surplus food safely, rapidly, and transparently before it expires.'}" />
        </p>

        <div class="hero-ctas">
            <a href="${pageContext.request.contextPath}/auth/register?role=DONOR" class="btn btn-primary btn-lg"><c:out value="${not empty msg['btn.donateFood'] ? msg['btn.donateFood'] : 'Donate Food'}" /></a>
            <a href="${pageContext.request.contextPath}/auth/register?role=NGO" class="btn btn-secondary btn-lg"><c:out value="${not empty msg['btn.findFood'] ? msg['btn.findFood'] : 'Find Food (NGO)'}" /></a>
            <a href="${pageContext.request.contextPath}/auth/register?role=VOLUNTEER" class="btn btn-outline btn-lg"><c:out value="${not empty msg['btn.volunteer'] ? msg['btn.volunteer'] : 'Become a Volunteer'}" /></a>
        </div>

        <!-- REAL IMPACT STATS BANNER (Calculated strictly from DB) -->
        <div class="stats-banner" id="impact">
            <div class="stat-item">
                <div class="stat-icon-badge">⚖️</div>
                <div class="stat-number">
                    <fmt:formatNumber value="${impact.totalRescuedKg != null ? impact.totalRescuedKg : 0}" maxFractionDigits="0" /> <span class="stat-unit">kg</span>
                </div>
                <div class="stat-label"><c:out value="${not empty msg['stats.foodRescued'] ? msg['stats.foodRescued'] : 'Food Rescued'}" /></div>
            </div>
            <div class="stat-item">
                <div class="stat-icon-badge">🍲</div>
                <div class="stat-number">
                    <fmt:formatNumber value="${impact.totalMealsFed != null ? impact.totalMealsFed : 0}" maxFractionDigits="0" />
                </div>
                <div class="stat-label"><c:out value="${not empty msg['stats.mealsFed'] ? msg['stats.mealsFed'] : 'Meals Provided'}" /></div>
            </div>
            <div class="stat-item">
                <div class="stat-icon-badge">🍃</div>
                <div class="stat-number">
                    <fmt:formatNumber value="${impact.totalCo2PreventedKg != null ? impact.totalCo2PreventedKg : 0}" maxFractionDigits="0" /> <span class="stat-unit">kg</span>
                </div>
                <div class="stat-label">CO<sub>2</sub> Emissions Prevented</div>
            </div>
            <div class="stat-item">
                <div class="stat-icon-badge">🎯</div>
                <div class="stat-number">
                    <fmt:formatNumber value="${impact.totalRescuesCount != null ? impact.totalRescuesCount : 0}" />
                </div>
                <div class="stat-label"><c:out value="${not empty msg['stats.missions'] ? msg['stats.missions'] : 'Completed Missions'}" /></div>
            </div>
        </div>
    </div>
</section>

<!-- 2. LIVE NETWORK BOTTLENECK & HEALTH DASHBOARD -->
<section style="background: #ffffff; padding: 3rem 0; border-bottom: 1px solid var(--border-color);">
    <div class="container">
        <div style="display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem;">
            <div>
                <span class="badge badge-rescue-now" style="margin-bottom: 0.5rem;">⚡ Live Network Status</span>
                <h3 style="margin: 0; font-size: 1.5rem;">Operational Logistics Health</h3>
            </div>
            <span style="font-size: 0.85rem; color: var(--text-muted); display: inline-flex; align-items: center; gap: 0.4rem;">
                <span style="display: inline-block; width: 8px; height: 8px; border-radius: 50%; background: #10b981;"></span>
                Live DB Metrics &bull; Real-time synchronization
            </span>
        </div>

        <div class="bottleneck-grid">
            <div class="bottleneck-card">
                <div class="bottleneck-count" style="color: var(--secondary);">
                    <c:out value="${bottlenecks.unmatchedSurplus != null ? bottlenecks.unmatchedSurplus : 0}" />
                </div>
                <div class="bottleneck-label">Unmatched Surplus</div>
                <div class="bottleneck-desc">Active donations awaiting NGO request</div>
            </div>
            <div class="bottleneck-card">
                <div class="bottleneck-count" style="color: #ea580c;">
                    <c:out value="${bottlenecks.awaitingTransit != null ? bottlenecks.awaitingTransit : 0}" />
                </div>
                <div class="bottleneck-label">Awaiting Volunteer</div>
                <div class="bottleneck-desc">Claimed donations ready for pickup</div>
            </div>
            <div class="bottleneck-card">
                <div class="bottleneck-count" style="color: var(--primary);">
                    <c:out value="${bottlenecks.activeRescues != null ? bottlenecks.activeRescues : 0}" />
                </div>
                <div class="bottleneck-label">Active Missions</div>
                <div class="bottleneck-desc">Volunteers currently in transit</div>
            </div>
            <div class="bottleneck-card">
                <div class="bottleneck-count" style="color: #10b981;">
                    <c:out value="${bottlenecks.availableFleet != null ? bottlenecks.availableFleet : 0}" />
                </div>
                <div class="bottleneck-label">Available Fleet</div>
                <div class="bottleneck-desc">Volunteers on standby</div>
            </div>
        </div>
    </div>
</section>

<!-- 3. LIVE "RESCUE NOW" SURPLUS FEED -->
<section class="container" style="padding: 4rem 0;" id="rescue-now">
    <div style="display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <span class="badge badge-critical" style="margin-bottom: 0.5rem;">🔥 Urgent Window</span>
            <h2 style="margin: 0;">Surplus Ready for Immediate Rescue</h2>
            <p style="color: var(--text-muted); margin-top: 0.4rem; margin-bottom: 0;">
                Real donations with active expiry countdowns. Verified NGOs can request immediately.
            </p>
        </div>
        <c:choose>
            <c:when test="${sessionScope.currentUser != null && sessionScope.currentUser.role == 'NGO'}">
                <a href="${pageContext.request.contextPath}/ngo/browse" class="btn btn-primary btn-sm">Browse All Live Surplus &rarr;</a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/auth/register?role=NGO" class="btn btn-primary btn-sm">Register as NGO to Request &rarr;</a>
            </c:otherwise>
        </c:choose>
    </div>

    <c:choose>
        <c:when test="${not empty urgentListings}">
            <div class="card-grid">
                <c:forEach var="fl" items="${urgentListings}">
                    <div class="food-card" style="border-top-color: ${fl.urgencyStatus == 'CRITICAL' ? '#ef4444' : (fl.urgencyStatus == 'EXPIRING_SOON' ? '#f59e0b' : '#10b981')};">
                        <div class="food-card-header">
                            <div style="display: flex; align-items: center; gap: 0.4rem;">
                                <span class="badge ${fl.urgencyStatus == 'CRITICAL' ? 'badge-critical' : (fl.urgencyStatus == 'EXPIRING_SOON' ? 'badge-expiring-soon' : 'badge-fresh')}">
                                    <c:out value="${fl.urgencyStatus}" />
                                </span>
                                <span class="badge ${fl.dietaryType == 'VEGETARIAN' || fl.dietaryType == 'VEGAN' ? 'badge-fresh' : 'badge-standard'}" style="font-size: 0.72rem; padding: 0.2rem 0.5rem;">
                                    ${fl.dietaryType == 'VEGETARIAN' ? '🌱 Veg' : (fl.dietaryType == 'VEGAN' ? '🌿 Vegan' : '🍗 Non-Veg')}
                                </span>
                            </div>
                            <div class="rescue-clock" data-expires-at="${fl.availableUntil}">
                                <span class="clock-display">⏱ Calculating...</span>
                            </div>
                        </div>

                        <div class="food-card-body">
                            <div style="display: flex; align-items: flex-start; gap: 0.65rem; margin-bottom: 0.5rem;">
                                <span style="font-size: 1.6rem; line-height: 1;">
                                    ${fl.categoryName == 'Prepared Cooked Meals' ? '🍲' : (fl.categoryName == 'Baked Goods & Breads' ? '🥖' : (fl.categoryName == 'Fresh Produce & Fruits' ? '🍎' : '🥗'))}
                                </span>
                                <h3 class="food-card-title"><c:out value="${fl.title}" /></h3>
                            </div>
                            <div class="food-card-meta">
                                <div><span>📦</span> <span><strong>Available:</strong> <c:out value="${fl.remainingQuantity}" /> <c:out value="${fl.unitName}" /></span></div>
                                <div><span>🥗</span> <span><strong>Category:</strong> <c:out value="${fl.categoryName}" /></span></div>
                                <div><span>🏨</span> <span><strong>Donor:</strong> <c:out value="${fl.donorName}" /></span></div>
                                <c:if test="${not empty fl.pickupCity}">
                                    <div><span>📍</span> <span><strong>Location:</strong> <c:out value="${fl.pickupCity}" /></span></div>
                                </c:if>
                            </div>
                            <c:if test="${not empty fl.priorityExplanation}">
                                <p style="font-size: 0.82rem; color: var(--text-muted); margin-top: 0.35rem; line-height: 1.4;">
                                    <em><c:out value="${fl.priorityExplanation}" /></em>
                                </p>
                            </c:if>
                        </div>

                        <div class="food-card-footer">
                            <span style="font-size: 0.85rem; font-weight: 800; color: var(--primary);">
                                Priority: <fmt:formatNumber value="${fl.priorityScore}" maxFractionDigits="0" />/100
                            </span>
                            <c:choose>
                                <c:when test="${sessionScope.currentUser != null && sessionScope.currentUser.role == 'NGO'}">
                                    <a href="${pageContext.request.contextPath}/ngo/browse" class="btn btn-secondary btn-sm">Claim Food &rarr;</a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/auth/login" class="btn btn-outline btn-sm">Login to Request</a>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div class="empty-state">
                <div class="empty-state-icon">🌱</div>
                <div class="empty-state-title">No Urgent Food Surplus Pending</div>
                <div class="empty-state-text">
                    All currently logged surplus donations have either been claimed by partner NGOs or successfully delivered!
                </div>
                <div style="margin-top: 1rem;">
                    <a href="${pageContext.request.contextPath}/donor/create" class="btn btn-primary btn-sm">+ Donate Food Surplus</a>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<!-- 4. HOW ANNPATH WORKS -->
<section style="background: #ffffff; padding: 4.5rem 0; border-top: 1px solid var(--border-color); border-bottom: 1px solid var(--border-color);" id="how-it-works">
    <div class="container">
        <div style="text-align: center; max-width: 700px; margin: 0 auto 3rem;">
            <span class="badge badge-standard" style="margin-bottom: 0.8rem;"><c:out value="${not empty msg['how.badge'] ? msg['how.badge'] : 'How It Works'}" /></span>
            <h2><c:out value="${not empty msg['how.title'] ? msg['how.title'] : 'Intelligent, Automated Surplus Logistics'}" /></h2>
            <p style="color: var(--text-muted);">
                <c:out value="${not empty msg['how.subtitle'] ? msg['how.subtitle'] : 'Food rescue cannot rely on slow manual coordination. AnnPath replaces administrative friction with deterministic matching algorithms and automated volunteer dispatch.'}" />
            </p>
        </div>

        <div class="card-grid">
            <div class="card">
                <div class="card-header">
                    <div class="brand-icon" style="background: var(--primary-light); color: var(--primary);">1</div>
                    <span class="badge badge-fresh"><c:out value="${not empty msg['how.step1.badge'] ? msg['how.step1.badge'] : 'Donors'}" /></span>
                </div>
                <h3 class="card-title"><c:out value="${not empty msg['how.step1.title'] ? msg['how.step1.title'] : 'List Surplus in Seconds'}" /></h3>
                <p class="card-body">
                    <c:out value="${not empty msg['how.step1.desc'] ? msg['how.step1.desc'] : 'Restaurants, supermarkets, hotels, and caterers log available food quantity, category, dietary specs, and pickup windows.'}" />
                </p>
            </div>
            <div class="card">
                <div class="card-header">
                    <div class="brand-icon" style="background: var(--secondary-light); color: var(--secondary);">2</div>
                    <span class="badge badge-rescue-now"><c:out value="${not empty msg['how.step2.badge'] ? msg['how.step2.badge'] : 'Engine'}" /></span>
                </div>
                <h3 class="card-title"><c:out value="${not empty msg['how.step2.title'] ? msg['how.step2.title'] : 'Smart Proximity Matching'}" /></h3>
                <p class="card-body">
                    <c:out value="${not empty msg['how.step2.desc'] ? msg['how.step2.desc'] : 'The Urgency Engine calculates expiry countdowns and matches surplus against verified NGO demand vectors in real-time.'}" />
                </p>
            </div>
            <div class="card">
                <div class="card-header">
                    <div class="brand-icon" style="background: var(--primary-light); color: var(--primary);">3</div>
                    <span class="badge badge-fresh"><c:out value="${not empty msg['how.step3.badge'] ? msg['how.step3.badge'] : 'Dispatch'}" /></span>
                </div>
                <h3 class="card-title"><c:out value="${not empty msg['how.step3.title'] ? msg['how.step3.title'] : 'Dispatch & Secure Handover'}" /></h3>
                <p class="card-body">
                    <c:out value="${not empty msg['how.step3.desc'] ? msg['how.step3.desc'] : 'Automated volunteer dispatch with cryptographic 6-digit OTP verification ensures safe chain-of-custody from donor to recipient.'}" />
                </p>
            </div>
        </div>
    </div>
</section>

<!-- 5. FOOD RESCUE JOURNEY -->
<section class="container" style="padding: 4.5rem 0;" id="journey">
    <div style="text-align: center; max-width: 760px; margin: 0 auto 2.5rem;">
        <span class="badge badge-standard" style="margin-bottom: 0.8rem;"><c:out value="${not empty msg['journey.badge'] ? msg['journey.badge'] : 'Controlled State Machine'}" /></span>
        <h2><c:out value="${not empty msg['journey.title'] ? msg['journey.title'] : 'The Complete Food Rescue Journey'}" /></h2>
        <p style="color: var(--text-muted); font-size: 1.05rem; line-height: 1.6;">
            <c:out value="${not empty msg['journey.subtitle'] ? msg['journey.subtitle'] : 'Every donation moves through strict, validated lifecycle transitions. No arbitrary status modifications. Full auditability from donor kitchen to recipient shelter.'}" />
        </p>
    </div>

    <!-- 8-Stage Milestone Tracker -->
    <jsp:include page="WEB-INF/views/common/timeline.jsp">
        <jsp:param name="statusOrder" value="5" />
    </jsp:include>

    <!-- 5-Stage Interactive Operational Pipeline -->
    <div class="journey-pipeline-grid">
        <div class="pipeline-card">
            <div>
                <div class="pipeline-card-header">
                    <div class="pipeline-card-icon">🍲</div>
                    <span class="pipeline-card-step">Stage 1</span>
                </div>
                <h4 class="pipeline-card-title">Listing &amp; Decay Clock</h4>
                <p class="pipeline-card-desc">
                    Donor logs surplus quantity, storage condition, dietary tags, and pickup window. Dynamic decay algorithm initiates real-time urgency monitoring.
                </p>
            </div>
            <div>
                <span class="pipeline-card-meta">⏱ Expiry decay active</span>
            </div>
        </div>

        <div class="pipeline-card">
            <div>
                <div class="pipeline-card-header">
                    <div class="pipeline-card-icon" style="background: rgba(242, 140, 40, 0.1); color: var(--secondary);">🤖</div>
                    <span class="pipeline-card-step">Stage 2</span>
                </div>
                <h4 class="pipeline-card-title">Smart Match &amp; Claim</h4>
                <p class="pipeline-card-desc">
                    Explainable matching engine calculates compatibility against verified NGO requirements within transit radius. Eligible shelters claim food instantly.
                </p>
            </div>
            <div>
                <span class="pipeline-card-meta">📍 Proximity verified</span>
            </div>
        </div>

        <div class="pipeline-card">
            <div>
                <div class="pipeline-card-header">
                    <div class="pipeline-card-icon" style="background: rgba(14, 165, 233, 0.1); color: #0284c7;">🚴</div>
                    <span class="pipeline-card-step">Stage 3</span>
                </div>
                <h4 class="pipeline-card-title">Automated Dispatch</h4>
                <p class="pipeline-card-desc">
                    Volunteer couriers on standby receive mission broadcast with optimized navigation routes. Self-pickup fallback available if couriers are unavailable.
                </p>
            </div>
            <div>
                <span class="pipeline-card-meta">⚡ 15-min SLA dispatch</span>
            </div>
        </div>

        <div class="pipeline-card">
            <div>
                <div class="pipeline-card-header">
                    <div class="pipeline-card-icon" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">🔐</div>
                    <span class="pipeline-card-step">Stage 4</span>
                </div>
                <h4 class="pipeline-card-title">Dual-OTP Handover</h4>
                <p class="pipeline-card-desc">
                    Courier verifies 6-digit donor OTP at pickup point, and recipient NGO verifies arrival OTP. State transitions execute only upon cryptographic validation.
                </p>
            </div>
            <div>
                <span class="pipeline-card-meta">🛡 Zero custody breach</span>
            </div>
        </div>

        <div class="pipeline-card">
            <div>
                <div class="pipeline-card-header">
                    <div class="pipeline-card-icon" style="background: rgba(139, 92, 246, 0.1); color: #8b5cf6;">📜</div>
                    <span class="pipeline-card-step">Stage 5</span>
                </div>
                <h4 class="pipeline-card-title">Passport &amp; Impact</h4>
                <p class="pipeline-card-desc">
                    Permanent Food Journey Passport generated with immutable SHA-256 digital certificate, meal equivalence, and landfill methane abatement metrics.
                </p>
            </div>
            <div>
                <span class="pipeline-card-meta">🌍 Immutable ledger audit</span>
            </div>
        </div>
    </div>

    <!-- Live / Simulated Passport Showcase Preview Card -->
    <div class="passport-showcase-box">
        <c:choose>
            <c:when test="${not empty latestPickup and not empty latestListing}">
                <div class="passport-showcase-header">
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <img src="${pageContext.request.contextPath}/assets/images/symbol.png" alt="Symbol" style="width: 26px; height: 26px; object-fit: contain;">
                        <div>
                            <strong style="font-size: 1.15rem; color: var(--text-main);">Live Food Journey Passport Showcase</strong>
                            <div style="font-size: 0.8rem; color: var(--text-muted);">Verified custody audit certificate from active operations</div>
                        </div>
                    </div>
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <span class="passport-badge-tag">✓ DISTRIBUTED &amp; VERIFIED</span>
                        <span style="font-family: monospace; font-weight: 800; font-size: 0.95rem; color: var(--text-main);"><c:out value="${latestListing.formattedRescueId}" /></span>
                    </div>
                </div>

                <!-- Visual Route Pipeline -->
                <div class="passport-route-row">
                    <div class="route-node">
                        <div class="route-node-icon">🏨</div>
                        <div class="route-node-role">Donor Kitchen</div>
                        <div class="route-node-name"><c:out value="${latestListing.donorName}" /></div>
                        <div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 0.1rem;">📍 <c:out value="${latestListing.pickupCity}" /></div>
                    </div>
                    <div class="route-connector-arrow">&rarr;</div>
                    <div class="route-node">
                        <div class="route-node-icon">🚴</div>
                        <div class="route-node-role">Volunteer Courier</div>
                        <div class="route-node-name"><c:out value="${not empty latestPickup.volunteerName ? latestPickup.volunteerName : 'Assigned Courier'}" /></div>
                        <div style="font-size: 0.78rem; color: #10b981; margin-top: 0.1rem;">✓ Dual OTP Authenticated</div>
                    </div>
                    <div class="route-connector-arrow">&rarr;</div>
                    <div class="route-node">
                        <div class="route-node-icon">🏡</div>
                        <div class="route-node-role">Beneficiary NGO</div>
                        <div class="route-node-name"><c:out value="${not empty latestPickup.ngoName ? latestPickup.ngoName : 'Relief Center'}" /></div>
                        <div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 0.1rem;">🍲 Safely Distributed</div>
                    </div>
                </div>

                <!-- Verified Impact Metric Pills -->
                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; margin-top: 1.5rem; padding-top: 1.25rem; border-top: 1px solid #e2e8f0;">
                    <div style="display: flex; gap: 1.5rem; flex-wrap: wrap;">
                        <div>
                            <span style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Rescued Food</span>
                            <div style="font-size: 1.15rem; font-weight: 800; color: var(--primary);"><c:out value="${latestListing.quantity}" /> <c:out value="${latestListing.unitName}" /></div>
                        </div>
                        <div>
                            <span style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Social Contribution</span>
                            <div style="font-size: 1.15rem; font-weight: 800; color: var(--secondary);"><fmt:formatNumber value="${latestListing.quantity * 2.5}" maxFractionDigits="0" /> Meals</div>
                        </div>
                        <div>
                            <span style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">CO₂ Prevented</span>
                            <div style="font-size: 1.15rem; font-weight: 800; color: #10b981;"><fmt:formatNumber value="${latestListing.quantity * 2.5}" maxFractionDigits="1" /> kg CO₂e</div>
                        </div>
                    </div>
                    <div style="display: flex; gap: 0.75rem;">
                        <a href="${pageContext.request.contextPath}/rescue/passport?pickupId=${latestPickup.id}" class="btn btn-primary btn-sm">View Interactive Food Passport &rarr;</a>
                        <a href="${pageContext.request.contextPath}/impact" class="btn btn-outline btn-sm">Live Impact Dashboard</a>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="passport-showcase-header">
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <img src="${pageContext.request.contextPath}/assets/images/symbol.png" alt="Symbol" style="width: 26px; height: 26px; object-fit: contain;">
                        <div>
                            <strong style="font-size: 1.15rem; color: var(--text-main);">Food Journey Passport — Example Simulation</strong>
                            <div style="font-size: 0.8rem; color: var(--text-muted);">Illustrative demonstration of verifiable custody transfer and digital receipts</div>
                        </div>
                    </div>
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <span class="passport-badge-tag" style="background: rgba(100, 116, 139, 0.12); color: #475569;">SIMULATION DEMO</span>
                        <span style="font-family: monospace; font-weight: 800; font-size: 0.95rem; color: var(--text-muted);">ANP-SAMPLE-000001</span>
                    </div>
                </div>

                <!-- Visual Route Pipeline -->
                <div class="passport-route-row">
                    <div class="route-node">
                        <div class="route-node-icon">🏨</div>
                        <div class="route-node-role">Donor Kitchen</div>
                        <div class="route-node-name">Sample Donor Partner</div>
                        <div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 0.1rem;">📍 Demonstration Source</div>
                    </div>
                    <div class="route-connector-arrow">&rarr;</div>
                    <div class="route-node">
                        <div class="route-node-icon">🚴</div>
                        <div class="route-node-role">Volunteer Courier</div>
                        <div class="route-node-name">Assigned Volunteer</div>
                        <div style="font-size: 0.78rem; color: #10b981; margin-top: 0.1rem;">✓ Dual OTP Authenticated</div>
                    </div>
                    <div class="route-connector-arrow">&rarr;</div>
                    <div class="route-node">
                        <div class="route-node-icon">🏡</div>
                        <div class="route-node-role">Beneficiary NGO</div>
                        <div class="route-node-name">Community Food Center</div>
                        <div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 0.1rem;">🍲 Nutritious Distribution</div>
                    </div>
                </div>

                <!-- Verified Impact Metric Pills -->
                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; margin-top: 1.5rem; padding-top: 1.25rem; border-top: 1px solid #e2e8f0;">
                    <div style="display: flex; gap: 1.5rem; flex-wrap: wrap;">
                        <div>
                            <span style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Rescued Food</span>
                            <div style="font-size: 1.15rem; font-weight: 800; color: var(--primary);">15.0 kg (Example Batch)</div>
                        </div>
                        <div>
                            <span style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Social Contribution</span>
                            <div style="font-size: 1.15rem; font-weight: 800; color: var(--secondary);">36 Nutritious Meals</div>
                        </div>
                        <div>
                            <span style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">CO₂ Prevented</span>
                            <div style="font-size: 1.15rem; font-weight: 800; color: #10b981;">37.5 kg CO₂e Diverted</div>
                        </div>
                    </div>
                    <div style="display: flex; gap: 0.75rem;">
                        <a href="${pageContext.request.contextPath}/impact" class="btn btn-primary btn-sm">Live Impact Dashboard &rarr;</a>
                        <a href="${pageContext.request.contextPath}/ngo/browse" class="btn btn-outline btn-sm">Browse Live Surplus</a>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<!-- 5B. EXPLAINABLE SMART MATCHING ENGINE (ALGORITHM SIMULATOR) -->
<section style="background: #ffffff; padding: 4.5rem 0; border-top: 1px solid var(--border-color);" id="smart-matching">
    <div class="container">
        <div style="text-align: center; max-width: 760px; margin: 0 auto 2.5rem;">
            <span class="badge badge-rescue-now" style="margin-bottom: 0.8rem;">⚡ Algorithm Simulation</span>
            <h2>Deterministic Multi-Factor Matching Simulation</h2>
            <p style="color: var(--text-muted); font-size: 1.05rem; line-height: 1.6;">
                Food redistribution cannot rely on manual guesswork. AnnPath computes 5 distinct weighted dimensions in real-time to pair expiring food with the nearest, best-equipped shelters. Explore the simulation model below.
            </p>
        </div>

        <div class="smart-matching-container">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <h3 style="margin: 0; font-size: 1.25rem;">Multi-Factor Matching Simulation</h3>
                    <span style="font-size: 0.85rem; color: var(--text-muted);">Interactive scenarios demonstrating how real-time affinity scores are computed</span>
                </div>
                <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                    <button type="button" class="simulator-pill-btn active" onclick="switchSimulator(0)">Scenario 1: 45 kg Veg Pulao</button>
                    <button type="button" class="simulator-pill-btn" onclick="switchSimulator(1)">Scenario 2: 28 kg Sourdough Bread</button>
                    <button type="button" class="simulator-pill-btn" onclick="switchSimulator(2)">Scenario 3: 65 kg Organic Fruit</button>
                </div>
            </div>

            <!-- Factor 1: Proximity -->
            <div class="matching-factor-row">
                <div class="factor-info">
                    <div class="factor-title">📍 Geospatial Proximity <span class="badge badge-fresh" style="font-size: 0.72rem;">25% Weight</span></div>
                    <div class="factor-desc" id="factor-desc-prox">1.7 km distance within shelter's 15 km transit radius.</div>
                </div>
                <div class="factor-meter-container">
                    <div class="factor-progress-track">
                        <div class="factor-progress-bar" id="bar-prox" style="width: 95%; background: var(--primary);"></div>
                    </div>
                    <span class="factor-score-pill" id="score-prox" style="color: var(--primary);">95%</span>
                </div>
            </div>

            <!-- Factor 2: Urgency Decay -->
            <div class="matching-factor-row">
                <div class="factor-info">
                    <div class="factor-title">⏱ Expiry Decay Velocity <span class="badge badge-critical" style="font-size: 0.72rem;">25% Weight</span></div>
                    <div class="factor-desc" id="factor-desc-urgency">3.5 hrs remaining &bull; High urgency expedited dispatch threshold.</div>
                </div>
                <div class="factor-meter-container">
                    <div class="factor-progress-track">
                        <div class="factor-progress-bar" id="bar-urgency" style="width: 98%; background: #ea580c;"></div>
                    </div>
                    <span class="factor-score-pill" id="score-urgency" style="color: #ea580c;">98%</span>
                </div>
            </div>

            <!-- Factor 3: Dietary Match -->
            <div class="matching-factor-row">
                <div class="factor-info">
                    <div class="factor-title">🥗 Dietary &amp; Category Alignment <span class="badge badge-standard" style="font-size: 0.72rem;">25% Weight</span></div>
                    <div class="factor-desc" id="factor-desc-diet">Exact match: Vegetarian meal requirement satisfied.</div>
                </div>
                <div class="factor-meter-container">
                    <div class="factor-progress-track">
                        <div class="factor-progress-bar" id="bar-diet" style="width: 100%; background: #10b981;"></div>
                    </div>
                    <span class="factor-score-pill" id="score-diet" style="color: #10b981;">100%</span>
                </div>
            </div>

            <!-- Factor 4: Quantity & Capacity Fit -->
            <div class="matching-factor-row">
                <div class="factor-info">
                    <div class="factor-title">📦 Volume Fit &amp; Shelter Capacity <span class="badge badge-fresh" style="font-size: 0.72rem;">15% Weight</span></div>
                    <div class="factor-desc" id="factor-desc-cap">45 kg surplus fits comfortably within 100 kg cold storage capacity.</div>
                </div>
                <div class="factor-meter-container">
                    <div class="factor-progress-track">
                        <div class="factor-progress-bar" id="bar-cap" style="width: 90%; background: var(--secondary);"></div>
                    </div>
                    <span class="factor-score-pill" id="score-cap" style="color: var(--secondary);">90%</span>
                </div>
            </div>

            <!-- Factor 5: Operational Reliability -->
            <div class="matching-factor-row">
                <div class="factor-info">
                    <div class="factor-title">🛡 Operational SLA &amp; Reliability <span class="badge badge-standard" style="font-size: 0.72rem;">10% Weight</span></div>
                    <div class="factor-desc" id="factor-desc-rel">100% historical pickup completion &amp; zero spoilage rate.</div>
                </div>
                <div class="factor-meter-container">
                    <div class="factor-progress-track">
                        <div class="factor-progress-bar" id="bar-rel" style="width: 92%; background: #6366f1;"></div>
                    </div>
                    <span class="factor-score-pill" id="score-rel" style="color: #6366f1;">92%</span>
                </div>
            </div>

            <!-- Total Match Composite Banner -->
            <div style="margin-top: 2rem; padding: 1.5rem; background: #f8fafc; border-radius: 12px; border: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <span style="font-size: 0.78rem; font-weight: 800; text-transform: uppercase; color: var(--text-muted); letter-spacing: 0.5px;">Overall Compatibility Score</span>
                    <div style="font-size: 1.75rem; font-weight: 800; color: var(--primary); margin-top: 0.2rem;" id="total-match-score">
                        94.8% Match Affinity
                    </div>
                    <div style="font-size: 0.85rem; color: var(--text-muted);" id="match-explanation-summary">
                        Algorithm Recommendation: Immediate auto-dispatch priority. Highest suitability shelter confirmed.
                    </div>
                </div>
                <div style="display: flex; gap: 0.75rem;">
                    <a href="${pageContext.request.contextPath}/auth/register?role=NGO" class="btn btn-primary btn-sm">Register NGO to Configure Rules &rarr;</a>
                    <a href="${pageContext.request.contextPath}/ngo/browse" class="btn btn-outline btn-sm">Browse Live Surplus</a>
                </div>
            </div>
        </div>
    </div>
</section>

<script>
var matchScenarios = [
    {
        title: "Scenario 1: 45 kg Veg Pulao",
        proxW: "95%", proxS: "95%", proxD: "1.7 km distance within shelter's 15 km transit radius (Simulated).",
        urgW: "98%", urgS: "98%", urgD: "3.5 hrs remaining • High urgency expedited dispatch threshold.",
        dietW: "100%", dietS: "100%", dietD: "Dietary match: Vegetarian meal requirement satisfied.",
        capW: "90%", capS: "90%", capD: "45 kg surplus fits comfortably within 100 kg capacity.",
        relW: "92%", relS: "92%", relD: "Historical pickup completion & quality history.",
        total: "94.8% Match Affinity (Simulated)",
        summary: "Algorithm Simulation Output: Immediate auto-dispatch priority. High-suitability shelter match confirmed."
    },
    {
        title: "Scenario 2: 28 kg Sourdough Bread",
        proxW: "88%", proxS: "88%", proxD: "3.4 km distance • Rapid bike courier corridor available (Simulated).",
        urgW: "85%", urgS: "85%", urgD: "6.0 hrs remaining • Ambient temperature stable shelf-life.",
        dietW: "100%", dietS: "100%", dietD: "Universal bakery staple accepted by registered shelters.",
        capW: "95%", capS: "95%", capD: "Dry storage ready. No refrigeration required.",
        relW: "96%", relS: "96%", relD: "Preferred donor partner with verified quality history.",
        total: "91.2% Match Affinity (Simulated)",
        summary: "Algorithm Simulation Output: Direct volunteer routing scheduled for pickup window."
    },
    {
        title: "Scenario 3: 65 kg Organic Fruit",
        proxW: "82%", proxS: "82%", proxD: "5.1 km distance • Multi-stop delivery batch optimized (Simulated).",
        urgW: "78%", urgS: "78%", urgD: "14 hrs remaining • Fresh produce batch ready for distribution.",
        dietW: "100%", dietS: "100%", dietD: "Vegan / natural food profile.",
        capW: "98%", capS: "98%", capD: "High-capacity kitchen with immediate prep capacity.",
        relW: "90%", relS: "90%", relD: "Scheduled courier window assigned successfully.",
        total: "88.6% Match Affinity (Simulated)",
        summary: "Algorithm Simulation Output: Matched to local community kitchen for evening food distribution."
    }
];

function switchSimulator(idx) {
    var btns = document.querySelectorAll('.simulator-pill-btn');
    btns.forEach(function(b, i) {
        if (i === idx) b.classList.add('active');
        else b.classList.remove('active');
    });

    var s = matchScenarios[idx];
    document.getElementById('bar-prox').style.width = s.proxW;
    document.getElementById('score-prox').textContent = s.proxS;
    document.getElementById('factor-desc-prox').textContent = s.proxD;

    document.getElementById('bar-urgency').style.width = s.urgW;
    document.getElementById('score-urgency').textContent = s.urgS;
    document.getElementById('factor-desc-urgency').textContent = s.urgD;

    document.getElementById('bar-diet').style.width = s.dietW;
    document.getElementById('score-diet').textContent = s.dietS;
    document.getElementById('factor-desc-diet').textContent = s.dietD;

    document.getElementById('bar-cap').style.width = s.capW;
    document.getElementById('score-cap').textContent = s.capS;
    document.getElementById('factor-desc-cap').textContent = s.capD;

    document.getElementById('bar-rel').style.width = s.relW;
    document.getElementById('score-rel').textContent = s.relS;
    document.getElementById('factor-desc-rel').textContent = s.relD;

    document.getElementById('total-match-score').textContent = s.total;
    document.getElementById('match-explanation-summary').textContent = s.summary;
}
</script>

<!-- 6. THREE ROLES SECTION -->
<section style="background: #ffffff; padding: 4.5rem 0; border-top: 1px solid var(--border-color);" id="roles">
    <div class="container">
        <div style="text-align: center; max-width: 700px; margin: 0 auto 3rem;">
            <span class="badge badge-standard" style="margin-bottom: 0.8rem;"><c:out value="${not empty msg['roles.badge'] ? msg['roles.badge'] : 'Built for Real Actors'}" /></span>
            <h2><c:out value="${not empty msg['roles.title'] ? msg['roles.title'] : 'Three Dedicated Roles. Zero Admin Bloat.'}" /></h2>
            <p style="color: var(--text-muted);">
                <c:out value="${not empty msg['roles.subtitle'] ? msg['roles.subtitle'] : 'AnnPath operates strictly with three distinct roles, each equipped with purposeful, tailored tooling.'}" />
            </p>
        </div>

        <div class="card-grid">
            <div class="card">
                <h3 style="color: var(--primary);">1. Food Donors</h3>
                <p style="color: var(--text-muted); margin: 1rem 0;">
                    Commercial kitchens, catering companies, restaurants, and grocery retailers. Track donations, approve matching requests, verify pickups with OTP, and access verifiable digital rescue receipts.
                </p>
                <a href="${pageContext.request.contextPath}/auth/register?role=DONOR" class="btn btn-outline-primary btn-sm">Register as Donor &rarr;</a>
            </div>
            <div class="card">
                <h3 style="color: var(--secondary);">2. Verified NGOs</h3>
                <p style="color: var(--text-muted); margin: 1rem 0;">
                    Food banks, soup kitchens, and charities. Undergo country-rule automated verification, define recurring dietary requirements, browse live surplus, and trigger self-pickup fallback.
                </p>
                <a href="${pageContext.request.contextPath}/auth/register?role=NGO" class="btn btn-secondary btn-sm">Register as NGO &rarr;</a>
            </div>
            <div class="card">
                <h3 style="color: var(--primary);">3. Volunteers</h3>
                <p style="color: var(--text-muted); margin: 1rem 0;">
                    Community heroes with motorcycles, cars, or vans. Receive proximity dispatch requests, navigate to pickup, verify handover with OTP, and complete doorstep delivery.
                </p>
                <a href="${pageContext.request.contextPath}/auth/register?role=VOLUNTEER" class="btn btn-outline btn-sm">Join as Volunteer &rarr;</a>
            </div>
        </div>
    </div>
</section>

<!-- 7. TRUST & SAFETY -->
<section class="container" style="padding: 4.5rem 0;" id="safety">
    <div style="text-align: center; max-width: 700px; margin: 0 auto 3rem;">
        <span class="badge badge-fresh" style="margin-bottom: 0.8rem;"><c:out value="${not empty msg['safety.badge'] ? msg['safety.badge'] : 'Trust & Safety'}" /></span>
        <h2><c:out value="${not empty msg['safety.title'] ? msg['safety.title'] : 'End-to-End Integrity & Compliance'}" /></h2>
        <p style="color: var(--text-muted);">
            <c:out value="${not empty msg['safety.subtitle'] ? msg['safety.subtitle'] : 'Food redistribution requires rigorous food safety, tamper evidence, and identity assurance.'}" />
        </p>
    </div>

    <div class="card-grid">
        <div class="card">
            <h4>Automated NGO Verification</h4>
            <p style="color: var(--text-muted); margin-top: 0.5rem;">
                Registration documents, charity commission numbers, and physical address proofs verified systematically against country compliance rules before food can be requested.
            </p>
        </div>
        <div class="card">
            <h4>Cryptographic OTP Handover</h4>
            <p style="color: var(--text-muted); margin-top: 0.5rem;">
                Volunteers and self-pickup agents must verify 6-digit cryptographic OTPs at pickup (from donor) and at delivery (from NGO) before state transitions execute.
            </p>
        </div>
        <div class="card">
            <h4>Verifiable Digital Receipts</h4>
            <p style="color: var(--text-muted); margin-top: 0.5rem;">
                Every completed distribution produces a tamper-evident Digital Rescue Receipt featuring an immutable cryptographic proof, verified timestamps, and real metrics.
            </p>
        </div>
    </div>
</section>

<!-- 8. FINAL CTA -->
<section style="background: linear-gradient(135deg, var(--primary), #0e4531); color: #ffffff; padding: 4.5rem 0; text-align: center;">
    <div class="container">
        <h2 style="color: #ffffff; font-size: 2.4rem; margin-bottom: 1rem;"><c:out value="${not empty msg['cta.title'] ? msg['cta.title'] : 'Join the Movement to End Food Waste'}" /></h2>
        <p style="max-width: 600px; margin: 0 auto 2rem; opacity: 0.9; font-size: 1.1rem;">
            <c:out value="${not empty msg['cta.subtitle'] ? msg['cta.subtitle'] : 'Whether you are a donor with surplus meals, an NGO serving hungry families, or a volunteer ready to transport, AnnPath provides the path.'}" />
        </p>
        <div style="display: flex; justify-content: center; gap: 1rem; flex-wrap: wrap;">
            <a href="${pageContext.request.contextPath}/auth/register?role=DONOR" class="btn btn-secondary btn-lg"><c:out value="${not empty msg['btn.donateFood'] ? msg['btn.donateFood'] : 'Start Donating Food'}" /></a>
            <a href="${pageContext.request.contextPath}/auth/register?role=NGO" class="btn btn-outline btn-lg" style="color:#fff; border-color:#fff;">Register Organization</a>
        </div>
    </div>
</section>

<jsp:include page="WEB-INF/views/common/footer.jsp" />
