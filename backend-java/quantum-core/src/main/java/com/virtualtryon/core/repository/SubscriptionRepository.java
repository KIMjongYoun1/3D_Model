package com.virtualtryon.core.repository;

import com.virtualtryon.core.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Subscription 리포지토리
 *
 * 사용자별 구독 조회, 활성 구독 조회 등.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    /** 전체 구독 목록 (최신순, 페이징) */
    Page<Subscription> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** 사용자별 구독 목록 (최신순) */
    List<Subscription> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /** 사용자별 구독 목록 (최신순, 페이징) */
    Page<Subscription> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /** 사용자의 활성 구독 (가장 최근 1건) */
    Optional<Subscription> findFirstByUserIdAndStatusOrderByExpiresAtDesc(UUID userId, String status);

    /** 사용자별 활성 구독 존재 여부 */
    boolean existsByUserIdAndStatus(UUID userId, String status);
}
