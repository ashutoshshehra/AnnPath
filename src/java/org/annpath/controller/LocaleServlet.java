package org.annpath.controller;

import org.annpath.config.Constants;
import org.annpath.dao.UserDao;
import org.annpath.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LocaleServlet", urlPatterns = {"/locale"})
public class LocaleServlet extends BaseServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String lang = request.getParameter("lang");
        String tz = request.getParameter("tz");
        HttpSession session = request.getSession(true);

        if (lang != null && !lang.trim().isEmpty()) {
            String cleanLang = lang.trim().toLowerCase();
            session.setAttribute(Constants.SESSION_LOCALE, cleanLang);
            if ("en".equalsIgnoreCase(cleanLang)) {
                javax.servlet.http.Cookie c1 = new javax.servlet.http.Cookie("googtrans", "");
                c1.setPath("/");
                c1.setMaxAge(0);
                response.addCookie(c1);
            } else {
                javax.servlet.http.Cookie c1 = new javax.servlet.http.Cookie("googtrans", "/en/" + cleanLang);
                c1.setPath("/");
                response.addCookie(c1);
            }
        }
        if (tz != null && !tz.trim().isEmpty()) {
            session.setAttribute(Constants.SESSION_TIMEZONE, tz.trim());
        }

        User user = getCurrentUser(request);
        if (user != null) {
            String updatedLang = lang != null ? lang.trim().toLowerCase() : user.getPreferredLanguage();
            String updatedTz = tz != null ? tz.trim() : user.getPreferredTimezone();
            try {
                userDao.updatePreferences(user.getId(), updatedLang, updatedTz);
                user.setPreferredLanguage(updatedLang);
                user.setPreferredTimezone(updatedTz);
            } catch (Exception ignored) {}
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty() && !referer.contains("/locale")) {
            response.sendRedirect(referer);
        } else {
            redirect("/home", request, response);
        }
    }
}
