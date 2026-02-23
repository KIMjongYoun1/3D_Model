package com.virtualtryon.admin.config;

import com.virtualtryon.core.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Admin 전용 JWT 인증 필터
 * 
 * core의 JwtAuthenticationFilter와 유사하지만,
 * JWT의 type 클레임이 "admin"인 경우에만 인증을 허용합니다.
 * 일반 사용자("user") 토큰으로는 Admin WAS에 접근할 수 없습니다.
 */
@Component
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public AdminJwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            if (jwtService.validateToken(jwt)) {
                // 토큰 타입 검증: admin 타입만 허용
                String tokenType = jwtService.extractTokenType(jwt);

                if (!"admin".equals(tokenType)) {
                    // admin 타입이 아닌 경우 인증 거부
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"관리자 권한이 필요합니다.\",\"code\":\"ADMIN_TOKEN_REQUIRED\"}");
                    return;
                }

                UUID userId = jwtService.extractUserId(jwt);

                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userId, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
