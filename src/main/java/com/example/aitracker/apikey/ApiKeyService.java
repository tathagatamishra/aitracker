package com.example.aitracker.apikey;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final EncryptionService encryptionService;

    public ApiKeyService(ApiKeyRepository apiKeyRepository,
                         EncryptionService encryptionService) {
        this.apiKeyRepository = apiKeyRepository;
        this.encryptionService = encryptionService;
    }

    /**
     * Store a new API key for an org.
     * The orgId has already been resolved from org-service by the controller.
     *
     * @throws IllegalStateException if a key for this provider+keyType already exists for the org
     */
    public ApiKey saveApiKey(UUID orgId, String provider, String keyType, String plainApiKey) {
        if (apiKeyRepository.existsByOrgIdAndProviderAndKeyType(orgId, provider, keyType)) {
            throw new IllegalStateException(
                "An API key for provider=" + provider + " keyType=" + keyType +
                " already exists for this organisation. Delete it first to replace it."
            );
        }

        String encryptedKey = encryptionService.encrypt(plainApiKey);

        ApiKey apiKey = ApiKey.builder()
                .orgId(orgId)
                .provider(provider)
                .keyType(keyType)
                .encryptedKey(encryptedKey)
                .build();

        return apiKeyRepository.save(apiKey);
    }

    public List<ApiKey> getApiKeysByOrg(UUID orgId) {
        return apiKeyRepository.findByOrgId(orgId);
    }

    public void deleteApiKey(UUID apiKeyId, UUID orgId) {
        ApiKey key = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + apiKeyId));
        if (!key.getOrgId().equals(orgId)) {
            throw new SecurityException("API key does not belong to your organisation");
        }
        apiKeyRepository.delete(key);
    }

    /** Used internally by the scheduler — returns the plaintext key for ingestion. */
    public String decryptStoredKey(ApiKey apiKey) {
        return encryptionService.decrypt(apiKey.getEncryptedKey());
    }

    /** Convenience for the controller's decrypt endpoint (admin / debug only). */
    public String decryptApiKey(UUID apiKeyId, UUID orgId) {
        ApiKey key = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + apiKeyId));
        if (!key.getOrgId().equals(orgId)) {
            throw new SecurityException("API key does not belong to your organisation");
        }
        return encryptionService.decrypt(key.getEncryptedKey());
    }

    /** Returns all orgIds that have at least one key — used by the scheduler. */
    public List<UUID> findDistinctOrgIds() {
        return apiKeyRepository.findDistinctOrgIds();
    }
}