package com.virtualtryon.core.payment;

/**
 * PG(결제대행사) 클라이언트 인터페이스
 *
 * 실제 PG(토스페이먼츠, 아임포트 등) 연동 시 이 인터페이스를 구현.
 * MockPgClient는 임의 응답만 반환.
 */
public interface PgClient {

    /**
     * 결제 승인 요청
     *
     * @param request 승인 요청 정보
     * @return PG 승인 결과
     */
    PgApprovalResponse approve(PgApprovalRequest request);

    /**
     * 결제 취소 요청. 승인 후 오류 발생 시 호출.
     *
     * @param request 취소 요청 정보
     * @return PG 취소 결과
     */
    PgCancelResponse cancel(PgCancelRequest request);
}
