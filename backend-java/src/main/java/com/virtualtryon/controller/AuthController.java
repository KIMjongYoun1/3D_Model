package com.virtualtryon.controller;

import com.virtualtryon.dto.LoginRequest;
import com.virtualtryon.dto.LoginResponse;
import com.virtualtryon.dto.RegisterRequest;
import com.virtualtryon.dto.UserResponse;
import com.virtualtryon.entity.User;
import com.virtualtryon.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 컨트롤러
 * 
 * 역할:
 * - 로그인/회원가입 API 엔드포인트 제공
 * - JWT 토큰 발급
 * 
 * ⚠️ 중요: 로그인은 Java Backend에서만 처리
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 로그인
     * 
     * POST /api/v1/auth/login
     * 
     * ⭐ 위변조/탈취 방지:
     * - 입력 검증 (@Valid)
     * - 비밀번호 검증 (BCrypt)
     * - Rate Limiting (SecurityConfig에서 처리)
     * 
     * @param request 로그인 요청 데이터
     * @return 로그인 응답 (JWT 토큰 포함)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 로그인 처리 (JWT 토큰 발급 및 사용자 정보)
            AuthService.LoginResult result = authService.login(request.getEmail(), request.getPassword());
            
            // 응답 생성
            LoginResponse response = LoginResponse.builder()
                    .accessToken(result.getToken())
                    .tokenType("Bearer")
                    .userId(result.getUser().getId())
                    .email(result.getUser().getEmail())
                    .name(result.getUser().getName())
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }
    
    /**
     * 회원가입
     * 
     * POST /api/v1/auth/register
     * 
     * ⭐ 위변조 방지:
     * - 입력 검증 (@Valid)
     * - 이메일 중복 체크
     * - 비밀번호 해싱 (BCrypt)
     * 
     * @param request 회원가입 요청 데이터
     * @return 사용자 정보
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // 회원가입 처리
            User user = authService.register(
                    request.getEmail(),
                    request.getPassword(),
                    request.getName()
            );
            
            // 응답 생성
            UserResponse response = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .subscription(user.getSubscription())
                    .createdAt(user.getCreatedAt())
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }
    
    /**
     * 현재 사용자 정보 조회
     * 
     * GET /api/v1/auth/me
     * 
     * ⚠️ TODO: JWT 토큰에서 사용자 정보 추출 필요
     * 
     * @return 사용자 정보
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        // TODO: JWT 토큰에서 사용자 ID 추출
        // TODO: 사용자 정보 조회
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}

