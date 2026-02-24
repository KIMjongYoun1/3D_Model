package com.virtualtryon.core.payment;

/**
 * 카드사 승인 응답 DTO
 */
public record CardAuthResponse(
        boolean approved,
        String authCode,
        String message
) {
}
