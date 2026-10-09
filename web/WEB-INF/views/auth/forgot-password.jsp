<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Reset Password — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="max-width: 480px; padding: 3.5rem 1rem;">
    <div class="card" style="padding: 2.5rem 2rem;">
        <div style="text-align: center; margin-bottom: 2rem;">
            <h2><c:out value="${not empty msg['forgot.title'] ? msg['forgot.title'] : 'Reset Password'}" /></h2>
            <p style="color: var(--text-muted); font-size: 0.95rem;"><c:out value="${not empty msg['forgot.subtitle'] ? msg['forgot.subtitle'] : 'Enter your email to receive a recovery code'}" /></p>
        </div>

        <jsp:include page="../common/alert.jsp" />

        <form action="${pageContext.request.contextPath}/auth/forgot-password" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}" />

            <div class="form-group">
                <label class="form-label" for="email"><c:out value="${not empty msg['form.email'] ? msg['form.email'] : 'Registered Email Address'}" /></label>
                <input type="email" id="email" name="email" class="form-control" required placeholder="you@organization.org" />
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%;"><c:out value="${not empty msg['forgot.sendBtn'] ? msg['forgot.sendBtn'] : 'Send Reset Code'}" /></button>
        </form>

        <div style="text-align: center; margin-top: 1.5rem; font-size: 0.9rem;">
            <a href="${pageContext.request.contextPath}/auth/login">&larr; <c:out value="${not empty msg['nav.login'] ? msg['nav.login'] : 'Back to Login'}" /></a>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
