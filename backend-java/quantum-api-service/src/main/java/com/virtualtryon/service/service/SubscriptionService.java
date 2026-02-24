package com.virtualtryon.service.service;

import com.virtualtryon.core.entity.Subscription;
import com.virtualtryon.core.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** 사용자 구독 서비스 - 본인 구독 해지 등 */
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /** 본인 활성 구독 해지. 당월 말까지 이용 가능, 이후 자동 갱신 중단 */
    @Transactional
    public void cancelMySubscription(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Subscription sub = subscriptionRepository.findFirstByUserIdAndStatusOrderByExpiresAtDesc(userId, "active")
                .orElseThrow(() -> new IllegalArgumentException("해지할 수 있는 활성 구독이 없습니다."));
        if ("cancelled".equals(sub.getStatus())) {
            throw new IllegalArgumentException("이미 취소된 구독입니다.");
        }
        sub.setStatus("cancelled");
        sub.setCancelledAt(LocalDateTime.now());
        sub.setAutoRenew(false);
        subscriptionRepository.save(sub);
    }
}
