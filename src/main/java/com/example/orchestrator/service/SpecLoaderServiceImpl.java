package com.example.orchestrator.service;

import com.example.orchestrator.model.Specification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SpecLoaderServiceImpl implements SpecLoaderService {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private static final String SPECS_DIRECTORY = "classpath:specs/";

    public SpecLoaderServiceImpl(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @Override
    public Specification loadSpec(String product) {
        String resourcePath = SPECS_DIRECTORY + product + ".json";
        Resource resource = resourceLoader.getResource(resourcePath);

        if (!resource.exists()) {
            throw new SpecNotFoundException("Specification for product '" + product + "' not found.");
        }

        try {
            return objectMapper.readValue(resource.getInputStream(), Specification.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse specification for product '" + product + "'", e);
        }
    }
}