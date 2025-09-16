package com.example.orchestrator.service;

import com.example.orchestrator.model.Output;
import com.example.orchestrator.model.OutputParameter;
import com.example.orchestrator.util.ExecutionContext;
import com.example.orchestrator.util.VariableResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OutputFormatter {

    private final VariableResolver variableResolver;

    @Autowired
    public OutputFormatter(VariableResolver variableResolver) {
        this.variableResolver = variableResolver;
    }

    public Map<String, Object> formatOutput(Output outputSpec, ExecutionContext context) {
        Map<String, Object> response = new HashMap<>();
        if (outputSpec != null && outputSpec.parameters() != null) {
            for (OutputParameter param : outputSpec.parameters()) {
                String fieldName = param.name();
                Object value = variableResolver.resolveVariables(param.value(), context.getMap());
                response.put(fieldName, value);
            }
        }
        return response;
    }
}