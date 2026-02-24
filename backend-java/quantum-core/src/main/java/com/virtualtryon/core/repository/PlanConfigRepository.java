package com.virtualtryon.core.repository;

import com.virtualtryon.core.entity.PlanConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 요금제/플랜 설정 리포지토리
 */
@Repository
public interface PlanConfigRepository extends JpaRepository<PlanConfig, UUID> {

    Optional<PlanConfig> findByPlanCode(String planCode);

    List<PlanConfig> findByIsActiveTrueOrderBySortOrderAsc();

    boolean existsByPlanCode(String planCode);
}
