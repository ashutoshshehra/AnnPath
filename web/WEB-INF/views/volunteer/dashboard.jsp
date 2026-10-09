<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Volunteer Dispatch Portal — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container dashboard-wrapper">
    <div class="dashboard-header">
        <div>
            <h2>Volunteer Rescue Hub</h2>
            <p style="color: var(--text-muted);">
                Courier: <strong><c:out value="${volunteerProfile.fullName}" /></strong> &bull;
                Vehicle: <strong><c:out value="${volunteerProfile.vehicleType}" /></strong> &bull;
                Max Distance: <strong><c:out value="${volunteerProfile.maxTravelDistanceKm}" /> km</strong>
            </p>
        </div>

        <!-- Availability Toggle & Profile Settings -->
        <div style="display: flex; gap: 0.8rem; align-items: center; flex-wrap: wrap;">
            <a href="${pageContext.request.contextPath}/volunteer/profile" class="btn btn-outline btn-sm">
                <c:out value="${not empty msg['dash.viewProfile'] ? msg['dash.viewProfile'] : '👤 Profile Settings'}" />
            </a>
            <form action="${pageContext.request.contextPath}/volunteer/availability" method="POST" style="display: flex; gap: 0.5rem; align-items: center; margin: 0;">
                <input type="hidden" name="csrfToken" value="${csrfToken}" />
                <span style="font-size: 0.9rem; font-weight: 600;">Status:</span>
                <select name="availabilityStatus" class="form-select" onchange="this.form.submit()" style="width: auto; padding: 0.4rem 0.8rem;">
                    <option value="AVAILABLE" ${volunteerProfile.availabilityStatus == 'AVAILABLE' ? 'selected' : ''}>🟢 AVAILABLE</option>
                    <option value="BUSY" ${volunteerProfile.availabilityStatus == 'BUSY' ? 'selected' : ''}>🟡 BUSY</option>
                    <option value="OFFLINE" ${volunteerProfile.availabilityStatus == 'OFFLINE' ? 'selected' : ''}>🔴 OFFLINE</option>
                </select>
            </form>
        </div>
    </div>

    <jsp:include page="../common/alert.jsp" />

    <!-- Separated Navigation Tabs -->
    <div class="dashboard-tabs">
        <button class="tab-btn active" data-tab="newDeliveriesTab">
            🚚 Active Missions (<c:out value="${openMissions.size() + newDeliveries.size()}" />)
        </button>
        <button class="tab-btn" data-tab="completedDeliveriesTab">
            ✅ Completed Deliveries (<c:out value="${completedDeliveries.size()}" />)
        </button>
    </div>

    <!-- TAB 1: NEW DELIVERIES (Broadcast Pickup Requests + Active In-Progress Tasks) -->
    <div id="newDeliveriesTab" class="tab-pane">

        <!-- Section A: Broadcast Pickup Requests Available to Accept -->
        <c:if test="${not empty openMissions}">
            <div style="background: rgba(234, 88, 12, 0.04); border: 1px solid rgba(234, 88, 12, 0.25); border-left: 5px solid #ea580c; border-radius: 12px; padding: 1.5rem; margin-bottom: 2rem;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.8rem; flex-wrap: wrap; gap: 0.5rem;">
                    <h3 style="margin: 0; color: #ea580c; font-size: 1.2rem; display: flex; align-items: center; gap: 0.5rem;">
                        <span>⚡</span> <span>Immediate Dispatch Broadcasts (<c:out value="${openMissions.size()}" />)</span>
                    </h3>
                    <span class="badge badge-rescue-now">
                        CLAIM TO LOCK EXCLUSIVE CUSTODY
                    </span>
                </div>
                <p style="margin: 0 0 1.25rem 0; font-size: 0.9rem; color: var(--text-muted);">
                    Donors approved these food rescue requests. Click <strong>✓ Accept Delivery Task</strong> to claim. Once accepted, the mission is locked to your route and hidden from other couriers.
                </p>

                <div style="display: flex; flex-direction: column; gap: 1.25rem;">
                    <c:forEach var="t" items="${openMissions}">
                        <div class="volunteer-mission-card">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 0.5rem;">
                                <div>
                                    <h3 style="margin-bottom: 0.3rem; font-size: 1.25rem;"><c:out value="${t.foodTitle}" /> (<c:out value="${t.quantity}" /> <c:out value="${t.unitName}" />)</h3>
                                    <span style="font-size: 0.88rem; color: var(--text-muted);">
                                        Category: <strong><c:out value="${t.foodCategory}" /></strong> &bull; Priority: <strong style="color: #ea580c;">READY FOR DISPATCH</strong>
                                    </span>
                                </div>
                                <span class="badge badge-rescue-now">
                                    AVAILABLE
                                </span>
                            </div>

                            <div class="mission-route-box">
                                <div class="route-point">
                                    <div class="route-icon route-pickup">A</div>
                                    <div class="route-info">
                                        <h4>PICKUP (DONOR)</h4>
                                        <p>
                                            <strong><c:out value="${t.donorName}" /></strong><br>
                                            <c:out value="${t.pickupAddress}" /><br>
                                            <a href="tel:${t.donorPhone}">📞 <c:out value="${t.donorPhone}" /></a>
                                        </p>
                                    </div>
                                </div>
                                <div class="route-point">
                                    <div class="route-icon route-drop">B</div>
                                    <div class="route-info">
                                        <h4>DELIVER TO (NGO)</h4>
                                        <p>
                                            <strong><c:out value="${t.ngoName}" /></strong><br>
                                            <c:out value="${t.ngoAddress}" /><br>
                                            <a href="tel:${t.ngoPhone}">📞 <c:out value="${t.ngoPhone}" /></a>
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.8rem; padding-top: 0.75rem; border-top: 1px dashed var(--border-color);">
                                <span style="font-size: 0.85rem; color: var(--text-muted);">
                                    First available volunteer claims the mission.
                                </span>
                                <form action="${pageContext.request.contextPath}/volunteer/claim" method="POST" style="margin: 0;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                    <input type="hidden" name="pickupId" value="${t.id}" />
                                    <button type="submit" class="btn btn-primary btn-sm" style="padding: 0.5rem 1.4rem; font-weight: 700; display: inline-flex; align-items: center; gap: 0.4rem;">
                                        ✓ Accept Delivery Task
                                    </button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </c:if>

        <!-- Section B: Active In-Progress Deliveries (Assigned to Logged-in Volunteer) -->
        <c:if test="${not empty newDeliveries}">
            <div style="margin-bottom: 1rem;">
                <h3 style="font-size: 1.2rem; color: var(--text-main); margin-bottom: 0.8rem; display: flex; align-items: center; gap: 0.5rem;">
                    <span>🚚</span> <span>My Active Tasks In-Progress (<c:out value="${newDeliveries.size()}" />)</span>
                </h3>
            </div>

            <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                <c:forEach var="t" items="${newDeliveries}">
                    <div class="volunteer-mission-card" style="border-left: 5px solid var(--secondary);">
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 0.5rem;">
                            <div>
                                <h3 style="margin-bottom: 0.3rem; font-size: 1.25rem;"><c:out value="${t.foodTitle}" /> (<c:out value="${t.quantity}" /> <c:out value="${t.unitName}" />)</h3>
                                <span style="font-size: 0.88rem; color: var(--text-muted);">
                                    Category: <strong><c:out value="${t.foodCategory}" /></strong> &bull; Assigned: <c:out value="${t.assignedAt}" />
                                </span>
                            </div>
                            <span class="badge ${t.status == 'PICKED_UP' ? 'badge-fresh' : 'badge-rescue-now'}">
                                <c:out value="${t.status}" />
                            </span>
                        </div>

                        <div class="mission-route-box">
                            <div class="route-point">
                                <div class="route-icon route-pickup">A</div>
                                <div class="route-info">
                                    <h4>PICKUP LOCATION</h4>
                                    <p>
                                        <strong><c:out value="${t.donorName}" /></strong><br>
                                        <c:out value="${t.pickupAddress}" /><br>
                                        <a href="tel:${t.donorPhone}">📞 <c:out value="${t.donorPhone}" /></a>
                                    </p>
                                </div>
                            </div>
                            <div class="route-point">
                                <div class="route-icon route-drop">B</div>
                                <div class="route-info">
                                    <h4>DELIVERY DESTINATION</h4>
                                    <p>
                                        <strong><c:out value="${t.ngoName}" /></strong><br>
                                        <c:out value="${t.ngoAddress}" /><br>
                                        <a href="tel:${t.ngoPhone}">📞 <c:out value="${t.ngoPhone}" /></a>
                                    </p>
                                </div>
                            </div>
                        </div>

                        <!-- Action Controls based on active state -->
                        <div style="display: flex; gap: 0.8rem; align-items: center; flex-wrap: wrap; padding-top: 0.5rem;">
                            <c:if test="${t.status == 'ASSIGNED' or t.status == 'VOLUNTEER_ASSIGNED'}">
                                <form action="${pageContext.request.contextPath}/volunteer/reject" method="POST" style="display:inline;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                    <input type="hidden" name="pickupId" value="${t.id}" />
                                    <input type="hidden" name="rejectionReason" value="Volunteer unavailable" />
                                    <button type="submit" class="btn btn-outline btn-sm" data-confirm="Decline this mission? It will be re-broadcast for other couriers to claim.">
                                        &times; Decline Task
                                    </button>
                                </form>

                                <!-- Confirm Pickup with Donor OTP -->
                                <form action="${pageContext.request.contextPath}/volunteer/pickup" method="POST" style="display:flex; gap:0.5rem; align-items: center; margin-left: auto; flex-wrap: wrap;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                    <input type="hidden" name="pickupId" value="${t.id}" />
                                    <input type="text" name="pickupOtp" class="form-control" required maxlength="6" placeholder="Donor's 6-digit OTP" style="width: 170px; padding: 0.45rem;" />
                                    <button type="submit" class="btn btn-secondary btn-sm">Confirm Pickup Handover &rarr;</button>
                                </form>
                            </c:if>

                            <c:if test="${t.status == 'PICKED_UP'}">
                                <div style="background: rgba(59, 130, 246, 0.08); border-left: 4px solid #3b82f6; padding: 0.8rem 1.2rem; border-radius: 8px; width: 100%; margin-bottom: 0.5rem; font-size: 0.9rem;">
                                    🚚 <strong>In Transit:</strong> Food is in your physical custody. Proceed safely to the NGO relief center and ask their intake manager for the 6-digit Delivery Acceptance OTP.
                                </div>
                                <form action="${pageContext.request.contextPath}/volunteer/deliver" method="POST" style="display:flex; gap:0.6rem; align-items: center; width: 100%; flex-wrap: wrap;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                    <input type="hidden" name="pickupId" value="${t.id}" />
                                    <input type="text" name="deliveryOtp" class="form-control" required maxlength="6" placeholder="Enter NGO's 6-digit Delivery OTP" style="width: 270px; padding: 0.45rem;" />
                                    <button type="submit" class="btn btn-primary btn-sm">Verify & Complete Delivery &rarr;</button>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:if>

        <!-- Empty state when both open missions and active deliveries are empty -->
        <c:if test="${empty openMissions and empty newDeliveries}">
            <div class="empty-state">
                <div class="empty-state-icon">🚚</div>
                <div class="empty-state-title">No Active Missions Pending</div>
                <div class="empty-state-text">
                    When donors approve food rescue requests, new dispatch tasks will appear here in real-time. Keep your status set to <strong>AVAILABLE</strong>!
                </div>
            </div>
        </c:if>
    </div>

    <!-- TAB 2: COMPLETED DELIVERIES (Delivered & Distributed History) -->
    <div id="completedDeliveriesTab" class="tab-pane" style="display: none;">
        <c:choose>
            <c:when test="${empty completedDeliveries}">
                <div class="empty-state">
                    <div class="empty-state-icon">📜</div>
                    <div class="empty-state-title">No Completed Deliveries Yet</div>
                    <div class="empty-state-text">
                        Completed food rescues, digital handover receipts, and certified impact records will be archived here.
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                    <c:forEach var="t" items="${completedDeliveries}">
                        <div class="card" style="border-left: 5px solid var(--primary); background: #ffffff;">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 0.5rem;">
                                <div>
                                    <h3 style="margin-bottom: 0.3rem; font-size: 1.2rem;"><c:out value="${t.foodTitle}" /> (<c:out value="${t.quantity}" /> <c:out value="${t.unitName}" />)</h3>
                                    <span style="font-size: 0.88rem; color: var(--text-muted);">
                                        Rescued from <strong><c:out value="${t.donorName}" /></strong> &bull; Delivered to <strong><c:out value="${t.ngoName}" /></strong>
                                    </span>
                                </div>
                                <span class="badge badge-fresh">
                                    ✅ <c:out value="${t.status}" />
                                </span>
                            </div>

                            <div class="mission-route-box" style="margin: 1rem 0;">
                                <div class="route-point">
                                    <div class="route-icon route-pickup">A</div>
                                    <div class="route-info">
                                        <h4>PICKUP LOCATION</h4>
                                        <p>
                                            <c:out value="${t.pickupAddress}" /><br>
                                            Donor: <c:out value="${t.donorName}" />
                                        </p>
                                    </div>
                                </div>
                                <div class="route-point">
                                    <div class="route-icon route-drop">B</div>
                                    <div class="route-info">
                                        <h4>RELIEF DESTINATION</h4>
                                        <p>
                                            <c:out value="${t.ngoAddress}" /><br>
                                            NGO Center: <c:out value="${t.ngoName}" />
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.6rem; padding-top: 0.75rem; border-top: 1px dashed #e2e8f0;">
                                <span style="font-size: 0.88rem; color: var(--primary); font-weight: 600;">
                                    ✓ Mission Delivered Successfully
                                </span>
                                <div style="display: flex; gap: 0.6rem; align-items: center; flex-wrap: wrap;">
                                    <a href="${pageContext.request.contextPath}/rescue/passport?pickupId=${t.id}" class="btn btn-primary btn-sm">
                                        🌍 Food Journey Passport
                                    </a>
                                    <a href="${pageContext.request.contextPath}/receipt/view?pickupId=${t.id}" class="btn btn-outline btn-sm">
                                        📜 View Receipt
                                    </a>
                                    <a href="${pageContext.request.contextPath}/complaint/file?pickupId=${t.id}" style="font-size: 0.85rem; color: #b33939; text-decoration: underline;">
                                        Report Issue
                                    </a>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
