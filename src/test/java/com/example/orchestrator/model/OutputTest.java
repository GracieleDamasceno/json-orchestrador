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
                      "name": "field1",
                      "value": "${step1.result}"
                    },
                    {
                      "name": "field2",
                      "value": "${step2.result}"
                    }
                  ]
                }
                """;

        Output output = objectMapper.readValue(json, Output.class);

        assertNotNull(output);
        assertNotNull(output.parameters());
        assertEquals(2, output.parameters().size());
        assertEquals("field1", output.parameters().get(0).name());
        assertEquals("${step1.result}", output.parameters().get(0).value());
        assertEquals("field2", output.parameters().get(1).name());
        assertEquals("${step2.result}", output.parameters().get(1).value());
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