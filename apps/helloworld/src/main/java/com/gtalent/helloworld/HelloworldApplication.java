package com.gtalent.helloworld;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.gtalent.helloworld.service.StorageProperties;
import com.gtalent.helloworld.service.StorageService;

@SpringBootApplication
@EnableScheduling
@EnableRetry
@EnableConfigurationProperties(StorageProperties.class)
public class HelloworldApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloworldApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			storageService.deleteAll();
			storageService.init();
		};
	}

}
