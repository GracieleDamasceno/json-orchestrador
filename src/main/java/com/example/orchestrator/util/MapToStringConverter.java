package com.example.orchestrator.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;
import java.util.Map;

@Converter
public class MapToStringConverter implements AttributeConverter<Map<String, String>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> stringStringMap) {
        try {
            return objectMapper.writeValueAsString(stringStringMap);
        } catch (JsonProcessingException e) {
            // Handle the exception, e.g., log it or throw a custom exception
            return null;
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String s) {
        try {
            return objectMapper.readValue(s, Map.class);
        } catch (IOException e) {
            // Handle the exception
            return null;
        }
    }
}