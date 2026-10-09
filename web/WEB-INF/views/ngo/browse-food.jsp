<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Browse Live Surplus — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container dashboard-wrapper" style="padding-bottom: 4rem;">
    <div class="dashboard-header">
        <div>
            <h2>Browse Live Food Surplus</h2>
            <p style="color: var(--text-muted);">Explore verified unreserved donations within your region in real-time.</p>
        </div>
        <div style="display: flex; gap: 0.6rem; align-items: center; flex-wrap: wrap;">
            <button type="button" id="btnToggleMapView" class="btn btn-outline btn-sm">
                🗺️ Switch to Map View
            </button>
            <a href="${pageContext.request.contextPath}/ngo/dashboard" class="btn btn-outline btn-sm">&larr; Back to Dashboard</a>
        </div>
    </div>

    <jsp:include page="../common/alert.jsp" />

    <c:if test="${isRegionalFallback}">
        <div class="alert alert-info" style="margin-bottom: 1.5rem; background: rgba(59, 130, 246, 0.08); border-left: 4px solid #3b82f6; padding: 0.9rem 1.25rem; border-radius: 8px;">
            <strong>🌐 Regional Active Donations:</strong> Showing live surplus food available across wider regions. You can request any available batch below.
        </div>
    </c:if>

    <!-- Category Filter Chips -->
    <div style="display: flex; gap: 0.5rem; overflow-x: auto; padding-bottom: 0.75rem; margin-bottom: 1.5rem; -webkit-overflow-scrolling: touch;">
        <a href="${pageContext.request.contextPath}/ngo/browse" class="filter-chip ${empty param.categoryId ? 'active' : ''}">
            All Surplus
        </a>
        <c:forEach var="cat" items="${categories}">
            <a href="${pageContext.request.contextPath}/ngo/browse?categoryId=${cat.id}"
               class="filter-chip ${param.categoryId == cat.id ? 'active' : ''}">
                <c:out value="${cat.categoryName}" />
            </a>
        </c:forEach>
    </div>

    <!-- Map View Container (Toggled via button) -->
    <div id="surplusMapViewWrapper" style="display: none; margin-bottom: 2rem;">
        <div class="card" style="padding: 1.25rem; background: #ffffff; border: 1px solid var(--border-color); box-shadow: 0 4px 15px rgba(0,0,0,0.05);">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.9rem; flex-wrap: wrap; gap: 0.75rem;">
                <div>
                    <span style="font-weight: 700; color: var(--primary); font-size: 1.1rem; display: flex; align-items: center; gap: 0.5rem;">
                        🗺️ Interactive Regional Surplus Map
                    </span>
                    <span style="display: block; font-size: 0.82rem; color: var(--text-muted); margin-top: 2px;">
                        Live visualization of donor surplus locations and rescue urgency tiers
                    </span>
                </div>
                <div class="map-legend" style="display: flex; gap: 0.75rem; align-items: center; font-size: 0.8rem; background: #f8fafc; padding: 0.4rem 0.8rem; border-radius: 20px; border: 1px solid var(--border-color); flex-wrap: wrap;">
                    <span style="display: inline-flex; align-items: center; gap: 5px;"><span style="display:inline-block; width:10px; height:10px; border-radius:50%; background:#10b981;"></span> Fresh</span>
                    <span style="display: inline-flex; align-items: center; gap: 5px;"><span style="display:inline-block; width:10px; height:10px; border-radius:50%; background:#f59e0b;"></span> Expiring Soon</span>
                    <span style="display: inline-flex; align-items: center; gap: 5px;"><span style="display:inline-block; width:10px; height:10px; border-radius:50%; background:#ef4444;"></span> Critical</span>
                    <c:if test="${ngoProfile != null && ngoProfile.latitude != null}">
                        <span style="display: inline-flex; align-items: center; gap: 5px;"><span style="display:inline-block; width:10px; height:10px; border-radius:50%; background:#2563eb;"></span> NGO Base</span>
                    </c:if>
                </div>
            </div>

            <c:if test="${empty ngoProfile.latitude || empty ngoProfile.longitude}">
                <div style="background: #fffbeb; border-left: 3px solid #f59e0b; padding: 0.65rem 1rem; margin-bottom: 0.85rem; border-radius: 6px; font-size: 0.85rem; color: #92400e; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 0.5rem;">
                    <span>📍 <strong>Location Notice:</strong> NGO facility GPS coordinates are not configured in your profile. Displaying all regional surplus donation pins.</span>
                    <a href="${pageContext.request.contextPath}/ngo/profile" style="color: var(--primary); font-weight: 700; text-decoration: underline;">Set Coordinates in Profile &rarr;</a>
                </div>
            </c:if>

            <div id="rescueMap" style="height: 520px; width: 100%; border-radius: 8px; border: 1px solid var(--border-color); position: relative; z-index: 1;"
                 data-center-lat="${ngoProfile.latitude != null ? ngoProfile.latitude : ''}"
                 data-center-lng="${ngoProfile.longitude != null ? ngoProfile.longitude : ''}"
                 data-ngo-name="<c:out value='${ngoProfile.organizationName != null ? ngoProfile.organizationName : \"Your NGO Base\"}' />"
                 data-zoom="12">
            </div>
        </div>
    </div>

    <!-- Grid View of Food Surplus Cards -->
    <div id="surplusGridView">
        <c:choose>
            <c:when test="${empty availableListings}">
                <div class="empty-state">
                    <div class="empty-state-icon">🌱</div>
                    <div class="empty-state-title">No Surplus Food Listed Right Now</div>
                    <div class="empty-state-text">All recent donations have been rescued or matched. Please check back shortly as donors publish new surplus batches.</div>
                    <div style="margin-top: 1.5rem;">
                        <a href="${pageContext.request.contextPath}/ngo/dashboard" class="btn btn-primary">Return to Dashboard</a>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="card-grid">
                    <c:forEach var="fl" items="${availableListings}">
                        <!-- Data element for Leaflet map pin marker -->
                        <div class="food-card"
                             id="listing-${fl.id}"
                             data-map-item="true"
                             data-listing-id="${fl.id}"
                             data-lat="${fl.pickupLatitude}"
                             data-lng="${fl.pickupLongitude}"
                             data-title="<c:out value='${fl.title}' />"
                             data-urgency="${fl.urgencyStatus}"
                             data-quantity="<c:out value='${fl.remainingQuantity}' /> <c:out value='${fl.unitName}' />"
                             data-category="<c:out value='${fl.categoryName}' />"
                             data-donor="<c:out value='${fl.donorName}' />"
                             data-city="<c:out value='${fl.pickupCity}' />"
                             data-address="<c:out value='${fl.pickupAddress}' />"
                             data-distance="<fmt:formatNumber value='${fl.distanceKm}' maxFractionDigits='1' />"
                             data-action-url="${pageContext.request.contextPath}/ngo/browse#listing-${fl.id}"
                             style="border-top-color: ${fl.urgencyStatus == 'CRITICAL' ? '#ef4444' : (fl.urgencyStatus == 'EXPIRING_SOON' ? '#f59e0b' : '#10b981')};">

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
                                    <div><span>📦</span> <span><strong>Remaining:</strong> <c:out value="${fl.remainingQuantity}" /> <c:out value="${fl.unitName}" /></span></div>
                                    <div><span>🥗</span> <span><strong>Category:</strong> <c:out value="${fl.categoryName}" /></span></div>
                                    <div><span>🏨</span> <span><strong>Donor:</strong> <c:out value="${fl.donorName}" /></span></div>
                                    <div><span>📍</span> <span><strong>Location:</strong> <c:out value="${fl.pickupCity}" /> (<c:out value="${fl.pickupAddress}" />)</span></div>
                                    <c:if test="${fl.distanceKm > 0}">
                                        <div><span>📏</span> <span><strong>Proximity:</strong> <fmt:formatNumber value="${fl.distanceKm}" maxFractionDigits="1" /> km away</span></div>
                                    </c:if>
                                </div>
                                <c:if test="${not empty fl.priorityExplanation}">
                                    <p style="font-size: 0.82rem; color: var(--text-muted); margin-top: 0.5rem; line-height: 1.4;">
                                        <em><c:out value="${fl.priorityExplanation}" /></em>
                                    </p>
                                </c:if>
                            </div>

                            <div class="food-card-footer">
                                <c:choose>
                                    <c:when test="${ngoProfile.verificationStatus == 'VERIFIED'}">
                                        <form action="${pageContext.request.contextPath}/ngo/request" method="POST" style="display: flex; gap: 0.5rem; width: 100%; align-items: center;">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                            <input type="hidden" name="listingId" value="${fl.id}" />
                                            <input type="number" name="requestedQuantity" max="${fl.remainingQuantity}" min="1" step="0.1"
                                                   value="${fl.remainingQuantity}" class="form-control" style="width: 90px; padding: 0.4rem;" required />
                                            <button type="submit" class="btn btn-secondary btn-sm" style="flex: 1; white-space: nowrap;">Request Surplus &rarr;</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
                                            <span style="font-size: 0.8rem; color: var(--text-muted);">Verification required to request</span>
                                            <a href="${pageContext.request.contextPath}/ngo/verify" class="btn btn-outline btn-sm" style="font-size: 0.75rem;">Verify NGO</a>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<!-- Leaflet & Rescue Map Scripts -->
<script src="${pageContext.request.contextPath}/assets/vendor/leaflet/leaflet.js"></script>
<script>
    if (typeof L === 'undefined') {
        document.write('<script src="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.js"><\/script>');
    }
</script>
<script src="${pageContext.request.contextPath}/assets/js/rescue-map.js"></script>

<jsp:include page="../common/footer.jsp" />
