package com.example.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.function.Consumer;

public record Step(
        @JsonProperty("id") String id,
        @JsonProperty("type") String type,
        @JsonProperty("method") String method, // Nullable for non-HTTP steps
        @JsonProperty("url") String url,       // Nullable
        @JsonProperty("headers") Map<String, String> headers, // Nullable
        @JsonProperty("operation") String operation, // Nullable for non-DB steps
        @JsonProperty("table") String table,   // Nullable
        @JsonProperty("data") JsonNode data,   // Nullable, use JsonNode for flexible data types
        @JsonProperty("output") String output
) {}