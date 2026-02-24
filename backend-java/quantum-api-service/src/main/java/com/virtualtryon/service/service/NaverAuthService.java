package com.virtualtryon.service.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.virtualtryon.core.dto.terms.TermsSummaryDto;
import com.virtualtryon.core.entity.User;
import com.virtualtryon.core.repository.UserRepository;
import com.virtualtryon.core.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** 네이버 소셜 로그인 서비스 */
@Service
public class NaverAuthService {

    private static final Logger log = LoggerFactory.getLogger(NaverAuthService.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TermsService termsService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    private static final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String NAVER_PROFILE_URL = "https://openapi.naver.com/v1/nid/me";

    public NaverAuthService(UserRepository userRepository, JwtService jwtService, TermsService termsService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.termsService = termsService;
    }

    /** 설정 로드 후 초기화 로그 출력 */
    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("NaverAuthService initialized with Client ID: {}", (clientId != null && !clientId.isEmpty()) ? "LOADED" : "MISSING");
        log.info("NaverAuthService initialized with Redirect URI: {}", redirectUri);
    }

    /** 네이버 로그인. 약관 동의 완료 시 JWT, 미동의 시 agreementToken+terms 반환 */
    @Transactional
    public NaverAuthResult loginWithNaver(String code, String state) {
        String naverAccessToken = getAccessToken(code, state);
        NaverProfile profile = getProfile(naverAccessToken);
        User user = processUser(profile);
        if (user.getSuspendedAt() != null) {
            throw new RuntimeException("정지된 계정입니다. 고객센터에 문의해 주세요.");
        }
        if (termsService.hasUserAgreedToAllRequired(user)) {
            String accessToken = jwtService.generateToken(user.getId());
            String refreshToken = jwtService.generateRefreshToken(user.getId());
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
            return NaverAuthResult.success(accessToken, refreshToken, user);
        }
        String agreementToken = jwtService.generateAgreementToken(user.getId());
        List<TermsSummaryDto> terms = termsService.getRequiredTermsSummary();
        return NaverAuthResult.needsAgreement(agreementToken, terms, user.getEmail(), user.getName());
    }

    /** 약관 동의 완료 후 JWT 발급 */
    @Transactional
    public AuthService.LoginResult completeTermsAgreement(String agreementToken, List<java.util.UUID> agreedTermIds, String ipAddress, String userAgent) {
        if (!jwtService.validateToken(agreementToken) || !"terms_agreement".equals(jwtService.extractTokenType(agreementToken))) {
            throw new RuntimeException("유효하지 않거나 만료된 동의 토큰입니다.");
        }
        java.util.UUID userId = Objects.requireNonNull(jwtService.extractUserId(agreementToken), "userId must not be null from token");
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        if (user.getSuspendedAt() != null) {
            throw new RuntimeException("정지된 계정입니다. 고객센터에 문의해 주세요.");
        }
        termsService.saveAgreements(user, agreedTermIds, ipAddress, userAgent);
        String accessToken = jwtService.generateToken(user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        return new AuthService.LoginResult(accessToken, refreshToken, user);
    }

    public static class NaverAuthResult {
        private final boolean success;
        private final String accessToken;
        private final String refreshToken;
        private final User user;
        private final String agreementToken;
        private final List<TermsSummaryDto> terms;
        private final String email;
        private final String name;

        private NaverAuthResult(boolean success, String accessToken, String refreshToken, User user,
                                String agreementToken, List<TermsSummaryDto> terms, String email, String name) {
            this.success = success;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
            this.agreementToken = agreementToken;
            this.terms = terms;
            this.email = email;
            this.name = name;
        }

        public static NaverAuthResult success(String accessToken, String refreshToken, User user) {
            Objects.requireNonNull(user, "user must not be null");
            return new NaverAuthResult(true, accessToken, refreshToken, user, null, null,
                    user.getEmail() != null ? user.getEmail() : "",
                    user.getName() != null ? user.getName() : "");
        }

        public static NaverAuthResult needsAgreement(String agreementToken, List<TermsSummaryDto> terms, String email, String name) {
            return new NaverAuthResult(false, null, null, null, agreementToken, terms, email, name);
        }

        public boolean isSuccess() { return success; }
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public User getUser() { return user; }
        public String getAgreementToken() { return agreementToken; }
        public List<TermsSummaryDto> getTerms() { return terms; }
        public String getEmail() { return email; }
        public String getName() { return name; }
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
        NaverTokenResponse body = response.getBody();
        if (response.getStatusCode() == HttpStatus.OK && body != null && body.getAccessToken() != null) {
            return body.getAccessToken();
        } else {
            String errorMsg = (body != null) ? body.getError() + ": " + body.getErrorDescription() : "Empty body";
            log.error("네이버 Access Token 획득 실패. Status: {}, Error: {}", response.getStatusCode(), errorMsg);
            throw new RuntimeException("네이버 Access Token 획득 실패: " + errorMsg);
        }
    }

    private NaverProfile getProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(Objects.requireNonNull(accessToken, "accessToken must not be null"));
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<NaverProfileResponse> response = restTemplate.exchange(
            Objects.requireNonNull(NAVER_PROFILE_URL, "URL must not be null"),
            Objects.requireNonNull(HttpMethod.GET, "Method must not be null"),
            request,
            NaverProfileResponse.class);
        NaverProfileResponse body = response.getBody();
        if (response.getStatusCode() == HttpStatus.OK && body != null && body.getResponse() != null) {
            return body.getResponse();
        } else {
            log.error("네이버 프로필 정보 획득 실패: {}", response.getStatusCode());
            throw new RuntimeException("네이버 프로필 정보 획득 실패");
        }
    }

    private User processUser(NaverProfile profile) {
        Objects.requireNonNull(profile, "profile must not be null");
        String profileId = Objects.requireNonNull(profile.getId(), "profile.id must not be null");
        String profileEmail = Objects.requireNonNull(profile.getEmail(), "profile.email must not be null");
        Optional<User> existingByProvider = userRepository.findByProviderAndProviderId("NAVER", profileId);
        if (existingByProvider.isPresent()) {
            User user = existingByProvider.orElseThrow();
            user.setName(profile.getName());
            user.setProfileImage(profile.getProfileImage());
            user.setMobile(profile.getMobile());
            user.setProvider("NAVER");
            return userRepository.save(user);
        }
        Optional<User> existingByEmail = userRepository.findByEmail(profileEmail);
        if (existingByEmail.isPresent()) {
            User user = existingByEmail.orElseThrow();
            user.setProvider("NAVER");
            user.setProviderId(profileId);
            user.setProfileImage(profile.getProfileImage());
            user.setMobile(profile.getMobile());
            return userRepository.save(user);
        }
        User newUser = new User(
            profileEmail,
            profile.getName() != null ? profile.getName() : "",
            profile.getProfileImage(),
            "NAVER",
            profileId,
            profile.getMobile()
        );
        return userRepository.save(newUser);
    }

    public static class NaverTokenResponse {
        @JsonProperty("access_token") private String accessToken;
        @JsonProperty("refresh_token") private String refreshToken;
        @JsonProperty("token_type") private String tokenType;
        @JsonProperty("expires_in") private String expiresIn;
        @JsonProperty("error") private String error;
        @JsonProperty("error_description") private String errorDescription;
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public String getExpiresIn() { return expiresIn; }
        public void setExpiresIn(String expiresIn) { this.expiresIn = expiresIn; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getErrorDescription() { return errorDescription; }
        public void setErrorDescription(String errorDescription) { this.errorDescription = errorDescription; }
    }

    public static class NaverProfileResponse {
        private String resultCode;
        private String message;
        private NaverProfile response;
        public String getResultCode() { return resultCode; }
        public void setResultCode(String resultCode) { this.resultCode = resultCode; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public NaverProfile getResponse() { return response; }
        public void setResponse(NaverProfile response) { this.response = response; }
    }

    public static class NaverProfile {
        private String id;
        private String email;
        private String name;
        @JsonProperty("mobile") private String mobile;
        @JsonProperty("profile_image") private String profileImage;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    }
}
