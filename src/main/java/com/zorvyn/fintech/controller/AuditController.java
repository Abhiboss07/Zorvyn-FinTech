package com.zorvyn.fintech.controller;

import com.zorvyn.fintech.dto.response.ApiResponse;
import com.zorvyn.fintech.dto.response.AuditLogResponse;
import com.zorvyn.fintech.dto.response.PagedResponse;
import com.zorvyn.fintech.entity.AuditLog;
import com.zorvyn.fintech.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit", description = "Audit log query endpoints (admin only)")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Query audit logs (admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> queryLogs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Instant startTime,
            @RequestParam(required = false) Instant endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<AuditLog> result = auditService.queryLogs(userId, action, startTime, endTime,
                PageRequest.of(page, size, Sort.by("timestamp").descending()));

        Page<AuditLogResponse> mapped = result.map(AuditLogResponse::fromEntity);
        PagedResponse<AuditLogResponse> paged = new PagedResponse<>(
                mapped.getContent(), mapped.getNumber(), mapped.getSize(),
                mapped.getTotalElements(), mapped.getTotalPages(), mapped.isLast());

        return ResponseEntity.ok(ApiResponse.success(paged));
    }
}
