package com.virtualtryon.service;

import com.virtualtryon.core.entity.Payment;
import com.virtualtryon.core.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * 결제 서비스 (시뮬레이션 모드)
 * 
 * ⚠️ Lombok 제거 및 Null Safety 강화 버전
 */
@Service
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    
    @Value("${payment.success-rate:0.9}")
    private double successRate;
    
    private final Random random = new Random();

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    
    @Transactional
    public Payment createPayment(
            UUID userId,
            UUID subscriptionId,
            String paymentMethod,
            Long amount
    ) {
        // Builder 대신 직접 생성자 사용 (Lombok 의존성 제거)
        Payment payment = new Payment(userId, paymentMethod, amount);
        payment.setSubscriptionId(subscriptionId);
        payment.setStatus("pending");
        payment.setPgProvider("simulation");
        
        payment.setPgTransactionId("SIM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        payment = paymentRepository.save(payment);
        processPaymentSimulation(payment);
        
        return payment;
    }
    
    private void processPaymentSimulation(Payment payment) {
        boolean isSuccess = random.nextDouble() < successRate;
        
        if (isSuccess) {
            payment.setStatus("completed");
            payment.setCompletedAt(LocalDateTime.now());
            payment.setPgResponse(String.format(
                    "{\"status\":\"success\",\"message\":\"결제가 성공적으로 완료되었습니다.\",\"transaction_id\":\"%s\"}",
                    payment.getPgTransactionId()
            ));
        } else {
            payment.setStatus("failed");
            payment.setPgResponse(String.format(
                    "{\"status\":\"failed\",\"message\":\"결제가 실패했습니다. (시뮬레이션)\",\"transaction_id\":\"%s\"}",
                    payment.getPgTransactionId()
            ));
        }
        
        paymentRepository.save(payment);
    }
    
    @Transactional
    public Payment forceSuccess(UUID paymentId) {
        Payment payment = paymentRepository.findById(Objects.requireNonNull(paymentId, "paymentId must not be null"))
                .orElseThrow(() -> new RuntimeException("결제를 찾을 수 없습니다."));
        
        payment.setStatus("completed");
        payment.setCompletedAt(LocalDateTime.now());
        payment.setPgResponse(String.format(
                "{\"status\":\"success\",\"message\":\"결제가 강제로 성공 처리되었습니다.\",\"transaction_id\":\"%s\"}",
                payment.getPgTransactionId()
        ));
        
        return paymentRepository.save(payment);
    }
    
    @Transactional
    public Payment forceFailure(UUID paymentId) {
        Payment payment = paymentRepository.findById(Objects.requireNonNull(paymentId, "paymentId must not be null"))
                .orElseThrow(() -> new RuntimeException("결제를 찾을 수 없습니다."));
        
        payment.setStatus("failed");
        payment.setPgResponse(String.format(
                "{\"status\":\"failed\",\"message\":\"결제가 강제로 실패 처리되었습니다.\",\"transaction_id\":\"%s\"}",
                payment.getPgTransactionId()
        ));
        
        return paymentRepository.save(payment);
    }
    
    public List<Payment> getUserPayments(UUID userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(Objects.requireNonNull(userId, "userId must not be null"));
    }
    
    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(Objects.requireNonNull(paymentId, "paymentId must not be null"))
                .orElseThrow(() -> new RuntimeException("결제를 찾을 수 없습니다."));
    }
}
