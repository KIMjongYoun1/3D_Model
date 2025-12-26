package com.virtualtryon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 * 
 * 역할:
 * - 인증/인가 설정
 * - 비밀번호 암호화 (BCrypt)
 * - JWT 토큰 필터 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    /**
     * 비밀번호 인코더 (BCrypt)
     * 
     * 알고리즘: BCrypt
     * - 자동 salt 생성
     * - 단방향 해싱 (복호화 불가능)
     * 
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Security Filter Chain 설정
     * 
     * - CORS 설정
     * - 세션 비활성화 (JWT 사용)
     * - 인증 필요 경로 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용 시 불필요)
            .csrf(csrf -> csrf.disable())
            
            // CORS 설정
            .cors(cors -> cors.configure(http))
            
            // 세션 비활성화 (JWT 사용)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 인증 필요 경로 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()  // 인증 API는 허용
                .requestMatchers("/health").permitAll()          // 헬스 체크 허용
                .anyRequest().authenticated()                    // 나머지는 인증 필요
            );
        
        return http.build();
    }
}

