package com.virtualtryon.core.dto;

import java.util.UUID;

/**
 * 로그인 응답 DTO
 *
 * Lombok 미사용: VS Code/Cursor 환경에서 Lombok 플러그인 호환 이슈로 수동 구현.
 * Lombok 사용 시 {@code @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder}로
 * 아래 Getter/Setter/Builder 클래스를 모두 대체 가능.
 */
public class LoginResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UUID userId;
    private String email;
    private String name;
    private String provider;

    public LoginResponse() {}

    public LoginResponse(String accessToken, String refreshToken, String tokenType, UUID userId, String email, String name, String provider) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.provider = provider;
    }

    /** Lombok @Getter @Setter 대체 */
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    /** Lombok @Builder 대체: 수동 구현한 빌더 패턴 */
    public static class LoginResponseBuilder {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private UUID userId;
        private String email;
        private String name;
        private String provider;

        public LoginResponseBuilder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public LoginResponseBuilder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public LoginResponseBuilder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public LoginResponseBuilder userId(UUID userId) { this.userId = userId; return this; }
        public LoginResponseBuilder email(String email) { this.email = email; return this; }
        public LoginResponseBuilder name(String name) { this.name = name; return this; }
        public LoginResponseBuilder provider(String provider) { this.provider = provider; return this; }

        public LoginResponse build() {
            return new LoginResponse(accessToken, refreshToken, tokenType, userId, email, name, provider);
        }
    }

    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }
}
