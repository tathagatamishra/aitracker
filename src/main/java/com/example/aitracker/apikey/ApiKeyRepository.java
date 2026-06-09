package com.example.aitracker.apikey;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    List<ApiKey> findByOrgId(UUID orgId);

    boolean existsByOrgIdAndProviderAndKeyType(UUID orgId, String provider, String keyType);

    /** Used by the scheduler to know which orgs have at least one key registered. */
    @Query("SELECT DISTINCT a.orgId FROM ApiKey a")
    List<UUID> findDistinctOrgIds();
}