package com.virtualtryon.controller;

import com.virtualtryon.dto.LoginRequest;
import com.virtualtryon.dto.LoginResponse;
import com.virtualtryon.dto.RegisterRequest;
import com.virtualtryon.dto.UserResponse;
import com.virtualtryon.entity.User;
import com.virtualtryon.service.AuthService;
import com.virtualtryon.service.NaverAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 컨트롤러
 * 
 * ⚠️ Lombok 제거 버전
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final AuthService authService;
    private final NaverAuthService naverAuthService;

    public AuthController(AuthService authService, NaverAuthService naverAuthService) {
        this.authService = authService;
        this.naverAuthService = naverAuthService;
    }
    
    /**
     * 네이버 소셜 로그인
     */
    @GetMapping("/naver/callback")
    public ResponseEntity<LoginResponse> naverLogin(@RequestParam String code, @RequestParam String state) {
        try {
            AuthService.LoginResult result = naverAuthService.loginWithNaver(code, state);
            
            LoginResponse response = LoginResponse.builder()
                    .accessToken(result.getToken())
                    .tokenType("Bearer")
                    .userId(result.getUser().getId())
                    .email(result.getUser().getEmail())
                    .name(result.getUser().getName())
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthService.LoginResult result = authService.login(request.getEmail(), request.getPassword());
            
            LoginResponse response = LoginResponse.builder()
                    .accessToken(result.getToken())
                    .tokenType("Bearer")
                    .userId(result.getUser().getId())
                    .email(result.getUser().getEmail())
                    .name(result.getUser().getName())
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(
                    request.getEmail(),
                    request.getPassword(),
                    request.getName()
            );
            
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
