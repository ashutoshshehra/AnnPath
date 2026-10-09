<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Donation Details — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container dashboard-wrapper" style="max-width: 900px;">
    <div class="dashboard-header">
        <div>
            <h2><c:out value="${listing.title}" /></h2>
            <span class="badge ${listing.urgencyStatus == 'CRITICAL' ? 'badge-critical' : 'badge-fresh'}">
                <c:out value="${listing.urgencyStatus}" />
            </span>
            <span class="badge badge-standard"><c:out value="${listing.listingStatus}" /></span>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/donor/dashboard" class="btn btn-outline btn-sm">&larr; Back to Dashboard</a>
        </div>
    </div>

    <!-- Food Journey Timeline -->
    <div class="card" style="margin-bottom: 2rem;">
        <h4 style="margin-bottom: 1rem;">Food Journey Progress</h4>
        <jsp:include page="../common/timeline.jsp">
            <jsp:param name="statusOrder" value="3" />
        </jsp:include>
    </div>

    <div class="card" style="padding: 2rem;">
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 2rem;">
            <div>
                <h4 style="color: var(--primary);">Rescue Urgency Breakdown</h4>
                <p><strong>Priority Score:</strong> <span style="font-size: 1.3rem; font-weight: 800; color: var(--secondary);"><c:out value="${listing.priorityScore}" /> / 100</span></p>
                <p><strong>Explanation:</strong> <c:out value="${listing.priorityExplanation}" /></p>
                <p><strong>Available Until:</strong> <c:out value="${listing.availableUntil}" /></p>
                <p><strong>Storage:</strong> <c:out value="${listing.storageCondition}" /></p>
                <p><strong>Packaging:</strong> <c:out value="${listing.packagingType}" /></p>
            </div>
            <div>
                <h4 style="color: var(--primary);">Inventory & Logistics</h4>
                <p><strong>Total Quantity:</strong> <c:out value="${listing.totalQuantity}" /> <c:out value="${listing.unitName}" /></p>
                <p><strong>Remaining Available:</strong> <span style="font-weight: 700; color: var(--primary);"><c:out value="${listing.remainingQuantity}" /> <c:out value="${listing.unitName}" /></span></p>
                <p><strong>Reserved for Relief:</strong> <c:out value="${listing.reservedQuantity}" /> <c:out value="${listing.unitName}" /></p>
                <p><strong>Pickup Address:</strong> <c:out value="${listing.pickupAddress}" />, <c:out value="${listing.pickupCity}" /></p>
                <p><strong>Contact:</strong> <c:out value="${listing.contactName}" /> (<c:out value="${listing.contactPhone}" />)</p>
            </div>
        </div>

        <c:if test="${not empty listing.pickupInstructions}">
            <div style="margin-top: 1.5rem; background: var(--bg-main); padding: 1rem; border-radius: 8px;">
                <strong>Pickup Instructions:</strong> <c:out value="${listing.pickupInstructions}" />
            </div>
        </c:if>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
