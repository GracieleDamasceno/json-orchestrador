package com.example.orchestrator.service;

import java.util.Map;

public interface OrchestratorService {
    Map<String, Object> executeOrchestration(String product, Map<String, Object> requestParams);
}