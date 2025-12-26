package com.virtualtryon.service;

import com.virtualtryon.entity.Payment;
import com.virtualtryon.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 결제 서비스 (시뮬레이션 모드)
 * 
 * 역할:
 * - 결제 요청 처리 (시뮬레이션)
 * - 결제 성공/실패 시뮬레이션
 * - 결제 이력 관리
 * 
 * 실제 PG사 연동 없이 시뮬레이션으로 동작합니다.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    
    /**
     * 결제 성공 확률 (0.0 ~ 1.0)
     * - 기본값: 0.9 (90% 성공)
     * - 환경 변수: PAYMENT_SUCCESS_RATE
     */
    @Value("${payment.success-rate:0.9}")
    private double successRate;
    
    private final Random random = new Random();
    
    /**
     * 결제 요청 생성 및 처리 (시뮬레이션)
     * 
     * ⭐ 직접 구현 필요: 이 메서드의 비즈니스 로직은 직접 구현해야 합니다.
     * - 사용량 체크 로직 추가 필요
     * - 구독 활성화 연동 로직 추가 필요
     * - 트랜잭션 관리 고려 필요
     * 
     * 구현 힌트:
     * 1. 사용량 체크: UsageService.checkUsageLimit() 호출
     * 2. 구독 활성화: 결제 성공 시 SubscriptionService.activate() 호출
     * 3. 트랜잭션: @Transactional로 전체 과정을 하나의 트랜잭션으로 처리
     * 
     * @param userId 사용자 ID
     * @param subscriptionId 구독 ID
     * @param paymentMethod 결제 수단 (card, bank_transfer)
     * @param amount 결제 금액 (원 단위)
     * @return 생성된 결제 정보
     */
    @Transactional
    public Payment createPayment(
            UUID userId,
            UUID subscriptionId,
            String paymentMethod,
            Long amount
    ) {
        // TODO: 직접 구현 필요 - 사용량 체크 로직 추가
        // 1. UsageService를 주입받아 사용량 체크
        // 2. 사용량 제한 초과 시 예외 발생
        
        // 결제 엔티티 생성
        // - Builder 패턴으로 결제 정보 설정
        // - pgTransactionId: 시뮬레이션용 거래 ID 생성 (SIM_ + UUID 앞 8자리)
        Payment payment = Payment.builder()
                .userId(userId)
                .subscriptionId(subscriptionId)
                .paymentMethod(paymentMethod)
                .amount(amount)
                .status("pending")  // 초기 상태: 대기 중
                .pgProvider("simulation")  // 시뮬레이션 모드
                .pgTransactionId("SIM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();
        
        // 결제 정보를 데이터베이스에 저장
        // - JPA의 save() 메서드로 INSERT 수행
        payment = paymentRepository.save(payment);
        
        // 시뮬레이션: 결제 처리 (성공/실패 랜덤 결정)
        // - processPaymentSimulation() 메서드에서 성공 확률에 따라 처리
        processPaymentSimulation(payment);
        
        // TODO: 직접 구현 필요 - 구독 활성화 로직 추가
        // 결제 성공 시 구독 활성화
        // if (payment.getStatus().equals("completed")) {
        //     subscriptionService.activateSubscription(subscriptionId, payment.getId());
        // }
        
        return payment;
    }
    
    /**
     * 결제 시뮬레이션 처리
     * - 성공 확률에 따라 성공/실패 결정
     * 
     * 동작 원리:
     * 1. Random.nextDouble()으로 0.0 ~ 1.0 사이의 랜덤 값 생성
     * 2. successRate(기본 0.9 = 90%)와 비교하여 성공/실패 결정
     * 3. 성공 시: status = "completed", completedAt 설정
     * 4. 실패 시: status = "failed"
     * 
     * @param payment 결제 정보
     */
    private void processPaymentSimulation(Payment payment) {
        // 성공 확률에 따라 성공/실패 결정
        // - random.nextDouble(): 0.0 ~ 1.0 사이의 랜덤 값
        // - successRate: 환경 변수에서 설정한 성공 확률 (기본 0.9 = 90%)
        // - 예: successRate가 0.9이면 90% 확률로 성공
        boolean isSuccess = random.nextDouble() < successRate;
        
        if (isSuccess) {
            // 결제 성공 처리
            payment.setStatus("completed");  // 상태: 완료
            payment.setCompletedAt(LocalDateTime.now());  // 완료 시간 설정
            // PG사 응답 데이터 생성 (JSON 형식)
            // - 실제 PG사 연동 시에는 실제 응답 데이터를 저장
            payment.setPgResponse(String.format(
                    "{\"status\":\"success\",\"message\":\"결제가 성공적으로 완료되었습니다.\",\"transaction_id\":\"%s\"}",
                    payment.getPgTransactionId()
            ));
        } else {
            // 결제 실패 처리
            payment.setStatus("failed");  // 상태: 실패
            // PG사 응답 데이터 생성 (실패 메시지 포함)
            payment.setPgResponse(String.format(
                    "{\"status\":\"failed\",\"message\":\"결제가 실패했습니다. (시뮬레이션)\",\"transaction_id\":\"%s\"}",
                    payment.getPgTransactionId()
            ));
        }
        
        // 변경된 결제 정보를 데이터베이스에 저장
        // - @Transactional로 인해 트랜잭션 내에서 자동 커밋
        paymentRepository.save(payment);
    }
    
    /**
     * 결제 강제 성공 (테스트용)
     * 
     * @param paymentId 결제 ID
     * @return 업데이트된 결제 정보
     */
    @Transactional
    public Payment forceSuccess(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("결제를 찾을 수 없습니다."));
        
        payment.setStatus("completed");
        payment.setCompletedAt(LocalDateTime.now());
        payment.setPgResponse(String.format(
                "{\"status\":\"success\",\"message\":\"결제가 강제로 성공 처리되었습니다.\",\"transaction_id\":\"%s\"}",
                payment.getPgTransactionId()
        ));
        
        return paymentRepository.save(payment);
    }
    
    /**
     * 결제 강제 실패 (테스트용)
     * 
     * @param paymentId 결제 ID
     * @return 업데이트된 결제 정보
     */
    @Transactional
    public Payment forceFailure(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("결제를 찾을 수 없습니다."));
        
        payment.setStatus("failed");
        payment.setPgResponse(String.format(
                "{\"status\":\"failed\",\"message\":\"결제가 강제로 실패 처리되었습니다.\",\"transaction_id\":\"%s\"}",
                payment.getPgTransactionId()
        ));
        
        return paymentRepository.save(payment);
    }
    
    /**
     * 사용자 결제 이력 조회
     * 
     * ⭐ 개선 필요: 페이징 처리 및 캐싱 추가 고려
     * - 대량의 결제 이력이 있을 경우 성능 문제 발생 가능
     * - 페이징: Pageable 사용하여 페이지 단위로 조회
     * - 캐싱: 자주 조회되는 데이터는 Redis에 캐싱
     * 
     * 개선 예시:
     * @Cacheable(value = "payments", key = "#userId")
     * public Page<Payment> getUserPayments(UUID userId, Pageable pageable) {
     *     return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
     * }
     * 
     * @param userId 사용자 ID
     * @return 결제 이력 목록 (최신순 정렬)
     */
    public List<Payment> getUserPayments(UUID userId) {
        // Repository를 통해 사용자별 결제 이력 조회
        // - findByUserIdOrderByCreatedAtDesc: 사용자 ID로 조회하고 생성일시 내림차순 정렬
        // - 최신 결제가 먼저 나오도록 정렬
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 결제 정보 조회
     * 
     * @param paymentId 결제 ID
     * @return 결제 정보
     */
    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("결제를 찾을 수 없습니다."));
    }
}

