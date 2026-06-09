package com.example.aitracker.apikey;

import com.example.aitracker.client.OrgServiceClient;
import com.example.aitracker.security.JwtHelper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages AI-provider API keys for a customer's organisation.
 *
 * All endpoints require a valid Cognito JWT (Bearer token).
 * The orgId is resolved from org-service using the userId from the JWT sub —
 * the customer never needs to pass their orgId in the request body.
 */
@RestController
@RequestMapping("/api/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final OrgServiceClient orgServiceClient;
    private final JwtHelper jwtHelper;

    public ApiKeyController(ApiKeyService apiKeyService,
                            OrgServiceClient orgServiceClient,
                            JwtHelper jwtHelper) {
        this.apiKeyService = apiKeyService;
        this.orgServiceClient = orgServiceClient;
        this.jwtHelper = jwtHelper;
    }

    // ── Request / Response records ────────────────────────────────────────────

    record SaveApiKeyRequest(String provider, String keyType, String apiKey) {}

    record ApiKeyResponse(UUID id, String provider, String keyType, LocalDateTime createdAt) {}

    // ── Endpoints ─────────────────────────────────────────────────────────────

    /**
     * POST /api/api-keys
     * Save a new AI provider API key for the authenticated customer's org.
     */
    @PostMapping
    public ResponseEntity<ApiKeyResponse> saveApiKey(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SaveApiKeyRequest request) {

        UUID orgId = resolveOrgId(jwt);

        ApiKey saved = apiKeyService.saveApiKey(
                orgId,
                request.provider(),
                request.keyType(),
                request.apiKey()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    /**
     * GET /api/api-keys
     * List all API keys (metadata only — no encrypted values) for the caller's org.
     */
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> getMyKeys(@AuthenticationPrincipal Jwt jwt) {
        UUID orgId = resolveOrgId(jwt);
        List<ApiKeyResponse> keys = apiKeyService.getApiKeysByOrg(orgId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(keys);
    }

    /**
     * DELETE /api/api-keys/{apiKeyId}
     * Delete a specific API key — only if it belongs to the caller's org.
     */
    @DeleteMapping("/{apiKeyId}")
    public ResponseEntity<Void> deleteApiKey(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID apiKeyId) {

        UUID orgId = resolveOrgId(jwt);
        apiKeyService.deleteApiKey(apiKeyId, orgId);
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Calls org-service GET /api/orgs/my to resolve the orgId for the
     * authenticated user.  Throws if the user has no linked org.
     */
    private UUID resolveOrgId(Jwt jwt) {
        String bearerToken = jwt.getTokenValue();
        return orgServiceClient.getMyOrgId(bearerToken);
    }

    private ApiKeyResponse toResponse(ApiKey key) {
        return new ApiKeyResponse(key.getId(), key.getProvider(), key.getKeyType(), key.getCreatedAt());
    }
}