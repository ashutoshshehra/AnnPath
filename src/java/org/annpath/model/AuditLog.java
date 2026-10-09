package org.annpath.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String userRole;
    private String eventType;
    private String ipAddress;
    private String userAgent;
    private String actionDescription;
    private String affectedEntity;
    private Long entityId;
    private String status; // SUCCESS, FAILURE
    private String detailsJson;
    private Timestamp createdAt;

    // Presentation field
    private String userEmail;

    public AuditLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getActionDescription() { return actionDescription; }
    public void setActionDescription(String actionDescription) { this.actionDescription = actionDescription; }

    public String getAffectedEntity() { return affectedEntity; }
    public void setAffectedEntity(String affectedEntity) { this.affectedEntity = affectedEntity; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDetailsJson() { return detailsJson; }
    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
