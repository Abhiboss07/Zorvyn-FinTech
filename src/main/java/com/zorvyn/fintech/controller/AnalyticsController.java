package com.zorvyn.fintech.controller;

import com.zorvyn.fintech.dto.response.AnalyticsSummaryResponse;
import com.zorvyn.fintech.dto.response.ApiResponse;
import com.zorvyn.fintech.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Dashboard analytics endpoints")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('READ')")
    @Operation(summary = "Get analytics dashboard summary (analyst+)")
    public ResponseEntity<ApiResponse<AnalyticsSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getSummary()));
    }
}
