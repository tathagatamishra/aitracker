package com.example.aitracker.snapshot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UsageSnapshotRepository extends JpaRepository<UsageSnapshot, UUID> {

    List<UsageSnapshot> findByOrgIdOrderByBucketStartTimeDesc(UUID orgId);

    List<UsageSnapshot> findByOrgIdAndBucketStartTimeGreaterThanEqualOrderByBucketStartTimeAsc(
            UUID orgId, Long fromEpochSecond);

    /** Deduplication check — avoids re-storing a bucket we already ingested. */
    boolean existsByOrgIdAndSnapshotTypeAndModelIdAndBucketStartTimeAndSourceType(
            UUID orgId, String snapshotType, String modelId, Long bucketStartTime, String sourceType);
}