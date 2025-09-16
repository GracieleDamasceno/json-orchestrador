package com.example.orchestrator.service;

import com.example.orchestrator.action.ActionExecutor;
import com.example.orchestrator.model.Specification;
import com.example.orchestrator.model.Step;
import com.example.orchestrator.model.StepExecutionResult;
import com.example.orchestrator.util.ExecutionContext;
import com.example.orchestrator.validation.InputValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class OrchestratorServiceImpl implements OrchestratorService {

    private final SpecLoaderService specLoaderService;
    private final List<ActionExecutor> actionExecutors;
    private final InputValidator inputValidator;
    private final RetryTemplate retryTemplate;
    private final OutputFormatter outputFormatter;

    public OrchestratorServiceImpl(SpecLoaderService specLoaderService, List<ActionExecutor> actionExecutors, InputValidator inputValidator, RetryTemplate retryTemplate, OutputFormatter outputFormatter) {
        this.specLoaderService = specLoaderService;
        this.actionExecutors = actionExecutors;
        this.inputValidator = inputValidator;
        this.retryTemplate = retryTemplate;
        this.outputFormatter = outputFormatter;
    }

    private ActionExecutor getExecutorForStep(String type) {
        return actionExecutors.stream()
                .filter(executor -> executor.getType().equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No action executor found for type: " + type));
    }

    @Override
    public Map<String, Object> executeOrchestration(String product, Map<String, Object> requestParams) {
        log.info("Received orchestration request for product: {}", product);
        try {
            Specification specification = specLoaderService.loadSpec(product);
            log.info("Loaded specification for product {}: {}", product, specification);

            inputValidator.validate(requestParams, specification.input());
            log.info("Input parameters validated for product: {}", product);

            ExecutionContext context = new ExecutionContext();
            context.put("input", requestParams);
            List<StepExecutionResult> trace = new ArrayList<>();

            for (Step step : specification.steps()) {
                log.info("Executing step: {} of type: {}", step.id(), step.type());
                ActionExecutor executor = getExecutorForStep(step.type());

                try {
                    Object stepResult = retryTemplate.execute(contextWithRetry -> {
                        log.debug("Attempting execution for step '{}', attempt {}", step.id(), contextWithRetry.getRetryCount() + 1);
                        return executor.execute(step, context, requestParams);
                    });

                    log.info("Step '{}' executed successfully. Result: {}", step.id(), stepResult);
                    Map<String, Object> outputMap = new LinkedHashMap<>();
                    if (step.output() != null && !step.output().isEmpty()) {
                        context.put(step.output(), stepResult);
                        outputMap.put(step.output(), stepResult);
                        log.info("Output of step '{}' stored in context under key: {}", step.id(), step.output());
                    }
                    trace.add(new StepExecutionResult(step.id(), "success", outputMap, null));

                } catch (Exception e) {
                    log.error("Step '{}' failed after retries: {}", step.id(), e.getMessage(), e);
                    StepExecutionResult failedResult = new StepExecutionResult(step.id(), "error", null, e.getMessage());
                    trace.add(failedResult);
                    return createErrorResponse("Orchestration failed: " + e.getMessage(), step.id(), trace);
                }
            }

            return createSuccessResponse(specification, context, trace);

        } catch (SpecNotFoundException e) {
            log.error("Specification not found for product: {}", product, e);
            return createErrorResponse(e.getMessage(), null, new ArrayList<>());
        } catch (InvalidInputException e) {
            log.error("Invalid input for product {}: {}", product, e.getMessage(), e);
            return createErrorResponse("Invalid input: " + e.getMessage(), null, new ArrayList<>());
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            log.error("Orchestration failed: {}", e.getMessage(), e);
            return createErrorResponse("Orchestration failed: " + e.getMessage(), null, new ArrayList<>());
        } catch (Exception e) {
            log.error("An unexpected error occurred during orchestration: {}", e.getMessage(), e);
            return createErrorResponse("An unexpected error occurred: " + e.getMessage(), null, new ArrayList<>());
        }
    }

    private Map<String, Object> createErrorResponse(String message, String stepId, List<StepExecutionResult> trace) {
        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("message", message);
        if (stepId != null) {
            errorDetails.put("step", stepId);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "error");
        response.put("error", errorDetails);
        response.put("trace", trace);
        return response;
    }

    private Map<String, Object> createSuccessResponse(Specification specification, ExecutionContext context, List<StepExecutionResult> trace) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("output", outputFormatter.formatOutput(specification.output(), context));
        response.put("trace", trace);
        return response;
    }
}