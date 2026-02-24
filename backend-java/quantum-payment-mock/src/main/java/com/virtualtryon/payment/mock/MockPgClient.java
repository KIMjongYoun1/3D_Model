package com.virtualtryon.payment.mock;

import com.virtualtryon.core.payment.CardAuthRequest;
import com.virtualtryon.core.payment.CardAuthResponse;
import com.virtualtryon.core.payment.PgApprovalRequest;
import com.virtualtryon.core.payment.PgApprovalResponse;
import com.virtualtryon.core.payment.PgCancelRequest;
import com.virtualtryon.core.payment.PgCancelResponse;
import com.virtualtryon.core.payment.PgClient;
import org.springframework.stereotype.Component;

/**
 * 가상 PG(결제대행사) 클라이언트
 *
 * 실제 PG API 호출 없이 MockCardCompanyClient를 통해 임의 응답만 반환.
 * 실제 토스페이먼츠 연동 시 이 구현체를 TossPaymentsClient로 교체.
 */
@Component
public class MockPgClient implements PgClient {

    private final MockCardCompanyClient mockCardCompanyClient;

    public MockPgClient(MockCardCompanyClient mockCardCompanyClient) {
        this.mockCardCompanyClient = mockCardCompanyClient;
    }

    /** PG 승인 시뮬레이션. MockCardCompanyClient 호출 후 응답 반환 */
    @Override
    public PgApprovalResponse approve(PgApprovalRequest request) {
        CardAuthResponse cardResponse = mockCardCompanyClient.authorize(
                new CardAuthRequest(
                        request.orderId(),
                        request.paymentKey(),
                        request.amount(),
                        null,
                        null,
                        null
                )
        );

        String rawResponse = String.format(
                "{\"status\":\"%s\",\"message\":\"%s\",\"transaction_id\":\"%s\"}",
                cardResponse.approved() ? "success" : "failed",
                cardResponse.approved() ? "결제가 성공적으로 완료되었습니다." : "결제가 실패했습니다. (시뮬레이션)",
                request.paymentKey()
        );

        return new PgApprovalResponse(
                cardResponse.approved(),
                request.paymentKey(),
                cardResponse.message(),
                rawResponse
        );
    }

    /** PG 취소 시뮬레이션. 가상 결제이므로 항상 취소 성공 */
    @Override
    public PgCancelResponse cancel(PgCancelRequest request) {
        return new PgCancelResponse(true, "결제가 취소되었습니다. (시뮬레이션)");
    }
}
