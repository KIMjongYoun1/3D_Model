package com.virtualtryon.admin.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

/**
 * Admin HttpOnly 세션 쿠키 헬퍼.
 * - admin_token: JWT 저장, XSS로 토큰 탈취 방지
 * - 창/탭 닫으면 자동 소멸 (session cookie)
 */
public final class AdminCookieHelper {

    public static final String ADMIN_TOKEN = "admin_token";

    private final boolean secure;

    public AdminCookieHelper(boolean secure) {
        this.secure = secure;
    }

    /** Admin JWT를 HttpOnly 쿠키로 응답에 추가 */
    public void addAdminTokenCookie(ResponseEntity.BodyBuilder builder, String accessToken) {
        Objects.requireNonNull(accessToken, "accessToken must not be null");
        var cookie = org.springframework.http.ResponseCookie.from(ADMIN_TOKEN, accessToken)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Strict")
                .maxAge(-1)  // session cookie: 창 닫으면 소멸
                .build();
        builder.header(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** 로그아웃: admin_token 쿠키 즉시 삭제 */
    public void clearAdminTokenCookie(ResponseEntity.BodyBuilder builder) {
        var clearCookie = org.springframework.http.ResponseCookie.from(ADMIN_TOKEN, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Strict")
                .maxAge(0)
                .build();
        builder.header(HttpHeaders.SET_COOKIE, clearCookie.toString());
    }

    /** 요청에서 admin_token 쿠키 값 추출 */
    public static String getAdminToken(HttpServletRequest request) {
        if (request == null) return null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (c != null && ADMIN_TOKEN.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
