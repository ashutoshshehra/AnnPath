<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Resolution Center & Complaints — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="padding: 2.5rem 1rem; max-width: 960px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <h2>Resolution & Dispute Center</h2>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                Peer-to-peer issue tracking and automated resolution across Donors, NGOs, and Volunteers.
            </p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/complaint/file" class="btn btn-primary btn-sm">+ Log New Issue</a>
        </div>
    </div>

    <jsp:include page="../common/alert.jsp" />

    <c:if test="${not empty param.successMessage}">
        <div class="alert alert-success" style="margin-bottom: 1.5rem;">
            <c:out value="${param.successMessage}" />
        </div>
    </c:if>
    <c:if test="${not empty param.errorMessage}">
        <div class="alert alert-danger" style="margin-bottom: 1.5rem;">
            <c:out value="${param.errorMessage}" />
        </div>
    </c:if>

    <div class="card" style="padding: 1.5rem;">
        <c:choose>
            <c:when test="${empty complaints}">
                <div style="text-align: center; padding: 3rem 1rem; color: var(--text-muted);">
                    <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🤝</div>
                    <h4>No Open Disputes or Issues</h4>
                    <p style="font-size: 0.9rem;">You have no active complaints filed or reported against your account.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div style="display: flex; flex-direction: column; gap: 1.2rem;">
                    <c:forEach var="c" items="${complaints}">
                        <div style="border: 1px solid var(--border-color); border-radius: 8px; padding: 1.2rem; background: #fafafa;">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.8rem;">
                                <div>
                                    <span class="badge ${c.status == 'RESOLVED' ? 'badge-fresh' : 'badge-rescue-now'}" style="margin-right: 0.5rem;">
                                        <c:out value="${c.status}" />
                                    </span>
                                    <span class="badge badge-standard"><c:out value="${c.category}" /></span>
                                    <span style="font-size: 0.8rem; color: #888; margin-left: 0.5rem;">
                                        Filed on <c:out value="${c.createdAt}" />
                                    </span>
                                </div>
                                <div style="font-size: 0.85rem; color: #555;">
                                    <c:choose>
                                        <c:when test="${c.reporterUserId == currentUser.id}">
                                            <em>Filed by You against <c:out value="${c.reportedName != null ? c.reportedName : 'Platform/Partner'}" /></em>
                                        </c:when>
                                        <c:otherwise>
                                            <strong style="color: #b33939;">Reported by <c:out value="${c.reporterName}" /></strong>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <p style="margin-bottom: 0.8rem; font-size: 0.95rem; line-height: 1.5;">
                                <c:out value="${c.description}" />
                            </p>

                            <c:if test="${not empty c.resolutionNotes}">
                                <div style="background: #e8f3ee; border-left: 3px solid #176B4D; padding: 0.6rem 0.9rem; border-radius: 4px; font-size: 0.85rem; margin-bottom: 0.8rem;">
                                    <strong>Resolution Note:</strong> <c:out value="${c.resolutionNotes}" />
                                </div>
                            </c:if>

                            <c:if test="${c.status != 'RESOLVED'}">
                                <form action="${pageContext.request.contextPath}/complaint/resolve" method="POST" style="margin-top: 0.8rem; display: flex; gap: 0.6rem; align-items: center; flex-wrap: wrap;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                    <input type="hidden" name="complaintId" value="${c.id}" />
                                    <input type="text" name="resolutionNotes" class="form-control" style="flex: 1; min-width: 240px; font-size: 0.85rem;" placeholder="Add resolution comment or mutual agreement..." required />
                                    <button type="submit" class="btn btn-outline-primary btn-sm">Mark Resolved</button>
                                </form>
                            </c:if>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
