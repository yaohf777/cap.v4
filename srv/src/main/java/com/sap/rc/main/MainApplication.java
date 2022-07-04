package com.sap.rc.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAutoConfiguration(exclude = { LiquibaseAutoConfiguration.class })
public class MainApplication extends SpringBootServletInitializer {
    
	public static void main(String[] args) {

		SpringApplication.run(MainApplication.class, args);

	}
}