<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${not empty sessionScope.flashSuccess}">
    <div class="alert alert-success">
        <span>✓ <c:out value="${sessionScope.flashSuccess}" /></span>
        <button type="button" onclick="this.parentElement.style.display='none';" style="background:none;border:none;cursor:pointer;font-size:1.1rem;">&times;</button>
    </div>
    <c:remove var="flashSuccess" scope="session" />
</c:if>

<c:if test="${not empty sessionScope.flashError}">
    <div class="alert alert-error">
        <span>⚠️ <c:out value="${sessionScope.flashError}" /></span>
        <button type="button" onclick="this.parentElement.style.display='none';" style="background:none;border:none;cursor:pointer;font-size:1.1rem;">&times;</button>
    </div>
    <c:remove var="flashError" scope="session" />
</c:if>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-error">
        <span>⚠️ <c:out value="${errorMessage}" /></span>
    </div>
</c:if>

<c:if test="${not empty successMessage}">
    <div class="alert alert-success">
        <span>✓ <c:out value="${successMessage}" /></span>
    </div>
</c:if>
