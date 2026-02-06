package com.virtualtryon.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT 토큰 서비스
 * 
 * 역할:
 * - JWT 토큰 생성
 * - JWT 토큰 검증
 * - 토큰에서 사용자 정보 추출
 * 
 * ⚠️ 중요: Python Backend와 같은 JWT_SECRET 사용해야 함
 */
@Service
public class JwtService {
    
    /**
     * JWT 비밀키
     * - application.yml에서 설정
     * - Python Backend와 동일한 값 사용
     */
    @Value("${jwt.secret:your-super-secret-key-change-this-in-production}")
    private String secret;
    
    /**
     * JWT 만료 시간 (밀리초)
     * - 기본값: 60분
     */
    @Value("${jwt.expire-minutes:60}")
    private int expireMinutes;
    
    /**
     * Secret Key 생성
     * 
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * JWT 토큰 생성
     * 
     * ⭐ 위변조 방지:
     * - 비밀키로 서명 생성
     * - 만료 시간 포함
     * - 발급 시간 포함
     * 
     * @param userId 사용자 ID
     * @return JWT 토큰 문자열
     */
    public String generateToken(UUID userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId.toString());  // subject: 사용자 ID
        
        return createToken(claims);
    }
    
    /**
     * JWT 토큰 생성 (내부 메서드)
     * 
     * @param claims 클레임 (토큰에 포함할 데이터)
     * @return JWT 토큰 문자열
     */
    private String createToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (expireMinutes * 60 * 1000));
        
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)  // 발급 시간 (iat)
                .expiration(expiryDate)  // 만료 시간 (exp)
                .signWith(getSigningKey())  // 서명 (알고리즘 자동 선택)
                .compact();
    }
    
    /**
     * JWT 토큰에서 사용자 ID 추출
     * 
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, Claims::getSubject);
        return UUID.fromString(userIdStr);
    }
    
    /**
     * JWT 토큰에서 만료 시간 추출
     * 
     * @param token JWT 토큰
     * @return 만료 시간
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * JWT 토큰에서 클레임 추출
     * 
     * @param token JWT 토큰
     * @param claimsResolver 클레임 추출 함수
     * @return 클레임 값
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * JWT 토큰에서 모든 클레임 추출
     * 
     * ⭐ 위변조 방지: 서명 검증 자동 수행
     * 
     * @param token JWT 토큰
     * @return 모든 클레임
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // 서명 검증
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * JWT 토큰 검증
     * 
     * ⭐ 위변조 방지:
     * - 서명 검증
     * - 만료 시간 검증
     * 
     * @param token JWT 토큰
     * @return 유효 여부
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);  // 서명 검증 및 만료 시간 검증
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 토큰 만료 여부 확인
     * 
     * @param token JWT 토큰
     * @return 만료 여부
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * 사용자 ID 검증
     * 
     * ⭐ 검증 패턴:
     * 1. 클레임에서 사용자 ID 추출
     * 2. 인자로 받은 사용자 ID와 비교
     * 
     * @param token JWT 토큰
     * @param expectedUserId 예상되는 사용자 ID
     * @return 일치 여부
     */
    public boolean validateUserId(String token, UUID expectedUserId) {
        try {
            UUID tokenUserId = extractUserId(token);
            return tokenUserId.equals(expectedUserId);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 커스텀 클레임 검증
     * 
     * ⭐ 검증 패턴:
     * 1. 클레임에서 값 추출
     * 2. 인자로 받은 값과 비교
     * 
     * @param token JWT 토큰
     * @param claimName 클레임 이름 (예: "email", "role")
     * @param expectedValue 예상되는 값
     * @return 일치 여부
     */
    public boolean validateClaim(String token, String claimName, Object expectedValue) {
        try {
            Claims claims = extractAllClaims(token);
            Object claimValue = claims.get(claimName);
            return expectedValue.equals(claimValue);
        } catch (Exception e) {
            return false;
        }
    }
}

