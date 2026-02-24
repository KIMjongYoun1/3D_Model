package com.virtualtryon.core.dto.payment;

import java.util.UUID;

/** 결제 승인 요청 DTO */
public class PaymentConfirmRequest {

    private UUID orderId;
    private String paymentKey;
    private Long amount;

    public PaymentConfirmRequest() {}

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public String getPaymentKey() { return paymentKey; }
    public void setPaymentKey(String paymentKey) { this.paymentKey = paymentKey; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
}
