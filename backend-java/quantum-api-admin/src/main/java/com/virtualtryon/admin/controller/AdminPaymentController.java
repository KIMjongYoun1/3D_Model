package com.virtualtryon.admin.controller;

import com.virtualtryon.admin.dto.payment.AdminPaymentDto;
import com.virtualtryon.admin.service.AdminPaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** 관리자 거래(결제) 관리 API */
@RestController
@RequestMapping("/api/admin/payments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    public AdminPaymentController(AdminPaymentService adminPaymentService) {
        this.adminPaymentService = adminPaymentService;
    }

    /** 결제 목록 (fromDate, toDate: YYYY-MM-DD, 결제 완료일 기준) */
    @GetMapping
    public ResponseEntity<Page<AdminPaymentDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime from = parseDate(fromDate, LocalTime.MIN);
        LocalDateTime to = parseDate(toDate, LocalTime.MAX);

        Page<AdminPaymentDto> result;
        if (userId != null && !userId.isBlank()) {
            Optional<UUID> uuidOpt = parseUuid(userId);
            if (uuidOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            result = adminPaymentService.findByUserId(uuidOpt.get(), pageable, from, to);
        } else {
            result = adminPaymentService.findAll(pageable, from, to);
        }
        return ResponseEntity.ok(result);
    }

    private static LocalDateTime parseDate(String s, LocalTime time) {
        if (s == null || s.isBlank()) return null;
        try {
            LocalDate d = LocalDate.parse(s.trim());
            return LocalDateTime.of(d, time);
        } catch (Exception e) {
            return null;
        }
    }

    /** 결제 상세 */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        Optional<UUID> uuidOpt = parseUuid(id);
        if (uuidOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "잘못된 ID"));
        try {
            return ResponseEntity.ok(adminPaymentService.findById(uuidOpt.get()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** 결제 취소 */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String id) {
        Optional<UUID> uuidOpt = parseUuid(id);
        if (uuidOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "잘못된 ID"));
        try {
            return ResponseEntity.ok(adminPaymentService.cancel(uuidOpt.get()));
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
