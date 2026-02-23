package com.virtualtryon.core.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 프로덕션 보안: HTTPS 강제 및 HSTS 헤더
 *
 * - app.https-only=true: HTTP 요청 시 403 (X-Forwarded-Proto 또는 request.isSecure() 기준)
 * - app.hsts=true: 응답에 Strict-Transport-Security 헤더 추가
 *
 * 개발 환경에서는 두 설정 모두 false로 두고, 운영에서 true로 설정해 사용한다.
 */
@Component
public class HttpsEnforcementFilter extends OncePerRequestFilter {

    @Value("${app.https-only:false}")
    private boolean httpsOnly;

    @Value("${app.hsts:false}")
    private boolean hsts;

    private static final String HSTS_HEADER_VALUE = "max-age=31536000; includeSubDomains";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (hsts) {
            response.setHeader("Strict-Transport-Security", HSTS_HEADER_VALUE);
        }

        if (httpsOnly && !isSecure(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"error\":\"HTTPS required.\",\"code\":\"HTTPS_REQUIRED\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSecure(HttpServletRequest request) {
        if (request.isSecure()) {
            return true;
        }
        String proto = request.getHeader("X-Forwarded-Proto");
        return "https".equalsIgnoreCase(proto);
    }
}
