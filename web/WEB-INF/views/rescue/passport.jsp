<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Food Journey Passport — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="padding: 2.5rem 1rem;">
    <!-- Action Bar -->
    <div style="max-width: 900px; margin: 0 auto 1.5rem; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
        <a href="javascript:history.back()" class="btn btn-outline btn-sm">&larr; Back to Dashboard</a>
        <div style="display: flex; gap: 0.5rem;">
            <button onclick="window.print()" class="btn btn-primary btn-sm">🖨️ Print / Save Passport PDF</button>
            <c:if test="${not empty receipt}">
                <a href="${pageContext.request.contextPath}/receipt/view?id=${receipt.receiptNumber}" class="btn btn-outline btn-sm">📜 View Legal Receipt</a>
            </c:if>
        </div>
    </div>

<c:choose>
    <c:when test="${rescueNotFound or (empty pickup and empty listing)}">
        <div class="passport-container" style="max-width: 720px; margin: 2rem auto; background: var(--bg-surface, #fff); border: 1px solid var(--border-color, #e2e8f0); border-radius: 16px; padding: 3.5rem 2rem; text-align: center; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05);">
            <div style="font-size: 3.5rem; margin-bottom: 1rem;">🔍</div>
            <h2 style="color: var(--text-main); margin-bottom: 0.5rem; font-size: 1.6rem;">Rescue Mission Passport Not Found</h2>
            <p style="color: var(--text-muted); max-width: 500px; margin: 0.5rem auto 1.75rem; line-height: 1.6; font-size: 0.95rem;">
                A valid rescue identifier is required to view an immutable Food Journey Passport. Please verify the Rescue ID code (e.g. <code>?code=ANP-2026-000101</code>) or Mission ID (e.g. <code>?pickupId=101</code>).
            </p>
            <div style="display: flex; gap: 0.75rem; justify-content: center; flex-wrap: wrap;">
                <a href="${pageContext.request.contextPath}/home" class="btn btn-outline btn-sm">&larr; Return to Homepage</a>
                <a href="${pageContext.request.contextPath}/impact" class="btn btn-primary btn-sm">View Platform Impact &rarr;</a>
            </div>
        </div>
    </c:when>
    <c:otherwise>
    <!-- Passport Card -->
    <div class="passport-container" style="max-width: 900px; margin: 0 auto; background: var(--bg-surface, #fff); border: 1px solid var(--border-color, #e2e8f0); border-radius: 16px; padding: 2rem; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05);">
        <!-- Header -->
        <div class="passport-header" style="display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 2px solid var(--border-color, #f1f5f9); padding-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem;">
            <div style="display: flex; align-items: center; gap: 1rem;">
                <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="AnnPath" style="height: 48px; object-fit: contain;">
                <div>
                    <h2 style="margin: 0; font-size: 1.6rem; color: var(--primary, #176b4d);">Food Journey Passport</h2>
                    <span style="font-size: 0.85rem; color: var(--text-muted, #64748b);">Verified Immutable Food Rescue Audit</span>
                </div>
            </div>
            <div style="text-align: right;">
                <span class="passport-badge" style="background: rgba(23, 107, 77, 0.1); color: var(--primary, #176b4d); padding: 0.35rem 0.8rem; border-radius: 9999px; font-weight: 700; font-size: 0.85rem;">✓ VERIFIED IMMUTABLE PASSPORT</span>
                <div style="font-size: 0.95rem; font-weight: 800; color: var(--text-main, #0f172a); margin-top: 0.4rem; font-family: monospace;">
                    Rescue ID: <c:out value="${rescueCode}" />
                </div>
            </div>
        </div>

        <!-- Autopilot Operational Intelligence Badge -->
        <c:if test="${not empty autopilotDecision}">
            <div style="background: #f8fafc; border-left: 4px solid ${autopilotDecision.riskLevel == 'CRITICAL' ? '#ef4444' : (autopilotDecision.riskLevel == 'HIGH' ? '#f59e0b' : '#10b981')}; border-radius: 8px; padding: 1.25rem; margin-top: 1.5rem;">
                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem; margin-bottom: 0.5rem;">
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                        <span style="font-size: 1.1rem;">🤖</span>
                        <strong style="font-size: 0.95rem; color: var(--text-main, #0f172a);">Rescue Autopilot Recommendation:</strong>
                        <span style="font-weight: 700; color: var(--primary, #176b4d);"><c:out value="${autopilotDecision.actionLabel}" /></span>
                    </div>
                    <div>
                        <span style="font-size: 0.75rem; font-weight: 700; padding: 0.25rem 0.6rem; border-radius: 6px; background: ${autopilotDecision.riskLevel == 'CRITICAL' ? '#fee2e2; color: #b91c1c;' : (autopilotDecision.riskLevel == 'HIGH' ? '#fef3c7; color: #b45309;' : '#d1fae5; color: #047857;')}">
                            Risk: <c:out value="${autopilotDecision.riskLevel}" /> (<c:out value="${autopilotDecision.riskScore}" />/100)
                        </span>
                    </div>
                </div>
                <p style="margin: 0; font-size: 0.85rem; color: var(--text-muted, #475569); line-height: 1.5;">
                    <c:out value="${autopilotDecision.rationale}" />
                </p>
                <c:if test="${not empty autopilotDecision.warnings}">
                    <div style="margin-top: 0.5rem; display: flex; flex-direction: column; gap: 0.25rem;">
                        <c:forEach items="${autopilotDecision.warnings}" var="w">
                            <span style="font-size: 0.8rem; color: #b45309;">⚠️ <c:out value="${w}" /></span>
                        </c:forEach>
                    </div>
                </c:if>
            </div>
        </c:if>

        <!-- Food Item Overview -->
        <div style="background: var(--bg-main, #f8fafc); border-radius: 12px; padding: 1.5rem; margin: 1.5rem 0; display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.2rem;">
            <div>
                <span style="font-size: 0.78rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted, #64748b); letter-spacing: 0.5px;">Food Description</span>
                <div style="font-size: 1.15rem; font-weight: 700; color: var(--text-main, #0f172a); margin-top: 0.2rem;">
                    <c:out value="${pickup != null ? pickup.foodTitle : listing.title}" />
                </div>
                <div style="font-size: 0.85rem; color: var(--primary, #176b4d); font-weight: 600; margin-top: 0.2rem;">
                    <c:out value="${pickup != null ? pickup.foodCategory : listing.categoryName}" /> &bull; <c:out value="${listing != null ? listing.dietaryType : 'Standard'}" />
                </div>
            </div>
            <div>
                <span style="font-size: 0.78rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted, #64748b); letter-spacing: 0.5px;">Surplus Quantity</span>
                <div style="font-size: 1.25rem; font-weight: 800; color: var(--secondary, #d97706); margin-top: 0.2rem;">
                    <c:out value="${pickup != null ? pickup.quantity : listing.totalQuantity}" /> <c:out value="${pickup != null ? pickup.unitName : listing.unitName}" />
                </div>
                <div style="font-size: 0.85rem; color: var(--text-muted, #64748b); margin-top: 0.2rem;">
                    100% Verified Surplus
                </div>
            </div>
            <div>
                <span style="font-size: 0.78rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted, #64748b); letter-spacing: 0.5px;">Current Stage</span>
                <div style="margin-top: 0.4rem;">
                    <span class="badge" style="font-size: 0.9rem; padding: 0.4rem 0.8rem; background: #e0f2fe; color: #0369a1; border-radius: 6px; font-weight: 700;">
                        <c:out value="${pickup != null ? pickup.status : listing.listingStatus}" />
                    </span>
                </div>
            </div>
        </div>

        <!-- Participants Route Grid -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 1.5rem; margin-bottom: 2.5rem;">
            <div style="border: 1px solid var(--border-color, #e2e8f0); border-radius: 10px; padding: 1.25rem;">
                <span style="font-size: 0.8rem; font-weight: 700; color: var(--primary, #176b4d); text-transform: uppercase;">Origin Donor</span>
                <h4 style="margin: 0.4rem 0 0.2rem;"><c:out value="${pickup != null ? pickup.donorName : listing.donorName}" /></h4>
                <p style="margin: 0; font-size: 0.85rem; color: var(--text-muted, #64748b);">
                    📍 <c:out value="${pickup != null ? pickup.pickupAddress : listing.pickupAddress}" />
                </p>
            </div>
            <div style="border: 1px solid var(--border-color, #e2e8f0); border-radius: 10px; padding: 1.25rem;">
                <span style="font-size: 0.8rem; font-weight: 700; color: var(--secondary, #d97706); text-transform: uppercase;">Transit Courier</span>
                <h4 style="margin: 0.4rem 0 0.2rem;">
                    <c:choose>
                        <c:when test="${not empty pickup and not empty pickup.volunteerName}">
                            <c:out value="${pickup.volunteerName}" /> (Volunteer)
                        </c:when>
                        <c:when test="${not empty pickup and not empty pickup.selfPickupPersonName}">
                            <c:out value="${pickup.selfPickupPersonName}" /> (NGO Representative)
                        </c:when>
                        <c:otherwise>
                            Assigned upon dispatch
                        </c:otherwise>
                    </c:choose>
                </h4>
                <p style="margin: 0; font-size: 0.85rem; color: var(--text-muted, #64748b);">
                    🚀 Cryptographic Dual-OTP Verified Handover
                </p>
            </div>
            <div style="border: 1px solid var(--border-color, #e2e8f0); border-radius: 10px; padding: 1.25rem;">
                <span style="font-size: 0.8rem; font-weight: 700; color: var(--primary, #176b4d); text-transform: uppercase;">Destination NGO</span>
                <h4 style="margin: 0.4rem 0 0.2rem;"><c:out value="${pickup != null ? pickup.ngoName : 'Matching Candidate NGO'}" /></h4>
                <p style="margin: 0; font-size: 0.85rem; color: var(--text-muted, #64748b);">
                    🎯 <c:out value="${pickup != null ? pickup.ngoAddress : 'Verified Community Food Center'}" />
                </p>
            </div>
        </div>

        <!-- Milestones Lifecycle (Backed by Immutable food_status_history) -->
        <h3 style="margin-bottom: 1.5rem; font-size: 1.25rem; display: flex; align-items: center; gap: 0.5rem; color: var(--text-main, #0f172a);">
            <span>⏱️</span> <span>Lifecycle Audit Milestones</span>
        </h3>

        <div style="border-left: 3px solid var(--primary, #176b4d); margin-left: 1.5rem; padding-left: 2rem; display: flex; flex-direction: column; gap: 1.75rem; margin-bottom: 3rem;">
            <c:choose>
                <c:when test="${not empty journeyEvents}">
                    <c:forEach items="${journeyEvents}" var="evt" varStatus="st">
                        <div style="position: relative;">
                            <div style="position: absolute; left: -2.75rem; top: 0; width: 24px; height: 24px; border-radius: 50%; background: var(--primary, #176b4d); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 0.75rem; font-weight: 700;">
                                <c:out value="${st.count}" />
                            </div>
                            <div style="display: flex; justify-content: space-between; align-items: baseline; flex-wrap: wrap; gap: 0.5rem;">
                                <div style="font-weight: 700; font-size: 1rem; color: var(--text-main, #0f172a);">
                                    <c:out value="${evt.newStatus}" />
                                </div>
                                <span style="font-size: 0.8rem; color: var(--text-muted, #64748b); font-family: monospace;">
                                    <c:out value="${evt.createdAt}" />
                                </span>
                            </div>
                            <div style="font-size: 0.85rem; color: var(--text-muted, #64748b); margin-top: 0.2rem;">
                                <c:out value="${evt.transitionReason}" /> &bull; Actor: <strong><c:out value="${evt.actorRole}" /></strong>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <!-- Milestone 1: Created -->
                    <div style="position: relative;">
                        <div style="position: absolute; left: -2.75rem; top: 0; width: 22px; height: 22px; border-radius: 50%; background: var(--primary, #176b4d); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 0.75rem; font-weight: 700;">1</div>
                        <div style="font-weight: 700; font-size: 1rem; color: var(--text-main, #0f172a);">Donation Created & Verified</div>
                        <div style="font-size: 0.85rem; color: var(--text-muted, #64748b); margin-top: 0.2rem;">
                            Surplus listed &bull; Logged at: <strong><c:out value="${listing != null ? listing.createdAt : pickup.createdAt}" /></strong>
                        </div>
                    </div>

                    <!-- Milestone 2: Request & Match -->
                    <div style="position: relative;">
                        <div style="position: absolute; left: -2.75rem; top: 0; width: 22px; height: 22px; border-radius: 50%; background: ${not empty pickup ? 'var(--primary, #176b4d)' : '#9ca3af'}; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 0.75rem; font-weight: 700;">2</div>
                        <div style="font-weight: 700; font-size: 1rem; color: var(--text-main, #0f172a);">NGO Request Matched & Approved</div>
                        <div style="font-size: 0.85rem; color: var(--text-muted, #64748b); margin-top: 0.2rem;">
                            <c:choose>
                                <c:when test="${not empty pickup}">
                                    Matched with <c:out value="${pickup.ngoName}" /> &bull; Approved: <strong><c:out value="${pickup.createdAt}" /></strong>
                                </c:when>
                                <c:otherwise>Pending matching with eligible registered NGO</c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- Milestone 3: Dispatch & Assignment -->
                    <div style="position: relative;">
                        <div style="position: absolute; left: -2.75rem; top: 0; width: 22px; height: 22px; border-radius: 50%; background: ${not empty pickup and (not empty pickup.volunteerUserId or not empty pickup.selfPickupPersonName) ? 'var(--primary, #176b4d)' : '#9ca3af'}; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 0.75rem; font-weight: 700;">3</div>
                        <div style="font-weight: 700; font-size: 1rem; color: var(--text-main, #0f172a);">Courier Assigned to Mission</div>
                        <div style="font-size: 0.85rem; color: var(--text-muted, #64748b); margin-top: 0.2rem;">
                            <c:choose>
                                <c:when test="${not empty pickup and not empty pickup.volunteerName}">
                                    Assigned to <c:out value="${pickup.volunteerName}" /> &bull; Timestamp: <strong><c:out value="${pickup.assignedAt != null ? pickup.assignedAt : pickup.createdAt}" /></strong>
                                </c:when>
                                <c:otherwise>Awaiting volunteer acceptance or self-pickup assignment</c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- Milestone 4: Physical Pickup -->
                    <div style="position: relative;">
                        <div style="position: absolute; left: -2.75rem; top: 0; width: 22px; height: 22px; border-radius: 50%; background: ${not empty pickup and not empty pickup.actualPickedUpAt ? 'var(--primary, #176b4d)' : '#9ca3af'}; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 0.75rem; font-weight: 700;">4</div>
                        <div style="font-weight: 700; font-size: 1rem; color: var(--text-main, #0f172a);">Pickup Verification & Custody Transfer</div>
                        <div style="font-size: 0.85rem; color: var(--text-muted, #64748b); margin-top: 0.2rem;">
                            <c:choose>
                                <c:when test="${not empty pickup and not empty pickup.actualPickedUpAt}">
                                    ✓ Verified via Donor Cryptographic OTP at <strong><c:out value="${pickup.actualPickedUpAt}" /></strong>
                                </c:when>
                                <c:otherwise>Pending OTP confirmation at donor pickup location</c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- Milestone 5: Delivery & Distribution -->
                    <div style="position: relative;">
                        <div style="position: absolute; left: -2.75rem; top: 0; width: 22px; height: 22px; border-radius: 50%; background: ${not empty pickup and not empty pickup.actualDeliveredAt ? 'var(--primary, #176b4d)' : '#9ca3af'}; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 0.75rem; font-weight: 700;">5</div>
                        <div style="font-weight: 700; font-size: 1rem; color: var(--text-main, #0f172a);">Safe Delivery & Distribution at NGO</div>
                        <div style="font-size: 0.85rem; color: var(--text-muted, #64748b); margin-top: 0.2rem;">
                            <c:choose>
                                <c:when test="${not empty pickup and not empty pickup.actualDeliveredAt}">
                                    ✓ Verified via NGO Cryptographic OTP at <strong><c:out value="${pickup.actualDeliveredAt}" /></strong>
                                </c:when>
                                <c:otherwise>In transit &bull; Scheduled delivery window active</c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Social & Environmental Impact Scorecard -->
        <div style="background: linear-gradient(135deg, rgba(23, 107, 77, 0.06), rgba(242, 140, 40, 0.06)); border: 1px solid rgba(23, 107, 77, 0.2); border-radius: 12px; padding: 1.75rem; margin-bottom: 2.5rem;">
            <h4 style="margin: 0 0 1.25rem 0; color: var(--primary, #176b4d); font-size: 1.1rem; text-align: center;">🌍 Certified Environmental & Social Contribution</h4>
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1.5rem; text-align: center;">
                <div>
                    <div style="font-size: 1.8rem; font-weight: 800; color: var(--primary, #176b4d);">
                        <c:out value="${pickup != null ? pickup.quantity : listing.totalQuantity}" /> <c:out value="${pickup != null ? pickup.unitName : listing.unitName}" />
                    </div>
                    <div style="font-size: 0.85rem; color: var(--text-muted, #64748b); margin-top: 0.2rem;">Surplus Food Rescued</div>
                </div>
                <div>
                    <div style="font-size: 1.8rem; font-weight: 800; color: var(--secondary, #d97706);">
                        <c:out value="${receipt != null && receipt.estimatedPeopleFed != null ? receipt.estimatedPeopleFed : '—'}" />
                    </div>
                    <div style="font-size: 0.85rem; color: var(--text-muted, #64748b); margin-top: 0.2rem;">Nourishing Meals Created</div>
                </div>
                <div>
                    <div style="font-size: 1.8rem; font-weight: 800; color: #10b981;">
                        <c:out value="${receipt != null && receipt.co2SavedKg != null ? receipt.co2SavedKg : '—'}" /> kg
                    </div>
                    <div style="font-size: 0.85rem; color: var(--text-muted, #64748b); margin-top: 0.2rem;">CO₂ Emissions Prevented</div>
                </div>
            </div>
        </div>

        <!-- Cryptographic Audit Footprint -->
        <div style="background: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 10px; padding: 1.25rem; font-family: monospace;">
            <div style="font-size: 0.8rem; font-weight: 700; color: var(--primary, #176b4d); margin-bottom: 0.4rem; display: flex; align-items: center; gap: 0.4rem;">
                <span>🔒</span> <span>SHA-256 AUDIT LOG AUTHENTICITY HASH</span>
            </div>
            <div style="font-size: 0.85rem; word-break: break-all; color: #334155;">
                <c:out value="${receipt.verificationHashSha256}" />
            </div>
            <div style="font-size: 0.75rem; color: #64748b; margin-top: 0.5rem;">
                Generated deterministically from immutable database transaction sequence. Verified by AnnPath State Engine.
            </div>
        </div>
    </div>
    </c:otherwise>
</c:choose>
</div>

<jsp:include page="../common/footer.jsp" />
