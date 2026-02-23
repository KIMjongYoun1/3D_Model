package com.virtualtryon.service.controller;

import com.virtualtryon.core.dto.TermsDetailDto;
import com.virtualtryon.core.dto.TermsSummaryDto;
import com.virtualtryon.service.TermsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/terms")
public class TermsController {

    private final TermsService termsService;

    public TermsController(TermsService termsService) {
        this.termsService = termsService;
    }

    /** 필수 약관 목록 (동의 화면용) */
    @GetMapping("")
    public ResponseEntity<List<TermsSummaryDto>> listRequired() {
        return ResponseEntity.ok(termsService.getRequiredTermsSummary());
    }

    /** 약관 상세 (전문 보기) */
    @GetMapping("/{id}")
    public ResponseEntity<TermsDetailDto> getDetail(@PathVariable UUID id) {
        return termsService.getTermsDetail(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
