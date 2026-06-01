package com.example.aitracker.openai;

import com.example.aitracker.openai.dto.OpenAICostResponse;
import com.example.aitracker.openai.dto.OpenAIUsageResponse;
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

    public OpenAIUsageResponse fetchUsageCompletions(String adminKey, long startTime) {
        return restClient.get()
                .uri("/organization/usage/completions?start_time=" + startTime + "&limit=31&bucket_width=1d")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminKey)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(OpenAIUsageResponse.class);
    }

    public OpenAICostResponse fetchCosts(String adminKey, long startTime) {
        return restClient.get()
                .uri("/organization/costs?start_time=" + startTime + "&limit=31&bucket_width=1d")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminKey)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(OpenAICostResponse.class);
    }
}