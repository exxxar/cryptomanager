package com.trustedsolutions.cryptographic;

import com.trustedsolutions.cryptographic.config.AppProperties;
import com.trustedsolutions.cryptographic.services.storage.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication

@EnableConfigurationProperties(value = { AppProperties.class , StorageProperties.class})
public class SpringSocialApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSocialApplication.class, args);
	}
}
