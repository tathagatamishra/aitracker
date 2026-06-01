package com.example.aitracker.scheduler;

import com.example.aitracker.apikey.ApiKey;
import com.example.aitracker.apikey.ApiKeyService;
import com.example.aitracker.openai.OpenAIClient;
import com.example.aitracker.openai.dto.OpenAICostResponse;
import com.example.aitracker.openai.dto.OpenAIUsageResponse;
import com.example.aitracker.organization.Organization;
import com.example.aitracker.organization.OrganizationRepository;
import com.example.aitracker.snapshot.UsageSnapshot;
import com.example.aitracker.snapshot.UsageSnapshotRepository;
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
    private final OpenAIClient openAIClient;
    private final UsageSnapshotRepository snapshotRepository;

    public IngestionScheduler(
            OrganizationRepository organizationRepository,
            ApiKeyService apiKeyService,
            OpenAIClient openAIClient,
            UsageSnapshotRepository snapshotRepository
    ) {
        this.organizationRepository = organizationRepository;
        this.apiKeyService = apiKeyService;
        this.openAIClient = openAIClient;
        this.snapshotRepository = snapshotRepository;
    }

    @Scheduled(fixedRateString = "${app.ingestion.fixed-rate-ms:300000}")
    public void ingestOpenAiData() {
        List<Organization> organizations = organizationRepository.findAll();
        // Fetch last 30 days on each run so we back-fill any gaps
        long startTime = Instant.now().minus(Duration.ofDays(30)).getEpochSecond();

        for (Organization organization : organizations) {
            Optional<ApiKey> adminKeyOpt = apiKeyService.getApiKeysByOrganization(organization.getId())
                    .stream()
                    .filter(k -> "openai".equalsIgnoreCase(k.getProvider()))
                    .filter(k -> "admin".equalsIgnoreCase(k.getKeyType()))
                    .findFirst();

            if (adminKeyOpt.isEmpty()) {
                log.debug("Skipping org {}: no OpenAI admin key", organization.getId());
                continue;
            }

            try {
                String adminKey = apiKeyService.decryptStoredKey(adminKeyOpt.get());
                ingestUsage(organization, adminKey, startTime);
                ingestCosts(organization, adminKey, startTime);
                log.info("Ingestion complete for org {}", organization.getId());
            } catch (Exception e) {
                log.warn("Ingestion failed for org {}: {}", organization.getId(), e.getMessage());
            }
        }
    }

    private void ingestUsage(Organization org, String adminKey, long startTime) {
        OpenAIUsageResponse response = openAIClient.fetchUsageCompletions(adminKey, startTime);
        if (response == null || response.data() == null) return;

        int saved = 0;
        for (OpenAIUsageResponse.Bucket bucket : response.data()) {
            if (bucket.results() == null) continue;
            for (OpenAIUsageResponse.Result result : bucket.results()) {
                // A result with no model data maps to the org-level aggregate — store as "all"
                String modelId = (result.modelIds() != null && !result.modelIds().isEmpty())
                        ? result.modelIds().get(0)
                        : "all";

                // Skip duplicate buckets we already stored
                if (snapshotRepository.existsByOrganization_IdAndSnapshotTypeAndModelIdAndBucketStartTimeAndSourceType(
                        org.getId(), "completions", modelId, bucket.startTime(), "usage")) {
                    continue;
                }

                UsageSnapshot snapshot = UsageSnapshot.builder()
                        .organization(org)
                        .snapshotType("completions")
                        .modelId(modelId)
                        .bucketStartTime(bucket.startTime())
                        .inputTokens(result.inputTokens())
                        .outputTokens(result.outputTokens())
                        .inputCachedTokens(result.inputCachedTokens())
                        .totalRequests(result.numModelRequests())
                        .sourceType("usage")
                        .build();

                snapshotRepository.save(snapshot);
                saved++;
            }
        }
        log.info("Saved {} usage snapshots for org {}", saved, org.getId());
    }

    private void ingestCosts(Organization org, String adminKey, long startTime) {
        OpenAICostResponse response = openAIClient.fetchCosts(adminKey, startTime);
        if (response == null || response.data() == null) return;

        int saved = 0;
        for (OpenAICostResponse.Bucket bucket : response.data()) {
            if (bucket.results() == null) continue;
            for (OpenAICostResponse.Result result : bucket.results()) {
                if (result.amount() == null) continue;

                String modelId = (result.modelIds() != null && !result.modelIds().isEmpty())
                        ? result.modelIds().get(0)
                        : "all";
                String lineItem = result.lineItem() != null ? result.lineItem() : "completions";

                if (snapshotRepository.existsByOrganization_IdAndSnapshotTypeAndModelIdAndBucketStartTimeAndSourceType(
                        org.getId(), lineItem, modelId, bucket.startTime(), "cost")) {
                    continue;
                }

                UsageSnapshot snapshot = UsageSnapshot.builder()
                        .organization(org)
                        .snapshotType(lineItem)
                        .modelId(modelId)
                        .bucketStartTime(bucket.startTime())
                        .costUsd(result.amount().value())
                        .sourceType("cost")
                        .build();

                snapshotRepository.save(snapshot);
                saved++;
            }
        }
        log.info("Saved {} cost snapshots for org {}", saved, org.getId());
    }
}