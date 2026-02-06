package com.virtualtryon.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.virtualtryon.entity.User;
import com.virtualtryon.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * 네이버 소셜 로그인 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NaverAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    private static final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String NAVER_PROFILE_URL = "https://openapi.naver.com/v1/nid/me";

    /**
     * 네이버 로그인 처리
     * 
     * @param code 네이버에서 전달받은 인증 코드
     * @param state 상태 값 (CSRF 방지)
     * @return 로그인 결과 (JWT 토큰 및 사용자 정보)
     */
    @Transactional
    public AuthService.LoginResult loginWithNaver(String code, String state) {
        // 1. Access Token 요청
        String accessToken = getAccessToken(code, state);
        
        // 2. 사용자 프로필 정보 요청
        NaverProfile profile = getProfile(accessToken);
        
        // 3. 사용자 연동 또는 생성
        User user = processUser(profile);
        
        // 4. JWT 토큰 생성
        String token = jwtService.generateToken(user.getId());
        
        return new AuthService.LoginResult(token, user);
    }

    private String getAccessToken(String code, String state) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("state", state);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<NaverTokenResponse> response = restTemplate.postForEntity(NAVER_TOKEN_URL, request, NaverTokenResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().getAccessToken();
        } else {
            throw new RuntimeException("네이버 Access Token 획득 실패");
        }
    }

    private NaverProfile getProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<NaverProfileResponse> response = restTemplate.exchange(NAVER_PROFILE_URL, HttpMethod.GET, request, NaverProfileResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().getResponse();
        } else {
            throw new RuntimeException("네이버 프로필 정보 획득 실패");
        }
    }

    private User processUser(NaverProfile profile) {
        // 1. provider_id로 기존 사용자 조회
        return userRepository.findByProviderAndProviderId("NAVER", profile.getId())
                .map(user -> {
                    // 기존 사용자 정보 업데이트 (선택 사항)
                    user.setName(profile.getName());
                    user.setProfileImage(profile.getProfileImage());
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    // 2. 신규 사용자 생성
                    // 이메일로 기존 LOCAL 사용자가 있는지 확인 (선택 사항: 연동 정책에 따라 다름)
                    return userRepository.findByEmail(profile.getEmail())
                            .map(existingUser -> {
                                // 기존 이메일 사용자가 있으면 소셜 정보 연동
                                existingUser.setProvider("NAVER");
                                existingUser.setProviderId(profile.getId());
                                existingUser.setProfileImage(profile.getProfileImage());
                                return userRepository.save(existingUser);
                            })
                            .orElseGet(() -> {
                                // 아예 새로운 사용자 생성
                                User newUser = User.builder()
                                        .email(profile.getEmail())
                                        .name(profile.getName())
                                        .profileImage(profile.getProfileImage())
                                        .provider("NAVER")
                                        .providerId(profile.getId())
                                        .subscription("free")
                                        .build();
                                return userRepository.save(newUser);
                            });
                });
    }

    @Data
    public static class NaverTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("refresh_token")
        private String refreshToken;
        @JsonProperty("token_type")
        private String tokenType;
        @JsonProperty("expires_in")
        private String expiresIn;
    }

    @Data
    public static class NaverProfileResponse {
        private String resultCode;
        private String message;
        private NaverProfile response;
    }

    @Data
    public static class NaverProfile {
        private String id;
        private String email;
        private String name;
        @JsonProperty("profile_image")
        private String profileImage;
    }
}
