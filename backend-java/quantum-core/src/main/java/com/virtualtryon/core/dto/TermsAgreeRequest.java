package com.virtualtryon.core.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

/** 약관 동의 완료 요청 */
public class TermsAgreeRequest {
    @NotEmpty(message = "agreementToken이 필요합니다.")
    private String agreementToken;

    @NotEmpty(message = "동의할 약관 목록이 필요합니다.")
    private List<UUID> agreedTermIds;

    public String getAgreementToken() { return agreementToken; }
    public void setAgreementToken(String agreementToken) { this.agreementToken = agreementToken; }
    public List<UUID> getAgreedTermIds() { return agreedTermIds; }
    public void setAgreedTermIds(List<UUID> agreedTermIds) { this.agreedTermIds = agreedTermIds; }
}
