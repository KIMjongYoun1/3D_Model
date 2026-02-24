package com.virtualtryon.admin.controller;

import com.virtualtryon.admin.dto.dashboard.AdminDashboardDto;
import com.virtualtryon.admin.service.AdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 관리자 매출 대시보드 API */
@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    /** 매출 통계 (period: week, month, quarter, half) */
    @GetMapping("/revenue")
    public ResponseEntity<AdminDashboardDto> getRevenueStats(
            @RequestParam(required = false, defaultValue = "month") String period
    ) {
        return ResponseEntity.ok(adminDashboardService.getRevenueStats(period));
    }
}
