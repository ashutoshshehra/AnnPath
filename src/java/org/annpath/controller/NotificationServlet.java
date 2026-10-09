package org.annpath.controller;

import org.annpath.model.Notification;
import org.annpath.model.User;
import org.annpath.service.NotificationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "NotificationServlet", urlPatterns = {"/notifications/*"})
public class NotificationServlet extends BaseServlet {

    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getCurrentUser(request);
        if (user == null) {
            redirect("/auth/login", request, response);
            return;
        }

        String path = request.getPathInfo();
        if ("/read-all".equals(path)) {
            notificationService.markAllAsRead(user.getId());
            redirect(getRefererOrHome(request), request, response);
            return;
        }

        List<Notification> list = notificationService.getUserNotifications(user.getId(), 50);
        request.setAttribute("notifications", list);
        request.setAttribute("unreadCount", notificationService.getUnreadCount(user.getId()));
        renderJsp("common/notifications.jsp", request, response);
    }

    private String getRefererOrHome(HttpServletRequest req) {
        String ref = req.getHeader("Referer");
        String cp = req.getContextPath();
        if (ref != null && cp != null && !cp.isEmpty() && ref.contains(cp)) {
            return ref.substring(ref.indexOf(cp) + cp.length());
        }
        return "/home";
    }
}
