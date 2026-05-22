package com.suporte.tickets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SuporteTicketsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuporteTicketsApplication.class, args);
    }
}