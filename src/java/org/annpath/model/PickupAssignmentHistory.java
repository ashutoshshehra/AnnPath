package org.annpath.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class PickupAssignmentHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long pickupId;
    private Long volunteerUserId;
    private Timestamp assignedAt;
    private Timestamp respondedAt;
    private String responseStatus; // PENDING, ACCEPTED, REJECTED, TIMEOUT_EXPIRED, CANCELLED
    private String rejectionReason;

    // Joined Presentation Fields
    private String volunteerName;
    private String volunteerPhone;

    public PickupAssignmentHistory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPickupId() { return pickupId; }
    public void setPickupId(Long pickupId) { this.pickupId = pickupId; }

    public Long getVolunteerUserId() { return volunteerUserId; }
    public void setVolunteerUserId(Long volunteerUserId) { this.volunteerUserId = volunteerUserId; }

    public Timestamp getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Timestamp assignedAt) { this.assignedAt = assignedAt; }

    public Timestamp getRespondedAt() { return respondedAt; }
    public void setRespondedAt(Timestamp respondedAt) { this.respondedAt = respondedAt; }

    public String getResponseStatus() { return responseStatus; }
    public void setResponseStatus(String responseStatus) { this.responseStatus = responseStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getVolunteerName() { return volunteerName; }
    public void setVolunteerName(String volunteerName) { this.volunteerName = volunteerName; }

    public String getVolunteerPhone() { return volunteerPhone; }
    public void setVolunteerPhone(String volunteerPhone) { this.volunteerPhone = volunteerPhone; }
}
