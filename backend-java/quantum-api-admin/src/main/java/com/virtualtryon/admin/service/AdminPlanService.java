package com.virtualtryon.admin.service;

import com.virtualtryon.admin.dto.plan.AdminPlanCreateRequest;
import com.virtualtryon.admin.dto.plan.AdminPlanUpdateRequest;
import com.virtualtryon.core.dto.plan.PlanResponse;
import com.virtualtryon.core.entity.PlanConfig;
import com.virtualtryon.core.repository.PlanConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/** 관리자 플랜 관리 서비스 */
@Service
public class AdminPlanService {

    private final PlanConfigRepository planConfigRepository;

    public AdminPlanService(PlanConfigRepository planConfigRepository) {
        this.planConfigRepository = planConfigRepository;
    }

    /** 전체 플랜 목록 (sort_order 순) */
    public List<PlanResponse> findAll() {
        return planConfigRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(
                        a.getSortOrder() != null ? a.getSortOrder() : 0,
                        b.getSortOrder() != null ? b.getSortOrder() : 0
                ))
                .map(PlanResponse::from)
                .collect(Collectors.toList());
    }

    /** 플랜 등록 */
    @Transactional
    public PlanResponse create(AdminPlanCreateRequest req) {
        if (req.getPlanCode() == null || req.getPlanCode().isBlank()) {
            throw new IllegalArgumentException("플랜 코드를 입력하세요.");
        }
        String code = req.getPlanCode().trim().toLowerCase();
        if (planConfigRepository.existsByPlanCode(code)) {
            throw new IllegalArgumentException("이미 존재하는 플랜 코드입니다: " + code);
        }
        if (req.getPlanName() == null || req.getPlanName().isBlank()) {
            throw new IllegalArgumentException("플랜 이름을 입력하세요.");
        }
        PlanConfig plan = new PlanConfig();
        plan.setPlanCode(code);
        plan.setPlanName(req.getPlanName().trim());
        plan.setPriceMonthly(req.getPriceMonthly() != null ? req.getPriceMonthly() : 0L);
        plan.setTokenLimit(req.getTokenLimit());
        plan.setDescription(req.getDescription());
        plan.setFeatures(req.getFeatures());
        plan.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        plan.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        planConfigRepository.save(plan);
        return PlanResponse.from(plan);
    }

    /** 플랜 상세 */
    public PlanResponse findById(UUID id) {
        UUID validId = Objects.requireNonNull(id, "id must not be null");
        Optional<PlanConfig> opt = planConfigRepository.findById(validId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("플랜을 찾을 수 없습니다: " + validId);
        }
        return PlanResponse.from(opt.get());
    }

    /** 플랜 수정 */
    @Transactional
    public PlanResponse update(UUID id, AdminPlanUpdateRequest req) {
        PlanConfig plan = planConfigRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new IllegalArgumentException("플랜을 찾을 수 없습니다: " + id));
        if (req.getPlanName() != null) plan.setPlanName(req.getPlanName());
        if (req.getPriceMonthly() != null) plan.setPriceMonthly(req.getPriceMonthly());
        if (req.getTokenLimit() != null) plan.setTokenLimit(req.getTokenLimit());
        if (req.getDescription() != null) plan.setDescription(req.getDescription());
        if (req.getFeatures() != null) plan.setFeatures(req.getFeatures());
        if (req.getIsActive() != null) plan.setIsActive(req.getIsActive());
        if (req.getSortOrder() != null) plan.setSortOrder(req.getSortOrder());
        plan.setUpdatedAt(LocalDateTime.now());
        planConfigRepository.save(plan);
        return PlanResponse.from(plan);
    }
}