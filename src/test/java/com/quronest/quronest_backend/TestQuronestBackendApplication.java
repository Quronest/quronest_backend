package com.quronest.quronest_backend;

import org.springframework.boot.SpringApplication;

public class TestQuronestBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(QuronestBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
