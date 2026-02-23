package com.virtualtryon.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Admin 약관 등록/수정 요청 DTO.
 * 타입 세이프, @SuppressWarnings 불필요.
 */
public record AdminTermsSaveRequest(
    String type,
    String version,
    String title,
    String content,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime effectiveAt
) {
    public String typeOrNull() { return blankToNull(type); }
    public String versionOrNull() { return blankToNull(version); }
    public String titleOrNull() { return blankToNull(title); }
    public String contentOrDefault() { return content != null ? content : ""; }
    public String contentOrNull() { return content; }
    public LocalDateTime effectiveAtOrNull() { return effectiveAt; }

    private static String blankToNull(String s) {
        return s != null && !s.isBlank() ? s.trim() : null;
    }
}
