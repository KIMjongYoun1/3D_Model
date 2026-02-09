package com.virtualtryon.core.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment 엔티티 (결제)
 * 
 * ⚠️ Lombok 제거 버전
 */
@Entity
@Table(name = "payments")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "subscription_id")
    private UUID subscriptionId;
    
    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;
    
    @Column(nullable = false)
    private Long amount;
    
    @Column(length = 20)
    private String status = "pending";
    
    @Column(name = "pg_provider", length = 20)
    private String pgProvider = "simulation";
    
    @Column(name = "pg_transaction_id", length = 100)
    private String pgTransactionId;
    
    @Column(name = "pg_response", columnDefinition = "TEXT")
    private String pgResponse;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    public Payment() {}

    public Payment(UUID userId, String paymentMethod, Long amount) {
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = "pending";
        this.pgProvider = "simulation";
    }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder 패턴 시뮬레이션
    public static class PaymentBuilder {
        private UUID userId;
        private String paymentMethod;
        private Long amount;
        private String status = "pending";
        private String pgProvider = "simulation";

        public PaymentBuilder userId(UUID userId) { this.userId = userId; return this; }
        public PaymentBuilder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public PaymentBuilder amount(Long amount) { this.amount = amount; return this; }
        public PaymentBuilder status(String status) { this.status = status; return this; }
        public PaymentBuilder pgProvider(String pgProvider) { this.pgProvider = pgProvider; return this; }

        public Payment build() {
            Payment payment = new Payment();
            payment.setUserId(this.userId);
            payment.setPaymentMethod(this.paymentMethod);
            payment.setAmount(this.amount);
            payment.setStatus(this.status);
            payment.setPgProvider(this.pgProvider);
            return payment;
        }
    }

    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }
}

