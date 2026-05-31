package com.example.aitracker.openai;

import org.springframework.stereotype.Service;

@Service
public class OpenAIUsageService {

    private final OpenAIClient openAIClient;

    public OpenAIUsageService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    public String getUsageCompletions(String adminKey, long startTime) {
        if (adminKey == null || adminKey.isBlank()) {
            throw new IllegalArgumentException("Admin key is required");
        }
        return openAIClient.fetchUsageCompletions(adminKey, startTime);
    }

    public String getCosts(String adminKey, long startTime) {
        if (adminKey == null || adminKey.isBlank()) {
            throw new IllegalArgumentException("Admin key is required");
        }
        return openAIClient.fetchCosts(adminKey, startTime);
    }

    public String testInference(String projectKey, String inputText) {
        if (projectKey == null || projectKey.isBlank()) {
            throw new IllegalArgumentException("Project key is required");
        }
        if (inputText == null || inputText.isBlank()) {
            throw new IllegalArgumentException("Input text is required");
        }
        return openAIClient.testChatCompletions(projectKey, inputText);
    }
}