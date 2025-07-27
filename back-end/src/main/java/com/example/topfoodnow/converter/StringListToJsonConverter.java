package com.example.topfoodnow.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter
public class StringListToJsonConverter implements AttributeConverter<List<String>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TypeReference<List<String>> typeRef = new TypeReference<List<String>>() {};

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting List<String> to JSON string: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty() || "[]".equals(dbData.trim())) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON string to List<String>: " + e.getMessage(), e);
        }
    }
}