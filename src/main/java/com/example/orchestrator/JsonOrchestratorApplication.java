package com.example.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example.orchestrator")
@EnableJpaRepositories(basePackages = "com.example.orchestrator.repository")
public class JsonOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsonOrchestratorApplication.class, args);
    }

}