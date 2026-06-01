package com.example.aitracker.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maps /v1/organization/usage/completions response.
 *
 * Shape:
 * {
 *   "object": "page",
 *   "data": [
 *     {
 *       "object": "bucket",
 *       "start_time": 1713744000,
 *       "end_time": 1713830400,
 *       "results": [
 *         {
 *           "object": "organization.usage.completions.result",
 *           "input_tokens": 5000,
 *           "output_tokens": 2000,
 *           "input_cached_tokens": 1000,
 *           "num_model_requests": 50,
 *           "model_ids": ["gpt-4o"]
 *         }
 *       ]
 *     }
 *   ]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAIUsageResponse(
        @JsonProperty("data") List<Bucket> data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Bucket(
            @JsonProperty("start_time") Long startTime,
            @JsonProperty("end_time") Long endTime,
            @JsonProperty("results") List<Result> results
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            @JsonProperty("input_tokens") Long inputTokens,
            @JsonProperty("output_tokens") Long outputTokens,
            @JsonProperty("input_cached_tokens") Long inputCachedTokens,
            @JsonProperty("num_model_requests") Long numModelRequests,
            @JsonProperty("model_ids") List<String> modelIds
    ) {}
}