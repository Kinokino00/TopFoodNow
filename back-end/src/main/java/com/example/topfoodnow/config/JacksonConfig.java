package com.example.topfoodnow.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // 標記為配置類
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // 允許 JSON 中包含註釋 (例如 // 或 /* */)，雖然不推薦在生產數據中使用
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // 允許非引用字段名 (例如 {field: value} 而不是 {"field": "value"})
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 允許單引號 (例如 {'field': 'value'})
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        return mapper;
    }
}