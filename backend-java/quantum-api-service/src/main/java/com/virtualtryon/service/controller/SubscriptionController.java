package com.virtualtryon.service.controller;

import com.virtualtryon.service.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * 사용자 구독 API
 * - POST /me/cancel: 본인 활성 구독 해지 신청
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /** 본인 활성 구독 해지 (인증 필요) */
    @PostMapping("/me/cancel")
    public ResponseEntity<Map<String, String>> cancelMySubscription(@AuthenticationPrincipal Object principal) {
        UUID userId = extractUserId(principal);
        subscriptionService.cancelMySubscription(userId);
        return ResponseEntity.ok(Map.of("message", "구독 해지가 완료되었습니다. 당월 말일까지 이용 가능합니다."));
    }

    private UUID extractUserId(Object principal) {
        if (principal == null || !(principal instanceof UUID)) {
            throw new IllegalStateException("인증이 필요합니다.");
        }
        return (UUID) principal;
    }
}
