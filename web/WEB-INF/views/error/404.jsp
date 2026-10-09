<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="404 Page Not Found — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="text-align: center; padding: 6rem 1rem;">
    <div class="card" style="max-width: 500px; margin: 0 auto; padding: 3rem 2rem;">
        <h1 style="font-size: 3.5rem; color: var(--primary); margin-bottom: 0.5rem;">404</h1>
        <h2>Page Not Found</h2>
        <p style="color: var(--text-muted); margin: 1rem 0 2rem;">
            The requested page, food listing, or mission could not be located.
        </p>
        <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">Return to Homepage</a>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
