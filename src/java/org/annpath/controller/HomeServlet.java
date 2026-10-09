package org.annpath.controller;

import org.annpath.dao.FoodListingDao;
import org.annpath.dao.ImpactDao;
import org.annpath.dao.PickupDao;
import org.annpath.model.FoodListing;
import org.annpath.model.Pickup;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "HomeServlet", urlPatterns = {"", "/home"})
public class HomeServlet extends BaseServlet {

    private final ImpactDao impactDao = new ImpactDao();
    private final FoodListingDao foodListingDao = new FoodListingDao();
    private final PickupDao pickupDao = new PickupDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Map<String, Object> impact = impactDao.getGlobalImpactSummary();
            request.setAttribute("impact", impact);

            Map<String, Object> bottlenecks = impactDao.getNetworkBottleneckMetrics();
            request.setAttribute("bottlenecks", bottlenecks);

            List<FoodListing> urgentListings = foodListingDao.findUrgentListings(6);
            request.setAttribute("urgentListings", urgentListings);

            Pickup latestPickup = pickupDao.findLatestActiveOrCompleted();
            if (latestPickup != null) {
                request.setAttribute("latestPickup", latestPickup);
                FoodListing latestListing = foodListingDao.findById(latestPickup.getFoodListingId());
                request.setAttribute("latestListing", latestListing);
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
        }
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
