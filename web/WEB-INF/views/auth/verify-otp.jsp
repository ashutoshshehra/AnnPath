<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Verify Email — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="max-width: 540px; padding: 2.5rem 1rem;">
    <div class="card" style="padding: 2.5rem 2rem; border-radius: 16px; box-shadow: 0 10px 30px rgba(0,0,0,0.06); background: #ffffff;">
        <div style="text-align: center; margin-bottom: 1.8rem;">
            <div class="brand-icon" style="margin: 0 auto 1rem; width: 56px; height: 56px; font-size: 1.8rem; display: flex; align-items: center; justify-content: center; background: #e8f3ee; border-radius: 50%; color: var(--primary, #176B4D);">
                🔐
            </div>
            <h2 style="font-size: 1.6rem; font-weight: 700; color: #1e293b; margin-bottom: 0.5rem;">
                Email Verification
            </h2>
            <p style="color: #64748b; font-size: 0.95rem; line-height: 1.5; margin: 0 auto; max-width: 440px;">
                We have sent a 6-digit verification code to your email address. Enter the code below to complete your registration.
            </p>
        </div>

        <jsp:include page="../common/alert.jsp" />

        <form action="${pageContext.request.contextPath}/otp/verify" method="POST" id="emailOtpForm">
            <input type="hidden" name="csrfToken" value="${csrfToken}" />

            <!-- EMAIL OTP BOX -->
            <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.5rem; margin-bottom: 1.5rem;">
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.75rem;">
                    <span style="font-weight: 700; color: #1e293b; display: flex; align-items: center; gap: 0.5rem;">
                        ✉️ Gmail / Email OTP
                    </span>
                    <span style="background: #dbeafe; color: #1e40af; font-size: 0.75rem; font-weight: 600; padding: 0.2rem 0.6rem; border-radius: 9999px;">
                        Direct Delivery
                    </span>
                </div>
                <c:set var="targetEmail" value="${not empty sessionScope.pendingVerificationEmail ? sessionScope.pendingVerificationEmail : sessionScope.pendingRegEmail}" />
                <c:if test="${not empty targetEmail}">
                    <p style="font-size: 0.88rem; color: #64748b; margin-bottom: 1rem;">
                        Sent to: <strong><c:out value="${targetEmail}" /></strong>
                    </p>
                </c:if>
                <div class="form-group" style="margin-bottom: 0.25rem;">
                    <input type="text" id="emailOtp" name="emailOtp" class="form-control" required maxlength="6" inputmode="numeric"
                           style="text-align: center; font-size: 1.85rem; letter-spacing: 8px; font-weight: 700; max-width: 260px; margin: 0 auto; border-radius: 8px; border: 2px solid #cbd5e1; background: #fff;"
                           placeholder="••••••" autocomplete="one-time-code" autofocus value="<c:out value='${emailOtp}' />" />
                </div>
                <small style="display: block; text-align: center; color: #94a3b8; font-size: 0.78rem; margin-top: 0.5rem;">
                    Check your inbox or spam folder for the 6-digit code (Valid for 10 minutes)
                </small>

                <c:if test="${not empty sessionScope.pendingRegOtpCode}">
                    <div style="background: #f0fdf4; border: 1px dashed #16a34a; border-radius: 8px; padding: 0.65rem 0.85rem; margin-top: 0.85rem; display: flex; align-items: center; justify-content: space-between;">
                        <span style="font-size: 0.85rem; color: #166534;">
                            <strong>In-App / Test Code:</strong> <code style="font-size: 1.05rem; font-weight: 700; color: #15803d; letter-spacing: 2px;"><c:out value="${sessionScope.pendingRegOtpCode}" /></code>
                        </span>
                        <button type="button" onclick="document.getElementById('emailOtp').value='${sessionScope.pendingRegOtpCode}'" class="btn btn-sm" style="background: #16a34a; color: #fff; font-size: 0.75rem; padding: 0.25rem 0.65rem; border-radius: 4px; border: none; cursor: pointer; font-weight: 600;">
                            Auto-Fill
                        </button>
                    </div>
                </c:if>

                <c:if test="${emailDeliveredLive == true}">
                    <div style="background: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 8px; padding: 0.5rem 0.75rem; margin-top: 0.85rem; font-size: 0.8rem; color: #065f46; text-align: center;">
                        ✓ Verification code dispatched to your email address.
                    </div>
                </c:if>
            </div>

            <!-- SUBMIT BUTTON -->
            <button type="submit" class="btn btn-primary" style="width: 100%; padding: 0.85rem; font-size: 1.05rem; font-weight: 600; border-radius: 8px; display: flex; align-items: center; justify-content: center; gap: 0.5rem;">
                <span>✓</span> Verify Email & Complete Registration
            </button>
        </form>

        <!-- RESEND OTP SECTION -->
        <div style="margin-top: 1.5rem; text-align: center; border-top: 1px dashed #e2e8f0; padding-top: 1.25rem;">
            <p style="font-size: 0.88rem; color: #64748b; margin-bottom: 0.5rem;">
                Did not receive the code?
            </p>
            <form action="${pageContext.request.contextPath}/otp/resend" method="POST" style="display: inline-block;">
                <input type="hidden" name="csrfToken" value="${csrfToken}" />
                <button type="submit" class="btn btn-outline btn-sm" style="border-radius: 6px; font-weight: 600; padding: 0.4rem 1rem;">
                    🔄 Resend Email OTP
                </button>
            </form>
        </div>

        <div style="margin-top: 1.25rem; text-align: center; font-size: 0.85rem;">
            <a href="${pageContext.request.contextPath}/auth/login" style="color: var(--primary, #176B4D); font-weight: 600; text-decoration: none;">
                ← Back to Login
            </a>
        </div>
    </div>
</div>

<script>
    // UX Helper: Enforce only numeric digits for OTP
    document.addEventListener('DOMContentLoaded', function() {
        const emailInput = document.getElementById('emailOtp');
        if (emailInput) {
            emailInput.addEventListener('input', function() {
                this.value = this.value.replace(/[^0-9]/g, '');
            });
        }
    });
</script>

<jsp:include page="../common/footer.jsp" />
