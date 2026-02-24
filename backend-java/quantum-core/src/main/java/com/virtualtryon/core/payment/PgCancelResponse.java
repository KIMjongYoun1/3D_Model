package com.virtualtryon.core.payment;

/** PG 결제 취소 응답 DTO */
public record PgCancelResponse(
        boolean success,
        String message
) {
}
