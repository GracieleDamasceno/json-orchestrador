package com.example.orchestrator.service;

import com.example.orchestrator.model.Output;
import com.example.orchestrator.model.OutputParameter;
import com.example.orchestrator.util.ExecutionContext;
import com.example.orchestrator.util.VariableResolver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OutputFormatter {

    public Map<String, Object> formatOutput(Output outputSpec, ExecutionContext context) {
        Map<String, Object> response = new HashMap<>();
        if (outputSpec != null && outputSpec.parameters() != null) {
            for (OutputParameter param : outputSpec.parameters()) {
                String fieldName = param.name();
                String value = VariableResolver.resolveVariables(param.value(), context.getMap());
                response.put(fieldName, value);
            }
        }
        return response;
    }
}