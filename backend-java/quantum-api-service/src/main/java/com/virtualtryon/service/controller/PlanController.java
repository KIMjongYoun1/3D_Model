package com.virtualtryon.service.controller;

import com.virtualtryon.core.dto.plan.PlanResponse;
import com.virtualtryon.core.entity.PlanConfig;
import com.virtualtryon.core.repository.PlanConfigRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 플랜/요금제 API (공개)
 *
 * - GET /api/v1/plans: 활성 플랜 목록 (plan_config 기반)
 */
@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {

    private final PlanConfigRepository planConfigRepository;

    public PlanController(PlanConfigRepository planConfigRepository) {
        this.planConfigRepository = planConfigRepository;
    }

    /** 활성 플랜 목록 조회 (plan_config 기반, 공개) */
    @GetMapping
    public ResponseEntity<List<PlanResponse>> getActivePlans() {
        List<PlanConfig> configs = planConfigRepository.findByIsActiveTrueOrderBySortOrderAsc();
        List<PlanResponse> responses = configs.stream()
                .map(PlanResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
