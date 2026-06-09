package com.example.aitracker.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * Calls org-service to resolve the orgId for the authenticated user.
 *
 * Uses GET {org-service.base-url}/api/orgs/my, forwarding the caller's
 * Cognito JWT in the Authorization header — org-service validates it
 * against the same Cognito pool.
 */
@Component
public class OrgServiceClient {

    private static final Logger log = LoggerFactory.getLogger(OrgServiceClient.class);

    private final RestClient restClient;
    private final String orgServiceBaseUrl;

    public OrgServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${org-service.base-url}") String orgServiceBaseUrl) {
        this.restClient = restClientBuilder.build();
        this.orgServiceBaseUrl = orgServiceBaseUrl;
    }

    /**
     * Returns the orgId linked to the authenticated user.
     *
     * @param bearerToken the raw JWT value (no "Bearer " prefix needed here — we add it)
     * @throws OrgResolutionException if org-service is unreachable or returns no org
     */
    public UUID getMyOrgId(String bearerToken) {
        try {
            OrgResponse response = restClient.get()
                    .uri(orgServiceBaseUrl + "/api/orgs/my")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .retrieve()
                    .body(OrgResponse.class);

            if (response == null || response.id() == null) {
                throw new OrgResolutionException("org-service returned no organisation for this user");
            }

            return response.id();

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("No org found for user via org-service: {}", e.getMessage());
            throw new OrgResolutionException("No organisation found for this user. Complete onboarding first.");
        } catch (OrgResolutionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call org-service: {}", e.getMessage());
            throw new OrgResolutionException("Unable to resolve organisation — org-service unavailable.");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OrgResponse(UUID id, String name) {}

    public static class OrgResolutionException extends RuntimeException {
        public OrgResolutionException(String message) {
            super(message);
        }
    }
}
