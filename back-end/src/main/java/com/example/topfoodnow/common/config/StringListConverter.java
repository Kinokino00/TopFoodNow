package com.example.topfoodnow.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;
import java.util.Collections;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    private static final Logger logger = LoggerFactory.getLogger(StringListConverter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> stringList) {
        if (stringList == null || stringList.isEmpty()) {
            return null; // 或者返回 "[]"
        }
        try {
            return objectMapper.writeValueAsString(stringList);
        } catch (JsonProcessingException e) {
            logger.error("Error converting List<String> to JSON string: {}", stringList, e);
            // 根據您的錯誤處理策略，可以拋出運行時異常或返回 null
            return null;
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String s) {
        if (s == null || s.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            // 使用 readValue(String json, Class<T> valueType) 或 readValue(String json, TypeReference<T> valueTypeRef)
            return objectMapper.readValue(s, List.class); // 這裡 List.class 雖然編譯器可能警告，但對於 List<String> 通常工作正常
            // 更嚴謹的寫法是使用 TypeReference:
            // return objectMapper.readValue(s, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            logger.error("Error converting JSON string to List<String>: {}", s, e);
            // 根據您的錯誤處理策略，可以拋出運行時異常或返回空列表
            return Collections.emptyList();
        }
    }
}