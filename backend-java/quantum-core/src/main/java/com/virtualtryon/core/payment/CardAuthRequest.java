package com.virtualtryon.core.payment;

import java.util.UUID;

/**
 * 카드사 승인 요청 DTO
 */
public record CardAuthRequest(
        UUID orderId,
        String paymentKey,
        Long amount,
        String cardNumber,
        String expiry,
        String cvc
) {
}
