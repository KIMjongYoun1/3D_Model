package com.virtualtryon.core.payment;

/**
 * PG 결제 승인 응답 DTO
 */
public record PgApprovalResponse(
        boolean success,
        String transactionId,
        String message,
        String rawResponse
) {
}
