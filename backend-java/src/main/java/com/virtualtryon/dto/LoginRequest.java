package com.virtualtryon.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 로그인 요청 DTO
 * 
 * 역할:
 * - 클라이언트에서 전송하는 로그인 요청 데이터
 * - 입력 검증
 */
@Data
public class LoginRequest {
    
    /**
     * 이메일
     * - @Email: 이메일 형식 검증
     * - @NotBlank: 필수 필드
     */
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    
    /**
     * 비밀번호
     * - @NotBlank: 필수 필드
     */
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}

