package com.virtualtryon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Virtual Try-On Java Backend 메인 애플리케이션
 * 
 * 역할:
 * - 비즈니스 로직 처리 (사용자 관리, 구독, 결제)
 * - 데이터베이스 관리 (JPA/Hibernate)
 * - 인증/인가 처리 (Spring Security + JWT)
 * - API 엔드포인트 제공 (REST API)
 * 
 * @SpringBootApplication: Spring Boot 자동 설정 활성화
 *   - @Configuration: 설정 클래스
 *   - @EnableAutoConfiguration: 자동 설정 활성화
 *   - @ComponentScan: 컴포넌트 스캔 (controller, service, repository 등)
 */
@SpringBootApplication
public class VirtualTryOnApplication {
    
    /**
     * 애플리케이션 진입점
     * 
     * @param args 명령줄 인수
     */
    public static void main(String[] args) {
        // Spring Boot 애플리케이션 실행
        // - 내장 톰캣 서버 시작
        // - application.yml 설정 로드
        // - 컴포넌트 스캔 및 의존성 주입
        SpringApplication.run(VirtualTryOnApplication.class, args);
    }
}

