<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="NGO Relief Portal — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container dashboard-wrapper">
    <div class="dashboard-header">
        <div>
            <h2>NGO Relief Logistics Portal</h2>
            <p style="color: var(--text-muted);">
                <strong><c:out value="${ngoProfile != null ? ngoProfile.organizationName : sessionScope.currentUser.email}" /></strong>
                &bull; 
                <c:choose>
                    <c:when test="${ngoProfile.verificationStatus == 'VERIFIED'}">
                        <span class="badge badge-fresh">✓ Verified NGO</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge badge-pending">Pending Verification</span>
                        <a href="${pageContext.request.contextPath}/ngo/verify" style="margin-left: 0.5rem; font-size: 0.85rem;">Upload Verification Documents &rarr;</a>
                    </c:otherwise>
                </c:choose>
            </p>
        </div>
        <div style="display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap;">
            <a href="${pageContext.request.contextPath}/ngo/profile" class="btn btn-outline btn-sm">🏢 Organization Profile</a>
            <a href="${pageContext.request.contextPath}/ngo/requirements" class="btn btn-outline btn-sm">📋 Requirement Profiles</a>
            <a href="${pageContext.request.contextPath}/ngo/browse" class="btn btn-primary btn-sm">🔍 Browse Live Surplus</a>
        </div>
    </div>

    <jsp:include page="../common/alert.jsp" />

    <!-- Tabs -->
    <div class="dashboard-tabs">
        <button class="tab-btn active" data-tab="recommendedTab">Recommended Surplus (<c:out value="${recommendedMatches.size()}" />)</button>
        <button class="tab-btn" data-tab="deliveriesTab">Active Deliveries (<c:out value="${activeDeliveries.size()}" />)</button>
        <button class="tab-btn" data-tab="requestsTab">My Requests (<c:out value="${myRequests.size()}" />)</button>
    </div>

    <!-- Recommended Matches Tab -->
    <div id="recommendedTab" class="tab-pane">
        <c:choose>
            <c:when test="${empty recommendedMatches}">
                <div class="empty-state">
                    <div class="empty-state-icon">📡</div>
                    <div class="empty-state-title">No Active Matches in Range</div>
                    <div class="empty-state-text">
                        No food surplus currently matches your recurring requirements within your active service radius (<c:out value="${ngoProfile.serviceRadiusKm}" /> km).
                    </div>
                    <div style="margin-top: 1.5rem; display: flex; gap: 0.75rem; justify-content: center; flex-wrap: wrap;">
                        <a href="${pageContext.request.contextPath}/ngo/browse" class="btn btn-primary">Browse All Regional Surplus</a>
                        <a href="${pageContext.request.contextPath}/ngo/requirements" class="btn btn-outline">Update Requirement Filters</a>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="card-grid">
                    <c:forEach var="m" items="${recommendedMatches}">
                        <div class="match-card">
                            <div class="card-header" style="padding: 1.25rem 1.25rem 0.5rem; display: flex; justify-content: space-between; align-items: center;">
                                <span class="badge ${m.listing.urgencyStatus == 'CRITICAL' ? 'badge-critical' : 'badge-fresh'}">
                                    <c:out value="${m.listing.urgencyStatus}" />
                                </span>
                                <div class="rescue-clock" data-expires-at="${m.listing.availableUntil}">
                                    <span class="clock-display">⏱ Calculating...</span>
                                </div>
                            </div>

                            <div style="padding: 0.75rem 1.25rem;">
                                <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 0.4rem;">
                                    <h3 style="margin: 0; font-size: 1.2rem; color: var(--text-main);"><c:out value="${m.listing.title}" /></h3>
                                    <span style="font-weight: 800; color: var(--primary); font-size: 1.05rem; white-space: nowrap;">
                                        <fmt:formatNumber value="${m.matchScore}" maxFractionDigits="0" />% Match
                                    </span>
                                </div>

                                <div style="font-size: 0.9rem; color: var(--text-muted); display: flex; flex-direction: column; gap: 0.25rem; margin-top: 0.5rem;">
                                    <div><strong>Available:</strong> <c:out value="${m.listing.remainingQuantity}" /> <c:out value="${m.listing.unitName}" /></div>
                                    <div><strong>Donor:</strong> <c:out value="${m.listing.donorName}" /></div>
                                    <div><strong>Proximity:</strong> 📍 <c:out value="${m.distanceKm}" /> km away</div>
                                    <div style="font-size: 0.82rem; color: var(--primary); font-weight: 600; margin-top: 0.2rem;">
                                        <em><c:out value="${m.matchReason}" /></em>
                                    </div>
                                </div>

                                <!-- Explainable Match Quality Expander -->
                                <button type="button" class="btn btn-sm" data-toggle-match="match-breakdown-${m.listing.id}"
                                        style="background: none; border: none; padding: 0.4rem 0; color: var(--secondary); font-size: 0.85rem; font-weight: 700; cursor: pointer; text-align: left; margin-top: 0.5rem;">
                                    ▾ Why Recommended? (Match Breakdown)
                                </button>

                                <div id="match-breakdown-${m.listing.id}" class="match-quality-panel" style="display: none;">
                                    <div class="breakdown-metric">
                                        <div class="breakdown-label">
                                            <span>Proximity Affinity</span>
                                            <span><c:out value="${m.distanceScore}" />%</span>
                                        </div>
                                        <div class="meter-bar"><div class="meter-fill" style="width: ${m.distanceScore}%;"></div></div>
                                    </div>

                                    <div class="breakdown-metric">
                                        <div class="breakdown-label">
                                            <span>Dietary & Category Fit</span>
                                            <span><c:out value="${m.foodCompatScore}" />%</span>
                                        </div>
                                        <div class="meter-bar"><div class="meter-fill" style="width: ${m.foodCompatScore}%;"></div></div>
                                    </div>

                                    <div class="breakdown-metric">
                                        <div class="breakdown-label">
                                            <span>Quantity Suitability</span>
                                            <span><c:out value="${m.quantityFitScore}" />%</span>
                                        </div>
                                        <div class="meter-bar"><div class="meter-fill" style="width: ${m.quantityFitScore}%;"></div></div>
                                    </div>

                                    <div class="breakdown-metric">
                                        <div class="breakdown-label">
                                            <span>Time Urgency Alignment</span>
                                            <span><c:out value="${m.timeCompatScore}" />%</span>
                                        </div>
                                        <div class="meter-bar"><div class="meter-fill" style="width: ${m.timeCompatScore}%;"></div></div>
                                    </div>
                                </div>
                            </div>

                            <div style="padding: 1rem 1.25rem; border-top: 1px solid var(--border-color); background: var(--bg-main);">
                                <c:choose>
                                    <c:when test="${ngoProfile.verificationStatus == 'VERIFIED'}">
                                        <form action="${pageContext.request.contextPath}/ngo/request" method="POST" style="display: flex; gap: 0.5rem; width: 100%; align-items: center;">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                            <input type="hidden" name="listingId" value="${m.listing.id}" />
                                            <input type="number" name="requestedQuantity" max="${m.listing.remainingQuantity}" min="1" step="0.1"
                                                   value="${m.listing.remainingQuantity}" class="form-control" style="width: 90px; padding: 0.4rem;" required />
                                            <button type="submit" class="btn btn-secondary btn-sm" style="flex: 1; white-space: nowrap;">
                                                Claim Food &rarr;
                                            </button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="font-size: 0.85rem; color: var(--text-muted);">
                                            🔒 Compliance verification required to claim
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Active Deliveries Tab -->
    <div id="deliveriesTab" class="tab-pane" style="display: none;">
        <c:choose>
            <c:when test="${empty activeDeliveries}">
                <div class="empty-state">
                    <div class="empty-state-icon">🚚</div>
                    <div class="empty-state-title">No Active Deliveries</div>
                    <div class="empty-state-text">You have no ongoing deliveries or pending distributions at this time.</div>
                </div>
            </c:when>
            <c:otherwise>
                <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                    <c:forEach var="p" items="${activeDeliveries}">
                        <div class="card" style="border-left: 5px solid var(--primary); background: #ffffff;">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 0.5rem;">
                                <div>
                                    <h3 style="margin-bottom: 0.2rem; font-size: 1.25rem;"><c:out value="${p.foodTitle}" /></h3>
                                    <span style="color: var(--text-muted); font-size: 0.9rem;">
                                        Quantity: <strong><c:out value="${p.quantity}" /> <c:out value="${p.unitName}" /></strong> from <strong><c:out value="${p.donorName}" /></strong>
                                        <c:if test="${not empty p.volunteerName}">
                                            &bull; Courier: <strong><c:out value="${p.volunteerName}" /></strong>
                                        </c:if>
                                    </span>
                                </div>
                                <span class="badge badge-rescue-now"><c:out value="${p.status}" /></span>
                            </div>

                            <!-- OTP Display -->
                            <c:if test="${p.status == 'ASSIGNED' || p.status == 'PICKED_UP' || p.status == 'NGO_SELF_PICKUP_ASSIGNED'}">
                                <div style="margin-top: 1.2rem; background: #ecfdf5; border: 1.5px dashed #10b981; border-radius: 10px; padding: 1rem 1.25rem; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem;">
                                    <div>
                                        <span style="color: #047857; font-weight: 700; font-size: 0.95rem; display: block; margin-bottom: 0.2rem;">
                                            🔑 Delivery Acceptance Verification OTP:
                                        </span>
                                        <span style="font-size: 0.85rem; color: #065f46;">
                                            Provide your 6-digit verification code to the courier upon food arrival to confirm safe delivery handover.
                                        </span>
                                    </div>
                                    <div style="background: #ffffff; border: 1px solid #10b981; border-radius: 8px; padding: 0.4rem 1rem;">
                                        <code style="font-size: 1.2rem; font-weight: 800; color: #047857; letter-spacing: 2px;">• • • • • •</code>
                                    </div>
                                </div>
                            </c:if>

                            <!-- Self-Pickup Fallback Trigger if VOLUNTEER_UNAVAILABLE -->
                            <c:if test="${p.status == 'VOLUNTEER_UNAVAILABLE'}">
                                <div class="recovery-card" style="margin: 1.2rem 0;">
                                    <div class="recovery-header">
                                        <span style="font-size: 1.2rem;">⚠️</span>
                                        <h4 style="margin: 0; color: #991b1b; font-size: 1rem;">No Volunteer Couriers Nearby — Urgent Spoilage Risk</h4>
                                    </div>
                                    <p style="font-size: 0.9rem; color: #7f1d1d; margin-bottom: 1rem;">
                                        Volunteer couriers are currently busy in this sector. To prevent surplus food spoilage, activate direct self-pickup with your organization's authorized vehicle.
                                    </p>
                                    <form action="${pageContext.request.contextPath}/ngo/self-pickup" method="POST" class="form-row" style="align-items: flex-end;">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                        <input type="hidden" name="pickupId" value="${p.id}" />
                                        <div class="form-group" style="margin: 0;">
                                            <label class="form-label" style="font-size: 0.8rem;">Authorized Representative Name *</label>
                                            <input type="text" name="selfPickupPersonName" class="form-control" required placeholder="Driver / Representative" />
                                        </div>
                                        <div class="form-group" style="margin: 0;">
                                            <label class="form-label" style="font-size: 0.8rem;">Contact Mobile *</label>
                                            <input type="tel" name="selfPickupPersonPhone" class="form-control" required placeholder="Mobile number" />
                                        </div>
                                        <div class="form-group" style="margin: 0;">
                                            <button type="submit" class="btn btn-secondary btn-sm" style="height: 38px;">Arrange Self-Pickup &rarr;</button>
                                        </div>
                                    </form>
                                </div>
                            </c:if>

                            <!-- Operational Actions -->
                            <div style="display: flex; gap: 0.8rem; margin-top: 1.25rem; align-items: center; flex-wrap: wrap;">
                                <c:if test="${p.status == 'DELIVERED'}">
                                    <form action="${pageContext.request.contextPath}/ngo/confirm-receipt" method="POST" style="display:inline;">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                        <input type="hidden" name="pickupId" value="${p.id}" />
                                        <button type="submit" class="btn btn-primary btn-sm">✓ Confirm Delivery at Center</button>
                                    </form>
                                </c:if>

                                <c:if test="${p.status == 'RECEIVED'}">
                                    <form action="${pageContext.request.contextPath}/ngo/distribute" method="POST" style="display:inline;">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                        <input type="hidden" name="pickupId" value="${p.id}" />
                                        <button type="submit" class="btn btn-secondary btn-sm" data-confirm="Confirm community distribution? This generates the verified Food Journey Passport and immutable impact records.">
                                            🎁 Complete Community Distribution
                                        </button>
                                    </form>
                                </c:if>

                                <c:if test="${p.status == 'DISTRIBUTED'}">
                                    <div style="display: flex; gap: 0.6rem; align-items: center; flex-wrap: wrap; width: 100%;">
                                        <a href="${pageContext.request.contextPath}/rescue/passport?pickupId=${p.id}" class="btn btn-primary btn-sm">🌍 Food Journey Passport</a>
                                        <a href="${pageContext.request.contextPath}/receipt/view?pickupId=${p.id}" class="btn btn-outline btn-sm">📜 View Legal Receipt</a>
                                        <a href="${pageContext.request.contextPath}/complaint/file?pickupId=${p.id}" style="font-size: 0.8rem; color: #b33939; text-decoration: underline;">Report Issue</a>
                                        <div style="margin-left: auto;">
                                            <form action="${pageContext.request.contextPath}/feedback/submit" method="POST" style="display:inline-flex; align-items:center; gap:0.4rem; flex-wrap:wrap;">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                                <input type="hidden" name="pickupId" value="${p.id}" />
                                                <input type="hidden" name="feedbackType" value="FOOD_QUALITY" />
                                                <span style="font-size: 0.85rem; font-weight: 600;">Quality:</span>
                                                <select name="rating" class="form-select" style="width:auto; padding:0.25rem 0.5rem; font-size:0.85rem;">
                                                    <option value="5">⭐⭐⭐⭐⭐ (5 - Fresh)</option>
                                                    <option value="4">⭐⭐⭐⭐ (4 - Good)</option>
                                                    <option value="3">⭐⭐⭐ (3 - Acceptable)</option>
                                                    <option value="2">⭐⭐ (2 - Marginal)</option>
                                                    <option value="1">⭐ (1 - Spoiled/Unusable)</option>
                                                </select>
                                                <input type="text" name="comment" placeholder="Quality comment..." style="padding:0.25rem 0.5rem; font-size:0.85rem; border-radius:4px; border:1px solid #ccc; min-width: 140px;" />
                                                <button type="submit" class="btn btn-outline btn-sm">Submit Review</button>
                                            </form>
                                        </div>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- My Requests Tab -->
    <div id="requestsTab" class="tab-pane" style="display: none;">
        <c:choose>
            <c:when test="${empty myRequests}">
                <div class="empty-state">
                    <div class="empty-state-icon">📋</div>
                    <div class="empty-state-title">No Requests Made Yet</div>
                    <div class="empty-state-text">You haven't requested any food surplus yet. Browse recommended food above or inspect the live surplus directory.</div>
                    <div style="margin-top: 1.5rem;">
                        <a href="${pageContext.request.contextPath}/ngo/browse" class="btn btn-primary">Browse Surplus Directory</a>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div style="display: flex; flex-direction: column; gap: 1rem;">
                    <c:forEach var="r" items="${myRequests}">
                        <div class="card" style="background: #ffffff;">
                            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem;">
                                <div>
                                    <h4 style="margin: 0; font-size: 1.1rem;"><c:out value="${r.foodTitle}" /></h4>
                                    <span style="font-size: 0.85rem; color: var(--text-muted);">
                                        Requested: <strong><c:out value="${r.requestedQuantity}" /> <c:out value="${r.unitName}" /></strong> &bull;
                                        Donor: <strong><c:out value="${r.donorName}" /></strong> &bull;
                                        Date: <c:out value="${r.createdAt}" />
                                    </span>
                                </div>
                                <span class="badge ${r.status == 'APPROVED' ? 'badge-fresh' : (r.status == 'REJECTED' ? 'badge-critical' : 'badge-pending')}">
                                    <c:out value="${r.status}" />
                                </span>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
