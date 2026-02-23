package com.virtualtryon.admin.controller;

import com.virtualtryon.admin.service.AdminAuthService;
import com.virtualtryon.core.entity.AdminUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Admin 인증 컨트롤러
 * 
 * 관리자 전용 로그인/등록 API
 * 일반 사용자 AuthController와 완전히 분리된 인증 체계
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * 관리자 로그인
     * POST /api/admin/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        try {
            String email = request != null ? request.get("email") : null;
            String password = request != null ? request.get("password") : null;
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "이메일을 입력하세요.", "code", "INVALID_INPUT"));
            }
            if (password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "비밀번호를 입력하세요.", "code", "INVALID_INPUT"));
            }
            email = email.trim();

            AdminAuthService.AdminLoginResult result = adminAuthService.login(email, password);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", result.getAccessToken());
            response.put("refreshToken", result.getRefreshToken());
            response.put("tokenType", "Bearer");
            response.put("adminId", result.getAdmin().getId());
            response.put("email", result.getAdmin().getEmail());
            response.put("name", result.getAdmin().getName());
            response.put("role", result.getAdmin().getRole());

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
            error.put("code", "INVALID_CREDENTIALS");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * 관리자 계정 생성 (초기 개발 단계용)
     * POST /api/admin/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        try {
            String email = request != null ? request.get("email") : null;
            String password = request != null ? request.get("password") : null;
            String name = request != null ? request.get("name") : null;
            String role = request != null ? request.get("role") : null;
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "이메일을 입력하세요.", "code", "INVALID_INPUT"));
            }
            if (password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "비밀번호를 입력하세요.", "code", "INVALID_INPUT"));
            }
            email = email.trim();

            AdminUser admin = adminAuthService.register(email, password, name, role);

            Map<String, Object> response = new HashMap<>();
            response.put("adminId", admin.getId());
            response.put("email", admin.getEmail());
            response.put("name", admin.getName());
            response.put("role", admin.getRole());
            response.put("createdAt", admin.getCreatedAt());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("code", "REGISTRATION_FAILED");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * 현재 관리자 정보 조회
     * GET /api/admin/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentAdmin() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UUID) {
                UUID adminId = (UUID) principal;
                AdminUser admin = adminAuthService.getCurrentAdmin(adminId);

                Map<String, Object> response = new HashMap<>();
                response.put("adminId", admin.getId());
                response.put("email", admin.getEmail());
                response.put("name", admin.getName());
                response.put("role", admin.getRole());
                response.put("isActive", admin.getIsActive());
                response.put("lastLoginAt", admin.getLastLoginAt());
                response.put("createdAt", admin.getCreatedAt());

                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
