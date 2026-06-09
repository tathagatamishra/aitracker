package com.example.aitracker.analytics;

import com.example.aitracker.client.OrgServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final OrgServiceClient orgServiceClient;

    public AnalyticsController(AnalyticsService analyticsService,
                               OrgServiceClient orgServiceClient) {
        this.analyticsService = analyticsService;
        this.orgServiceClient = orgServiceClient;
    }

    /**
     * GET /api/analytics/summary?days=30
     *
     * Resolves orgId from the caller's Cognito JWT via org-service.
     * Returns total tokens, cost, requests, model breakdown, and daily series
     * for the last N days (default 30).
     */
    @GetMapping("/summary")
    public ResponseEntity<AnalyticsService.OrgSummary> getSummary(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "30") int days
    ) {
        if (days < 1 || days > 365) {
            return ResponseEntity.badRequest().build();
        }
        UUID orgId = orgServiceClient.getMyOrgId(jwt.getTokenValue());
        return ResponseEntity.ok(analyticsService.getOrgSummary(orgId, days));
    }
}
