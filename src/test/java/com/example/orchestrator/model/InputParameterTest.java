package com.example.orchestrator.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputParameterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testInputParameterDeserializationWithValidation() throws Exception {
        String json = """
                {
                  "name": "param1",
                  "type": "string",
                  "required": true,
                  "validation": {
                    "minLength": 1,
                    "maxLength": 10,
                    "pattern": "[a-zA-Z]+"
                  }
                }
                """;

        InputParameter inputParam = objectMapper.readValue(json, InputParameter.class);

        assertNotNull(inputParam);
        assertEquals("param1", inputParam.name());
        assertEquals("string", inputParam.type());
        assertTrue(inputParam.required());
        assertNotNull(inputParam.validation());
        assertEquals(1, inputParam.validation().minLength());
        assertEquals(10, inputParam.validation().maxLength());
        assertEquals("[a-zA-Z]+", inputParam.validation().pattern());
    }

    @Test
    void testInputParameterDeserializationWithoutValidation() throws Exception {
        String json = """
                {
                  "name": "param2",
                  "type": "integer",
                  "required": false
                }
                """;

        InputParameter inputParam = objectMapper.readValue(json, InputParameter.class);

        assertNotNull(inputParam);
        assertEquals("param2", inputParam.name());
        assertEquals("integer", inputParam.type());
        assertFalse(inputParam.required());
        assertNull(inputParam.validation());
    }

    @Test
    void testInputParameterDeserializationWithDefaultRequired() throws Exception {
        String json = """
                {
                  "name": "param3",
                  "type": "boolean"
                }
                """;

        InputParameter inputParam = objectMapper.readValue(json, InputParameter.class);

        assertNotNull(inputParam);
        assertEquals("param3", inputParam.name());
        assertEquals("boolean", inputParam.type());
        assertFalse(inputParam.required()); // Default value should be false
        assertNull(inputParam.validation());
    }
}