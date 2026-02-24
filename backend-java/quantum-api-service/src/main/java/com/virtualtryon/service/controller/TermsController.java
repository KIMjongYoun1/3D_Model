package com.virtualtryon.service.controller;

import com.virtualtryon.core.dto.terms.TermsDetailDto;

import java.util.List;
import com.virtualtryon.service.service.TermsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/terms")
public class TermsController {

    private final TermsService termsService;

    public TermsController(TermsService termsService) {
        this.termsService = termsService;
    }

    /** 필수 약관 목록 (동의 화면용). content, allVersions 포함 */
    @GetMapping("")
    public ResponseEntity<List<TermsDetailDto>> listRequired() {
        return ResponseEntity.ok(termsService.getRequiredTermsWithContent());
    }

    /** 결제 시 표시할 약관 (구독/환불 등) */
    @GetMapping("/payment")
    public ResponseEntity<List<TermsDetailDto>> listPaymentTerms() {
        return ResponseEntity.ok(termsService.getPaymentTerms());
    }

    /** 약관 상세 (전문 보기) */
    @GetMapping("/{id}")
    public ResponseEntity<TermsDetailDto> getDetail(@PathVariable UUID id) {
        return termsService.getTermsDetail(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
