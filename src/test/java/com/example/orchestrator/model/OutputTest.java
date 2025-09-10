package com.example.orchestrator.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutputTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testOutputDeserialization() throws Exception {
        String json = """
                {
                  "parameters": [
                    {
                      "type": "string"
                    },
                    {
                      "type": "integer"
                    }
                  ]
                }
                """;

        Output output = objectMapper.readValue(json, Output.class);

        assertNotNull(output);
        assertNotNull(output.parameters());
        assertEquals(2, output.parameters().size());
        assertEquals("string", output.parameters().get(0).type());
        assertEquals("integer", output.parameters().get(1).type());
    }

    @Test
    void testOutputDeserializationEmpty() throws Exception {
        String json = """
                {
                  "parameters": []
                }
                """;

        Output output = objectMapper.readValue(json, Output.class);

        assertNotNull(output);
        assertNotNull(output.parameters());
        assertTrue(output.parameters().isEmpty());
    }
}