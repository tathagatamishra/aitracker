package com.example.aitracker.snapshot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UsageSnapshotRepository extends JpaRepository<UsageSnapshot, UUID> {

    List<UsageSnapshot> findByOrganization_IdOrderByBucketStartTimeDesc(UUID organizationId);

    List<UsageSnapshot> findByOrganization_IdAndBucketStartTimeGreaterThanEqualOrderByBucketStartTimeAsc(
            UUID organizationId, Long fromEpochSecond);

    // Check if we already ingested this exact bucket to avoid duplicates
    boolean existsByOrganization_IdAndSnapshotTypeAndModelIdAndBucketStartTimeAndSourceType(
            UUID organizationId, String snapshotType, String modelId, Long bucketStartTime, String sourceType);
}