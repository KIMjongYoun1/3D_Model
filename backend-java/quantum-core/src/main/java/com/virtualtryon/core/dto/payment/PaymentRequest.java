package com.virtualtryon.core.dto.payment;

import java.util.List;
import java.util.UUID;

/** 결제 요청 DTO */
public class PaymentRequest {

    private UUID userId;
    private UUID subscriptionId;
    private String planId;
    private String paymentMethod;
    private Long amount;
    private List<UUID> agreedTermIds;  // 동의한 약관 ID 목록 (필수 결제 약관 포함 필요)

    public PaymentRequest() {}

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
    public List<UUID> getAgreedTermIds() { return agreedTermIds; }
    public void setAgreedTermIds(List<UUID> agreedTermIds) { this.agreedTermIds = agreedTermIds; }
}
