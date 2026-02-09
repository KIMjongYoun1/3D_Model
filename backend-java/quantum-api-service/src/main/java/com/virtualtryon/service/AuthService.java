package com.virtualtryon.service;

import com.virtualtryon.core.entity.User;
import com.virtualtryon.core.repository.UserRepository;
import com.virtualtryon.core.service.JwtService;
import com.virtualtryon.core.service.PasswordService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * 인증 서비스
 * 
 * 역할:
 * - 로그인 처리
 * - 회원가입 처리
 * - JWT 토큰 발급
 * 
 * ⚠️ 중요: 로그인/회원가입은 Java Backend에서만 처리
 */
@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordService passwordService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }
    
    /**
     * 로그인
     */
    @Transactional
    public LoginResult login(String email, String password) {
        // 1. 사용자 조회
        User user = userRepository.findByEmail(Objects.requireNonNull(email, "email must not be null"))
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));
        
        // 2. 삭제된 사용자 체크
        if (user.getDeletedAt() != null) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        
        // 3. 비밀번호 검증 (소셜 로그인 사용자는 passwordHash가 null일 수 있음)
        String storedHash = user.getPasswordHash();
        if (storedHash == null || !passwordService.matches(password, storedHash)) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        
        // 4. JWT 토큰 생성
        String accessToken = jwtService.generateToken(user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        
        // 5. Refresh Token 저장
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        
        return new LoginResult(accessToken, refreshToken, user);
    }
    
    /**
     * 토큰 갱신
     */
    @Transactional
    public LoginResult refresh(String refreshToken) {
        // 1. 토큰 유효성 검사
        if (!jwtService.validateToken(refreshToken)) {
            throw new BadCredentialsException("유효하지 않은 Refresh Token입니다.");
        }
        
        // 2. Refresh Token으로 사용자 조회 (DB에서 직접 조회하여 보안 강화)
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("유효하지 않은 Refresh Token입니다."));
        
        // 3. 삭제된 사용자 체크
        if (user.getDeletedAt() != null) {
            throw new BadCredentialsException("사용자를 찾을 수 없습니다.");
        }
        
        // 4. 새로운 토큰 생성
        String newAccessToken = jwtService.generateToken(user.getId());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());
        
        // 5. 새로운 Refresh Token 저장
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);
        
        return new LoginResult(newAccessToken, newRefreshToken, user);
    }
    
    /**
     * 로그인 결과 클래스
     */
    public static class LoginResult {
        private final String accessToken;
        private final String refreshToken;
        private final User user;
        
        public LoginResult(String accessToken, String refreshToken, User user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
        }
        
        public String getAccessToken() {
            return accessToken;
        }
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public User getUser() {
            return user;
        }
    }
    
    /**
     * 회원가입
     */
    @Transactional
    public User register(String email, String password, String name) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(Objects.requireNonNull(email, "email must not be null"))) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        
        // 2. 비밀번호 해싱
        String passwordHash = passwordService.encode(Objects.requireNonNull(password, "password must not be null"));
        
        // 3. 사용자 생성 (Lombok 제거로 인해 builder() 직접 구현체 사용)
        User user = User.builder()
                .email(email)
                .name(name)
                .build();
        user.setPasswordHash(passwordHash);
        user.setProvider("LOCAL");
        user.setSubscription("free");
        
        // 4. 저장
        user = userRepository.save(user);
        
        return user;
    }
    
    /**
     * 사용자 조회 (ID로)
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(Objects.requireNonNull(userId, "userId must not be null"))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
    
    /**
     * 사용자 조회 (이메일로)
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(Objects.requireNonNull(email, "email must not be null"))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
