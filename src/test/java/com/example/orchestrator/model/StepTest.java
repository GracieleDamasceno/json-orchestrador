package com.example.orchestrator.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StepTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testStepDeserializationHttp() throws Exception {
        String json = """
                {
                  "id": "step1",
                  "type": "http",
                  "method": "GET",
                  "url": "https://api.example.com/data",
                  "headers": {
                    "Authorization": "Bearer token",
                    "Content-Type": "application/json"
                  },
                  "output": "apiResponse"
                }
                """;

        Step step = objectMapper.readValue(json, Step.class);

        assertNotNull(step);
        assertEquals("step1", step.id());
        assertEquals("http", step.type());
        assertEquals("GET", step.method());
        assertEquals("https://api.example.com/data", step.url());
        assertNotNull(step.headers());
        assertEquals("Bearer token", step.headers().get("Authorization"));
        assertEquals("application/json", step.headers().get("Content-Type"));
        assertNull(step.operation());
        assertNull(step.table());
        assertNull(step.data());
        assertEquals("apiResponse", step.output());
    }

    @Test
    void testStepDeserializationDb() throws Exception {
        String json = """
                {
                  "id": "step2",
                  "type": "db",
                  "operation": "select",
                  "table": "users",
                  "data": {
                    "query": "SELECT * FROM users WHERE id = 1"
                  },
                  "output": "dbResult"
                }
                """;

        Step step = objectMapper.readValue(json, Step.class);

        assertNotNull(step);
        assertEquals("step2", step.id());
        assertEquals("db", step.type());
        assertNull(step.method());
        assertNull(step.url());
        assertNull(step.headers());
        assertEquals("select", step.operation());
        assertEquals("users", step.table());
        assertNotNull(step.data());
        assertTrue(step.data().isObject());
        assertEquals("SELECT * FROM users WHERE id = 1", step.data().get("query").asText());
        assertEquals("dbResult", step.output());
    }

    @Test
    void testStepDeserializationMinimal() throws Exception {
        String json = """
                {
                  "id": "step3",
                  "type": "custom",
                  "output": "customResult"
                }
                """;

        Step step = objectMapper.readValue(json, Step.class);

        assertNotNull(step);
        assertEquals("step3", step.id());
        assertEquals("custom", step.type());
        assertNull(step.method());
        assertNull(step.url());
        assertNull(step.headers());
        assertNull(step.operation());
        assertNull(step.table());
        assertNull(step.data());
        assertEquals("customResult", step.output());
    }
}