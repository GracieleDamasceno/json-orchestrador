package com.example.orchestrator.service;

public class SpecNotFoundException extends RuntimeException {
    public SpecNotFoundException(String message) {
        super(message);
    }
}