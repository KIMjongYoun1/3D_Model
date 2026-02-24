package com.virtualtryon.core.payment;

import java.util.UUID;

/** PG 결제 취소 요청 DTO */
public record PgCancelRequest(
        UUID orderId,
        String paymentKey,
        Long amount
) {
}
