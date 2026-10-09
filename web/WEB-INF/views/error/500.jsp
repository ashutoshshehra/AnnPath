<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="500 Server Error — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="text-align: center; padding: 6rem 1rem;">
    <div class="card" style="max-width: 520px; margin: 0 auto; padding: 3rem 2rem;">
        <h1 style="font-size: 3.5rem; color: var(--danger); margin-bottom: 0.5rem;">500</h1>
        <h2>Internal Server Error</h2>
        <p style="color: var(--text-muted); margin: 1rem 0 2rem;">
            An unexpected error occurred while processing your request. Our platform error logging and recovery engine has recorded this incident for audit and investigation.
        </p>
        <div style="display: flex; gap: 1rem; justify-content: center;">
            <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">Return to Homepage</a>
            <button onclick="window.history.back()" class="btn btn-secondary">Go Back</button>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
