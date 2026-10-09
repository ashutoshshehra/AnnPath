package org.annpath.controller;

import org.annpath.config.Constants;
import org.annpath.dao.ReferenceDataDao;
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

@WebServlet(name = "AuthServlet", urlPatterns = {"/auth/*"})
public class AuthServlet extends BaseServlet {

    private final AuthService authService = new AuthService();
    private final OtpService otpService = new OtpService();
    private final UserDao userDao = new UserDao();
    private final ReferenceDataDao refDao = new ReferenceDataDao();
    private final AuditService auditService = new AuditService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "/login";

        switch (path) {
            case "/login":
                renderJsp("auth/login.jsp", request, response);
                break;
            case "/register":
                try {
                    request.setAttribute("countries", refDao.getAllCountries());
                } catch (Exception ignored) {}
                renderJsp("auth/register.jsp", request, response);
                break;
            case "/forgot-password":
                renderJsp("auth/forgot-password.jsp", request, response);
                break;
            case "/reset-password":
                renderJsp("auth/reset-password.jsp", request, response);
                break;
            case "/logout":
                HttpSession session = request.getSession(false);
                if (session != null) {
                    User user = (User) session.getAttribute(Constants.SESSION_USER);
                    if (user != null) {
                        auditService.log(user, "LOGOUT", "User logged out successfully", "User", user.getId(), "SUCCESS", null, request);
                    }
                    session.invalidate();
                }
                redirect("/auth/login?loggedOut=true", request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "/login";

        switch (path) {
            case "/login":
                handleLogin(request, response);
                break;
            case "/register":
                handleRegister(request, response);
                break;
            case "/forgot-password":
                handleForgotPassword(request, response);
                break;
            case "/reset-password":
                handleResetPassword(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String redirectUrl = request.getParameter("redirect");

        try {
            User user = authService.authenticate(email, password);

            // Defeat session fixation: create fresh session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute(Constants.SESSION_USER, user);
            session.setAttribute(Constants.SESSION_USER_ROLE, user.getRole());
            session.setAttribute(Constants.SESSION_LOCALE, user.getPreferredLanguage());
            session.setAttribute(Constants.SESSION_TIMEZONE, user.getPreferredTimezone());

            auditService.log(user, "LOGIN_SUCCESS", "User logged in successfully", "User", user.getId(), "SUCCESS", null, request);

            if (redirectUrl != null && !redirectUrl.trim().isEmpty() && !redirectUrl.contains("://")) {
                response.sendRedirect(redirectUrl);
                return;
            }

            // Redirect to appropriate role dashboard
            if (Constants.ROLE_DONOR.equalsIgnoreCase(user.getRole())) {
                redirect("/donor/dashboard", request, response);
            } else if (Constants.ROLE_NGO.equalsIgnoreCase(user.getRole())) {
                redirect("/ngo/dashboard", request, response);
            } else if (Constants.ROLE_VOLUNTEER.equalsIgnoreCase(user.getRole())) {
                redirect("/volunteer/dashboard", request, response);
            } else {
                redirect("/home", request, response);
            }
        } catch (Exception e) {
            auditService.log(null, "LOGIN_FAILED", "Failed login attempt for: " + email + " - " + e.getMessage(), "User", null, "FAILURE", null, request);
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("email", email);
            renderJsp("auth/login.jsp", request, response);
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String phoneCode = request.getParameter("phoneCountryCode");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        String lang = request.getParameter("preferredLanguage");
        String timezone = request.getParameter("preferredTimezone");

        try {
            // Strictly validate inputs without inserting into database yet
            authService.validateRegistrationInputs(email, phone, phoneCode, password, role, lang, timezone);

            HttpSession session = request.getSession(true);
            session.setAttribute("pendingRegEmail", email.trim().toLowerCase());
            session.setAttribute("pendingRegPhone", phone.trim());
            session.setAttribute("pendingRegPhoneCode", phoneCode != null ? phoneCode : "+91");
            session.setAttribute("pendingRegPassword", password);
            session.setAttribute("pendingRegRole", role.toUpperCase());
            session.setAttribute("pendingRegLang", lang != null ? lang : "en");
            session.setAttribute("pendingRegTimezone", timezone != null ? timezone : "UTC");

            session.setAttribute("pendingVerificationEmail", email.trim().toLowerCase());
            session.setAttribute("pendingVerificationPhone", ((phoneCode != null ? phoneCode : "+91") + " " + phone.trim()));
            session.setAttribute("pendingVerificationRole", role.toUpperCase());
            session.removeAttribute("pendingVerificationUserId");

            // Dispatch Email OTP via Gmail SMTP
            String emailOtp = otpService.generateAndSendRegistrationOtp(email);
            session.setAttribute("pendingRegOtpCode", emailOtp);
            session.setAttribute("emailDeliveredLive", NotificationGatewayService.isLastEmailDelivered());
            session.setAttribute("emailGatewayNotice", NotificationGatewayService.getLastEmailNotice());

            redirect("/otp/verify", request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);
            request.setAttribute("role", role);
            try {
                request.setAttribute("countries", refDao.getAllCountries());
            } catch (Exception ignored) {}
            renderJsp("auth/register.jsp", request, response);
        }
    }

    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Please provide your registered email address.");
            renderJsp("auth/forgot-password.jsp", request, response);
            return;
        }

        try {
            User user = userDao.findByEmail(email.trim().toLowerCase());
            if (user == null) {
                request.setAttribute("errorMessage", "No account found registered with " + email);
                renderJsp("auth/forgot-password.jsp", request, response);
                return;
            }

            // Dispatch OTP for Password Reset to registered Email
            String resetOtp = otpService.generateAndSendOtp(user.getId(), user.getEmail(), "EMAIL", "PASSWORD_RESET");

            // If user has a mobile number, dispatch SMS as well
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
                String fullPhone = (user.getPhoneCountryCode() != null ? user.getPhoneCountryCode() : "+91") + user.getPhoneNumber().trim();
                otpService.generateAndSendOtp(user.getId(), fullPhone, "SMS", "PASSWORD_RESET");
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("resetPasswordEmail", user.getEmail());
            session.setAttribute("pendingResetOtpCode", resetOtp);

            redirect("/auth/reset-password", request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
            renderJsp("auth/forgot-password.jsp", request, response);
        }
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        if (email == null || email.trim().isEmpty()) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                email = (String) session.getAttribute("resetPasswordEmail");
            }
        }
        String otpCode = request.getParameter("otpCode");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        try {
            authService.resetPassword(email, otpCode, newPassword, confirmPassword);

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute("resetPasswordEmail");
                session.removeAttribute("pendingResetOtpCode");
            }

            User user = userDao.findByEmail(email.trim().toLowerCase());
            if (user != null) {
                auditService.log(user, "PASSWORD_RESET", "Password successfully changed via OTP verification", "User", user.getId(), "SUCCESS", null, request);
            }

            request.setAttribute("successMessage", "Password has been successfully updated! You can now log in with your new password.");
            renderJsp("auth/login.jsp", request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("email", email);
            renderJsp("auth/reset-password.jsp", request, response);
        }
    }
}
