package com.example.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Output(
        @JsonProperty("parameters") List<OutputParameter> parameters
) {}