package org.annpath.filter;

import org.annpath.config.Constants;
import org.annpath.model.User;
import org.annpath.util.I18nUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@WebFilter(filterName = "LocalizationFilter", urlPatterns = {"/*"})
public class LocalizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        if (request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpSession session = req.getSession(true);

            String langParam = req.getParameter("lang");
            if (langParam != null && !langParam.trim().isEmpty()) {
                session.setAttribute(Constants.SESSION_LOCALE, langParam.trim().toLowerCase());
            }

            String currentLang = (String) session.getAttribute(Constants.SESSION_LOCALE);
            if (currentLang == null) {
                User u = (User) session.getAttribute(Constants.SESSION_USER);
                if (u != null && u.getPreferredLanguage() != null) {
                    currentLang = u.getPreferredLanguage();
                } else {
                    currentLang = "en";
                }
                session.setAttribute(Constants.SESSION_LOCALE, currentLang);
            }

            Locale locale = I18nUtil.parseLocale(currentLang);
            req.setAttribute("currentLocale", locale);
            req.setAttribute("currentLang", currentLang);

            // JSTL standard locale & bundle configuration
            javax.servlet.jsp.jstl.core.Config.set(req, javax.servlet.jsp.jstl.core.Config.FMT_LOCALE, locale);
            javax.servlet.jsp.jstl.core.Config.set(session, javax.servlet.jsp.jstl.core.Config.FMT_LOCALE, locale);

            try {
                ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
                javax.servlet.jsp.jstl.core.Config.set(session, javax.servlet.jsp.jstl.core.Config.FMT_LOCALIZATION_CONTEXT,
                        new javax.servlet.jsp.jstl.fmt.LocalizationContext(bundle, locale));
                Map<String, String> msgMap = new HashMap<>();
                for (String key : bundle.keySet()) {
                    msgMap.put(key, bundle.getString(key));
                }
                req.setAttribute("msg", msgMap);
            } catch (Exception ignored) {}

            String tz = (String) session.getAttribute(Constants.SESSION_TIMEZONE);
            if (tz == null) {
                User u = (User) session.getAttribute(Constants.SESSION_USER);
                tz = (u != null && u.getPreferredTimezone() != null) ? u.getPreferredTimezone() : "UTC";
                session.setAttribute(Constants.SESSION_TIMEZONE, tz);
            }
            req.setAttribute("currentTimezone", tz);

            if (response instanceof HttpServletResponse) {
                HttpServletResponse res = (HttpServletResponse) response;
                if ("en".equalsIgnoreCase(currentLang)) {
                    Cookie langCookie = new Cookie("googtrans", "");
                    langCookie.setPath("/");
                    langCookie.setMaxAge(0);
                    res.addCookie(langCookie);
                } else {
                    Cookie langCookie = new Cookie("googtrans", "/en/" + currentLang);
                    langCookie.setPath("/");
                    res.addCookie(langCookie);
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
