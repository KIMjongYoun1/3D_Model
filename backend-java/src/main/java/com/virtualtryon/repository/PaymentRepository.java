package com.virtualtryon.repository;

import com.virtualtryon.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
     * 사용자별 결제 이력 조회 (최신순)
     * 
     * @param userId 사용자 ID
     * @return 결제 이력 목록
     */
    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
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
}





