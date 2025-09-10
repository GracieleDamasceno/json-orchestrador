package com.example.orchestrator.action;

import com.example.orchestrator.model.Step;
import com.example.orchestrator.util.ExecutionContext;
import com.example.orchestrator.util.VariableResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class HttpActionExecutor implements ActionExecutor {

    private final RestClient restClient;

    public HttpActionExecutor(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public String getType() {
        return "http";
    }

    @Override
    public Object execute(Step step, ExecutionContext context, Map<String, Object> requestParams) {
        if (!"http".equals(step.type())) {
            throw new IllegalArgumentException("HttpActionExecutor can only handle 'http' type steps.");
        }

        // For now, only implement GET method
        if (!"GET".equalsIgnoreCase(step.method())) {
            throw new UnsupportedOperationException("Only GET method is supported for HTTP steps at the moment.");
        }

        Map<String, Object> resolutionContext = new HashMap<>(requestParams);
        resolutionContext.putAll(context.getMap());

        String resolvedUrl = VariableResolver.resolveVariables(step.url(), resolutionContext);
        if (resolvedUrl == null || resolvedUrl.isEmpty()) {
            throw new IllegalArgumentException("Resolved URL cannot be null or empty for HTTP GET step.");
        }

        // Resolve variables in headers
        Map<String, String> resolvedHeaders = new HashMap<>();
        Optional.ofNullable(step.headers()).ifPresent(headers ->
            headers.forEach((key, value) ->
                resolvedHeaders.put(key, VariableResolver.resolveVariables(value, resolutionContext))
            )
        );

        // Execute GET request
        return restClient.get()
                .uri(resolvedUrl)
                .headers(httpHeaders -> resolvedHeaders.forEach(httpHeaders::add))
                .retrieve()
                .body(Map.class); // Assuming response body is a Map
    }
}