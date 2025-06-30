// src/main/java/com/example/topfoodnow/config/WebConfig.java

package com.example.topfoodnow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${file.dynamic-content-base-dir}")
    private String dynamicContentBaseDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/dynamic-content/**").addResourceLocations("file:" + dynamicContentBaseDir);
    }
}