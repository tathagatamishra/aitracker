package com.example.aitracker.snapshot;

import com.example.aitracker.organization.Organization;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usage_snapshots", indexes = {
    @Index(name = "idx_snapshot_org_bucket", columnList = "organization_id, bucket_start_time"),
    @Index(name = "idx_snapshot_model", columnList = "model_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    // "completions", "embeddings", etc. — matches the OpenAI endpoint bucket type
    @Column(name = "snapshot_type", nullable = false)
    private String snapshotType;

    // The model string from OpenAI e.g. "gpt-4o", "gpt-4o-mini"
    @Column(name = "model_id")
    private String modelId;

    // Unix epoch second — the start of the bucket OpenAI returns
    @Column(name = "bucket_start_time", nullable = false)
    private Long bucketStartTime;

    @Column(name = "input_tokens")
    private Long inputTokens;

    @Column(name = "output_tokens")
    private Long outputTokens;

    @Column(name = "input_cached_tokens")
    private Long inputCachedTokens;

    @Column(name = "total_requests")
    private Long totalRequests;

    // Cost in USD, scale 10 to handle micro-cent precision from OpenAI
    @Column(name = "cost_usd", precision = 18, scale = 10)
    private BigDecimal costUsd;

    // Which key type produced this row — "usage" or "cost"
    @Column(name = "source_type", nullable = false)
    private String sourceType;

    @Column(name = "ingested_at", nullable = false)
    private LocalDateTime ingestedAt;

    @PrePersist
    protected void onCreate() {
        this.ingestedAt = LocalDateTime.now();
    }
}