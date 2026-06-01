package com.example.aitracker.analytics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * GET /api/analytics/{orgId}/summary?days=30
     *
     * Returns total tokens, cost, requests, model breakdown, and daily series
     * for the given organisation over the last N days (default 30).
     */
    @GetMapping("/{orgId}/summary")
    public ResponseEntity<AnalyticsService.OrgSummary> getSummary(
            @PathVariable UUID orgId,
            @RequestParam(defaultValue = "30") int days
    ) {
        if (days < 1 || days > 365) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(analyticsService.getOrgSummary(orgId, days));
    }
}