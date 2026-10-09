package org.annpath.service;

import org.annpath.config.AppConfig;
import org.annpath.dao.NotificationDao;
import org.annpath.model.Notification;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());
    private final NotificationDao notificationDao = new NotificationDao();

    public void notifyUser(Long userId, String type, String title, String message, String refType, Long refId) {
        if (userId == null) return;
        try {
            Notification notif = new Notification();
            notif.setUserId(userId);
            notif.setNotificationType(type);
            notif.setTitle(title);
            notif.setMessage(message);
            notif.setReferenceType(refType);
            notif.setReferenceId(refId);
            notif.setChannel("IN_APP");
            notif.setRead(false);

            notificationDao.send(notif);

            // Log simulated email/SMS delivery for production monitoring
            if (AppConfig.getBoolean("notification.emailSimulated", true)) {
                LOGGER.info("[EMAIL DISPATCHED] To User#" + userId + " | Title: " + title + " | Content: " + message);
            }
            if (AppConfig.getBoolean("notification.smsSimulated", true)) {
                LOGGER.info("[SMS DISPATCHED] To User#" + userId + " | Message: " + message);
            }
        } catch (SQLException e) {
            LOGGER.warning("Failed to save in-app notification: " + e.getMessage());
        }
    }

    public List<Notification> getUserNotifications(Long userId, int limit) {
        if (userId == null) return Collections.emptyList();
        try {
            return notificationDao.findByUser(userId, limit);
        } catch (SQLException e) {
            LOGGER.warning("Failed to fetch notifications: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public int getUnreadCount(Long userId) {
        if (userId == null) return 0;
        try {
            return notificationDao.getUnreadCount(userId);
        } catch (SQLException e) {
            return 0;
        }
    }

    public void markAllAsRead(Long userId) {
        if (userId == null) return;
        try {
            notificationDao.markAllRead(userId);
        } catch (SQLException e) {
            LOGGER.warning("Failed to mark notifications read: " + e.getMessage());
        }
    }
}
