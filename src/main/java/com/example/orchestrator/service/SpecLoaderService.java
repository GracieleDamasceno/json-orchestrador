package com.example.orchestrator.service;

import com.example.orchestrator.model.Specification;

public interface SpecLoaderService {
    Specification loadSpec(String product);
}