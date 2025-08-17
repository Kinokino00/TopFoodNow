package com.example.topfoodnow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"gcp.storage.credentials.location=classpath:test-gcs-key.json", // 假的路徑
		"spring.cloud.gcp.project-id=test-project-id" // 假的專案ID
})
class TopfoodnowApplicationTests {
	@Test
	void contextLoads() {
	}
}