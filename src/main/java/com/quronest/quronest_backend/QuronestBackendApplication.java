package com.quronest.quronest_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class QuronestBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuronestBackendApplication.class, args);
	}

}
