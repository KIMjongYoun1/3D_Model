package com.virtualtryon.core.dto;

import java.time.LocalDateTime;


/** 약관 상세 (전문 보기용). content/getContent는 null 대신 빈 문자열 반환. */
public class TermsDetailDto extends TermsSummaryDto {
    private String content = "";
    private LocalDateTime effectiveAt;

    public TermsDetailDto() {}

    public String getContent() { return content != null ? content : ""; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getEffectiveAt() { return effectiveAt; }
    public void setEffectiveAt(LocalDateTime effectiveAt) { this.effectiveAt = effectiveAt; }
}
