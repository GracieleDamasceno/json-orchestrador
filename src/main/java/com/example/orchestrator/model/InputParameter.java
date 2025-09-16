package com.example.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InputParameter(
        @JsonProperty("name") String name,
        @JsonProperty("type") String type,
        @JsonProperty("required") boolean required,
        @JsonProperty("validation") Validation validation
) {}