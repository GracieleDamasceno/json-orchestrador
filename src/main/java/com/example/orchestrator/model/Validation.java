package com.example.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Validation(
        @JsonProperty("minLength") Integer minLength,
        @JsonProperty("maxLength") Integer maxLength,
        @JsonProperty("pattern") String pattern,
        @JsonProperty("min") Integer min,
        @JsonProperty("max") Integer max
) {}