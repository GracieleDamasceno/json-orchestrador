package com.example.orchestrator.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VariableResolver {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    public <T> T resolveVariables(T template, Map<String, Object> context) {
        if (template == null) {
            return null;
        }

        if (template instanceof String) {
            String stringTemplate = (String) template;
            Matcher matcher = VARIABLE_PATTERN.matcher(stringTemplate);
            if (matcher.matches()) {
                String key = matcher.group(1);
                return (T) getValueFromContext(key, context);
            }
            return (T) resolveString(stringTemplate, context);
        } else if (template instanceof Map) {
            return (T) resolveMap((Map<String, Object>) template, context);
        }
        // For other types, return as is
        return template;
    }

    private String resolveString(String template, Map<String, Object> context) {
        if (template.isEmpty()) {
            return template;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = getValueFromContext(key, context);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    private Map<String, Object> resolveMap(Map<String, Object> map, Map<String, Object> context) {
        Map<String, Object> resolvedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            resolvedMap.put(entry.getKey(), resolveVariables(entry.getValue(), context));
        }
        return resolvedMap;
    }

    private Object getValueFromContext(String key, Map<String, Object> context) {
        String[] parts = key.split("\\.");
        Object value = context;
        for (String part : parts) {
            if (value instanceof Map) {
                value = ((Map<?, ?>) value).get(part);
            } else {
                throw new VariableNotFoundException("Variable '" + key + "' not found in context.");
            }
        }
        if (value == null) {
            throw new VariableNotFoundException("Variable '" + key + "' not found in context.");
        }
        return value;
    }
}