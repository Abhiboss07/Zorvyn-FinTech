package com.zorvyn.fintech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ZorvynFintechApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZorvynFintechApplication.class, args);
    }
}
