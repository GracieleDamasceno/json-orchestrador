package com.example.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Specification(
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("input") Input input,
        @JsonProperty("steps") List<Step> steps,
        @JsonProperty("output") Output output
) {}