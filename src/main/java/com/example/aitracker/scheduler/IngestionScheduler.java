package com.example.aitracker.scheduler;

import com.example.aitracker.apikey.ApiKey;
import com.example.aitracker.apikey.ApiKeyService;
import com.example.aitracker.organization.Organization;
import com.example.aitracker.organization.OrganizationRepository;
import com.example.aitracker.openai.OpenAIUsageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class IngestionScheduler {

    private static final Logger log = LoggerFactory.getLogger(IngestionScheduler.class);

    private final OrganizationRepository organizationRepository;
    private final ApiKeyService apiKeyService;
    private final OpenAIUsageService openAIUsageService;

    public IngestionScheduler(
            OrganizationRepository organizationRepository,
            ApiKeyService apiKeyService,
            OpenAIUsageService openAIUsageService
    ) {
        this.organizationRepository = organizationRepository;
        this.apiKeyService = apiKeyService;
        this.openAIUsageService = openAIUsageService;
    }

    @Scheduled(fixedRateString = "${app.ingestion.fixed-rate-ms:300000}")
    public void ingestOpenAiData() {
        List<Organization> organizations = organizationRepository.findAll();
        long startTime = Instant.now().minus(Duration.ofHours(24)).getEpochSecond();

        for (Organization organization : organizations) {
            Optional<ApiKey> adminKeyOpt = apiKeyService.getApiKeysByOrganization(organization.getId())
                    .stream()
                    .filter(key -> "openai".equalsIgnoreCase(key.getProvider()))
                    .filter(key -> "admin".equalsIgnoreCase(key.getKeyType()))
                    .findFirst();

            if (adminKeyOpt.isEmpty()) {
                log.debug("Skipping organization {}: no OpenAI admin key found", organization.getId());
                continue;
            }

            try {
                String adminKey = apiKeyService.decryptStoredKey(adminKeyOpt.get());

                String usageJson = openAIUsageService.getUsageCompletions(adminKey, startTime);
                String costsJson = openAIUsageService.getCosts(adminKey, startTime);

                log.info(
                        "Fetched OpenAI telemetry for organization {}. usageBytes={}, costBytes={}",
                        organization.getId(),
                        usageJson != null ? usageJson.length() : 0,
                        costsJson != null ? costsJson.length() : 0
                );

                // Next step: parse JSON and persist normalized snapshots to the database.
            } catch (Exception e) {
                log.warn("Failed to ingest OpenAI data for organization {}: {}", organization.getId(), e.getMessage());
            }
        }
    }
}