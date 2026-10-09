<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="File an Issue — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="padding: 2.5rem 1rem; max-width: 680px;">
    <div class="card" style="padding: 2rem;">
        <h2 style="margin-bottom: 0.5rem;">File an Issue / Complaint</h2>
        <p style="color: var(--text-muted); font-size: 0.95rem; margin-bottom: 1.5rem;">
            Submit an operational or hygiene grievance. The platform will automatically acknowledge and open peer-resolution tracking.
        </p>

        <jsp:include page="../common/alert.jsp" />

        <form action="${pageContext.request.contextPath}/complaint/file" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}" />
            <c:if test="${not empty param.pickupId}">
                <input type="hidden" name="pickupId" value="${param.pickupId}" />
            </c:if>
            <c:if test="${not empty param.foodListingId}">
                <input type="hidden" name="foodListingId" value="${param.foodListingId}" />
            </c:if>

            <div class="form-group" style="margin-bottom: 1.2rem;">
                <label for="category" style="font-weight: 600; display: block; margin-bottom: 0.4rem;">Grievance Category:</label>
                <select name="category" id="category" class="form-control" required style="width: 100%; padding: 0.6rem;">
                    <option value="HYGIENE">Food Hygiene & Packaging Issue</option>
                    <option value="SPOILED_FOOD">Spoiled / Expired Food</option>
                    <option value="MISREPRESENTATION">Quantity / Description Mismatch</option>
                    <option value="NO_SHOW">No-Show at Scheduled Window</option>
                    <option value="BEHAVIOR">Unprofessional Conduct</option>
                    <option value="OTHER">Other Operational Issue</option>
                </select>
            </div>

            <div class="form-group" style="margin-bottom: 1.2rem;">
                <label for="description" style="font-weight: 600; display: block; margin-bottom: 0.4rem;">Detailed Description:</label>
                <textarea name="description" id="description" class="form-control" rows="5" required
                          style="width: 100%; padding: 0.6rem;" placeholder="Please describe what happened, including dates, quantities, and observed issues..."></textarea>
            </div>

            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 1.5rem;">
                <a href="${pageContext.request.contextPath}/complaint/list" class="btn btn-outline btn-sm">Cancel</a>
                <button type="submit" class="btn btn-primary">Submit Dispute</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
