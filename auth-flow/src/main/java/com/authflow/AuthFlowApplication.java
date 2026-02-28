package com.authflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuthFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthFlowApplication.class, args);
	}

}
