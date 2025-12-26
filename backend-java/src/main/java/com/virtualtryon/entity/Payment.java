package com.virtualtryon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment 엔티티 (결제)
 * 
 * 역할:
 * - 결제 정보 저장
 * - 시뮬레이션 모드로 동작 (실제 PG사 연동 없음)
 * - 결제 성공/실패 상태 관리
 * 
 * @Entity: JPA 엔티티로 인식
 * @Table: 테이블명 지정 (payments)
 */
@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    /**
     * Primary Key: UUID
     * - 자동 생성 (데이터베이스에서)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * 사용자 ID (FK)
     * - 결제한 사용자
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    /**
     * 구독 ID (FK)
     * - 결제와 연결된 구독
     */
    @Column(name = "subscription_id")
    private UUID subscriptionId;
    
    /**
     * 결제 수단
     * - card: 카드 결제
     * - bank_transfer: 계좌 이체
     */
    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;
    
    /**
     * 결제 금액 (원 단위)
     */
    @Column(nullable = false)
    private Long amount;
    
    /**
     * 결제 상태
     * - pending: 결제 대기
     * - completed: 결제 완료
     * - failed: 결제 실패
     * - cancelled: 결제 취소
     */
    @Column(length = 20)
    @Builder.Default
    private String status = "pending";
    
    /**
     * PG사 (시뮬레이션 모드)
     * - simulation: 시뮬레이션 모드
     */
    @Column(name = "pg_provider", length = 20)
    @Builder.Default
    private String pgProvider = "simulation";
    
    /**
     * 거래 ID (시뮬레이션용)
     * - 시뮬레이션 모드에서 자동 생성
     */
    @Column(name = "pg_transaction_id", length = 100)
    private String pgTransactionId;
    
    /**
     * 응답 데이터 (시뮬레이션 응답)
     * - JSON 형식으로 저장
     */
    @Column(name = "pg_response", columnDefinition = "jsonb")
    private String pgResponse;
    
    /**
     * 생성일시
     * - 자동 설정 (데이터베이스에서)
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 완료일시
     * - 결제 완료 시 설정
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    /**
     * 취소일시
     * - 결제 취소 시 설정
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}

