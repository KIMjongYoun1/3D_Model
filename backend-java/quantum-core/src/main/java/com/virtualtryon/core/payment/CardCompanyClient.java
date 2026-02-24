package com.virtualtryon.core.payment;

/**
 * 카드사 클라이언트 인터페이스
 *
 * PG가 내부적으로 카드사에 승인 요청 시 사용.
 * MockCardCompanyClient는 임의 응답만 반환.
 */
public interface CardCompanyClient {

    /**
     * 카드 승인 요청 (PG가 카드사에 호출)
     *
     * @param request 카드 승인 요청
     * @return 카드사 승인 결과
     */
    CardAuthResponse authorize(CardAuthRequest request);
}
