package com.virtualtryon.core.dto.terms;

import java.time.LocalDateTime;


/** 약관 상세 (전문 보기용) */
public class TermsDetailDto extends TermsSummaryDto {

    private String content = "";
    private LocalDateTime effectiveAt;
    private String category = "SIGNUP";  // SIGNUP, PAYMENT
    private Boolean required = true;    // 필수/선택
    private Boolean isActive = true;    // 노출 여부

    public TermsDetailDto() {}

    public String getContent() { return content != null ? content : ""; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getEffectiveAt() { return effectiveAt; }
    public void setEffectiveAt(LocalDateTime effectiveAt) { this.effectiveAt = effectiveAt; }
    public String getCategory() { return category != null ? category : "SIGNUP"; }
    public void setCategory(String category) { this.category = category; }
    public Boolean getRequired() { return required != null ? required : true; }
    public void setRequired(Boolean required) { this.required = required; }
    public Boolean getIsActive() { return isActive != null ? isActive : true; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
