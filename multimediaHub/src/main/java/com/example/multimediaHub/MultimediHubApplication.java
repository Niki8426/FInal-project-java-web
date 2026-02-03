package com.example.multimediaHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.example.multimediaHub.client")
@SpringBootApplication
public class MultimediHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultimediHubApplication.class, args);
	}

}
