// IjeApplication.java
package com.shellhacks.ije;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class IjeApplication {

	public static void main(String[] args) {
		SpringApplication.run(IjeApplication.class, args);
	}

}
