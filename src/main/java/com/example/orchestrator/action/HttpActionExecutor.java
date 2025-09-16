package com.example.orchestrator.action;

import com.example.orchestrator.model.Step;
import com.example.orchestrator.util.ExecutionContext;
import com.example.orchestrator.util.VariableResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class HttpActionExecutor implements ActionExecutor {

    private final RestClient restClient;
    private final VariableResolver variableResolver;
    private final ObjectMapper objectMapper;

    public HttpActionExecutor(RestClient restClient, VariableResolver variableResolver, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.variableResolver = variableResolver;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getType() {
        return "http";
    }

    @Override
    public Object execute(Step step, ExecutionContext context, Map<String, Object> requestParams) {
        log.info("Executing HTTP step: {} with method: {} and URL: {}", step.id(), step.method(), step.url());
        if (!"http".equals(step.type())) {
            log.error("HttpActionExecutor received a step of type '{}' but can only handle 'http' type steps.", step.type());
            throw new IllegalArgumentException("HttpActionExecutor can only handle 'http' type steps.");
        }

        Map<String, Object> resolutionContext = new HashMap<>();
        resolutionContext.put("input", requestParams);
        resolutionContext.putAll(context.getMap());

        String resolvedUrl = variableResolver.resolveVariables(step.url(), resolutionContext);
        if (resolvedUrl == null || resolvedUrl.isEmpty()) {
            log.error("Resolved URL cannot be null or empty for HTTP step: {}", step.id());
            throw new IllegalArgumentException("Resolved URL cannot be null or empty for HTTP step.");
        }
        log.debug("Resolved URL for step {}: {}", step.id(), resolvedUrl);

        Map<String, String> resolvedHeaders = new HashMap<>();
        Optional.ofNullable(step.headers()).ifPresent(headers ->
            headers.forEach((key, value) -> {
                String resolvedValue = variableResolver.resolveVariables(value, resolutionContext);
                resolvedHeaders.put(key, resolvedValue);
                log.debug("Resolved header for step {}: {} = {}", step.id(), key, resolvedValue);
            })
        );

        Object responseBody;
        switch (step.method().toUpperCase()) {
            case "GET":
                responseBody = restClient.get()
                        .uri(resolvedUrl)
                        .headers(httpHeaders -> resolvedHeaders.forEach(httpHeaders::add))
                        .retrieve()
                        .body(Map.class);
                log.info("HTTP GET step '{}' executed successfully. Response: {}", step.id(), responseBody);
                break;
            case "POST":
                Object requestBody = null;
                if (step.data() != null) {
                    // Convert JsonNode to Map<String, Object> for variable resolution
                    Map<String, Object> dataMap = objectMapper.convertValue(step.data(), Map.class);
                    requestBody = variableResolver.resolveVariables(dataMap, resolutionContext);
                }

                responseBody = restClient.post()
                        .uri(resolvedUrl)
                        .headers(httpHeaders -> resolvedHeaders.forEach(httpHeaders::add))
                        .body(requestBody)
                        .retrieve()
                        .body(Map.class);
                log.info("HTTP POST step '{}' executed successfully. Response: {}", step.id(), responseBody);
                break;
            default:
                log.error("Unsupported HTTP method for step '{}': {}", step.id(), step.method());
                throw new IllegalArgumentException("Unsupported HTTP method: " + step.method() + ". Only GET and POST methods are supported for HTTP steps at the moment.");
        }
        return responseBody;
    }
}