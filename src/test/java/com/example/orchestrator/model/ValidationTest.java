package com.example.orchestrator.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testValidationDeserializationAllFields() throws Exception {
        String json = """
                {
                  "minLength": 1,
                  "maxLength": 10,
                  "pattern": "[a-zA-Z]+",
                  "min": 0,
                  "max": 100
                }
                """;

        Validation validation = objectMapper.readValue(json, Validation.class);

        assertNotNull(validation);
        assertEquals(1, validation.minLength());
        assertEquals(10, validation.maxLength());
        assertEquals("[a-zA-Z]+", validation.pattern());
        assertEquals(0, validation.min());
        assertEquals(100, validation.max());
    }

    @Test
    void testValidationDeserializationPartialFields() throws Exception {
        String json = """
                {
                  "minLength": 5,
                  "max": 50
                }
                """;

        Validation validation = objectMapper.readValue(json, Validation.class);

        assertNotNull(validation);
        assertEquals(5, validation.minLength());
        assertNull(validation.maxLength());
        assertNull(validation.pattern());
        assertNull(validation.min());
        assertEquals(50, validation.max());
    }

    @Test
    void testValidationDeserializationEmpty() throws Exception {
        String json = "{}";

        Validation validation = objectMapper.readValue(json, Validation.class);

        assertNotNull(validation);
        assertNull(validation.minLength());
        assertNull(validation.maxLength());
        assertNull(validation.pattern());
        assertNull(validation.min());
        assertNull(validation.max());
    }
}