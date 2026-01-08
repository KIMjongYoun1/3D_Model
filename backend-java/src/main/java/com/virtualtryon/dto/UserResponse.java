package com.virtualtryon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 응답 DTO
 * 
 * 역할:
 * - 서버에서 클라이언트로 반환하는 사용자 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private UUID id;
    private String email;
    private String name;
    private String profileImage;
    private String subscription;
    private LocalDateTime createdAt;
}

