<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Donor Dashboard — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container dashboard-wrapper">
    <div class="dashboard-header">
        <div>
            <h2>Donor Operations Portal</h2>
            <p style="color: var(--text-muted);">
                Welcome, <strong><c:out value="${donorProfile != null ? donorProfile.displayName : sessionScope.currentUser.email}" /></strong>
                &bull; <span class="badge badge-fresh">Active Donor</span>
            </p>
        </div>
        <div style="display: flex; gap: 0.6rem; align-items: center; flex-wrap: wrap;">
            <a href="${pageContext.request.contextPath}/donor/profile" class="btn btn-outline btn-sm">
                <c:out value="${not empty msg['dash.viewProfile'] ? msg['dash.viewProfile'] : '👤 Profile Settings'}" />
            </a>
            <a href="${pageContext.request.contextPath}/donor/create" class="btn btn-primary btn-sm">
                <c:out value="${not empty msg['dash.newDonation'] ? msg['dash.newDonation'] : '+ Create Food Donation'}" />
            </a>
        </div>
    </div>

    <jsp:include page="../common/alert.jsp" />

    <!-- Tabs -->
    <div class="dashboard-tabs">
        <button class="tab-btn active" data-tab="activeDonationsTab">Active Donations (<c:out value="${donations.size()}" />)</button>
        <button class="tab-btn" data-tab="pendingRequestsTab">Pending Requests (<c:out value="${pendingRequests.size()}" />)</button>
        <button class="tab-btn" data-tab="scheduledPickupsTab">Scheduled Pickups (<c:out value="${activePickups.size()}" />)</button>
    </div>

    <!-- Active Donations Tab -->
    <div id="activeDonationsTab" class="tab-pane">
        <c:choose>
            <c:when test="${empty donations}">
                <div class="empty-state">
                    <div class="empty-state-icon">🍲</div>
                    <div class="empty-state-title">No Active Food Donations</div>
                    <div class="empty-state-text">You haven't listed any surplus food yet. Create your first donation to connect with local relief partners.</div>
                    <div style="margin-top: 1.5rem;">
                        <a href="${pageContext.request.contextPath}/donor/create" class="btn btn-primary">+ List Your First Food Donation</a>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="card-grid">
                    <c:forEach var="d" items="${donations}">
                        <div class="food-card" style="border-top-color: ${d.urgencyStatus == 'CRITICAL' ? '#ef4444' : (d.urgencyStatus == 'EXPIRING_SOON' ? '#f59e0b' : '#10b981')};">
                            <div class="food-card-header">
                                <div style="display: flex; align-items: center; gap: 0.4rem;">
                                    <span class="badge ${d.urgencyStatus == 'CRITICAL' ? 'badge-critical' : (d.urgencyStatus == 'EXPIRING_SOON' ? 'badge-expiring-soon' : 'badge-fresh')}">
                                        <c:out value="${d.urgencyStatus}" />
                                    </span>
                                    <span class="badge ${d.dietaryType == 'VEGETARIAN' || d.dietaryType == 'VEGAN' ? 'badge-fresh' : 'badge-standard'}" style="font-size: 0.72rem; padding: 0.2rem 0.5rem;">
                                        ${d.dietaryType == 'VEGETARIAN' ? '🌱 Veg' : (d.dietaryType == 'VEGAN' ? '🌿 Vegan' : '🍗 Non-Veg')}
                                    </span>
                                </div>
                                <div class="rescue-clock" data-expires-at="${d.availableUntil}">
                                    <span class="clock-display">⏱ Calculating...</span>
                                </div>
                            </div>

                            <div class="food-card-body">
                                <div style="display: flex; align-items: flex-start; gap: 0.65rem; margin-bottom: 0.5rem;">
                                    <span style="font-size: 1.6rem; line-height: 1;">
                                        ${d.categoryName == 'Prepared Cooked Meals' ? '🍲' : (d.categoryName == 'Baked Goods & Breads' ? '🥖' : (d.categoryName == 'Fresh Produce & Fruits' ? '🍎' : '🥗'))}
                                    </span>
                                    <h3 class="food-card-title"><c:out value="${d.title}" /></h3>
                                </div>
                                <div class="food-card-meta">
                                    <div><span>📦</span> <span><strong>Remaining:</strong> <c:out value="${d.remainingQuantity}" /> / <c:out value="${d.totalQuantity}" /> <c:out value="${d.unitName}" /></span></div>
                                    <div><span>🥗</span> <span><strong>Category:</strong> <c:out value="${d.categoryName}" /></span></div>
                                    <div><span>❄️</span> <span><strong>Storage:</strong> <c:out value="${d.storageCondition}" /> &bull; <c:out value="${d.packagingType}" /></span></div>
                                    <div><span>⏰</span> <span><strong>Expires:</strong> <c:out value="${d.availableUntil}" /></span></div>
                                </div>
                                <c:if test="${not empty d.priorityExplanation}">
                                    <p style="font-size: 0.82rem; color: var(--text-muted); margin-top: 0.5rem; line-height: 1.4;">
                                        <em><c:out value="${d.priorityExplanation}" /></em>
                                    </p>
                                </c:if>
                            </div>

                            <div class="food-card-footer">
                                <span class="badge badge-standard"><c:out value="${d.listingStatus}" /></span>
                                <a href="${pageContext.request.contextPath}/donor/view?id=${d.id}" class="btn btn-outline btn-sm">View Details &rarr;</a>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Pending Requests Tab -->
    <div id="pendingRequestsTab" class="tab-pane" style="display: none;">
        <c:choose>
            <c:when test="${empty pendingRequests}">
                <div class="empty-state">
                    <div class="empty-state-icon">📬</div>
                    <div class="empty-state-title">No Pending Requests</div>
                    <div class="empty-state-text">No pending requests from relief NGOs at this moment. You will be notified as soon as an organization requests your surplus.</div>
                </div>
            </c:when>
            <c:otherwise>
                <div style="display: flex; flex-direction: column; gap: 1.25rem;">
                    <c:forEach var="r" items="${pendingRequests}">
                        <div class="card" style="border-left: 5px solid var(--secondary); background: #ffffff;">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 0.5rem; margin-bottom: 0.75rem;">
                                <div>
                                    <h3 style="margin: 0; font-size: 1.2rem; color: var(--text-main);">
                                        <c:out value="${r.ngoName}" /> requested <c:out value="${r.requestedQuantity}" /> <c:out value="${r.unitName}" />
                                    </h3>
                                    <span style="font-size: 0.85rem; color: var(--text-muted);">
                                        Target Surplus: <strong><c:out value="${r.foodTitle}" /></strong> (Available Balance: <c:out value="${r.remainingQuantity}" />)
                                    </span>
                                </div>
                                <span class="badge badge-pending">Action Required</span>
                            </div>

                            <c:if test="${not empty r.requestNotes}">
                                <div style="background: var(--bg-main); padding: 0.85rem 1rem; border-radius: 8px; font-size: 0.9rem; margin: 0.5rem 0; border: 1px solid var(--border-color);">
                                    <strong>NGO Intake Note:</strong> "<em><c:out value="${r.requestNotes}" /></em>"
                                </div>
                            </c:if>

                            <div style="display: flex; gap: 0.75rem; margin-top: 1rem; flex-wrap: wrap;">
                                <form action="${pageContext.request.contextPath}/donor/approve" method="POST" style="display:inline;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                    <input type="hidden" name="requestId" value="${r.id}" />
                                    <button type="submit" class="btn btn-primary btn-sm" data-confirm="Approve request and reserve inventory? Volunteer dispatch will begin immediately.">
                                        ✓ Approve Request & Dispatch
                                    </button>
                                </form>

                                <form action="${pageContext.request.contextPath}/donor/reject" method="POST" style="display:inline;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                    <input type="hidden" name="requestId" value="${r.id}" />
                                    <input type="hidden" name="rejectionReason" value="Quantity reserved for other relief" />
                                    <button type="submit" class="btn btn-outline btn-sm" data-confirm="Reject this request?">
                                        &times; Decline Request
                                    </button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Scheduled Pickups Tab -->
    <div id="scheduledPickupsTab" class="tab-pane" style="display: none;">
        <c:choose>
            <c:when test="${empty activePickups}">
                <div class="empty-state">
                    <div class="empty-state-icon">🚚</div>
                    <div class="empty-state-title">No Scheduled Pickups Yet</div>
                    <div class="empty-state-text">When you approve an NGO request, pickup logistics and your cryptographic verification OTP will appear here.</div>
                </div>
            </c:when>
            <c:otherwise>
                <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                    <c:forEach var="p" items="${activePickups}">
                        <div class="card" style="border-left: 5px solid var(--primary); background: #ffffff;">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 0.5rem;">
                                <div>
                                    <h3 style="margin-bottom: 0.2rem; font-size: 1.25rem;"><c:out value="${p.foodTitle}" /></h3>
                                    <span style="color: var(--text-muted); font-size: 0.9rem;">
                                        Quantity: <strong><c:out value="${p.quantity}" /> <c:out value="${p.unitName}" /></strong> &bull;
                                        Destination: <strong><c:out value="${p.ngoName}" /></strong>
                                        <c:if test="${not empty p.volunteerName}">
                                            &bull; Courier: <strong><c:out value="${p.volunteerName}" /></strong>
                                        </c:if>
                                    </span>
                                </div>
                                <span class="badge badge-rescue-now"><c:out value="${p.status}" /></span>
                            </div>

                            <!-- OTP Box -->
                            <c:if test="${p.status == 'ASSIGNED' || p.status == 'NGO_SELF_PICKUP_ASSIGNED'}">
                                <div style="margin-top: 1.2rem; background: #ecfdf5; border: 1.5px dashed #10b981; border-radius: 10px; padding: 1rem 1.25rem; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem;">
                                    <div>
                                        <span style="color: #047857; font-weight: 700; font-size: 0.95rem; display: block; margin-bottom: 0.2rem;">
                                            🔑 Pickup Verification Handover Code:
                                        </span>
                                        <span style="font-size: 0.85rem; color: #065f46;">
                                            Provide your 6-digit verification code to the assigned courier upon physical handover to transfer custody.
                                        </span>
                                    </div>
                                    <div style="background: #ffffff; border: 1px solid #10b981; border-radius: 8px; padding: 0.4rem 1rem;">
                                        <code style="font-size: 1.2rem; font-weight: 800; color: #047857; letter-spacing: 2px;">• • • • • •</code>
                                    </div>
                                </div>
                            </c:if>

                            <c:if test="${p.status == 'PICKED_UP'}">
                                <div style="margin-top: 1rem; background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px; padding: 0.85rem 1.2rem; font-size: 0.9rem; color: #1e40af;">
                                    🚚 <strong>In Transit:</strong> Courier has picked up the food and is en route to the relief center.
                                </div>
                            </c:if>

                            <c:if test="${p.status == 'DELIVERED' || p.status == 'RECEIVED' || p.status == 'DISTRIBUTED'}">
                                <div style="margin-top: 1rem; background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 8px; padding: 0.85rem 1.2rem; font-size: 0.9rem; color: #065f46; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 0.75rem;">
                                    <span>✅ <strong>Rescue Completed:</strong> Food has safely reached the relief center.</span>
                                    <div style="display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap;">
                                        <a href="${pageContext.request.contextPath}/rescue/passport?pickupId=${p.id}" class="btn btn-primary btn-sm">🌍 Food Journey Passport</a>
                                        <a href="${pageContext.request.contextPath}/receipt/view?pickupId=${p.id}" class="btn btn-outline btn-sm">📜 View Receipt</a>
                                        <a href="${pageContext.request.contextPath}/complaint/file?pickupId=${p.id}" style="font-size: 0.8rem; color: #b33939; text-decoration: underline;">Report Issue</a>
                                    </div>
                                </div>
                                <div style="margin-top: 0.75rem; padding-top: 0.75rem; border-top: 1px dashed #e2e8f0;">
                                    <form action="${pageContext.request.contextPath}/feedback/submit" method="POST" style="display:inline-flex; align-items:center; gap:0.5rem; flex-wrap:wrap;">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                        <input type="hidden" name="pickupId" value="${p.id}" />
                                        <input type="hidden" name="feedbackType" value="NGO_RATING" />
                                        <span style="font-size: 0.85rem; font-weight: 600;">Rate Relief Partner:</span>
                                        <select name="rating" class="form-select" style="width:auto; padding:0.3rem 0.6rem; font-size:0.85rem;">
                                            <option value="5">⭐⭐⭐⭐⭐ (5 - Exceptional)</option>
                                            <option value="4">⭐⭐⭐⭐ (4 - Good)</option>
                                            <option value="3">⭐⭐⭐ (3 - Acceptable)</option>
                                            <option value="2">⭐⭐ (2 - Subpar)</option>
                                            <option value="1">⭐ (1 - Unsatisfactory)</option>
                                        </select>
                                        <input type="text" name="comment" placeholder="Optional comments..." style="padding:0.3rem 0.6rem; font-size:0.85rem; border-radius:6px; border:1px solid #ccc; min-width: 180px;" />
                                        <button type="submit" class="btn btn-outline btn-sm">Submit Review</button>
                                    </form>
                                </div>
                            </c:if>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
