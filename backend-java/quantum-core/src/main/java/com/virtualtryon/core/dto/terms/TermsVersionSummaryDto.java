package com.virtualtryon.core.dto.terms;

import java.time.LocalDateTime;
import java.util.UUID;

/** 약관 버전 요약 (이전 버전 선택용) */
public class TermsVersionSummaryDto {

    private UUID id;
    private String version;
    private String title;
    private LocalDateTime effectiveAt;

    public TermsVersionSummaryDto() {}

    public TermsVersionSummaryDto(UUID id, String version, String title, LocalDateTime effectiveAt) {
        this.id = id;
        this.version = version;
        this.title = title;
        this.effectiveAt = effectiveAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getEffectiveAt() { return effectiveAt; }
    public void setEffectiveAt(LocalDateTime effectiveAt) { this.effectiveAt = effectiveAt; }
}
