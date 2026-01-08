package com.virtualtryon.service;

import com.virtualtryon.entity.User;
import com.virtualtryon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    
    /**
     * 로그인
     * 
     * ⭐ 위변조/탈취 방지:
     * - 비밀번호 검증 (BCrypt)
     * - Rate Limiting은 SecurityConfig에서 처리
     * 
     * @param email 이메일
     * @param password 비밀번호
     * @return 로그인 결과 (토큰과 사용자 정보)
     * @throws BadCredentialsException 인증 실패 시
     */
    @Transactional(readOnly = true)
    public LoginResult login(String email, String password) {
        // 1. 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));
        
        // 2. 삭제된 사용자 체크
        if (user.getDeletedAt() != null) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        
        // 3. 비밀번호 검증
        if (!passwordService.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        
        // 4. JWT 토큰 생성
        String token = jwtService.generateToken(user.getId());
        
        return new LoginResult(token, user);
    }
    
    /**
     * 로그인 결과 클래스
     */
    public static class LoginResult {
        private final String token;
        private final User user;
        
        public LoginResult(String token, User user) {
            this.token = token;
            this.user = user;
        }
        
        public String getToken() {
            return token;
        }
        
        public User getUser() {
            return user;
        }
    }
    
    /**
     * 회원가입
     * 
     * ⭐ 위변조 방지:
     * - 이메일 중복 체크
     * - 비밀번호 해싱 (BCrypt)
     * - 입력 검증 (DTO에서)
     * 
     * @param email 이메일
     * @param password 비밀번호
     * @param name 사용자 이름
     * @return 생성된 사용자
     * @throws IllegalArgumentException 이메일 중복 시
     */
    @Transactional
    public User register(String email, String password, String name) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        
        // 2. 비밀번호 해싱
        String passwordHash = passwordService.encode(password);
        
        // 3. 사용자 생성
        User user = User.builder()
                .email(email)
                .passwordHash(passwordHash)
                .name(name)
                .subscription("free")
                .build();
        
        // 4. 저장
        user = userRepository.save(user);
        
        return user;
    }
    
    /**
     * 사용자 조회 (ID로)
     * 
     * @param userId 사용자 ID
     * @return 사용자
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
    
    /**
     * 사용자 조회 (이메일로)
     * 
     * @param email 이메일
     * @return 사용자
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}

