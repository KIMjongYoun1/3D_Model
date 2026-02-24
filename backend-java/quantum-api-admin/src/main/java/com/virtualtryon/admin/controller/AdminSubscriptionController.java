package com.virtualtryon.admin.controller;

import com.virtualtryon.admin.dto.subscription.AdminSubscriptionDto;
import com.virtualtryon.admin.service.AdminSubscriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** 관리자 구독 관리 API */
@RestController
@RequestMapping("/api/admin/subscriptions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminSubscriptionController {

    private final AdminSubscriptionService adminSubscriptionService;

    public AdminSubscriptionController(AdminSubscriptionService adminSubscriptionService) {
        this.adminSubscriptionService = adminSubscriptionService;
    }

    /** 구독 목록 */
    @GetMapping
    public ResponseEntity<Page<AdminSubscriptionDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String userId
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminSubscriptionDto> result;
        if (userId != null && !userId.isBlank()) {
            Optional<UUID> uuidOpt = parseUuid(userId);
            if (uuidOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            result = adminSubscriptionService.findByUserId(uuidOpt.get(), pageable);
        } else {
            result = adminSubscriptionService.findAll(pageable);
        }
        return ResponseEntity.ok(result);
    }

    /** 구독 상세 */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        Optional<UUID> uuidOpt = parseUuid(id);
        if (uuidOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "잘못된 ID"));
        try {
            return ResponseEntity.ok(adminSubscriptionService.findById(uuidOpt.get()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** 구독 취소 */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String id) {
        Optional<UUID> uuidOpt = parseUuid(id);
        if (uuidOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "잘못된 ID"));
        try {
            return ResponseEntity.ok(adminSubscriptionService.cancel(uuidOpt.get()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private static Optional<UUID> parseUuid(String s) {
        if (s == null || s.isBlank()) return Optional.empty();
        try {
            return Optional.of(UUID.fromString(s.trim()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
