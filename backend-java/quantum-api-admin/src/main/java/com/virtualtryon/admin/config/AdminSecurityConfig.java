package com.virtualtryon.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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

import com.virtualtryon.core.config.HttpsEnforcementFilter;

import java.util.Arrays;

/**
 * Admin WAS 전용 Security 설정
 * 
 * quantum-core의 SecurityConfig를 오버라이드하여
 * Admin 전용 인증/인가를 적용합니다.
 * 
 * 차이점:
 * - /api/admin/auth/** → permitAll (Admin 로그인/등록)
 * - /api/admin/** → authenticated (Admin JWT type="admin" 필수)
 * - 일반 사용자 JWT로는 접근 불가 (AdminJwtAuthenticationFilter에서 검증)
 */
@Configuration
@EnableWebSecurity
public class AdminSecurityConfig {

    private final AdminJwtAuthenticationFilter adminJwtAuthenticationFilter;
    private final HttpsEnforcementFilter httpsEnforcementFilter;

    public AdminSecurityConfig(
            AdminJwtAuthenticationFilter adminJwtAuthenticationFilter,
            HttpsEnforcementFilter httpsEnforcementFilter) {
        this.adminJwtAuthenticationFilter = adminJwtAuthenticationFilter;
        this.httpsEnforcementFilter = httpsEnforcementFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Admin WAS Security Filter Chain
     * 
     * @Primary를 사용하여 core의 SecurityConfig보다 우선 적용
     */
    @Bean
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/auth/**").permitAll()  // Admin 로그인/등록 허용
                .requestMatchers("/health").permitAll()              // 헬스 체크 허용
                .anyRequest().authenticated()                        // 나머지는 Admin JWT 인증 필요
            )
            .addFilterBefore(httpsEnforcementFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(adminJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3001"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
