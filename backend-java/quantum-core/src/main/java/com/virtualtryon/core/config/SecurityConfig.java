package com.virtualtryon.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final HttpsEnforcementFilter httpsEnforcementFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            HttpsEnforcementFilter httpsEnforcementFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.httpsEnforcementFilter = httpsEnforcementFilter;
    }

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
            
            // CORS 설정 (Bean으로 등록된 corsConfigurationSource 사용)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 세션 비활성화 (JWT 사용)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 인증 필요 경로 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()  // 인증 API는 허용
                .requestMatchers("/api/v1/terms/**").permitAll()  // 약관 조회는 공개
                .requestMatchers("/health").permitAll()          // 헬스 체크 허용
                .anyRequest().authenticated()                    // 나머지는 인증 필요
            )
            
            // HTTPS 강제 / HSTS (app.https-only, app.hsts 설정 시)
            .addFilterBefore(httpsEnforcementFilter, UsernamePasswordAuthenticationFilter.class)
            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * CORS 설정 소스
     * - 프론트엔드(localhost:3000)의 접근을 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 서비스용(3000)과 관리자용(3001) 오리진 모두 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}





