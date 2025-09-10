package com.example.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OutputParameter(
        @JsonProperty("type") String type
) {}