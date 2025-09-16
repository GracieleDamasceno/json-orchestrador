package com.example.orchestrator.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VariableResolverTest {

    @InjectMocks
    private VariableResolver variableResolver;

    private Map<String, Object> context;

    @BeforeEach
    void setUp() {
        context = new HashMap<>();
        context.put("param1", "value1");
        context.put("param2", "value2");
        context.put("anotherParam", "anotherValue");
        context.put("param_with_special_chars", "value-with-!@#$");
        context.put("token", "mySecretToken123");
        context.put("username", "john.doe");
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("nestedKey", "${param1}-nested");
        context.put("nestedMap", nestedMap);
    }

    @Test
    void resolveVariables_string_noVariables() {
        String template = "http://example.com/api/data";
        String result = variableResolver.resolveVariables(template, context);
        assertEquals("http://example.com/api/data", result);
    }

    @Test
    void resolveVariables_string_oneVariable() {
        String template = "http://example.com/api/${param1}";
        String result = variableResolver.resolveVariables(template, context);
        assertEquals("http://example.com/api/value1", result);
    }

    @Test
    void resolveVariables_string_multipleVariables() {
        String template = "http://example.com/api/${param1}/${param2}";
        String result = variableResolver.resolveVariables(template, context);
        assertEquals("http://example.com/api/value1/value2", result);
    }

    @Test
    void resolveVariables_string_variableNotFound() {
        String template = "http://example.com/api/${nonExistentParam}";
        VariableNotFoundException thrown = assertThrows(VariableNotFoundException.class, () -> {
            variableResolver.resolveVariables(template, context);
        });
        assertTrue(thrown.getMessage().contains("Variable 'nonExistentParam' not found in context."));
    }

    @Test
    void resolveVariables_string_emptyTemplate() {
        String template = "";
        String result = variableResolver.resolveVariables(template, context);
        assertEquals("", result);
    }

    @Test
    void resolveVariables_string_nullTemplate() {
        String template = null;
        String result = variableResolver.resolveVariables(template, context);
        assertNull(result);
    }

    @Test
    void resolveVariables_string_variableWithSpecialCharacters() {
        String template = "http://example.com/api/${param_with_special_chars}";
        String result = variableResolver.resolveVariables(template, context);
        assertEquals("http://example.com/api/value-with-!@#$", result);
    }

    @Test
    void resolveVariables_string_variableInHeader() {
        String template = "Bearer ${token}";
        String result = variableResolver.resolveVariables(template, context);
        assertEquals("Bearer mySecretToken123", result);
    }

    @Test
    void resolveVariables_string_multipleOccurrencesOfSameVariable() {
        String template = "User: ${username}, Email: ${username}@example.com";
        String result = variableResolver.resolveVariables(template, context);
        assertEquals("User: john.doe, Email: john.doe@example.com", result);
    }

    @Test
    void resolveVariables_map_noVariables() {
        Map<String, Object> mapTemplate = new HashMap<>();
        mapTemplate.put("key1", "valueA");
        mapTemplate.put("key2", 123);

        Map<String, Object> result = variableResolver.resolveVariables(mapTemplate, context);
        assertEquals("valueA", result.get("key1"));
        assertEquals(123, result.get("key2"));
    }

    @Test
    void resolveVariables_map_withVariables() {
        Map<String, Object> mapTemplate = new HashMap<>();
        mapTemplate.put("key1", "Hello ${username}");
        mapTemplate.put("key2", "${param1}");
        mapTemplate.put("key3", 456);

        Map<String, Object> result = variableResolver.resolveVariables(mapTemplate, context);
        assertEquals("Hello john.doe", result.get("key1"));
        assertEquals("value1", result.get("key2"));
        assertEquals(456, result.get("key3"));
    }

    @Test
    void resolveVariables_map_withNestedMap() {
        Map<String, Object> mapTemplate = new HashMap<>();
        mapTemplate.put("outerKey", "Outer ${param2}");
        mapTemplate.put("innerMap", context.get("nestedMap"));

        Map<String, Object> result = variableResolver.resolveVariables(mapTemplate, context);
        assertEquals("Outer value2", result.get("outerKey"));
        assertTrue(result.get("innerMap") instanceof Map);
        Map<String, Object> innerMap = (Map<String, Object>) result.get("innerMap");
        assertEquals("value1-nested", innerMap.get("nestedKey"));
    }

    @Test
    void resolveVariables_map_variableNotFound() {
        Map<String, Object> mapTemplate = new HashMap<>();
        mapTemplate.put("key1", "Hello ${nonExistentParam}");

        VariableNotFoundException thrown = assertThrows(VariableNotFoundException.class, () -> {
            variableResolver.resolveVariables(mapTemplate, context);
        });
        assertTrue(thrown.getMessage().contains("Variable 'nonExistentParam' not found in context."));
    }
}