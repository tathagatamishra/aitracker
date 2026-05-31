package com.example.aitracker.apikey;

import com.example.aitracker.organization.Organization;
import com.example.aitracker.organization.OrganizationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final OrganizationRepository organizationRepository;
    private final EncryptionService encryptionService;

    public ApiKeyService(ApiKeyRepository apiKeyRepository,
                         OrganizationRepository organizationRepository,
                         EncryptionService encryptionService) {
        this.apiKeyRepository = apiKeyRepository;
        this.organizationRepository = organizationRepository;
        this.encryptionService = encryptionService;
    }

    public ApiKey saveApiKey(UUID organizationId, String provider, String keyType, String plainApiKey) {
        if (apiKeyRepository.existsByOrganization_IdAndProviderAndKeyType(organizationId, provider, keyType)) {
            throw new RuntimeException("API key already exists for this provider and key type");
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        String encryptedKey = encryptionService.encrypt(plainApiKey);

        ApiKey apiKey = ApiKey.builder()
                .organization(organization)
                .provider(provider)
                .keyType(keyType)
                .encryptedKey(encryptedKey)
                .createdAt(LocalDateTime.now())
                .build();

        return apiKeyRepository.save(apiKey);
    }

    public List<ApiKey> getApiKeysByOrganization(UUID organizationId) {
        return apiKeyRepository.findByOrganization_Id(organizationId);
    }

    public String decryptApiKey(UUID apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new RuntimeException("API key not found"));

        return encryptionService.decrypt(apiKey.getEncryptedKey());
    }

    public String decryptStoredKey(ApiKey apiKey) {
        return encryptionService.decrypt(apiKey.getEncryptedKey());
    }
}