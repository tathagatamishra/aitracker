package com.example.aitracker.scheduler;

import com.example.aitracker.apikey.ApiKey;
import com.example.aitracker.apikey.ApiKeyService;
import com.example.aitracker.openai.OpenAIClient;
import com.example.aitracker.openai.dto.OpenAICostResponse;
import com.example.aitracker.openai.dto.OpenAIUsageResponse;
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
import java.util.UUID;

/**
 * Periodically fetches usage and cost data from OpenAI for every org that has
 * a registered OpenAI admin key.
 *
 * Source-of-truth for "which orgs to process" is the {@code api_keys} table —
 * we no longer query a local organization table (that's org-service's concern).
 *
 * Schedule: every 6 hours by default (configurable via app.ingestion.fixed-rate-ms).
 */
@Component
public class IngestionScheduler {

    private static final Logger log = LoggerFactory.getLogger(IngestionScheduler.class);

    private final ApiKeyService apiKeyService;
    private final OpenAIClient openAIClient;
    private final UsageSnapshotRepository snapshotRepository;

    public IngestionScheduler(
            ApiKeyService apiKeyService,
            OpenAIClient openAIClient,
            UsageSnapshotRepository snapshotRepository) {
        this.apiKeyService = apiKeyService;
        this.openAIClient = openAIClient;
        this.snapshotRepository = snapshotRepository;
    }

    @Scheduled(fixedRateString = "${app.ingestion.fixed-rate-ms:21600000}")
    public void ingestOpenAiData() {
        // Discover orgs purely from the api_keys table — no call to org-service needed here.
        List<UUID> orgIds = apiKeyService.findDistinctOrgIds();

        if (orgIds.isEmpty()) {
            log.debug("No orgs with API keys registered — skipping ingestion run.");
            return;
        }

        // Back-fill last 30 days on every run to close any gaps from transient failures.
        long startTime = Instant.now().minus(Duration.ofDays(30)).getEpochSecond();

        log.info("Starting ingestion run for {} org(s)", orgIds.size());

        for (UUID orgId : orgIds) {
            Optional<ApiKey> adminKeyOpt = apiKeyService.getApiKeysByOrg(orgId)
                    .stream()
                    .filter(k -> "openai".equalsIgnoreCase(k.getProvider()))
                    .filter(k -> "admin".equalsIgnoreCase(k.getKeyType()))
                    .findFirst();

            if (adminKeyOpt.isEmpty()) {
                log.debug("Skipping org {}: no OpenAI admin key registered", orgId);
                continue;
            }

            try {
                String adminKey = apiKeyService.decryptStoredKey(adminKeyOpt.get());
                ingestUsage(orgId, adminKey, startTime);
                ingestCosts(orgId, adminKey, startTime);
                log.info("Ingestion complete for org {}", orgId);
            } catch (Exception e) {
                // Isolate per-org failures — one bad key must not stop other orgs.
                log.warn("Ingestion failed for org {}: {}", orgId, e.getMessage());
            }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void ingestUsage(UUID orgId, String adminKey, long startTime) {
        OpenAIUsageResponse response = openAIClient.fetchUsageCompletions(adminKey, startTime);
        if (response == null || response.data() == null) return;

        int saved = 0;
        for (OpenAIUsageResponse.Bucket bucket : response.data()) {
            if (bucket.results() == null) continue;
            for (OpenAIUsageResponse.Result result : bucket.results()) {
                String modelId = (result.modelIds() != null && !result.modelIds().isEmpty())
                        ? result.modelIds().get(0)
                        : "all";

                if (snapshotRepository.existsByOrgIdAndSnapshotTypeAndModelIdAndBucketStartTimeAndSourceType(
                        orgId, "completions", modelId, bucket.startTime(), "usage")) {
                    continue;
                }

                snapshotRepository.save(UsageSnapshot.builder()
                        .orgId(orgId)
                        .snapshotType("completions")
                        .modelId(modelId)
                        .bucketStartTime(bucket.startTime())
                        .inputTokens(result.inputTokens())
                        .outputTokens(result.outputTokens())
                        .inputCachedTokens(result.inputCachedTokens())
                        .totalRequests(result.numModelRequests())
                        .sourceType("usage")
                        .build());
                saved++;
            }
        }
        log.info("Saved {} usage snapshots for org {}", saved, orgId);
    }

    private void ingestCosts(UUID orgId, String adminKey, long startTime) {
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

                if (snapshotRepository.existsByOrgIdAndSnapshotTypeAndModelIdAndBucketStartTimeAndSourceType(
                        orgId, lineItem, modelId, bucket.startTime(), "cost")) {
                    continue;
                }

                snapshotRepository.save(UsageSnapshot.builder()
                        .orgId(orgId)
                        .snapshotType(lineItem)
                        .modelId(modelId)
                        .bucketStartTime(bucket.startTime())
                        .costUsd(result.amount().value())
                        .sourceType("cost")
                        .build());
                saved++;
            }
        }
        log.info("Saved {} cost snapshots for org {}", saved, orgId);
    }
}