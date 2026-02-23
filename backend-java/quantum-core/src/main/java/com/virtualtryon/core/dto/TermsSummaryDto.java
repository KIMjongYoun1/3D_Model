package com.virtualtryon.core.dto;

import java.util.UUID;

/** 약관 목록/요약 (동의 화면용). Lombok 미사용. */
public class TermsSummaryDto {
    private UUID id;
    private String type;
    private String title;
    private String version;

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
}
