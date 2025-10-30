package com.philomath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MicroService1 {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(MicroService1.class);
        springApplication.run(args);
    }
}