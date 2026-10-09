<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Manage Food Requirements — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container dashboard-wrapper" style="max-width: 920px; padding: 2.5rem 1rem 4rem;">
    <div class="dashboard-header">
        <div>
            <h2>Relief Intake Profiles & Recurring Needs</h2>
            <p style="color: var(--text-muted);">
                Define recurring food categories and intake capacities so the Urgency Engine routes surplus to your center automatically.
            </p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/ngo/dashboard" class="btn btn-outline btn-sm">&larr; Back to Dashboard</a>
        </div>
    </div>

    <jsp:include page="../common/alert.jsp" />

    <!-- Add New Requirement Card -->
    <div class="card" style="padding: 2.5rem 2rem; margin-bottom: 2.5rem; background: #ffffff;">
        <h3 style="margin-bottom: 0.5rem; color: var(--primary);">+ Define Community Intake Requirement</h3>
        <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem;">
            When food matching these specifications is donated nearby, your organization receives high-affinity priority matches.
        </p>

        <form action="${pageContext.request.contextPath}/ngo/requirements/add" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}" />

            <div class="form-group">
                <label class="form-label" for="title">Requirement Title / Program Name *</label>
                <input type="text" id="title" name="title" class="form-control" required
                       placeholder="e.g. Daily Cooked Lunch for 150 Shelter Residents" />
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="foodCategoryId">Accepted Food Category *</label>
                    <select id="foodCategoryId" name="foodCategoryId" class="form-select" required>
                        <c:forEach var="c" items="${categories}">
                            <option value="${c.id}"><c:out value="${c.categoryName}" /></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label" for="unitId">Measurement Unit *</label>
                    <select id="unitId" name="unitId" class="form-select" required>
                        <c:forEach var="u" items="${units}">
                            <option value="${u.id}"><c:out value="${u.unitName}" /> (<c:out value="${u.unitCode}" />)</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="minQuantity">Minimum Usable Batch *</label>
                    <input type="number" id="minQuantity" name="minQuantity" class="form-control" required min="1" step="0.1" placeholder="10" />
                    <span style="font-size: 0.8rem; color: var(--text-muted);">Smallest donation quantity your team can intake.</span>
                </div>
                <div class="form-group">
                    <label class="form-label" for="maxQuantity">Maximum Intake Capacity *</label>
                    <input type="number" id="maxQuantity" name="maxQuantity" class="form-control" required min="1" step="0.1" placeholder="150" />
                    <span style="font-size: 0.8rem; color: var(--text-muted);">Maximum storage capacity per rescue.</span>
                </div>
            </div>

            <!-- Dietary Selection Chips -->
            <div class="form-group" style="margin-top: 1rem;">
                <label class="form-label">Dietary Classification Acceptance *</label>
                <div class="chip-selector-grid">
                    <label class="chip-option selected">
                        <input type="radio" name="dietaryType" value="ANY" checked style="display: none;" />
                        <span>🌱 ANY / ALL</span>
                    </label>
                    <label class="chip-option">
                        <input type="radio" name="dietaryType" value="VEGETARIAN" style="display: none;" />
                        <span>🥦 VEGETARIAN ONLY</span>
                    </label>
                    <label class="chip-option">
                        <input type="radio" name="dietaryType" value="VEGAN" style="display: none;" />
                        <span>🌿 VEGAN ONLY</span>
                    </label>
                    <label class="chip-option">
                        <input type="radio" name="dietaryType" value="NON_VEGETARIAN" style="display: none;" />
                        <span>🍖 NON-VEG ACCEPTED</span>
                    </label>
                    <label class="chip-option">
                        <input type="radio" name="dietaryType" value="HALAL" style="display: none;" />
                        <span>🌙 HALAL ONLY</span>
                    </label>
                </div>
            </div>

            <!-- Priority Level Chips -->
            <div class="form-group" style="margin-top: 1rem;">
                <label class="form-label">Community Urgency Level *</label>
                <div class="chip-selector-grid">
                    <label class="chip-option">
                        <input type="radio" name="priorityLevel" value="URGENT" style="display: none;" />
                        <span>🔥 URGENT DAILY NEED</span>
                    </label>
                    <label class="chip-option selected">
                        <input type="radio" name="priorityLevel" value="HIGH" checked style="display: none;" />
                        <span>⭐ HIGH PRIORITY</span>
                    </label>
                    <label class="chip-option">
                        <input type="radio" name="priorityLevel" value="MEDIUM" style="display: none;" />
                        <span>📅 MEDIUM (STANDARD)</span>
                    </label>
                </div>
            </div>

            <div style="margin-top: 2rem;">
                <button type="submit" class="btn btn-primary btn-lg" style="padding: 0.75rem 2.5rem;">
                    Save Requirement Profile &rarr;
                </button>
            </div>
        </form>
    </div>

    <!-- Existing Requirements List -->
    <div style="margin-bottom: 1.5rem; display: flex; justify-content: space-between; align-items: center;">
        <h3 style="margin: 0; font-size: 1.35rem;">Active Intake Profiles (<c:out value="${requirements.size()}" />)</h3>
    </div>

    <c:choose>
        <c:when test="${empty requirements}">
            <div class="empty-state">
                <div class="empty-state-icon">📝</div>
                <div class="empty-state-title">No Intake Profiles Created</div>
                <div class="empty-state-text">
                    Add your first requirement profile above to help the engine match local surplus directly to your center.
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div style="display: flex; flex-direction: column; gap: 1rem;">
                <c:forEach var="r" items="${requirements}">
                    <div class="card" style="padding: 1.5rem; background: #ffffff; border-left: 4px solid var(--primary);">
                        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                            <div>
                                <div style="display: flex; align-items: center; gap: 0.6rem; margin-bottom: 0.3rem;">
                                    <h4 style="margin: 0; font-size: 1.15rem; color: var(--text-main);"><c:out value="${r.title}" /></h4>
                                    <span class="badge ${r.priorityLevel == 'URGENT' ? 'badge-critical' : 'badge-fresh'}">
                                        <c:out value="${r.priorityLevel}" />
                                    </span>
                                </div>
                                <div style="font-size: 0.9rem; color: var(--text-muted); display: flex; flex-wrap: wrap; gap: 1rem;">
                                    <span><strong>Category:</strong> <c:out value="${r.categoryName}" /></span>
                                    <span><strong>Dietary:</strong> <c:out value="${r.dietaryType}" /></span>
                                    <span><strong>Intake Range:</strong> <c:out value="${r.minQuantity}" /> &ndash; <c:out value="${r.maxQuantity}" /> <c:out value="${r.unitName}" /></span>
                                </div>
                            </div>
                            <form action="${pageContext.request.contextPath}/ngo/requirements/delete" method="POST" style="margin: 0;">
                                <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                <input type="hidden" name="requirementId" value="${r.id}" />
                                <button type="submit" class="btn btn-outline btn-sm" data-confirm="Delete this requirement profile?">
                                    🗑️ Remove
                                </button>
                            </form>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../common/footer.jsp" />
