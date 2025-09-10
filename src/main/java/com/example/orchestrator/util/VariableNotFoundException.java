package com.example.orchestrator.util;

public class VariableNotFoundException extends RuntimeException {
    public VariableNotFoundException(String message) {
        super(message);
    }
}