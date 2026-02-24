package com.virtualtryon.core.repository;

import com.virtualtryon.core.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Payment 리포지토리
 * 
 * 역할:
 * - 결제 데이터 접근
 * - 사용자별 결제 이력 조회
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    /**
     * 전체 결제 목록 (최신순, 페이징)
     */
    Page<Payment> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 사용자별 결제 이력 조회 (최신순)
     * 
     * @param userId 사용자 ID
     * @return 결제 이력 목록
     */
    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * 사용자별 결제 이력 조회 (최신순, 페이징)
     */
    Page<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    /**
     * 구독별 결제 조회
     * 
     * @param subscriptionId 구독 ID
     * @return 결제 목록
     */
    List<Payment> findBySubscriptionId(UUID subscriptionId);
    
    /**
     * 결제 상태별 조회
     * 
     * @param status 결제 상태
     * @return 결제 목록
     */
    List<Payment> findByStatus(String status);

    /** 완료된 결제 총 매출 */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'completed'")
    Long sumCompletedAmount();

    /** 완료된 결제 - 날짜 범위 (거래내역 날짜별 조회) */
    @Query("SELECT p FROM Payment p WHERE p.status = 'completed' AND p.completedAt BETWEEN :from AND :to ORDER BY p.completedAt DESC")
    Page<Payment> findCompletedByCompletedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, org.springframework.data.domain.Pageable pageable);

    /** 완료된 결제 - 사용자+날짜 범위 */
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.status = 'completed' AND p.completedAt BETWEEN :from AND :to ORDER BY p.completedAt DESC")
    Page<Payment> findCompletedByUserIdAndCompletedAtBetween(@Param("userId") UUID userId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, org.springframework.data.domain.Pageable pageable);

    /** 완료된 결제 전체 (날짜 필터 없이, 대시보드 집계용) */
    @Query("SELECT p FROM Payment p WHERE p.status = 'completed' AND p.completedAt IS NOT NULL")
    List<Payment> findAllCompletedWithCompletedAt();
}





