<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="header.jsp">
    <jsp:param name="pageTitle" value="Notifications — AnnPath" />
</jsp:include>
<jsp:include page="navbar.jsp" />

<div class="container dashboard-wrapper">
    <div class="dashboard-header">
        <div>
            <h2>Notifications</h2>
            <p style="color: var(--text-muted);">Real-time alerts, operational updates, and rescue events.</p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/notifications/read-all" class="btn btn-outline btn-sm">Mark All as Read</a>
        </div>
    </div>

    <jsp:include page="alert.jsp" />

    <c:choose>
        <c:when test="${empty notifications}">
            <div class="card" style="text-align: center; padding: 3rem;">
                <p style="color: var(--text-muted);">You have no notifications at this time.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                <c:forEach var="n" items="${notifications}">
                    <div class="card" style="padding: 1.2rem; ${n.read ? 'opacity: 0.8; background: #fafafa;' : 'border-left: 4px solid var(--primary);'}">
                        <div style="display: flex; justify-content: space-between; margin-bottom: 0.3rem;">
                            <strong style="font-size: 1rem;"><c:out value="${n.title}" /></strong>
                            <span style="font-size: 0.8rem; color: var(--text-light);"><c:out value="${n.sentAt}" /></span>
                        </div>
                        <p style="color: var(--text-muted); font-size: 0.95rem; margin: 0;"><c:out value="${n.message}" /></p>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="footer.jsp" />
