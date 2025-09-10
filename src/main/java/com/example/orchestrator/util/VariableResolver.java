package com.example.orchestrator.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableResolver {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    public static String resolveVariables(String template, Map<String, Object> context) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            if (!context.containsKey(key)) {
                throw new VariableNotFoundException("Variable '" + key + "' not found in context.");
            }
            Object value = context.get(key);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}