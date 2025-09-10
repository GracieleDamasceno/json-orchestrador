package com.example.orchestrator.service;

import com.example.orchestrator.model.Specification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

@Service
public class OrchestratorServiceImpl implements OrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorServiceImpl.class);
    private final SpecLoaderService specLoaderService;

    public OrchestratorServiceImpl(SpecLoaderService specLoaderService) {
        this.specLoaderService = specLoaderService;
    }

    @Override
    public Map<String, Object> executeOrchestration(String product, Map<String, Object> requestParams) {
        logger.info("Received orchestration request for product: {}", product);
        try {
            Specification specification = specLoaderService.loadSpec(product);
            logger.info("Loaded specification for product {}: {}", product, specification);
            // For now, return a hardcoded response
            return Map.of("status", "success", "message", "Orchestration for product " + product + " started");
        } catch (SpecNotFoundException e) {
            logger.error("Specification not found for product: {}", product, e);
            return Map.of("status", "error", "message", e.getMessage());
        }
    }
}