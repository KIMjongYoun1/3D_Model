package com.virtualtryon.admin.dto.subscription;

import java.time.LocalDateTime;
import java.util.UUID;

/** 관리자 구독 목록/상세 DTO */
public class AdminSubscriptionDto {

    private UUID id;
    private UUID userId;
    private String planType;
    private String status;
    private Integer tryonLimit;
    private Integer tryonUsed;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private UUID paymentId;
    private Boolean autoRenew;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;

    public AdminSubscriptionDto() {}

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
}
