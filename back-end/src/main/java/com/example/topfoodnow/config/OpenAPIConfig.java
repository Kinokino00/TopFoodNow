package com.example.topfoodnow.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Top Food Now API",
        version = "1.0",
        description = "為用戶提供推薦店家功能，讓用戶可以分享他們喜愛的美食地點。",
        contact = @Contact(
            name = "Kino",
            url = "https://github.com/Kinokino00",
            email = "your.email@example.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "本地開發環境"),
        @Server(url = "http://13.236.137.186:8080", description = "EC2 測試環境")
    }
)
public class OpenAPIConfig {
}