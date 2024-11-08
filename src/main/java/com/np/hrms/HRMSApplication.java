package com.np.hrms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HRMSApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(HRMSApplication.class, args);
	}

}
