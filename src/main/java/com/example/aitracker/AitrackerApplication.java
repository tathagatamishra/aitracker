package com.example.aitracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AitrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AitrackerApplication.class, args);
	}

}
