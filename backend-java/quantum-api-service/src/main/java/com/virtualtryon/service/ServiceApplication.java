package com.virtualtryon.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.virtualtryon.service", "com.virtualtryon.core", "com.virtualtryon.payment.mock"})
@EntityScan(basePackages = "com.virtualtryon.core.entity")
@EnableJpaRepositories(basePackages = "com.virtualtryon.core.repository")
public class ServiceApplication {
    
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory("../../") // 루트 디렉토리 탐색 (quantum-api-service 에서 루트까지 2단계 상위)
                .ignoreIfMissing()
                .load();
        
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(ServiceApplication.class, args);
    }
}

