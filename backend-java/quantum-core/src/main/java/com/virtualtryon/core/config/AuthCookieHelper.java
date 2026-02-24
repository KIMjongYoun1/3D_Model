package com.virtualtryon.core.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

/**
 * HttpOnly 세션 쿠키 헬퍼.
 * - 창/탭 닫으면 자동 소멸 (session cookie)
 * - XSS로 토큰 탈취 방지
 */
public final class AuthCookieHelper {

    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";

    private final boolean secure;

    public AuthCookieHelper(boolean secure) {
        this.secure = secure;
    }

    /** JWT를 HttpOnly 쿠키로 응답에 추가 */
    public void addAuthCookies(ResponseEntity.BodyBuilder builder, String accessToken, String refreshToken) {
        Objects.requireNonNull(accessToken, "accessToken must not be null");
        Objects.requireNonNull(refreshToken, "refreshToken must not be null");
        var access = org.springframework.http.ResponseCookie.from(ACCESS_TOKEN, accessToken)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Strict")
                .maxAge(-1)  // session cookie: 창 닫으면 소멸
                .build();
        var refresh = org.springframework.http.ResponseCookie.from(REFRESH_TOKEN, refreshToken)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Strict")
                .maxAge(-1)
                .build();
        builder.header(HttpHeaders.SET_COOKIE, access.toString());
        builder.header(HttpHeaders.SET_COOKIE, refresh.toString());
    }

    /** Access Token만 HttpOnly 쿠키로 추가 */
    public void addAccessCookie(ResponseEntity.BodyBuilder builder, String accessToken) {
        Objects.requireNonNull(accessToken, "accessToken must not be null");
        var cookie = org.springframework.http.ResponseCookie.from(ACCESS_TOKEN, accessToken)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Strict")
                .maxAge(-1)
                .build();
        builder.header(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** 로그아웃: 쿠키 즉시 삭제 */
    public void clearAuthCookies(ResponseEntity.BodyBuilder builder) {
        var clearAccess = org.springframework.http.ResponseCookie.from(ACCESS_TOKEN, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Strict")
                .maxAge(0)
                .build();
        var clearRefresh = org.springframework.http.ResponseCookie.from(REFRESH_TOKEN, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Strict")
                .maxAge(0)
                .build();
        builder.header(HttpHeaders.SET_COOKIE, clearAccess.toString());
        builder.header(HttpHeaders.SET_COOKIE, clearRefresh.toString());
    }

    /** 요청에서 쿠키 값 추출 */
    public static String getCookieValue(HttpServletRequest request, String name) {
        if (request == null || name == null) return null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (c != null && name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
