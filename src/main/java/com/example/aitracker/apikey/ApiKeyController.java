package com.example.aitracker.apikey;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    record SaveApiKeyRequest(UUID organizationId, String provider, String keyType, String apiKey) {}

    @PostMapping
    public ResponseEntity<?> saveApiKey(@RequestBody SaveApiKeyRequest request) {
        ApiKey saved = apiKeyService.saveApiKey(
                request.organizationId(),
                request.provider(),
                request.keyType(),
                request.apiKey()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiKeyResponse(
                saved.getId(),
                saved.getProvider(),
                saved.getKeyType(),
                saved.getCreatedAt()
        ));
    }

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<ApiKeyResponse>> getKeysByOrganization(@PathVariable UUID organizationId) {
        List<ApiKeyResponse> response = apiKeyService.getApiKeysByOrganization(organizationId)
                .stream()
                .map(key -> new ApiKeyResponse(
                        key.getId(),
                        key.getProvider(),
                        key.getKeyType(),
                        key.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{apiKeyId}/decrypt")
    public ResponseEntity<String> decryptKey(@PathVariable UUID apiKeyId) {
        return ResponseEntity.ok(apiKeyService.decryptApiKey(apiKeyId));
    }

    public record ApiKeyResponse(
            UUID id,
            String provider,
            String keyType,
            java.time.LocalDateTime createdAt
    ) {}
}