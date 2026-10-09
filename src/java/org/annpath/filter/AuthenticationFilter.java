package org.annpath.filter;

import org.annpath.config.Constants;
import org.annpath.model.User;

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

@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"/donor/*", "/ngo/*", "/volunteer/*"})
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;

            HttpSession session = req.getSession(false);
            User currentUser = (session != null) ? (User) session.getAttribute(Constants.SESSION_USER) : null;

            String contextPath = req.getContextPath();
            String uri = req.getRequestURI();

            if (currentUser == null) {
                res.sendRedirect(contextPath + "/auth/login?redirect=" + java.net.URLEncoder.encode(uri, "UTF-8"));
                return;
            }

            String role = currentUser.getRole();
            if (uri.startsWith(contextPath + "/donor/") && !Constants.ROLE_DONOR.equalsIgnoreCase(role)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access restricted to Donor accounts.");
                return;
            }
            if (uri.startsWith(contextPath + "/ngo/") && !Constants.ROLE_NGO.equalsIgnoreCase(role)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access restricted to Verified NGO accounts.");
                return;
            }
            if (uri.startsWith(contextPath + "/volunteer/") && !Constants.ROLE_VOLUNTEER.equalsIgnoreCase(role)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access restricted to Volunteer accounts.");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
