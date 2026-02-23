package com.virtualtryon.admin;

import com.virtualtryon.core.config.JwtAuthenticationFilter;
import com.virtualtryon.core.config.SecurityConfig;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@ComponentScan(
    basePackages = {"com.virtualtryon.admin", "com.virtualtryon.core"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
    )
)
@EntityScan(basePackages = "com.virtualtryon.core.entity")
@EnableJpaRepositories(basePackages = "com.virtualtryon.core.repository")
public class AdminApplication {

    private static final Logger log = LoggerFactory.getLogger(AdminApplication.class);

    public static void main(String[] args) {
        Path cwd = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath();
        Path projectRoot = cwd;
        if (cwd.toString().contains("quantum-api-admin")) {
            projectRoot = cwd.getParent().getParent(); // quantum-api-admin -> backend-java -> project root
        } else if (cwd.toString().contains("backend-java")) {
            projectRoot = cwd.getParent();
        }

        Dotenv dotenv = Dotenv.configure()
                .directory(projectRoot.toString())
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        String lawOc = System.getProperty("LAW_API_OC");
        if (lawOc == null || lawOc.isBlank()) {
            log.warn("LAW_API_OC가 로드되지 않았습니다. .env 파일 경로: {} | user.dir: {}", projectRoot, cwd);
        } else {
            log.info("LAW_API_OC 로드됨 (길이={})", lawOc.length());
        }

        SpringApplication.run(AdminApplication.class, args);
    }
}
