package com.example.orchestrator.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VariableResolverTest {

    @Test
    void resolveVariables_noVariables() {
        String template = "http://example.com/api/data";
        Map<String, Object> context = new HashMap<>();
        context.put("param1", "value1");
        context.put("param2", "value2");

        String result = VariableResolver.resolveVariables(template, context);
        assertEquals("http://example.com/api/data", result);
    }

    @Test
    void resolveVariables_oneVariable() {
        String template = "http://example.com/api/${param1}";
        Map<String, Object> context = new HashMap<>();
        context.put("param1", "value1");

        String result = VariableResolver.resolveVariables(template, context);
        assertEquals("http://example.com/api/value1", result);
    }

    @Test
    void resolveVariables_multipleVariables() {
        String template = "http://example.com/api/${param1}/${param2}";
        Map<String, Object> context = new HashMap<>();
        context.put("param1", "value1");
        context.put("param2", "value2");

        String result = VariableResolver.resolveVariables(template, context);
        assertEquals("http://example.com/api/value1/value2", result);
    }

    @Test
    void resolveVariables_variableNotFound() {
        String template = "http://example.com/api/${param1}";
        Map<String, Object> context = new HashMap<>();
        context.put("anotherParam", "anotherValue");

        VariableNotFoundException thrown = assertThrows(VariableNotFoundException.class, () -> {
            VariableResolver.resolveVariables(template, context);
        });
        assertTrue(thrown.getMessage().contains("Variable 'param1' not found in context."));
    }

    @Test
    void resolveVariables_emptyTemplate() {
        String template = "";
        Map<String, Object> context = new HashMap<>();
        context.put("param1", "value1");

        String result = VariableResolver.resolveVariables(template, context);
        assertEquals("", result);
    }

    @Test
    void resolveVariables_nullTemplate() {
        String template = null;
        Map<String, Object> context = new HashMap<>();
        context.put("param1", "value1");

        String result = VariableResolver.resolveVariables(template, context);
        assertNull(result);
    }

    @Test
    void resolveVariables_variableWithSpecialCharacters() {
        String template = "http://example.com/api/${param_with_special_chars}";
        Map<String, Object> context = new HashMap<>();
        context.put("param_with_special_chars", "value-with-!@#$");

        String result = VariableResolver.resolveVariables(template, context);
        assertEquals("http://example.com/api/value-with-!@#$", result);
    }

    @Test
    void resolveVariables_variableInHeader() {
        String template = "Bearer ${token}";
        Map<String, Object> context = new HashMap<>();
        context.put("token", "mySecretToken123");

        String result = VariableResolver.resolveVariables(template, context);
        assertEquals("Bearer mySecretToken123", result);
    }

    @Test
    void resolveVariables_multipleOccurrencesOfSameVariable() {
        String template = "User: ${username}, Email: ${username}@example.com";
        Map<String, Object> context = new HashMap<>();
        context.put("username", "john.doe");

        String result = VariableResolver.resolveVariables(template, context);
        assertEquals("User: john.doe, Email: john.doe@example.com", result);
    }
}