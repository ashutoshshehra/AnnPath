package org.annpath.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory sliding window rate limiter to protect sensitive authentication & OTP endpoints.
 */
@WebFilter(filterName = "RateLimitingFilter", urlPatterns = {"/auth/*", "/otp/*"})
public class RateLimitingFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 30;
    private static final long WINDOW_MILLIS = 60_000L;

    private static class RequestCounter {
        long windowStart;
        AtomicInteger count;

        RequestCounter(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(1);
        }
    }

    private final Map<String, RequestCounter> ipCounters = new ConcurrentHashMap<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;

            // Only rate limit state-mutating requests (POST)
            if ("POST".equalsIgnoreCase(req.getMethod())) {
                String clientIp = getClientIp(req);
                long now = System.currentTimeMillis();

                RequestCounter counter = ipCounters.compute(clientIp, (ip, existing) -> {
                    if (existing == null || (now - existing.windowStart) > WINDOW_MILLIS) {
                        return new RequestCounter(now);
                    }
                    existing.count.incrementAndGet();
                    return existing;
                });

                if (counter.count.get() > MAX_REQUESTS_PER_MINUTE) {
                    res.setStatus(429);
                    res.setContentType("text/plain;charset=UTF-8");
                    res.getWriter().write("Too Many Requests. Please slow down and try again in a minute.");
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.trim().isEmpty()) {
            return xf.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    @Override
    public void destroy() {
        ipCounters.clear();
    }
}
