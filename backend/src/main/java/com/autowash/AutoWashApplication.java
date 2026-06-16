package com.autowash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutoWashApplication {

    public static void main(String[] args) {

        SpringApplication.run(AutoWashApplication.class, args);
    }

}