package com.zorvyn.fintech.dto.response;

import com.zorvyn.fintech.entity.AuditLog;

import java.time.Instant;
import java.util.Map;

public class AuditLogResponse {

    private String id;
    private String userId;
    private String action;
    private String resource;
    private String resourceId;
    private Map<String, Object> metadata;
    private String ipAddress;
    private Instant timestamp;

    public static AuditLogResponse fromEntity(AuditLog log) {
        AuditLogResponse r = new AuditLogResponse();
        r.id = log.getId();
        r.userId = log.getUserId();
        r.action = log.getAction();
        r.resource = log.getResource();
        r.resourceId = log.getResourceId();
        r.metadata = log.getMetadata();
        r.ipAddress = log.getIpAddress();
        r.timestamp = log.getTimestamp();
        return r;
    }

    // ── Getters ──

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getAction() { return action; }
    public String getResource() { return resource; }
    public String getResourceId() { return resourceId; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getIpAddress() { return ipAddress; }
    public Instant getTimestamp() { return timestamp; }
}
