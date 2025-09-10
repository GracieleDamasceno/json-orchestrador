package com.example.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InputParameter(
        @JsonProperty("name") String name,
        @JsonProperty("type") String type,
        @JsonProperty("required") Boolean required,
        @JsonProperty("validation") Validation validation
) {
    public InputParameter {
        required = required != null ? required : false;
    }
}