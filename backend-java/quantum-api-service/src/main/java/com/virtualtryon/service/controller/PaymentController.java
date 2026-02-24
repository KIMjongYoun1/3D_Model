package com.virtualtryon.service.controller;

import com.virtualtryon.core.dto.payment.*;
import com.virtualtryon.core.entity.Payment;
import com.virtualtryon.service.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 결제 컨트롤러
 *
 * - POST /request: 주문 생성 (인증 필요), orderId·paymentKey 반환
 * - POST /confirm: PG 승인 시뮬레이션 (인증 필요)
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** 주문 생성. orderId·paymentKey 반환 (인증 필요) */
    @PostMapping("/request")
    public ResponseEntity<PaymentRequestResponse> createPaymentRequest(
            @AuthenticationPrincipal Object principal,
            @RequestBody PaymentRequest request
    ) {
        UUID userId = extractUserId(principal);
        Payment payment = paymentService.createPaymentRequest(
                userId,
                request.getPlanId(),
                request.getPaymentMethod() != null ? request.getPaymentMethod() : "card",
                Objects.requireNonNull(request.getAmount(), "amount is required"),
                request.getAgreedTermIds()
        );
        return ResponseEntity.ok(new PaymentRequestResponse(
                payment.getId(),
                payment.getPgTransactionId(),
                payment.getAmount(),
                payment.getPlanId()
        ));
    }

    /** PG 승인 처리. 성공 시 Subscription 생성 (인증 필요) */
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @AuthenticationPrincipal Object principal,
            @RequestBody PaymentConfirmRequest request
    ) {
        UUID userId = extractUserId(principal);
        Payment payment = paymentService.confirmPayment(
                userId,
                Objects.requireNonNull(request.getOrderId(), "orderId is required"),
                request.getPaymentKey(),
                Objects.requireNonNull(request.getAmount(), "amount is required")
        );
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    private UUID extractUserId(Object principal) {
        if (principal == null || !(principal instanceof UUID)) {
            throw new IllegalStateException("인증이 필요합니다.");
        }
        return (UUID) principal;
    }

    /** 본인 결제 이력 조회 (인증 필요) */
    @GetMapping("/me")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(@AuthenticationPrincipal Object principal) {
        UUID userId = extractUserId(principal);
        List<Payment> payments = paymentService.getUserPayments(userId);
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /** 결제 단건 조회 */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        Payment payment = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /** 사용자별 결제 이력 조회 (최신순) */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(@PathVariable UUID userId) {
        List<Payment> payments = paymentService.getUserPayments(userId);
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /** 결제 강제 성공 (테스트/관리용) */
    @PostMapping("/{paymentId}/force-success")
    public ResponseEntity<PaymentResponse> forceSuccess(@PathVariable UUID paymentId) {
        Payment payment = paymentService.forceSuccess(paymentId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /** 결제 강제 실패 (테스트/관리용) */
    @PostMapping("/{paymentId}/force-failure")
    public ResponseEntity<PaymentResponse> forceFailure(@PathVariable UUID paymentId) {
        Payment payment = paymentService.forceFailure(paymentId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
}
