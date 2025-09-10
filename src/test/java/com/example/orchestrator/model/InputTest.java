package com.example.orchestrator.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InputTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testInputDeserialization() throws Exception {
        String json = """
                {
                  "parameters": [
                    {
                      "name": "param1",
                      "type": "string",
                      "required": true,
                      "validation": {
                        "minLength": 1
                      }
                    },
                    {
                      "name": "param2",
                      "type": "integer"
                    }
                  ]
                }
                """;

        Input input = objectMapper.readValue(json, Input.class);

        assertNotNull(input);
        assertNotNull(input.parameters());
        assertEquals(2, input.parameters().size());

        InputParameter param1 = input.parameters().get(0);
        assertEquals("param1", param1.name());
        assertEquals("string", param1.type());
        assertTrue(param1.required());
        assertNotNull(param1.validation());
        assertEquals(1, param1.validation().minLength());
        assertNull(param1.validation().maxLength());

        InputParameter param2 = input.parameters().get(1);
        assertEquals("param2", param2.name());
        assertEquals("integer", param2.type());
        assertFalse(param2.required()); // Default value
        assertNull(param2.validation());
    }
}