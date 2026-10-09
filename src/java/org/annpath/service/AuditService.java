package org.annpath.service;

import org.annpath.dao.AuditLogDao;
import org.annpath.model.AuditLog;
import org.annpath.model.User;

import javax.servlet.http.HttpServletRequest;

public class AuditService {

    private final AuditLogDao auditLogDao = new AuditLogDao();

    public void log(User actor, String eventType, String actionDescription, String entityName, Long entityId,
                    String status, String detailsJson, HttpServletRequest request) {
        AuditLog log = new AuditLog();
        if (actor != null) {
            log.setUserId(actor.getId());
            log.setUserRole(actor.getRole());
        }
        log.setEventType(eventType);
        log.setActionDescription(actionDescription);
        log.setAffectedEntity(entityName);
        log.setEntityId(entityId);
        log.setStatus(status != null ? status : "SUCCESS");
        log.setDetailsJson(detailsJson);

        if (request != null) {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.trim().isEmpty()) {
                ip = request.getRemoteAddr();
            } else {
                ip = ip.split(",")[0].trim();
            }
            log.setIpAddress(ip);
            log.setUserAgent(request.getHeader("User-Agent"));
        } else {
            log.setIpAddress("127.0.0.1");
            log.setUserAgent("System/Backend");
        }

        auditLogDao.log(log);
    }
}
