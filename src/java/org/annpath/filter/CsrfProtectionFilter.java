package org.annpath.filter;

import org.annpath.config.Constants;
import org.annpath.security.CsrfTokenManager;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "CsrfProtectionFilter", urlPatterns = {"/*"})
public class CsrfProtectionFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;

            HttpSession session = req.getSession(true);
            String token = CsrfTokenManager.getToken(session);
            req.setAttribute(Constants.SESSION_CSRF_TOKEN, token);

            String method = req.getMethod();
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
                // Exclude static assets
                String uri = req.getRequestURI();
                if (!uri.contains("/assets/") && !uri.contains("/api/")) {
                    if (!CsrfTokenManager.isValid(req)) {
                        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        res.setContentType("text/html;charset=UTF-8");
                        res.getWriter().write("<h3>403 Forbidden — Security Token Validation Failed (CSRF). Please refresh and try again.</h3>");
                        return;
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
