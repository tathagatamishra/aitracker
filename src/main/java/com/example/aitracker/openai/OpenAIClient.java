package com.example.aitracker.openai;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAIClient {

    private final RestClient restClient;

    public OpenAIClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://api.openai.com/v1")
                .build();
    }

    public String fetchUsageCompletions(String adminKey, long startTime) {
        return restClient.get()
                .uri("/organization/usage/completions?start_time=" + startTime)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminKey)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }

    public String fetchCosts(String adminKey, long startTime) {
        return restClient.get()
                .uri("/organization/costs?start_time=" + startTime)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminKey)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }

    public String testChatCompletions(String projectKey, String inputText) {
        return restClient.post()
                .uri("/responses")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {
                      "model": "gpt-4.1-mini",
                      "input": "%s"
                    }
                    """.formatted(inputText.replace("\"", "\\\"")))
                .retrieve()
                .body(String.class);
    }
}