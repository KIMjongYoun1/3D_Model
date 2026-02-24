package com.virtualtryon.admin.dto.terms;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/** Admin 약관 등록/수정 요청 DTO */
public record AdminTermsSaveRequest(
    String type,
    String version,
    String title,
    String content,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime effectiveAt,
    String category,   // SIGNUP, PAYMENT
    Boolean required,  // 필수(true) / 선택(false)
    Boolean isActive   // 노출(true) / 미노출(false)
) {
    public String typeOrNull() { return blankToNull(type); }
    public String versionOrNull() { return blankToNull(version); }
    public String titleOrNull() { return blankToNull(title); }
    public String contentOrDefault() { return content != null ? content : ""; }
    public String contentOrNull() { return content; }
    public LocalDateTime effectiveAtOrNull() { return effectiveAt; }
    public String categoryOrDefault() { return category != null && !category.isBlank() ? category.trim() : "SIGNUP"; }
    public Boolean requiredOrDefault() { return required != null ? required : true; }
    public Boolean isActiveOrDefault() { return isActive != null ? isActive : true; }

    private static String blankToNull(String s) {
        return s != null && !s.isBlank() ? s.trim() : null;
    }
}
