package com.example.orchestrator.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutputParameterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testOutputParameterDeserialization() throws Exception {
        String json = """
                {
                  "name": "testName",
                  "value": "${testValue}"
                }
                """;

        OutputParameter outputParam = objectMapper.readValue(json, OutputParameter.class);

        assertNotNull(outputParam);
        assertEquals("testName", outputParam.name());
        assertEquals("${testValue}", outputParam.value());
    }
}