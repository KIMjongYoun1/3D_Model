package com.virtualtryon.service.service;

import com.virtualtryon.core.entity.User;
import com.virtualtryon.core.repository.UserRepository;
import com.virtualtryon.core.service.JwtService;
import com.virtualtryon.core.service.PasswordService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/** 인증 서비스 - 로그인, 회원가입, JWT 토큰 발급 */
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

    /** 이메일/비밀번호 로그인. JWT 발급 */
    @Transactional
    public LoginResult login(String email, String password) {
        User user = userRepository.findByEmail(Objects.requireNonNull(email, "email must not be null"))
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (user.getDeletedAt() != null) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        String storedHash = user.getPasswordHash();
        if (storedHash == null || !passwordService.matches(password, storedHash)) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        String accessToken = jwtService.generateToken(user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        return new LoginResult(accessToken, refreshToken, user);
    }

    /** Refresh Token으로 Access Token 갱신 */
    @Transactional
    public LoginResult refresh(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new BadCredentialsException("유효하지 않은 Refresh Token입니다.");
        }
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("유효하지 않은 Refresh Token입니다."));
        if (user.getDeletedAt() != null) {
            throw new BadCredentialsException("사용자를 찾을 수 없습니다.");
        }
        String newAccessToken = jwtService.generateToken(user.getId());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);
        return new LoginResult(newAccessToken, newRefreshToken, user);
    }

    public static class LoginResult {
        private final String accessToken;
        private final String refreshToken;
        private final User user;

        public LoginResult(String accessToken, String refreshToken, User user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
        }
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public User getUser() { return user; }
    }

    /** 회원가입. 이메일 중복 시 예외 */
    @Transactional
    public User register(String email, String password, String name) {
        if (userRepository.existsByEmail(Objects.requireNonNull(email, "email must not be null"))) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        String passwordHash = passwordService.encode(Objects.requireNonNull(password, "password must not be null"));
        User user = User.builder().email(email).name(name).build();
        user.setPasswordHash(passwordHash);
        user.setProvider("LOCAL");
        user.setSubscription("free");
        return userRepository.save(user);
    }

    /** 사용자 ID로 조회 */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(Objects.requireNonNull(userId, "userId must not be null"))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /** 사용자 이메일로 조회 */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(Objects.requireNonNull(email, "email must not be null"))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
