package com.virtualtryon.admin.controller;

import com.virtualtryon.admin.dto.member.AdminMemberDto;
import com.virtualtryon.admin.service.AdminMemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/** 관리자 회원 관리 API */
@RestController
@RequestMapping("/api/admin/members")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    public AdminMemberController(AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
    }

    /** 회원 목록 */
    @GetMapping
    public ResponseEntity<Page<AdminMemberDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminMemberService.findAll(pageable));
    }

    /** 회원 상세 */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        UUID uuid = parseUuid(id);
        if (uuid == null) return ResponseEntity.badRequest().body(Map.of("error", "잘못된 ID"));
        try {
            return ResponseEntity.ok(adminMemberService.findById(uuid));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** 회원 정지 */
    @PostMapping("/{id}/suspend")
    public ResponseEntity<?> suspend(@PathVariable String id) {
        UUID uuid = parseUuid(id);
        if (uuid == null) return ResponseEntity.badRequest().body(Map.of("error", "잘못된 ID"));
        try {
            return ResponseEntity.ok(adminMemberService.suspend(uuid));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 회원 정지 해제 */
    @PostMapping("/{id}/unsuspend")
    public ResponseEntity<?> unsuspend(@PathVariable String id) {
        UUID uuid = parseUuid(id);
        if (uuid == null) return ResponseEntity.badRequest().body(Map.of("error", "잘못된 ID"));
        try {
            return ResponseEntity.ok(adminMemberService.unsuspend(uuid));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 회원 탈퇴 처리 (소프트 삭제) */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        UUID uuid = parseUuid(id);
        if (uuid == null) return ResponseEntity.badRequest().body(Map.of("error", "잘못된 ID"));
        try {
            adminMemberService.delete(uuid);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private static UUID parseUuid(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return UUID.fromString(s.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
