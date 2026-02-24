package com.virtualtryon.core.dto.payment;

import com.virtualtryon.core.entity.Payment;

import java.util.UUID;

/** 결제 API 응답 DTO */
public class PaymentResponse {

    private UUID id;
    private UUID userId;
    private UUID subscriptionId;
    private String planId;
    private String paymentMethod;
    private Long amount;
    private String status;
    private String pgProvider;
    private String pgTransactionId;
    private String pgResponse;
    private String createdAt;
    private String completedAt;
    private String cancelledAt;

    public PaymentResponse() {}

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
    public String getPgResponse() { return pgResponse; }
    public void setPgResponse(String pgResponse) { this.pgResponse = pgResponse; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    public String getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(String cancelledAt) { this.cancelledAt = cancelledAt; }

    public static PaymentResponse from(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setUserId(payment.getUserId());
        response.setSubscriptionId(payment.getSubscriptionId());
        response.setPlanId(payment.getPlanId());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setPgProvider(payment.getPgProvider());
        response.setPgTransactionId(payment.getPgTransactionId());
        response.setPgResponse(payment.getPgResponse());
        response.setCreatedAt(payment.getCreatedAt() != null ? payment.getCreatedAt().toString() : null);
        response.setCompletedAt(payment.getCompletedAt() != null ? payment.getCompletedAt().toString() : null);
        response.setCancelledAt(payment.getCancelledAt() != null ? payment.getCancelledAt().toString() : null);
        return response;
    }
}
