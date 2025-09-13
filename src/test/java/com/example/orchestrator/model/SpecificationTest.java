package com.example.orchestrator.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpecificationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSpecificationDeserialization() throws Exception {
        String json = """
                {
                  "input": {
                    "parameters": [
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
                    ]
                  },
                  "steps": [
                    {
                      "id": "step1",
                      "type": "http",
                      "method": "GET",
                      "url": "https://api.example.com/data",
                      "headers": {
                        "Authorization": "Bearer token"
                      },
                      "output": "apiResponse"
                    }
                  ],
                  "output": {
                    "parameters": [
                      {
                        "name": "outputField",
                        "value": "${apiResponse}"
                      }
                    ]
                  }
                }
                """;

        Specification spec = objectMapper.readValue(json, Specification.class);

        assertNotNull(spec);
        assertNotNull(spec.input());
        assertNotNull(spec.input().parameters());
        assertEquals(1, spec.input().parameters().size());

        InputParameter inputParam = spec.input().parameters().get(0);
        assertEquals("param1", inputParam.name());
        assertEquals("string", inputParam.type());
        assertTrue(inputParam.required());
        assertNotNull(inputParam.validation());
        assertEquals(1, inputParam.validation().minLength());
        assertEquals(10, inputParam.validation().maxLength());
        assertEquals("[a-zA-Z]+", inputParam.validation().pattern());

        assertNotNull(spec.steps());
        assertEquals(1, spec.steps().size());

        Step step = spec.steps().get(0);
        assertEquals("step1", step.id());
        assertEquals("http", step.type());
        assertEquals("GET", step.method());
        assertEquals("https://api.example.com/data", step.url());
        assertNotNull(step.headers());
        assertEquals("Bearer token", step.headers().get("Authorization"));
        assertEquals("apiResponse", step.output());

        assertNotNull(spec.output());
        assertNotNull(spec.output().parameters());
        assertEquals(1, spec.output().parameters().size());
        assertEquals("outputField", spec.output().parameters().get(0).name());
        assertEquals("${apiResponse}", spec.output().parameters().get(0).value());
    }
}