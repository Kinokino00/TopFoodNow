package com.example.topfoodnow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class TopfoodnowApplication {
	public static void main(String[] args) {
		SpringApplication.run(TopfoodnowApplication.class, args);
	}
}
