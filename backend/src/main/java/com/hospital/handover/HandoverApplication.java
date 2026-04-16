package com.hospital.handover;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HandoverApplication {

    public static void main(String[] args) {
        SpringApplication.run(HandoverApplication.class, args);
    }
}