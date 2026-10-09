package org.annpath.controller;

import org.annpath.dao.ImpactDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ImpactServlet", urlPatterns = {"/impact"})
public class ImpactServlet extends BaseServlet {

    private final ImpactDao impactDao = new ImpactDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Map<String, Object> summary = impactDao.getGlobalImpactSummary();
            Map<String, Object> bottlenecks = impactDao.getNetworkBottleneckMetrics();
            List<Map<String, Object>> activities = impactDao.getRecentImpactActivities(12);

            request.setAttribute("summary", summary);
            request.setAttribute("bottlenecks", bottlenecks);
            request.setAttribute("activities", activities);

            renderJsp("impact/index.jsp", request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error loading impact metrics: " + e.getMessage());
            renderJsp("error/500.jsp", request, response);
        }
    }
}
