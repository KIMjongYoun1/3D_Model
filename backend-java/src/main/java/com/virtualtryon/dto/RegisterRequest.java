package com.virtualtryon.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 회원가입 요청 DTO
 * 
 * 역할:
 * - 클라이언트에서 전송하는 회원가입 요청 데이터
 * - 입력 검증
 */
@Data
public class RegisterRequest {
    
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
     * - @Size: 최소 길이 검증
     */
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;
    
    /**
     * 사용자 이름
     * - @NotBlank: 필수 필드
     */
    @NotBlank(message = "이름을 입력해주세요.")
    private String name;
}

