package com.example.orchestrator.model;

import java.util.Map;

public record StepExecutionResult(
        String stepId,
        String status,
        Map<String, Object> output,
        String error
) {}