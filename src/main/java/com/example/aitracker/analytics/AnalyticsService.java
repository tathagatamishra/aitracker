package com.example.aitracker.analytics;

import com.example.aitracker.snapshot.UsageSnapshot;
import com.example.aitracker.snapshot.UsageSnapshotRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AnalyticsService {

    private final UsageSnapshotRepository snapshotRepository;

    public AnalyticsService(UsageSnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    /**
     * Returns the summary card numbers for the org dashboard:
     * total tokens, total requests, total cost, model breakdown, daily series.
     */
    public OrgSummary getOrgSummary(UUID orgId, int days) {
        long fromEpoch = Instant.now()
                .minusSeconds((long) days * 86400)
                .getEpochSecond();

        List<UsageSnapshot> snapshots = snapshotRepository
                .findByOrgIdAndBucketStartTimeGreaterThanEqualOrderByBucketStartTimeAsc(
                        orgId, fromEpoch);

        // ---- totals ----
        long totalInput = 0, totalOutput = 0, totalCached = 0, totalRequests = 0;
        BigDecimal totalCost = BigDecimal.ZERO;

        // ---- model breakdown ----
        Map<String, ModelStats> modelMap = new LinkedHashMap<>();

        // ---- daily series (keyed by date string "2024-05-01") ----
        Map<String, DailyPoint> dailyMap = new TreeMap<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

        for (UsageSnapshot s : snapshots) {
            String dateKey = fmt.format(Instant.ofEpochSecond(s.getBucketStartTime()));

            if ("usage".equals(s.getSourceType())) {
                long inp  = nvl(s.getInputTokens());
                long out  = nvl(s.getOutputTokens());
                long cach = nvl(s.getInputCachedTokens());
                long req  = nvl(s.getTotalRequests());

                totalInput    += inp;
                totalOutput   += out;
                totalCached   += cach;
                totalRequests += req;

                modelMap.computeIfAbsent(s.getModelId(), ModelStats::new)
                        .add(inp, out, cach, req, BigDecimal.ZERO);

                dailyMap.computeIfAbsent(dateKey, DailyPoint::new)
                        .addTokens(inp + out);
            }

            if ("cost".equals(s.getSourceType()) && s.getCostUsd() != null) {
                totalCost = totalCost.add(s.getCostUsd());

                modelMap.computeIfAbsent(s.getModelId(), ModelStats::new)
                        .addCost(s.getCostUsd());

                dailyMap.computeIfAbsent(dateKey, DailyPoint::new)
                        .addCost(s.getCostUsd());
            }
        }

        return new OrgSummary(
                totalInput, totalOutput, totalCached,
                totalInput + totalOutput, totalRequests,
                totalCost,
                new ArrayList<>(modelMap.values()),
                new ArrayList<>(dailyMap.values())
        );
    }

    // ---- DTOs ----

    public record OrgSummary(
            long totalInputTokens,
            long totalOutputTokens,
            long totalCachedTokens,
            long totalTokens,
            long totalRequests,
            BigDecimal totalCostUsd,
            List<ModelStats> modelBreakdown,
            List<DailyPoint> dailySeries
    ) {}

    public static class ModelStats {
        public final String modelId;
        public long inputTokens;
        public long outputTokens;
        public long cachedTokens;
        public long requests;
        public BigDecimal costUsd;

        public ModelStats(String modelId) {
            this.modelId = modelId;
            this.costUsd = BigDecimal.ZERO;
        }

        void add(long inp, long out, long cach, long req, BigDecimal cost) {
            this.inputTokens  += inp;
            this.outputTokens += out;
            this.cachedTokens += cach;
            this.requests     += req;
            this.costUsd = this.costUsd.add(cost);
        }

        void addCost(BigDecimal cost) {
            this.costUsd = this.costUsd.add(cost);
        }
    }

    public static class DailyPoint {
        public final String date;
        public long tokens;
        public BigDecimal costUsd;

        public DailyPoint(String date) {
            this.date = date;
            this.costUsd = BigDecimal.ZERO;
        }

        void addTokens(long t) { this.tokens += t; }
        void addCost(BigDecimal c) { this.costUsd = this.costUsd.add(c); }
    }

    private static long nvl(Long v) { return v == null ? 0 : v; }
}
