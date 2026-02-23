package com.virtualtryon.core.dto;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 네이버 콜백 응답 (로그인 성공 또는 약관 동의 필요)
 * - needsAgreement=false: accessToken, refreshToken 등 포함 (정상 로그인)
 * - needsAgreement=true: agreementToken, terms 포함 (동의 페이지로 이동)
 */
public class NaverCallbackResponse {
    private Boolean needsAgreement;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UUID userId;
    private String email;
    private String name;
    private String provider;
    private String agreementToken;
    private List<TermsSummaryDto> terms;

    public static NaverCallbackResponse loginSuccess(LoginResponse login) {
        Objects.requireNonNull(login, "login must not be null");
        NaverCallbackResponse r = new NaverCallbackResponse();
        r.setNeedsAgreement(false);
        r.setUserId(login.getUserId());
        r.setEmail(login.getEmail() != null ? login.getEmail() : "");
        r.setName(login.getName() != null ? login.getName() : "");
        r.setProvider(login.getProvider() != null ? login.getProvider() : "NAVER");
        return r;
    }

    public static NaverCallbackResponse needsAgreement(String agreementToken, List<TermsSummaryDto> terms, String email, String name) {
        Objects.requireNonNull(agreementToken, "agreementToken must not be null");
        NaverCallbackResponse r = new NaverCallbackResponse();
        r.setNeedsAgreement(true);
        r.setAgreementToken(agreementToken);
        r.setTerms(terms);
        r.setEmail(email != null ? email : "");
        r.setName(name != null ? name : "");
        return r;
    }

    public Boolean getNeedsAgreement() { return needsAgreement; }
    public void setNeedsAgreement(Boolean needsAgreement) { this.needsAgreement = needsAgreement; }
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
    public String getAgreementToken() { return agreementToken; }
    public void setAgreementToken(String agreementToken) { this.agreementToken = agreementToken; }
    public List<TermsSummaryDto> getTerms() { return terms; }
    public void setTerms(List<TermsSummaryDto> terms) { this.terms = terms; }
}
