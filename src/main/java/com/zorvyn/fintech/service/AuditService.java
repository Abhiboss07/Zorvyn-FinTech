package com.zorvyn.fintech.service;

import com.zorvyn.fintech.entity.AuditLog;
import com.zorvyn.fintech.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable audit logging service — writes to MongoDB Atlas.
 * No update or delete operations exposed.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_LOGGER");

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Asynchronously write an audit log entry to MongoDB.
     */
    @Async
    public void log(String userId, String action, String resource, String resourceId,
                    Map<String, Object> metadata, String ipAddress, String userAgent) {
        try {
            AuditLog entry = new AuditLog();
            entry.setUserId(userId);
            entry.setAction(action);
            entry.setResource(resource);
            entry.setResourceId(resourceId);
            entry.setMetadata(metadata);
            entry.setIpAddress(ipAddress);
            entry.setUserAgent(userAgent);
            entry.setTimestamp(Instant.now());

            auditLogRepository.save(entry);

            auditLogger.info("AUDIT | user={} | action={} | resource={} | resourceId={}",
                    userId, action, resource, resourceId);
        } catch (Exception e) {
            // Never let audit logging failure crash the main flow
            log.error("Failed to write audit log: userId={}, action={}", userId, action, e);
        }
    }

    /**
     * Query audit logs with filtering — admin only.
     */
    public Page<AuditLog> queryLogs(String userId, String action, Instant startTime, Instant endTime, Pageable pageable) {
        if (userId != null && action != null) {
            return auditLogRepository.findByUserIdAndAction(userId, action, pageable);
        } else if (userId != null) {
            return auditLogRepository.findByUserId(userId, pageable);
        } else if (action != null) {
            return auditLogRepository.findByAction(action, pageable);
        } else if (startTime != null && endTime != null) {
            return auditLogRepository.findByTimestampBetween(startTime, endTime, pageable);
        }
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }
}
