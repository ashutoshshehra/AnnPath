<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Log In — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="max-width: 480px; padding: 3.5rem 1rem;">
    <div class="card" style="padding: 2.5rem 2rem;">
        <div style="text-align: center; margin-bottom: 2rem;">
            <a href="${pageContext.request.contextPath}/home" style="display: inline-block; margin-bottom: 1rem;">
                <img src="${pageContext.request.contextPath}/assets/images/symbol.png" alt="AnnPath Symbol" style="height: 64px; object-fit: contain;">
            </a>
            <h2><c:out value="${not empty msg['login.title'] ? msg['login.title'] : 'Welcome Back'}" /></h2>
            <p style="color: var(--text-muted); font-size: 0.95rem;"><c:out value="${not empty msg['login.subtitle'] ? msg['login.subtitle'] : 'Log in to access your AnnPath dashboard'}" /></p>
        </div>

        <jsp:include page="../common/alert.jsp" />

        <form action="${pageContext.request.contextPath}/auth/login" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}" />
            <c:if test="${not empty param.redirect}">
                <input type="hidden" name="redirect" value="<c:out value='${param.redirect}' />" />
            </c:if>

            <div class="form-group">
                <label class="form-label" for="email"><c:out value="${not empty msg['form.email'] ? msg['form.email'] : 'Email Address'}" /></label>
                <input type="email" id="email" name="email" class="form-control" required
                       value="<c:out value='${email != null ? email : param.email}' />" placeholder="you@organization.org" />
            </div>

            <div class="form-group">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.35rem;">
                    <label class="form-label" for="password" style="margin:0;"><c:out value="${not empty msg['form.password'] ? msg['form.password'] : 'Password'}" /></label>
                    <a href="${pageContext.request.contextPath}/auth/forgot-password" style="font-size: 0.85rem;"><c:out value="${not empty msg['login.forgot'] ? msg['login.forgot'] : 'Forgot?'}" /></a>
                </div>
                <input type="password" id="password" name="password" class="form-control" required placeholder="••••••••" />
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;"><c:out value="${not empty msg['nav.login'] ? msg['nav.login'] : 'Log In'}" /></button>
        </form>

        <div style="text-align: center; margin-top: 1.5rem; font-size: 0.9rem; color: var(--text-muted);">
            <c:choose>
                <c:when test="${not empty msg['login.noAccount']}"><c:out value="${msg['login.noAccount']}" /></c:when>
                <c:otherwise>Don't have an account?</c:otherwise>
            </c:choose>
            <a href="${pageContext.request.contextPath}/auth/register"><c:out value="${not empty msg['login.createAccount'] ? msg['login.createAccount'] : 'Create an account'}" /></a>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
