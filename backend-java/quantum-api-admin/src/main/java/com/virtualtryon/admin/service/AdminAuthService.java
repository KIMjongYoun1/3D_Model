package com.virtualtryon.admin.service;

import com.virtualtryon.core.entity.AdminUser;
import com.virtualtryon.core.repository.AdminUserRepository;
import com.virtualtryon.core.service.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Admin 인증 서비스
 * 
 * 역할:
 * - 관리자 로그인 처리
 * - 관리자 계정 생성
 * - Admin JWT 토큰 발급 (type="admin")
 */
@Service
public class AdminAuthService {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthService.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminAuthService(AdminUserRepository adminUserRepository,
                            PasswordEncoder passwordEncoder,
                            JwtService jwtService) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * 관리자 로그인
     */
    @Transactional
    public AdminLoginResult login(String email, String password) {
        String emailTrimmed = Objects.requireNonNull(email, "email must not be null").trim();
        // 1. 활성 상태의 관리자 조회
        AdminUser admin = adminUserRepository.findByEmailAndIsActiveTrue(emailTrimmed)
                .orElseThrow(() -> {
                    log.debug("Admin 로그인 실패: 이메일 없음 또는 비활성 계정. email={}", emailTrimmed);
                    return new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
                });

        // 2. 비밀번호 검증 (저장된 해시가 없으면 로그인 불가)
        String storedHash = admin.getPasswordHash();
        if (storedHash == null || storedHash.isBlank()) {
            log.warn("Admin 계정에 password_hash 없음. adminId={}", admin.getId());
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!passwordEncoder.matches(password, storedHash)) {
            log.debug("Admin 로그인 실패: 비밀번호 불일치. email={}", emailTrimmed);
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 3. Admin JWT 토큰 생성 (type="admin")
        String accessToken = jwtService.generateToken(admin.getId(), "admin");
        String refreshToken = jwtService.generateRefreshToken(admin.getId(), "admin");

        // 4. 마지막 로그인 시간 갱신
        admin.setLastLoginAt(LocalDateTime.now());
        adminUserRepository.save(admin);

        return new AdminLoginResult(accessToken, refreshToken, admin);
    }

    /**
     * 관리자 계정 생성 (초기 개발 단계용)
     */
    @Transactional
    public AdminUser register(String email, String password, String name, String role) {
        String emailTrimmed = Objects.requireNonNull(email, "email must not be null").trim();
        // 1. 이메일 중복 체크
        if (adminUserRepository.existsByEmail(emailTrimmed)) {
            throw new IllegalArgumentException("이미 사용 중인 관리자 이메일입니다.");
        }

        // 2. 비밀번호 해싱 (BCrypt 단방향 해시, 로그인 시 matches()로 비교)
        String passwordHash = passwordEncoder.encode(
                Objects.requireNonNull(password, "password must not be null"));

        // 3. 관리자 생성
        AdminUser admin = AdminUser.builder()
                .email(emailTrimmed)
                .name(Objects.requireNonNull(name, "name must not be null"))
                .role(role != null ? role : "ADMIN")
                .build();
        admin.setPasswordHash(passwordHash);

        // 4. 저장
        return adminUserRepository.save(admin);
    }

    /**
     * 현재 관리자 정보 조회
     */
    @Transactional(readOnly = true)
    public AdminUser getCurrentAdmin(UUID adminId) {
        return adminUserRepository.findById(
                Objects.requireNonNull(adminId, "adminId must not be null")
        ).orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));
    }

    /**
     * 관리자 로그인 결과 클래스
     */
    public static class AdminLoginResult {
        private final String accessToken;
        private final String refreshToken;
        private final AdminUser admin;

        public AdminLoginResult(String accessToken, String refreshToken, AdminUser admin) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.admin = admin;
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public AdminUser getAdmin() { return admin; }
    }
}
