package com.virtualtryon.core.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Subscription 엔티티 (구독)
 *
 * 사용자의 구독 기간·플랜 정보를 저장.
 * 결제 성공 시 생성/갱신되며, started_at ~ expires_at 으로 유효 기간 관리.
 */
@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "plan_type", nullable = false, length = 20)
    private String planType;

    @Column(name = "status", length = 20)
    private String status = "active";

    @Column(name = "tryon_limit")
    private Integer tryonLimit;

    @Column(name = "tryon_used")
    private Integer tryonUsed = 0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "auto_renew")
    private Boolean autoRenew = false;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Subscription() {}

    public Subscription(UUID userId, String planType) {
        this.userId = userId;
        this.planType = planType;
        this.status = "active";
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTryonLimit() { return tryonLimit; }
    public void setTryonLimit(Integer tryonLimit) { this.tryonLimit = tryonLimit; }

    public Integer getTryonUsed() { return tryonUsed; }
    public void setTryonUsed(Integer tryonUsed) { this.tryonUsed = tryonUsed; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public Boolean getAutoRenew() { return autoRenew; }
    public void setAutoRenew(Boolean autoRenew) { this.autoRenew = autoRenew; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** 만료 여부 (expires_at이 지났거나 status가 expired/cancelled) */
    public boolean isExpired() {
        if ("expired".equals(status) || "cancelled".equals(status)) return true;
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
