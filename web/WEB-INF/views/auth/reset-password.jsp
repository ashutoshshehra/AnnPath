<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Create New Password — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="max-width: 500px; padding: 3.5rem 1rem;">
    <div class="card" style="padding: 2.5rem 2rem;">
        <div style="text-align: center; margin-bottom: 2rem;">
            <div class="brand-icon" style="margin: 0 auto 1rem; width: 52px; height: 52px; font-size: 1.6rem; display: flex; align-items: center; justify-content: center; background: #e8f3ee; border-radius: 50%;">🔑</div>
            <h2><c:out value="${not empty msg['reset.title'] ? msg['reset.title'] : 'Create New Password'}" /></h2>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                <c:out value="${not empty msg['reset.subtitle'] ? msg['reset.subtitle'] : 'Enter the 6-digit verification code sent to your email and set your new password.'}" />
            </p>
        </div>

        <jsp:include page="../common/alert.jsp" />

        <form action="${pageContext.request.contextPath}/auth/reset-password" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}" />

            <div class="form-group" style="margin-bottom: 1.25rem;">
                <label class="form-label" for="email"><c:out value="${not empty msg['form.email'] ? msg['form.email'] : 'Account Email'}" /></label>
                <input type="email" id="email" name="email" class="form-control" required
                       value="<c:out value='${not empty email ? email : sessionScope.resetPasswordEmail}' />"
                       placeholder="you@organization.org" />
            </div>

            <c:if test="${not empty sessionScope.pendingResetOtpCode}">
                <div style="background: #f0fdf4; border: 1px dashed #16a34a; border-radius: 8px; padding: 0.65rem 0.85rem; margin-bottom: 1.25rem; display: flex; align-items: center; justify-content: space-between;">
                    <span style="font-size: 0.85rem; color: #166534;">
                        <strong>In-App / Test Code:</strong> <code style="font-size: 1.05rem; font-weight: 700; color: #15803d; letter-spacing: 2px;"><c:out value="${sessionScope.pendingResetOtpCode}" /></code>
                    </span>
                    <button type="button" onclick="document.getElementById('otpCode').value='${sessionScope.pendingResetOtpCode}'" class="btn btn-sm" style="background: #16a34a; color: #fff; font-size: 0.75rem; padding: 0.25rem 0.65rem; border-radius: 4px; border: none; cursor: pointer; font-weight: 600;">
                        Auto-Fill
                    </button>
                </div>
            </c:if>

            <div class="form-group" style="margin-bottom: 1.25rem;">
                <label class="form-label" for="otpCode"><c:out value="${not empty msg['reset.code'] ? msg['reset.code'] : '6-Digit Verification Code'}" /></label>
                <input type="text" id="otpCode" name="otpCode" class="form-control" required maxlength="6"
                       style="text-align: center; font-size: 1.6rem; letter-spacing: 6px; font-weight: 700; max-width: 240px; margin: 0 auto;"
                       placeholder="••••••" autocomplete="one-time-code" />
            </div>

            <div class="form-group" style="margin-bottom: 1.25rem;">
                <label class="form-label" for="newPassword"><c:out value="${not empty msg['reset.newPassword'] ? msg['reset.newPassword'] : 'New Password'}" /></label>
                <input type="password" id="newPassword" name="newPassword" class="form-control" required minlength="8"
                       placeholder="••••••••" autocomplete="new-password" />
            </div>

            <div class="form-group" style="margin-bottom: 1.5rem;">
                <label class="form-label" for="confirmPassword"><c:out value="${not empty msg['reset.confirmNewPassword'] ? msg['reset.confirmNewPassword'] : 'Confirm New Password'}" /></label>
                <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" required minlength="8"
                       placeholder="••••••••" autocomplete="new-password" />
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; padding: 0.75rem; font-size: 1.05rem;">
                <c:out value="${not empty msg['reset.submitBtn'] ? msg['reset.submitBtn'] : 'Reset Password'}" />
            </button>
        </form>

        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 1.5rem; font-size: 0.9rem; border-top: 1px solid #eee; padding-top: 1rem;">
            <a href="${pageContext.request.contextPath}/auth/forgot-password" style="color: var(--text-muted);">&larr; <c:out value="${not empty msg['otp.resendBtn'] ? msg['otp.resendBtn'] : 'Resend Code'}" /></a>
            <a href="${pageContext.request.contextPath}/auth/login" style="color: var(--primary); font-weight: 600;"><c:out value="${not empty msg['nav.login'] ? msg['nav.login'] : 'Back to Login'}" /></a>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
