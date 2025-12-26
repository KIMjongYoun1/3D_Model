package com.virtualtryon.controller;

import com.virtualtryon.entity.Payment;
import com.virtualtryon.service.PaymentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 결제 컨트롤러
 * 
 * 역할:
 * - 결제 API 엔드포인트 제공
 * - 시뮬레이션 모드로 결제 처리
 * - 결제 이력 조회
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * 결제 요청 생성
     * 
     * POST /api/v1/payments/request
     * 
     * 요청 흐름:
     * 1. 클라이언트에서 결제 요청 데이터 전송
     * 2. PaymentService.createPayment() 호출하여 결제 처리
     * 3. 시뮬레이션 모드로 성공/실패 결정
     * 4. 결제 결과를 PaymentResponse로 변환하여 반환
     * 
     * 시뮬레이션 모드:
     * - 실제 PG사 연동 없이 결제 처리
     * - 성공 확률에 따라 성공/실패 결정 (기본 90%)
     * 
     * ⭐ 직접 구현 필요: 입력 검증 로직 추가
     * - @Valid 어노테이션으로 DTO 검증
     * - 금액 범위 검증 (최소/최대 금액)
     * - 결제 수단 검증
     * 
     * @param request 결제 요청 데이터 (userId, subscriptionId, paymentMethod, amount)
     * @return 결제 결과 (PaymentResponse)
     */
    @PostMapping("/request")
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request
    ) {
        // TODO: 직접 구현 필요 - 입력 검증
        // 1. @Valid 어노테이션 추가
        // 2. PaymentRequest에 검증 어노테이션 추가 (@NotNull, @Min, @Max 등)
        // 3. 금액 범위 검증 (예: 최소 1000원, 최대 1000000원)
        
        // PaymentService를 통해 결제 처리
        // - 비즈니스 로직은 Service 계층에서 처리
        Payment payment = paymentService.createPayment(
                request.getUserId(),
                request.getSubscriptionId(),
                request.getPaymentMethod(),
                request.getAmount()
        );
        
        // Payment 엔티티를 PaymentResponse DTO로 변환
        // - 엔티티를 직접 반환하지 않고 DTO로 변환하여 반환
        // - API 계약을 명확하게 하고, 내부 구조를 숨김
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
    
    /**
     * 결제 정보 조회
     * 
     * GET /api/v1/payments/{paymentId}
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        Payment payment = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
    
    /**
     * 사용자 결제 이력 조회
     * 
     * GET /api/v1/payments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(@PathVariable UUID userId) {
        List<Payment> payments = paymentService.getUserPayments(userId);
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 결제 강제 성공 (테스트용)
     * 
     * POST /api/v1/payments/{paymentId}/force-success
     */
    @PostMapping("/{paymentId}/force-success")
    public ResponseEntity<PaymentResponse> forceSuccess(@PathVariable UUID paymentId) {
        Payment payment = paymentService.forceSuccess(paymentId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
    
    /**
     * 결제 강제 실패 (테스트용)
     * 
     * POST /api/v1/payments/{paymentId}/force-failure
     */
    @PostMapping("/{paymentId}/force-failure")
    public ResponseEntity<PaymentResponse> forceFailure(@PathVariable UUID paymentId) {
        Payment payment = paymentService.forceFailure(paymentId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
    
    /**
     * 결제 요청 DTO (Data Transfer Object)
     * 
     * 역할:
     * - 클라이언트에서 서버로 전송되는 결제 요청 데이터를 담는 객체
     * - 엔티티와 분리하여 API 계약을 명확하게 함
     * 
     * ⭐ 직접 구현 필요: 입력 검증 어노테이션 추가
     * - @NotNull: 필수 필드 검증
     * - @Min, @Max: 금액 범위 검증
     * - @Pattern: 결제 수단 형식 검증
     * 
     * 예시:
     * @NotNull(message = "사용자 ID는 필수입니다.")
     * private UUID userId;
     * 
     * @Min(value = 1000, message = "최소 결제 금액은 1,000원입니다.")
     * @Max(value = 1000000, message = "최대 결제 금액은 1,000,000원입니다.")
     * private Long amount;
     */
    @Data
    public static class PaymentRequest {
        /**
         * 사용자 ID
         * - 결제를 요청하는 사용자의 고유 식별자
         */
        private UUID userId;
        
        /**
         * 구독 ID (선택사항)
         * - 결제와 연결된 구독 정보
         * - null 가능 (일반 결제의 경우)
         */
        private UUID subscriptionId;
        
        /**
         * 결제 수단
         * - "card": 카드 결제
         * - "bank_transfer": 계좌 이체
         */
        private String paymentMethod;
        
        /**
         * 결제 금액
         * - 원 단위로 저장
         * - 예: 10000 = 10,000원
         */
        private Long amount;
    }
    
    /**
     * 결제 응답 DTO (Data Transfer Object)
     * 
     * 역할:
     * - 서버에서 클라이언트로 반환되는 결제 결과 데이터를 담는 객체
     * - Payment 엔티티를 DTO로 변환하여 반환
     * - 내부 구조를 숨기고 필요한 정보만 노출
     * 
     * 변환 메서드:
     * - from(): Payment 엔티티를 PaymentResponse로 변환
     * - 정적 팩토리 메서드 패턴 사용
     */
    @Data
    public static class PaymentResponse {
        /**
         * 결제 ID
         * - 결제의 고유 식별자
         */
        private UUID id;
        
        /**
         * 사용자 ID
         * - 결제한 사용자의 고유 식별자
         */
        private UUID userId;
        
        /**
         * 구독 ID
         * - 결제와 연결된 구독 정보 (선택사항)
         */
        private UUID subscriptionId;
        
        /**
         * 결제 수단
         * - card: 카드 결제
         * - bank_transfer: 계좌 이체
         */
        private String paymentMethod;
        
        /**
         * 결제 금액
         * - 원 단위
         */
        private Long amount;
        
        /**
         * 결제 상태
         * - pending: 결제 대기 중
         * - completed: 결제 완료
         * - failed: 결제 실패
         * - cancelled: 결제 취소
         */
        private String status;
        
        /**
         * PG사
         * - simulation: 시뮬레이션 모드
         */
        private String pgProvider;
        
        /**
         * 거래 ID
         * - PG사에서 발급한 거래 고유 식별자
         * - 시뮬레이션: SIM_ + UUID 앞 8자리
         */
        private String pgTransactionId;
        
        /**
         * PG사 응답 데이터
         * - JSON 형식의 문자열
         * - 실제 PG사 연동 시 전체 응답 데이터 저장
         */
        private String pgResponse;
        
        /**
         * 생성일시
         * - 결제 요청이 생성된 시간
         * - ISO 8601 형식 문자열로 변환
         */
        private String createdAt;
        
        /**
         * 완료일시
         * - 결제가 완료된 시간 (성공 시)
         * - null 가능 (대기 중이거나 실패한 경우)
         */
        private String completedAt;
        
        /**
         * 취소일시
         * - 결제가 취소된 시간
         * - null 가능 (취소되지 않은 경우)
         */
        private String cancelledAt;
        
        /**
         * Payment 엔티티를 PaymentResponse로 변환
         * 
         * 정적 팩토리 메서드 패턴:
         * - PaymentResponse.from(payment) 형태로 사용
         * - 엔티티의 모든 필드를 DTO로 복사
         * - LocalDateTime을 String으로 변환 (JSON 직렬화를 위해)
         * 
         * @param payment Payment 엔티티
         * @return PaymentResponse DTO
         */
        public static PaymentResponse from(Payment payment) {
            PaymentResponse response = new PaymentResponse();
            
            // 기본 정보 복사
            response.setId(payment.getId());
            response.setUserId(payment.getUserId());
            response.setSubscriptionId(payment.getSubscriptionId());
            response.setPaymentMethod(payment.getPaymentMethod());
            response.setAmount(payment.getAmount());
            response.setStatus(payment.getStatus());
            response.setPgProvider(payment.getPgProvider());
            response.setPgTransactionId(payment.getPgTransactionId());
            response.setPgResponse(payment.getPgResponse());
            
            // 날짜 필드 변환 (LocalDateTime → String)
            // - null 체크 후 toString()으로 ISO 8601 형식 문자열로 변환
            // - JSON 직렬화 시 문자열로 변환되어 전송됨
            response.setCreatedAt(payment.getCreatedAt() != null ? payment.getCreatedAt().toString() : null);
            response.setCompletedAt(payment.getCompletedAt() != null ? payment.getCompletedAt().toString() : null);
            response.setCancelledAt(payment.getCancelledAt() != null ? payment.getCancelledAt().toString() : null);
            
            return response;
        }
    }
}

