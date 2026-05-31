package com.example.aitracker.apikey;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    List<ApiKey> findByOrganization_Id(UUID organizationId);

    List<ApiKey> findByProvider(String provider);

    boolean existsByOrganization_IdAndProviderAndKeyType(UUID organizationId, String provider, String keyType);
}