package com.virtualtryon.payment.mock;

import com.virtualtryon.core.payment.CardAuthRequest;
import com.virtualtryon.core.payment.CardAuthResponse;
import com.virtualtryon.core.payment.CardCompanyClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 가상 카드사 클라이언트
 *
 * 실제 카드사 API 호출 없이 임의 승인/거절만 반환.
 * success-rate 설정에 따라 성공/실패 시뮬레이션.
 */
@Component
public class MockCardCompanyClient implements CardCompanyClient {

    @Value("${payment.success-rate:0.9}")
    private double successRate;

    private final Random random = new Random();

    /** 카드 승인 시뮬레이션. success-rate 확률로 승인/거절 */
    @Override
    public CardAuthResponse authorize(CardAuthRequest request) {
        boolean approved = random.nextDouble() < successRate;
        String authCode = "MOCK_" + Integer.toHexString(random.nextInt(0xFFFFFF)).toUpperCase();
        String message = approved ? "승인완료" : "한도초과";
        return new CardAuthResponse(approved, authCode, message);
    }
}
