package com.talentscout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TalentscoutApplication {
    public static void main(String[] args) {
        SpringApplication.run(TalentscoutApplication.class, args);
    }
}