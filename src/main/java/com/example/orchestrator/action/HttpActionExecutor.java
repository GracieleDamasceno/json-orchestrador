package com.example.orchestrator.action;

import com.example.orchestrator.model.Step;
import com.example.orchestrator.util.ExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

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
    public Object execute(Step step, ExecutionContext context) {
        if (!"http".equals(step.type())) {
            throw new IllegalArgumentException("HttpActionExecutor can only handle 'http' type steps.");
        }

        // For now, only implement GET method
        if (!"GET".equalsIgnoreCase(step.method())) {
            throw new UnsupportedOperationException("Only GET method is supported for HTTP steps at the moment.");
        }

        String url = step.url();
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty for HTTP GET step.");
        }

        // Execute GET request
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(Map.class); // Assuming response body is a Map
    }
}