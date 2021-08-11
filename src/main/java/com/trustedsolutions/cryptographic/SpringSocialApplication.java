package com.trustedsolutions.cryptographic;

import com.trustedsolutions.cryptographic.config.AppProperties;
import com.trustedsolutions.cryptographic.services.storage.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication

@EnableConfigurationProperties(value = { AppProperties.class , StorageProperties.class})
public class SpringSocialApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SpringSocialApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringSocialApplication.class, args);
	}
}
