package com.virtualtryon.admin.dto.payment;

import java.time.LocalDateTime;
import java.util.UUID;

/** 관리자 결제 목록/상세 DTO */
public class AdminPaymentDto {

    private UUID id;
    private UUID userId;
    private UUID subscriptionId;
    private String planId;
    private String paymentMethod;
    private Long amount;
    private String status;
    private String pgProvider;
    private String pgTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    public AdminPaymentDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(UUID subscriptionId) { this.subscriptionId = subscriptionId; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPgProvider() { return pgProvider; }
    public void setPgProvider(String pgProvider) { this.pgProvider = pgProvider; }
    public String getPgTransactionId() { return pgTransactionId; }
    public void setPgTransactionId(String pgTransactionId) { this.pgTransactionId = pgTransactionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
}
