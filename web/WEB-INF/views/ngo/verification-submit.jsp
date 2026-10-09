<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="NGO Verification Compliance — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container dashboard-wrapper" style="max-width: 800px;">
    <div class="dashboard-header">
        <div>
            <h2>Automated NGO Verification</h2>
            <p style="color: var(--text-muted);">
                Current Status: 
                <c:choose>
                    <c:when test="${ngoProfile.verificationStatus == 'VERIFIED'}">
                        <span class="badge badge-fresh">✓ Verified</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge badge-pending">Pending Verification</span>
                    </c:otherwise>
                </c:choose>
            </p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/ngo/dashboard" class="btn btn-outline btn-sm">&larr; Back to Dashboard</a>
        </div>
    </div>

    <jsp:include page="../common/alert.jsp" />

    <c:if test="${not empty ngoProfile.rejectionReason}">
        <div class="alert alert-warning">
            <strong>Compliance Notes:</strong> <c:out value="${ngoProfile.rejectionReason}" />
        </div>
    </c:if>

    <!-- Document Upload Card -->
    <div class="card" style="padding: 2rem; margin-bottom: 2rem;">
        <h3 style="margin-bottom: 1rem; color: var(--primary);">Upload Verification Document</h3>
        <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem;">
            Governing Rule Standard: <strong><c:out value="${rule != null ? rule.ruleName : 'Standard Country Non-Profit Compliance'}" /></strong>.
            Supported formats: PDF, JPG, PNG (Max 5MB).
        </p>

        <form action="${pageContext.request.contextPath}/ngo/verify/upload" method="POST" enctype="multipart/form-data">
            <input type="hidden" name="csrfToken" value="${csrfToken}" />

            <div class="form-group">
                <label class="form-label" for="documentType">Document Type *</label>
                <select id="documentType" name="documentType" class="form-select" required>
                    <option value="REGISTRATION_CERTIFICATE">Official Non-Profit Registration Certificate</option>
                    <option value="ORGANIZATION_DOCUMENT">Non-Profit Organization Status / Registration Proof</option>
                    <option value="ADDRESS_PROOF">Utility Bill / Official Address Proof</option>
                    <option value="IDENTITY_PROOF">Authorized Representative ID Proof</option>
                </select>
            </div>

            <div class="form-group">
                <label class="form-label" for="documentFile">Select File *</label>
                <input type="file" id="documentFile" name="documentFile" class="form-control" required accept=".pdf,.jpg,.jpeg,.png" />
            </div>

            <button type="submit" class="btn btn-primary">Upload & Trigger Verification &rarr;</button>
        </form>
    </div>

    <!-- Uploaded Documents History -->
    <h3>Submitted Documents</h3>
    <c:choose>
        <c:when test="${empty documents}">
            <div class="card" style="text-align: center; padding: 2rem; color: var(--text-muted);">
                No documents uploaded yet.
            </div>
        </c:when>
        <c:otherwise>
            <div style="display: flex; flex-direction: column; gap: 0.75rem; margin-top: 1rem;">
                <c:forEach var="doc" items="${documents}">
                    <div class="card" style="padding: 1rem 1.25rem;">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <strong><c:out value="${doc.documentType}" /></strong>
                                <span style="font-size: 0.85rem; color: var(--text-muted); display: block;">
                                    Original: <c:out value="${doc.originalFileName}" /> (<c:out value="${doc.fileSizeBytes / 1024}" /> KB) &bull; Uploaded: <c:out value="${doc.uploadedAt}" />
                                </span>
                            </div>
                            <span class="badge badge-fresh">Stored & Verified</span>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../common/footer.jsp" />
