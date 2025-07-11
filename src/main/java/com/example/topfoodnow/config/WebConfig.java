package com.example.topfoodnow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.dynamic-content-base-dir}")
    private String dynamicContentBaseDir;

    @Value("${file.screenshot-sub-dir}")
    private String screenshotSubDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String fullDynamicContentPath = Paths.get(dynamicContentBaseDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/dynamic-content/**")
                .addResourceLocations(fullDynamicContentPath);
    }
}