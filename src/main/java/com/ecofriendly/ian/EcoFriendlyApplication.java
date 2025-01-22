package com.ecofriendly.ian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class EcoFriendlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcoFriendlyApplication.class, args);
	}

}
