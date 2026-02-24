package com.virtualtryon.core.payment;

import java.util.UUID;

/**
 * PG 결제 승인 요청 DTO
 */
public record PgApprovalRequest(
        UUID orderId,
        String paymentKey,
        Long amount,
        String paymentMethod
) {
}
