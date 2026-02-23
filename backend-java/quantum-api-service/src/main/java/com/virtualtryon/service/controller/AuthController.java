package com.virtualtryon.service.controller;

import com.virtualtryon.core.config.AuthCookieHelper;
import com.virtualtryon.core.dto.*;
import com.virtualtryon.core.entity.User;
import com.virtualtryon.service.AuthService;
import com.virtualtryon.service.NaverAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 인증 컨트롤러
 * 
 * JWT는 HttpOnly 세션 쿠키로 전달. 창/탭 닫으면 소멸.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final NaverAuthService naverAuthService;
    private final AuthCookieHelper cookieHelper;

    public AuthController(AuthService authService, NaverAuthService naverAuthService, AuthCookieHelper cookieHelper) {
        this.authService = authService;
        this.naverAuthService = naverAuthService;
        this.cookieHelper = cookieHelper;
    }
    
    /**
     * 네이버 소셜 로그인 콜백.
     * - 약관 동의 완료 시: accessToken, refreshToken 반환
     * - 약관 미동의 시: needsAgreement=true, agreementToken, terms 반환
     */
    @GetMapping("/naver/callback")
    public ResponseEntity<NaverCallbackResponse> naverLogin(@RequestParam String code, @RequestParam String state) {
        try {
            NaverAuthService.NaverAuthResult result = naverAuthService.loginWithNaver(code, state);

            if (result.isSuccess()) {
                User user = Objects.requireNonNull(result.getUser(), "user must not be null when success");
                String at = Objects.requireNonNull(result.getAccessToken(), "accessToken must not be null");
                String rt = Objects.requireNonNull(result.getRefreshToken(), "refreshToken must not be null");
                ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
                cookieHelper.addAuthCookies(builder, at, rt);
                LoginResponse login = LoginResponse.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .provider(user.getProvider() != null ? user.getProvider() : "NAVER")
                        .build();
                return builder.body(NaverCallbackResponse.loginSuccess(login));
            }

            String at = result.getAgreementToken();
            if (at == null || at.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok(NaverCallbackResponse.needsAgreement(
                    at,
                    result.getTerms() != null ? result.getTerms() : List.of(),
                    result.getEmail(),
                    result.getName()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * 약관 동의 완료 후 로그인 확정.
     * agreementToken(5분 유효)으로 사용자 식별, agreedTermIds로 동의 저장 후 JWT 발급.
     */
    @PostMapping("/agree")
    public ResponseEntity<LoginResponse> completeTermsAgreement(
            @Valid @RequestBody TermsAgreeRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ip = httpRequest != null ? httpRequest.getRemoteAddr() : null;
            String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : null;

            AuthService.LoginResult result = naverAuthService.completeTermsAgreement(
                    request.getAgreementToken(),
                    request.getAgreedTermIds(),
                    ip,
                    userAgent
            );

            User user = Objects.requireNonNull(result.getUser(), "user must not be null");
            String at = Objects.requireNonNull(result.getAccessToken(), "accessToken must not be null");
            String rt = Objects.requireNonNull(result.getRefreshToken(), "refreshToken must not be null");
            ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
            cookieHelper.addAuthCookies(builder, at, rt);
            LoginResponse response = LoginResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .provider(user.getProvider() != null ? user.getProvider() : "NAVER")
                    .build();
            return builder.body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthService.LoginResult result = authService.login(request.getEmail(), request.getPassword());
            User user = Objects.requireNonNull(result.getUser(), "user must not be null");
            ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
            cookieHelper.addAuthCookies(builder, result.getAccessToken(), result.getRefreshToken());
            LoginResponse response = LoginResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .provider(user.getProvider() != null ? user.getProvider() : "NAVER")
                    .build();
            return builder.body(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /** 쿠키의 refresh_token으로 갱신. credentials 포함 요청 필요. */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request) {
        String refreshToken = AuthCookieHelper.getCookieValue(request, AuthCookieHelper.REFRESH_TOKEN);
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            AuthService.LoginResult result = authService.refresh(refreshToken);
            User user = Objects.requireNonNull(result.getUser(), "user must not be null");
            ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
            cookieHelper.addAuthCookies(builder, result.getAccessToken(), result.getRefreshToken());
            LoginResponse response = LoginResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .provider(user.getProvider() != null ? user.getProvider() : "NAVER")
                    .build();
            return builder.body(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /** 로그아웃: HttpOnly 쿠키 삭제 */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        cookieHelper.clearAuthCookies(builder);
        return builder.build();
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
                    .provider(user.getProvider())
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal Object principal) {
        if (principal == null || !(principal instanceof UUID)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            UUID userId = (UUID) principal;
            User user = authService.getUserById(userId);
            return ResponseEntity.ok(UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .subscription(user.getSubscription() != null ? user.getSubscription() : "free")
                    .createdAt(user.getCreatedAt())
                    .provider(user.getProvider() != null ? user.getProvider() : "LOCAL")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
