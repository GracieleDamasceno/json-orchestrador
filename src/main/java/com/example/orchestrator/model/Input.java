package com.example.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Input(
        @JsonProperty("parameters") List<InputParameter> parameters
) {}