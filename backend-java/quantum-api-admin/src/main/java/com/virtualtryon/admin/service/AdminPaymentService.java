package com.virtualtryon.admin.service;

import com.virtualtryon.admin.dto.payment.AdminPaymentDto;
import com.virtualtryon.core.entity.Payment;
import com.virtualtryon.core.repository.PaymentRepository;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** 관리자 결제/거래 관리 서비스 */
@Service
public class AdminPaymentService {

    private final PaymentRepository paymentRepository;

    public AdminPaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /** 전체 결제 목록 (페이징, from/to: 결제 완료일 필터) */
    public Page<AdminPaymentDto> findAll(Pageable pageable, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            return paymentRepository.findCompletedByCompletedAtBetween(from, to, pageable).map(this::toDto);
        }
        return paymentRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toDto);
    }

    /** 사용자별 결제 목록 (페이징, from/to: 결제 완료일 필터) */
    public Page<AdminPaymentDto> findByUserId(UUID userId, Pageable pageable, LocalDateTime from, LocalDateTime to) {
        UUID validUserId = Objects.requireNonNull(userId, "userId must not be null");
        if (from != null && to != null) {
            return paymentRepository.findCompletedByUserIdAndCompletedAtBetween(validUserId, from, to, pageable).map(this::toDto);
        }
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(validUserId, pageable).map(this::toDto);
    }

    /** 결제 상세 */
    public AdminPaymentDto findById(UUID id) {
        UUID validId = Objects.requireNonNull(id, "id must not be null");
        Payment payment = paymentRepository.findById(validId)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다: " + validId));
        return toDto(payment);
    }

    /** 결제 취소 (관리자) */
    @Transactional
    public AdminPaymentDto cancel(UUID id) {
        Payment payment = paymentRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다: " + id));
        if ("cancelled".equals(payment.getStatus())) {
            throw new IllegalArgumentException("이미 취소된 결제입니다.");
        }
        if ("failed".equals(payment.getStatus())) {
            throw new IllegalArgumentException("실패한 결제는 취소할 수 없습니다.");
        }
        payment.setStatus("cancelled");
        payment.setCancelledAt(LocalDateTime.now());
        paymentRepository.save(payment);
        return toDto(payment);
    }

    private AdminPaymentDto toDto(Payment p) {
        AdminPaymentDto d = new AdminPaymentDto();
        d.setId(p.getId());
        d.setUserId(p.getUserId());
        d.setSubscriptionId(p.getSubscriptionId());
        d.setPlanId(p.getPlanId());
        d.setPaymentMethod(p.getPaymentMethod());
        d.setAmount(p.getAmount());
        d.setStatus(p.getStatus());
        d.setPgProvider(p.getPgProvider());
        d.setPgTransactionId(p.getPgTransactionId());
        d.setCreatedAt(p.getCreatedAt());
        d.setCompletedAt(p.getCompletedAt());
        d.setCancelledAt(p.getCancelledAt());
        return d;
    }
}
