package org.annpath.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String notificationType;
    private String title;
    private String message;
    private String referenceType; // FOOD_LISTING, FOOD_REQUEST, PICKUP
    private Long referenceId;
    private String channel; // IN_APP, EMAIL, SMS
    private boolean read;
    private Timestamp sentAt;
    private Timestamp readAt;

    public Notification() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Timestamp getSentAt() { return sentAt; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }

    public Timestamp getReadAt() { return readAt; }
    public void setReadAt(Timestamp readAt) { this.readAt = readAt; }
}
