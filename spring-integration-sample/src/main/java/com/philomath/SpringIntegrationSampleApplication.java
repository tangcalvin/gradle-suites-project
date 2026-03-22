package com.philomath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringIntegrationSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringIntegrationSampleApplication.class, args);
    }
}
