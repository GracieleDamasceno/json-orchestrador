package com.example.orchestrator.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ExecutionContext {
    private final Map<String, Object> contextMap;

    public ExecutionContext() {
        this.contextMap = new ConcurrentHashMap<>();
    }

    public void put(String key, Object value) {
        contextMap.put(key, value);
    }

    public Object get(String key) {
        return contextMap.get(key);
    }
}