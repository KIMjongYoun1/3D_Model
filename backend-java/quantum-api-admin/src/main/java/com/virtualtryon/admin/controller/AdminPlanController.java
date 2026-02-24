package com.virtualtryon.admin.controller;

import com.virtualtryon.admin.dto.plan.AdminPlanCreateRequest;
import com.virtualtryon.admin.dto.plan.AdminPlanUpdateRequest;
import com.virtualtryon.admin.service.AdminPlanService;
import com.virtualtryon.core.dto.plan.PlanResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** 관리자 플랜 관리 API */
@RestController
@RequestMapping("/api/admin/plans")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminPlanController {

    private final AdminPlanService adminPlanService;

    public AdminPlanController(AdminPlanService adminPlanService) {
        this.adminPlanService = adminPlanService;
    }

    /** 플랜 목록 */
    @GetMapping
    public ResponseEntity<List<PlanResponse>> list() {
        return ResponseEntity.ok(adminPlanService.findAll());
    }

    /** 플랜 등록 */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody AdminPlanCreateRequest req) {
        try {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                    .body(adminPlanService.create(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 플랜 상세 */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        Optional<UUID> uuidOpt = parseUuid(id);
        if (uuidOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "잘못된 ID"));
        try {
            return ResponseEntity.ok(adminPlanService.findById(uuidOpt.get()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** 플랜 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody AdminPlanUpdateRequest req) {
        Optional<UUID> uuidOpt = parseUuid(id);
        if (uuidOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "잘못된 ID"));
        try {
            return ResponseEntity.ok(adminPlanService.update(uuidOpt.get(), req));
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
