package com.virtualtryon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 로그인 응답 DTO
 * 
 * 역할:
 * - 서버에서 클라이언트로 반환하는 로그인 결과 데이터
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    /**
     * JWT 액세스 토큰
     */
    private String accessToken;
    
    /**
     * 토큰 타입 (Bearer)
     */
    private String tokenType = "Bearer";
    
    /**
     * 사용자 ID
     */
    private UUID userId;
    
    /**
     * 사용자 이메일
     */
    private String email;
    
    /**
     * 사용자 이름
     */
    private String name;
}

