package com.quronest.quronest_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.liquibase.enabled=true")
class QuronestBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
