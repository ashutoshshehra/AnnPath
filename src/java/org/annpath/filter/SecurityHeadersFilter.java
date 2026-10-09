package org.annpath.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "SecurityHeadersFilter", urlPatterns = {"/*"})
public class SecurityHeadersFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse res = (HttpServletResponse) response;
            res.setHeader("X-Frame-Options", "SAMEORIGIN");
            res.setHeader("X-Content-Type-Options", "nosniff");
            res.setHeader("X-XSS-Protection", "1; mode=block");
            res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            res.setHeader("Content-Security-Policy",
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://translate.google.com https://translate.googleapis.com; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net https://translate.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com; " +
                    "img-src 'self' data: https: https://www.google.com https://translate.google.com; " +
                    "frame-src 'self' https://translate.google.com; " +
                    "connect-src 'self' https://translate.googleapis.com https://translate.google.com https://nominatim.openstreetmap.org;");

            if (org.annpath.config.AppConfig.isProduction() || request.isSecure()) {
                res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            }

            javax.servlet.http.HttpServletResponseWrapper responseWrapper = new javax.servlet.http.HttpServletResponseWrapper(res) {
                @Override
                public void addHeader(String name, String value) {
                    if ("Set-Cookie".equalsIgnoreCase(name) && value != null && !value.toLowerCase().contains("samesite")) {
                        value = value + "; SameSite=Lax";
                    }
                    super.addHeader(name, value);
                }

                @Override
                public void setHeader(String name, String value) {
                    if ("Set-Cookie".equalsIgnoreCase(name) && value != null && !value.toLowerCase().contains("samesite")) {
                        value = value + "; SameSite=Lax";
                    }
                    super.setHeader(name, value);
                }
            };
            chain.doFilter(request, responseWrapper);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
