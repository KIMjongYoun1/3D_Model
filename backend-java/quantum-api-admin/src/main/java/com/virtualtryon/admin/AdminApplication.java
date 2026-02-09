package com.virtualtryon.admin;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.virtualtryon.admin", "com.virtualtryon.core"})
@EntityScan(basePackages = "com.virtualtryon.core.entity")
@EnableJpaRepositories(basePackages = "com.virtualtryon.core.repository")
public class AdminApplication {
    
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory("../../") // 루트 디렉토리 탐색 (quantum-api-admin 에서 루트까지 2단계 상위)
                .ignoreIfMissing()
                .load();
        
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(AdminApplication.class, args);
    }
}
