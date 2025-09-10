package com.example.orchestrator.action;

import com.example.orchestrator.model.Step;
import com.example.orchestrator.util.ExecutionContext;

import java.util.Map;

public interface ActionExecutor {
    String getType();
    Object execute(Step step, ExecutionContext context, Map<String, Object> requestParams);
}