package com.virtualtryon.core.dto.terms;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** 약관 목록/요약 (동의 화면용) */
public class TermsSummaryDto {

    private UUID id;
    private String type;
    private String title;
    private String version;
    private List<TermsVersionSummaryDto> allVersions = new ArrayList<>();  // 이전 버전 포함 전체 목록

    public TermsSummaryDto() {}

    public TermsSummaryDto(UUID id, String type, String title, String version) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.version = version;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public List<TermsVersionSummaryDto> getAllVersions() { return allVersions != null ? allVersions : new ArrayList<>(); }
    public void setAllVersions(List<TermsVersionSummaryDto> allVersions) { this.allVersions = allVersions; }
}
