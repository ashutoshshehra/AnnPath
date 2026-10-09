<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Global Impact & Transparency — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<!-- Hero Banner -->
<section style="background: linear-gradient(135deg, #176B4D, #0d4632); color: #ffffff; padding: 4rem 1rem 3rem; text-align: center;">
    <div class="container" style="max-width: 860px;">
        <span class="badge badge-fresh" style="background: rgba(255,255,255,0.15); color: #ffffff; border-color: rgba(255,255,255,0.3); margin-bottom: 1rem;">
            🌍 Live Ecological & Social Accounting
        </span>
        <h1 style="color: #ffffff; font-size: 2.6rem; margin: 0 0 1rem 0;">Real-World Food Rescue Impact</h1>
        <p style="font-size: 1.1rem; opacity: 0.92; line-height: 1.6; margin: 0 auto 2rem; max-width: 680px;">
            Every gram of food redirected from landfills to community kitchens is backed by tamper-evident database verification logs and dual-OTP custody handovers.
        </p>

        <!-- Macro Stats Banner -->
        <div class="stats-banner" style="margin-top: 2rem;">
            <div class="stat-item">
                <span class="stat-icon-badge">⚖️</span>
                <div class="stat-number">
                    <fmt:formatNumber value="${summary.totalRescuedKg != null ? summary.totalRescuedKg : 0}" maxFractionDigits="0" /><span class="stat-unit">kg</span>
                </div>
                <div class="stat-label">Total Surplus Rescued</div>
            </div>
            <div class="stat-item">
                <span class="stat-icon-badge">🍲</span>
                <div class="stat-number">
                    <fmt:formatNumber value="${summary.totalMealsFed != null ? summary.totalMealsFed : 0}" maxFractionDigits="0" /><span class="stat-unit">meals</span>
                </div>
                <div class="stat-label">Nourishing Meals Created</div>
            </div>
            <div class="stat-item">
                <span class="stat-icon-badge">🍃</span>
                <div class="stat-number">
                    <fmt:formatNumber value="${summary.totalCo2PreventedKg != null ? summary.totalCo2PreventedKg : 0}" maxFractionDigits="0" /><span class="stat-unit">kg</span>
                </div>
                <div class="stat-label">CO<sub>2</sub> Emissions Diverted</div>
            </div>
            <div class="stat-item">
                <span class="stat-icon-badge">🎯</span>
                <div class="stat-number">
                    <fmt:formatNumber value="${summary.totalRescuesCount != null ? summary.totalRescuesCount : 0}" /><span class="stat-unit">rescues</span>
                </div>
                <div class="stat-label">Completed Missions</div>
            </div>
        </div>
    </div>
</section>

<!-- Network Fleet & Logistics Bottleneck Grid -->
<section style="background: #ffffff; padding: 3rem 0; border-bottom: 1px solid var(--border-color);">
    <div class="container">
        <div style="text-align: center; max-width: 680px; margin: 0 auto 2rem;">
            <h2 style="font-size: 1.6rem; margin-bottom: 0.4rem;">Live Logistics Capacity & Network State</h2>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                Real-time operational distribution counters across active donors, shelters, and couriers.
            </p>
        </div>

        <div class="bottleneck-grid">
            <div class="bottleneck-card">
                <div class="bottleneck-count" style="color: var(--secondary);">
                    <c:out value="${bottlenecks.unmatchedSurplus != null ? bottlenecks.unmatchedSurplus : 0}" />
                </div>
                <div class="bottleneck-label">Unmatched Surplus</div>
                <div class="bottleneck-desc">Donations awaiting shelter claim</div>
            </div>
            <div class="bottleneck-card">
                <div class="bottleneck-count" style="color: #ea580c;">
                    <c:out value="${bottlenecks.awaitingTransit != null ? bottlenecks.awaitingTransit : 0}" />
                </div>
                <div class="bottleneck-label">Awaiting Volunteer Courier</div>
                <div class="bottleneck-desc">Approved food ready for pickup</div>
            </div>
            <div class="bottleneck-card">
                <div class="bottleneck-count" style="color: var(--primary);">
                    <c:out value="${bottlenecks.activeRescues != null ? bottlenecks.activeRescues : 0}" />
                </div>
                <div class="bottleneck-label">Active Missions In Transit</div>
                <div class="bottleneck-desc">Couriers moving food to shelters</div>
            </div>
            <div class="bottleneck-card">
                <div class="bottleneck-count" style="color: #10b981;">
                    <c:out value="${bottlenecks.availableFleet != null ? bottlenecks.availableFleet : 0}" />
                </div>
                <div class="bottleneck-label">Available Couriers</div>
                <div class="bottleneck-desc">Verified volunteers on standby</div>
            </div>
        </div>
    </div>
</section>

<!-- Methodology & Scientific Formulation -->
<section class="container" style="padding: 4rem 1rem;">
    <div style="text-align: center; max-width: 720px; margin: 0 auto 3rem;">
        <span class="badge badge-standard" style="margin-bottom: 0.6rem;">Scientific Audit Formula</span>
        <h2 style="font-size: 1.8rem;">How AnnPath Computes Environmental Savings</h2>
        <p style="color: var(--text-muted);">
            Transparent parameters backed by global food-waste lifecycle assessment methodologies.
        </p>
    </div>

    <div class="card-grid">
        <div class="card" style="background: #ffffff; padding: 2rem;">
            <div style="font-size: 2rem; margin-bottom: 0.5rem;">🍲</div>
            <h3 style="font-size: 1.2rem; color: var(--primary); margin-bottom: 0.5rem;">Meal Equivalence Ratio</h3>
            <p style="color: var(--text-muted); font-size: 0.9rem; line-height: 1.5;">
                <strong>1 kg = 2.5 standard meal portions.</strong> Evaluated across cooked meals and edible packaged surplus according to UN FAO nutritional baselines.
            </p>
        </div>
        <div class="card" style="background: #ffffff; padding: 2rem;">
            <div style="font-size: 2rem; margin-bottom: 0.5rem;">🍃</div>
            <h3 style="font-size: 1.2rem; color: #10b981; margin-bottom: 0.5rem;">Landfill Methane Abatement</h3>
            <p style="color: var(--text-muted); font-size: 0.9rem; line-height: 1.5;">
                <strong>1 kg surplus = ~1.9 kg CO₂e avoided.</strong> Preventing organic decomposition in municipal anaerobic landfills halts dangerous fugitive methane emissions.
            </p>
        </div>
        <div class="card" style="background: #ffffff; padding: 2rem;">
            <div style="font-size: 2rem; margin-bottom: 0.5rem;">🔐</div>
            <h3 style="font-size: 1.2rem; color: var(--secondary); margin-bottom: 0.5rem;">Cryptographic Audit Trail</h3>
            <p style="color: var(--text-muted); font-size: 0.9rem; line-height: 1.5;">
                Every gram rescued is linked to an immutable SHA-256 digital receipt hash containing dual-OTP pickup and delivery timestamps.
            </p>
        </div>
    </div>
</section>

<!-- Verified Mission Activity Stream -->
<section style="background: #ffffff; padding: 4rem 1rem; border-top: 1px solid var(--border-color);">
    <div class="container" style="max-width: 960px;">
        <div style="display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
            <div>
                <span class="badge badge-fresh" style="margin-bottom: 0.5rem;">Audit Registry</span>
                <h2 style="margin: 0; font-size: 1.8rem;">Recent Verified Rescue Missions</h2>
            </div>
            <span style="font-size: 0.85rem; color: var(--text-muted);">
                Showing latest verified missions from the database
            </span>
        </div>

        <c:choose>
            <c:when test="${empty activities}">
                <div class="empty-state">
                    <div class="empty-state-icon">🌱</div>
                    <div class="empty-state-title">No Completed Missions Logged Yet</div>
                    <div class="empty-state-text">
                        As soon as donors, shelters, and couriers complete safe delivery distributions, the verified mission audits will appear here.
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div style="display: flex; flex-direction: column; gap: 1.2rem;">
                    <c:forEach var="a" items="${activities}">
                        <div class="card" style="padding: 1.5rem; background: #ffffff; border-left: 4px solid var(--primary);">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem;">
                                <div>
                                    <h3 style="margin: 0 0 0.3rem 0; font-size: 1.2rem;"><c:out value="${a.foodTitle}" /></h3>
                                    <div style="font-size: 0.88rem; color: var(--text-muted);">
                                        Rescued from <strong><c:out value="${a.donorName}" /></strong> &bull; Delivered to <strong><c:out value="${a.ngoName}" /></strong> &bull; <c:out value="${a.createdAt}" />
                                    </div>
                                </div>
                                <div style="display: flex; align-items: center; gap: 1.5rem;">
                                    <div style="text-align: right;">
                                        <div style="font-size: 1.15rem; font-weight: 800; color: var(--primary);">
                                            <fmt:formatNumber value="${a.rescuedQuantityKg}" maxFractionDigits="1" /> kg
                                        </div>
                                        <div style="font-size: 0.78rem; color: var(--text-muted);">
                                            <c:out value="${a.estimatedPeopleFed}" /> meals &bull; <fmt:formatNumber value="${a.co2SavedKg}" maxFractionDigits="1" /> kg CO₂e
                                        </div>
                                    </div>
                                    <a href="${pageContext.request.contextPath}/rescue/passport?pickupId=${a.pickupId}" class="btn btn-outline btn-sm">
                                        🌍 Passport &rarr;
                                    </a>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<jsp:include page="../common/footer.jsp" />
