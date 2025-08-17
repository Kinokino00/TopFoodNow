package com.example.topfoodnow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TopfoodnowApplicationTests {

	@Test
	void contextLoads() {
	}
}