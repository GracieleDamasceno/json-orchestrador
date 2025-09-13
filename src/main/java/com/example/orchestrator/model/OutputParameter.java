package com.example.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OutputParameter(
        @JsonProperty("name") String name,
        @JsonProperty("value") String value
) {}