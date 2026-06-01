package com.example.aitracker.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Maps /v1/organization/costs response.
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
 *           "object": "organization.costs.result",
 *           "amount": { "value": 0.0042, "currency": "usd" },
 *           "line_item": "completions",
 *           "model_ids": ["gpt-4o"]
 *         }
 *       ]
 *     }
 *   ]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAICostResponse(
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
            @JsonProperty("amount") Amount amount,
            @JsonProperty("line_item") String lineItem,
            @JsonProperty("model_ids") List<String> modelIds
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Amount(
            @JsonProperty("value") BigDecimal value,
            @JsonProperty("currency") String currency
    ) {}
}