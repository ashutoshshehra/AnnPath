<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Digital Rescue Receipt — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="padding: 2rem 1rem;">
    <div style="display: flex; justify-content: flex-end; max-width: 820px; margin: 0 auto 1rem; gap: 0.5rem; flex-wrap: wrap;">
        <a href="${pageContext.request.contextPath}/rescue/passport?pickupId=${receipt.pickupId}" class="btn btn-outline btn-sm">🌍 View Food Journey Passport</a>
        <a href="${pageContext.request.contextPath}/receipt/print?id=${receipt.receiptNumber}" target="_blank" class="btn btn-outline btn-sm">🖨️ Print / Download PDF</a>
    </div>

    <div class="receipt-container">
        <div class="receipt-header">
            <div class="logo-area">
                <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="AnnPath Logo" style="height: 46px; object-fit: contain; margin-bottom: 4px;">
            </div>
            <div class="receipt-meta">
                <div class="receipt-num">RECEIPT #<c:out value="${receipt.receiptNumber}" /></div>
                <div>Issued: <c:out value="${receipt.issuedAt}" /></div>
            </div>
        </div>

        <div class="receipt-details-grid">
            <div class="receipt-block">
                <h4>Surplus Donor</h4>
                <p><strong><c:out value="${receipt.donorName}" /></strong></p>
                <p><c:out value="${receipt.donorAddress}" /></p>
                <p style="font-size: 0.85rem; color: #666;">Verified Commercial Donor</p>
            </div>
            <div class="receipt-block">
                <h4>Recipient Relief NGO</h4>
                <p><strong><c:out value="${receipt.ngoName}" /></strong></p>
                <p><c:out value="${receipt.ngoAddress}" /></p>
                <p style="font-size: 0.85rem; color: #666;">Verified Non-Profit Distribution Center</p>
            </div>
        </div>

        <table class="receipt-table">
            <thead>
                <tr>
                    <th>Food Item & Classification</th>
                    <th>Category</th>
                    <th>Rescued Quantity</th>
                    <th>Verified Handover</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><strong><c:out value="${receipt.foodTitle}" /></strong></td>
                    <td><c:out value="${receipt.foodCategoryName}" /></td>
                    <td><span style="font-size: 1.1rem; font-weight: 700; color: #176B4D;"><c:out value="${receipt.rescuedQuantity}" /> <c:out value="${receipt.unitName}" /></span></td>
                    <td><c:out value="${receipt.distributionTimestamp}" /></td>
                </tr>
            </tbody>
        </table>

        <div class="receipt-details-grid" style="margin-bottom: 1.5rem;">
            <div class="receipt-block">
                <h4>Verified Rescue Chain of Custody</h4>
                <p><strong>Pickup Time:</strong> <c:out value="${receipt.pickupTimestamp}" /></p>
                <p><strong>Delivery Time:</strong> <c:out value="${receipt.deliveryTimestamp}" /></p>
                <p><strong>Community Distribution:</strong> <c:out value="${receipt.distributionTimestamp}" /></p>
                <c:if test="${not empty receipt.volunteerName}">
                    <p><strong>Transported By:</strong> Volunteer <c:out value="${receipt.volunteerName}" /></p>
                </c:if>
            </div>
            <div class="receipt-block">
                <h4>Environmental & Social Impact</h4>
                <p><strong>Estimated People Fed:</strong> <span style="font-weight: 700;"><c:out value="${receipt.estimatedPeopleFed}" /> meals</span></p>
                <p><strong>CO₂ Emissions Prevented:</strong> <span style="font-weight: 700;"><c:out value="${receipt.co2SavedKg}" /> kg CO₂e</span></p>
                <p><strong>Landfill Diversion:</strong> 100% of usable surplus</p>
            </div>
        </div>

        <div class="receipt-verification">
            <div style="font-size: 0.85rem; font-weight: 700; margin-bottom: 0.3rem; color: #176B4D;">
                ✓ CRYPTOGRAPHIC AUDIT VERIFICATION SIGNATURE (SHA-256)
            </div>
            <code><c:out value="${receipt.verificationHashSha256}" /></code>
            <div style="font-size: 0.75rem; color: #666; margin-top: 0.3rem;">
                This digital receipt is tamper-evident and generated directly from immutable database execution logs.
            </div>
            <div style="font-size: 0.72rem; color: #777; margin-top: 0.4rem; border-top: 1px dashed #ccc; padding-top: 0.4rem;">
                <em>Official Verifiable Redistribution Record. This document serves as a verified cryptographic record of food surplus diversion and chain of custody.</em>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
