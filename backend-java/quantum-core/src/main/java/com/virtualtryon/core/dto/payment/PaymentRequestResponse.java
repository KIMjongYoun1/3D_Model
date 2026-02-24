package com.virtualtryon.core.dto.payment;

import java.util.UUID;

/** 결제 요청 API 응답 DTO (orderId, paymentKey 반환) */
public class PaymentRequestResponse {

    private UUID orderId;
    private String paymentKey;
    private Long amount;
    private String planId;

    public PaymentRequestResponse() {}

    public PaymentRequestResponse(UUID orderId, String paymentKey, Long amount, String planId) {
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.planId = planId;
    }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public String getPaymentKey() { return paymentKey; }
    public void setPaymentKey(String paymentKey) { this.paymentKey = paymentKey; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
}
