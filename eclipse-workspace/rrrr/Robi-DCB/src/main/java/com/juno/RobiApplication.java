package com.juno;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
@ComponentScan(basePackages = {"com.juno", "com.juno.controller", "com.juno.redisRepo", "com.juno.util"})
public class RobiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RobiApplication.class, args);
	}

}
