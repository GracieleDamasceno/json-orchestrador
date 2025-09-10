package com.example.orchestrator.service;

import com.example.orchestrator.action.ActionExecutor;
import com.example.orchestrator.model.Specification;
import com.example.orchestrator.model.Step;
import com.example.orchestrator.util.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Service
public class OrchestratorServiceImpl implements OrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorServiceImpl.class);
    private final SpecLoaderService specLoaderService;
    private final List<ActionExecutor> actionExecutors;

    public OrchestratorServiceImpl(SpecLoaderService specLoaderService, List<ActionExecutor> actionExecutors) {
        this.specLoaderService = specLoaderService;
        this.actionExecutors = actionExecutors;
    }

    private ActionExecutor getExecutorForStep(String type) {
        return actionExecutors.stream()
                .filter(executor -> executor.getType().equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No action executor found for type: " + type));
    }

    @Override
    public Map<String, Object> executeOrchestration(String product, Map<String, Object> requestParams) {
        logger.info("Received orchestration request for product: {}", product);
        try {
            Specification specification = specLoaderService.loadSpec(product);
            logger.info("Loaded specification for product {}: {}", product, specification);

            ExecutionContext context = new ExecutionContext();

            for (Step step : specification.steps()) {
                logger.info("Executing step: {} of type: {}", step.id(), step.type());
                ActionExecutor executor = getExecutorForStep(step.type());
                Object stepResult = executor.execute(step, context, requestParams);
                logger.info("Step '{}' executed. Result: {}", step.id(), stepResult);

                if (step.output() != null && !step.output().isEmpty()) {
                    context.put(step.output(), stepResult);
                    logger.info("Output of step '{}' stored in context under key: {}", step.id(), step.output());
                }
            }

            // For now, return a hardcoded response
            return Map.of("status", "success", "message", "Orchestration for product " + product + " completed");
        } catch (SpecNotFoundException e) {
            logger.error("Specification not found for product: {}", product, e);
            return Map.of("status", "error", "message", e.getMessage());
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            logger.error("Orchestration failed: {}", e.getMessage(), e);
            return Map.of("status", "error", "message", "Orchestration failed: " + e.getMessage());
        }
    }
}