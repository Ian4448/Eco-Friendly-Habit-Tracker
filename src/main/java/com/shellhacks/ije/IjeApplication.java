package com.shellhacks.ije;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={"com.shellhacks.ije.model.User","path.to.my.othercomponents"})
public class IjeApplication {

	public static void main(String[] args) {
		SpringApplication.run(IjeApplication.class, args);
	}

}
