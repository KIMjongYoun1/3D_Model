package com.virtualtryon.service.controller;

import com.virtualtryon.core.entity.Payment;
import com.virtualtryon.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 결제 컨트롤러
 * 
 * ⚠️ Lombok 제거 버전
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @PostMapping("/request")
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request
    ) {
        Payment payment = paymentService.createPayment(
                request.getUserId(),
                request.getSubscriptionId(),
                request.getPaymentMethod(),
                request.getAmount()
        );
        
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
    
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        Payment payment = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(@PathVariable UUID userId) {
        List<Payment> payments = paymentService.getUserPayments(userId);
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping("/{paymentId}/force-success")
    public ResponseEntity<PaymentResponse> forceSuccess(@PathVariable UUID paymentId) {
        Payment payment = paymentService.forceSuccess(paymentId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
    
    @PostMapping("/{paymentId}/force-failure")
    public ResponseEntity<PaymentResponse> forceFailure(@PathVariable UUID paymentId) {
        Payment payment = paymentService.forceFailure(paymentId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
    
    public static class PaymentRequest {
        private UUID userId;
        private UUID subscriptionId;
        private String paymentMethod;
        private Long amount;

        public PaymentRequest() {}

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public UUID getSubscriptionId() { return subscriptionId; }
        public void setSubscriptionId(UUID subscriptionId) { this.subscriptionId = subscriptionId; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public Long getAmount() { return amount; }
        public void setAmount(Long amount) { this.amount = amount; }
    }
    
    public static class PaymentResponse {
        private UUID id;
        private UUID userId;
        private UUID subscriptionId;
        private String paymentMethod;
        private Long amount;
        private String status;
        private String pgProvider;
        private String pgTransactionId;
        private String pgResponse;
        private String createdAt;
        private String completedAt;
        private String cancelledAt;

        public PaymentResponse() {}

        // Getter & Setter
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public UUID getSubscriptionId() { return subscriptionId; }
        public void setSubscriptionId(UUID subscriptionId) { this.subscriptionId = subscriptionId; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public Long getAmount() { return amount; }
        public void setAmount(Long amount) { this.amount = amount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPgProvider() { return pgProvider; }
        public void setPgProvider(String pgProvider) { this.pgProvider = pgProvider; }
        public String getPgTransactionId() { return pgTransactionId; }
        public void setPgTransactionId(String pgTransactionId) { this.pgTransactionId = pgTransactionId; }
        public String getPgResponse() { return pgResponse; }
        public void setPgResponse(String pgResponse) { this.pgResponse = pgResponse; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getCompletedAt() { return completedAt; }
        public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
        public String getCancelledAt() { return cancelledAt; }
        public void setCancelledAt(String cancelledAt) { this.cancelledAt = cancelledAt; }

        public static PaymentResponse from(Payment payment) {
            PaymentResponse response = new PaymentResponse();
            response.setId(payment.getId());
            response.setUserId(payment.getUserId());
            response.setSubscriptionId(payment.getSubscriptionId());
            response.setPaymentMethod(payment.getPaymentMethod());
            response.setAmount(payment.getAmount());
            response.setStatus(payment.getStatus());
            response.setPgProvider(payment.getPgProvider());
            response.setPgTransactionId(payment.getPgTransactionId());
            response.setPgResponse(payment.getPgResponse());
            response.setCreatedAt(payment.getCreatedAt() != null ? payment.getCreatedAt().toString() : null);
            response.setCompletedAt(payment.getCompletedAt() != null ? payment.getCompletedAt().toString() : null);
            response.setCancelledAt(payment.getCancelledAt() != null ? payment.getCancelledAt().toString() : null);
            return response;
        }
    }
}
