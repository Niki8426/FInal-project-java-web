package com.example.gift_svc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GiftSvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(GiftSvcApplication.class, args);
	}

}
