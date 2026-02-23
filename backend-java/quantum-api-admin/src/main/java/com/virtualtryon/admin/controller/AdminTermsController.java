package com.virtualtryon.admin.controller;

import com.virtualtryon.admin.dto.AdminTermsSaveRequest;
import com.virtualtryon.admin.service.AdminTermsService;
import com.virtualtryon.core.dto.TermsDetailDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Admin용 약관 CRUD API.
 * 관리자 페이지에서 수정 시 DB에 즉시 반영되며,
 * Studio의 /api/v1/terms가 같은 DB를 사용하므로 코드 재배포 없이 동의 화면에 바로 반영됨.
 */
@RestController
@RequestMapping("/api/admin/terms")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminTermsController {

    private final AdminTermsService adminTermsService;

    public AdminTermsController(AdminTermsService adminTermsService) {
        this.adminTermsService = adminTermsService;
    }

    /** 약관 목록 */
    @GetMapping
    public ResponseEntity<List<TermsDetailDto>> list() {
        return ResponseEntity.ok(adminTermsService.findAll());
    }

    /** 약관 상세 (편집용) */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        Optional<UUID> uuidOpt = parseUuid(id);
        if (uuidOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "잘못된 ID 형식입니다."));
        }
        return adminTermsService.findById(uuidOpt.get())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 약관 등록 */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody AdminTermsSaveRequest req) {
        try {
            TermsDetailDto created = adminTermsService.create(
                    req.typeOrNull(),
                    req.versionOrNull(),
                    req.titleOrNull(),
                    req.contentOrDefault(),
                    req.effectiveAtOrNull()
            );
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "요청 오류"));
        }
    }

    /** 약관 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody AdminTermsSaveRequest req) {
        Optional<UUID> uuidOpt = parseUuid(id);
        if (uuidOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "잘못된 ID 형식입니다."));
        }
        try {
            TermsDetailDto updated = adminTermsService.update(
                    uuidOpt.get(),
                    req.typeOrNull(),
                    req.versionOrNull(),
                    req.titleOrNull(),
                    req.contentOrNull(),
                    req.effectiveAtOrNull()
            );
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "요청 오류"));
        }
    }

    /** 약관 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Optional<UUID> uuidOpt = parseUuid(id);
        if (uuidOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "잘못된 ID 형식입니다."));
        }
        try {
            adminTermsService.delete(uuidOpt.get());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
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
