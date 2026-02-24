package com.virtualtryon.admin.service;

import com.virtualtryon.admin.dto.subscription.AdminSubscriptionDto;
import com.virtualtryon.core.entity.Subscription;
import com.virtualtryon.core.repository.SubscriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** 관리자 구독 관리 서비스 */
@Service
public class AdminSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public AdminSubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /** 전체 구독 목록 (페이징) */
    public Page<AdminSubscriptionDto> findAll(Pageable pageable) {
        return subscriptionRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toDto);
    }

    /** 사용자별 구독 목록 (페이징) */
    public Page<AdminSubscriptionDto> findByUserId(UUID userId, Pageable pageable) {
        UUID validUserId = Objects.requireNonNull(userId, "userId must not be null");
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(validUserId, pageable).map(this::toDto);
    }

    /** 구독 상세 */
    public AdminSubscriptionDto findById(UUID id) {
        UUID validId = Objects.requireNonNull(id, "id must not be null");
        Subscription sub = subscriptionRepository.findById(validId)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다: " + validId));
        return toDto(sub);
    }

    /** 구독 취소 */
    @Transactional
    public AdminSubscriptionDto cancel(UUID id) {
        Subscription sub = subscriptionRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다: " + id));
        if ("cancelled".equals(sub.getStatus())) {
            throw new IllegalArgumentException("이미 취소된 구독입니다.");
        }
        sub.setStatus("cancelled");
        sub.setCancelledAt(LocalDateTime.now());
        sub.setAutoRenew(false);
        subscriptionRepository.save(sub);
        return toDto(sub);
    }

    private AdminSubscriptionDto toDto(Subscription s) {
        AdminSubscriptionDto d = new AdminSubscriptionDto();
        d.setId(s.getId());
        d.setUserId(s.getUserId());
        d.setPlanType(s.getPlanType());
        d.setStatus(s.getStatus());
        d.setTryonLimit(s.getTryonLimit());
        d.setTryonUsed(s.getTryonUsed());
        d.setStartedAt(s.getStartedAt());
        d.setExpiresAt(s.getExpiresAt());
        d.setPaymentId(s.getPaymentId());
        d.setAutoRenew(s.getAutoRenew());
        d.setCancelledAt(s.getCancelledAt());
        d.setCreatedAt(s.getCreatedAt());
        return d;
    }
}
