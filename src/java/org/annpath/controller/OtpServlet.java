package org.annpath.controller;

import org.annpath.config.Constants;
import org.annpath.dao.UserDao;
import org.annpath.model.User;
import org.annpath.service.AuditService;
import org.annpath.service.AuthService;
import org.annpath.service.NotificationGatewayService;
import org.annpath.service.OtpService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "OtpServlet", urlPatterns = {"/otp/*"})
public class OtpServlet extends BaseServlet {

    private final OtpService otpService = new OtpService();
    private final AuthService authService = new AuthService();
    private final UserDao userDao = new UserDao();
    private final AuditService auditService = new AuditService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || "/verify".equals(path)) {
            request.setAttribute("smsDeliveredLive", NotificationGatewayService.isLastSmsDelivered());
            request.setAttribute("smsGatewayNotice", NotificationGatewayService.getLastSmsNotice());
            renderJsp("auth/verify-otp.jsp", request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if ("/resend".equals(path)) {
            handleResend(request, response);
        } else {
            handleVerify(request, response);
        }
    }

    private void handleVerify(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String email = (session != null) ? (String) session.getAttribute("pendingVerificationEmail") : null;
        if (email == null && session != null) {
            email = (String) session.getAttribute("pendingRegEmail");
        }
        if (email == null) {
            email = request.getParameter("email");
        }

        String phone = (session != null) ? (String) session.getAttribute("pendingRegPhone") : null;
        String phoneCode = (session != null) ? (String) session.getAttribute("pendingRegPhoneCode") : null;
        if (phone == null && session != null) {
            phone = (String) session.getAttribute("pendingVerificationPhone");
        }
        if (phone == null) {
            phone = request.getParameter("phone");
        }

        String emailOtp = request.getParameter("emailOtp");
        String singleOtp = request.getParameter("otpCode");

        // Single OTP field compatibility
        if ((emailOtp == null || emailOtp.trim().isEmpty()) && singleOtp != null && !singleOtp.trim().isEmpty()) {
            emailOtp = singleOtp.trim();
        }

        try {
            boolean valid = otpService.verifyOtp(email, "REGISTRATION", emailOtp);

            if (valid) {
                String pendingPassword = (session != null) ? (String) session.getAttribute("pendingRegPassword") : null;
                if (pendingPassword != null && session != null) {
                    // Post-OTP Registration: Insert into database ONLY NOW!
                    String regPhone = (String) session.getAttribute("pendingRegPhone");
                    String regPhoneCode = (String) session.getAttribute("pendingRegPhoneCode");
                    String role = (String) session.getAttribute("pendingRegRole");
                    String lang = (String) session.getAttribute("pendingRegLang");
                    String timezone = (String) session.getAttribute("pendingRegTimezone");

                    User user = authService.registerUser(email, regPhone, regPhoneCode, pendingPassword, role, lang, timezone);

                    // Clean up registration session state
                    session.removeAttribute("pendingRegEmail");
                    session.removeAttribute("pendingRegPhone");
                    session.removeAttribute("pendingRegPhoneCode");
                    session.removeAttribute("pendingRegPassword");
                    session.removeAttribute("pendingRegRole");
                    session.removeAttribute("pendingRegLang");
                    session.removeAttribute("pendingRegTimezone");
                    session.removeAttribute("pendingRegOtpCode");
                    session.removeAttribute("pendingVerificationUserId");
                    session.removeAttribute("pendingVerificationEmail");
                    session.removeAttribute("pendingVerificationPhone");
                    session.removeAttribute("pendingVerificationRole");
                    session.removeAttribute("pendingRegPhoneOtp");
                    session.removeAttribute("emailDeliveredLive");
                    session.removeAttribute("emailGatewayNotice");
                    session.removeAttribute("smsDeliveredLive");
                    session.removeAttribute("smsGatewayNotice");

                    session.setAttribute(Constants.SESSION_USER, user);
                    session.setAttribute(Constants.SESSION_USER_ROLE, user.getRole());
                    session.setAttribute(Constants.SESSION_LOCALE, user.getPreferredLanguage());
                    session.setAttribute(Constants.SESSION_TIMEZONE, user.getPreferredTimezone());

                    auditService.log(user, "REGISTRATION_COMPLETED", "New " + role + " account registered and verified via Email OTP", "User", user.getId(), "SUCCESS", null, request);

                    if (Constants.ROLE_DONOR.equalsIgnoreCase(role)) {
                        redirect("/donor/dashboard", request, response);
                    } else if (Constants.ROLE_NGO.equalsIgnoreCase(role)) {
                        redirect("/ngo/dashboard", request, response);
                    } else if (Constants.ROLE_VOLUNTEER.equalsIgnoreCase(role)) {
                        redirect("/volunteer/dashboard", request, response);
                    } else {
                        redirect("/home", request, response);
                    }
                    return;
                }

                // Fallback for pre-existing accounts needing verification
                Long userId = (session != null) ? (Long) session.getAttribute("pendingVerificationUserId") : null;
                String role = (session != null) ? (String) session.getAttribute("pendingVerificationRole") : null;
                if (userId != null) {
                    userDao.updateVerification(userId, true, true);
                    User user = userDao.findById(userId);
                    session.setAttribute(Constants.SESSION_USER, user);
                    session.setAttribute(Constants.SESSION_USER_ROLE, user.getRole());
                    session.removeAttribute("pendingVerificationUserId");
                    session.removeAttribute("pendingVerificationEmail");
                    session.removeAttribute("pendingRegOtpCode");

                    auditService.log(user, "OTP_VERIFIED", "Account ownership verified successfully", "User", userId, "SUCCESS", null, request);

                    if (Constants.ROLE_DONOR.equalsIgnoreCase(role)) {
                        redirect("/donor/dashboard", request, response);
                    } else if (Constants.ROLE_NGO.equalsIgnoreCase(role)) {
                        redirect("/ngo/dashboard", request, response);
                    } else if (Constants.ROLE_VOLUNTEER.equalsIgnoreCase(role)) {
                        redirect("/volunteer/dashboard", request, response);
                    } else {
                        redirect("/home", request, response);
                    }
                    return;
                }
            }
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("emailOtp", emailOtp);
            renderJsp("auth/verify-otp.jsp", request, response);
            return;
        }
    }

    private void handleResend(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String email = (session != null) ? (String) session.getAttribute("pendingVerificationEmail") : null;
        if (email == null && session != null) {
            email = (String) session.getAttribute("pendingRegEmail");
        }
        if (email == null) {
            email = request.getParameter("email");
        }

        try {
            String freshOtp = otpService.generateAndSendRegistrationOtp(email);
            if (session != null) {
                session.setAttribute("pendingRegOtpCode", freshOtp);
                session.setAttribute("emailDeliveredLive", NotificationGatewayService.isLastEmailDelivered());
                session.setAttribute("emailGatewayNotice", NotificationGatewayService.getLastEmailNotice());
            }
            request.setAttribute("emailDeliveredLive", NotificationGatewayService.isLastEmailDelivered());
            request.setAttribute("emailGatewayNotice", NotificationGatewayService.getLastEmailNotice());
            request.setAttribute("successMessage", "A fresh 6-digit verification code has been dispatched to your email address (" + email + ").");
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
        }
        renderJsp("auth/verify-otp.jsp", request, response);
    }
}
