package org.annpath.controller;

import org.annpath.config.Constants;
import org.annpath.model.User;
import org.annpath.util.I18nUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

public abstract class BaseServlet extends HttpServlet {

    protected User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute(Constants.SESSION_USER);
        }
        return null;
    }

    protected Locale getUserLocale(HttpServletRequest request) {
        Locale l = (Locale) request.getAttribute("currentLocale");
        return l != null ? l : Locale.ENGLISH;
    }

    protected String getUserTimezone(HttpServletRequest request) {
        String tz = (String) request.getAttribute("currentTimezone");
        return tz != null ? tz : "UTC";
    }

    protected void renderJsp(String jspPath, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/" + jspPath).forward(request, response);
    }

    protected void redirect(String path, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    protected void redirectWithMsg(String path, String msgParam, String msgText, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(true);
        session.setAttribute(msgParam, msgText);
        response.sendRedirect(request.getContextPath() + path);
    }

    protected void flashSuccess(HttpServletRequest request, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute("flashSuccess", message);
    }

    protected void flashError(HttpServletRequest request, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute("flashError", message);
    }

    protected Long getLongParam(HttpServletRequest request, String paramName) {
        String val = request.getParameter(paramName);
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected int getIntParam(HttpServletRequest request, String paramName, int defaultValue) {
        String val = request.getParameter(paramName);
        if (val == null || val.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
