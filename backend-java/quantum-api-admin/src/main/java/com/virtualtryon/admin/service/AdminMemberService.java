package com.virtualtryon.admin.service;

import com.virtualtryon.admin.dto.member.AdminMemberDto;
import com.virtualtryon.core.entity.User;
import com.virtualtryon.core.repository.PaymentRepository;
import com.virtualtryon.core.repository.SubscriptionRepository;
import com.virtualtryon.core.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** 관리자 회원 관리 서비스 */
@Service
public class AdminMemberService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    public AdminMemberService(UserRepository userRepository, PaymentRepository paymentRepository,
                              SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    /** 회원 목록 (탈퇴 제외, 페이징) */
    public Page<AdminMemberDto> findAll(Pageable pageable) {
        return userRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable)
            .map(this::toDto);
    }

    /** 회원 상세 */
    public AdminMemberDto findById(UUID userId) {
        UUID validId = Objects.requireNonNull(userId, "userId must not be null");
        User user = userRepository.findById(validId)
            .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + validId));
        AdminMemberDto dto = toDto(user);
        dto.setPaymentCount((int) paymentRepository.findByUserIdOrderByCreatedAtDesc(validId).size());
        dto.setSubscriptionCount((int) subscriptionRepository.findByUserIdOrderByCreatedAtDesc(validId).size());
        return dto;
    }

    /** 회원 정지 */
    @Transactional
    public AdminMemberDto suspend(UUID userId) {
        User user = userRepository.findById(Objects.requireNonNull(userId))
            .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + userId));
        if (user.getDeletedAt() != null) {
            throw new IllegalArgumentException("이미 탈퇴한 회원입니다.");
        }
        user.setSuspendedAt(LocalDateTime.now());
        userRepository.save(user);
        return toDto(user);
    }

    /** 회원 정지 해제 */
    @Transactional
    public AdminMemberDto unsuspend(UUID userId) {
        User user = userRepository.findById(Objects.requireNonNull(userId))
            .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + userId));
        user.setSuspendedAt(null);
        userRepository.save(user);
        return toDto(user);
    }

    /** 회원 탈퇴 처리 (소프트 삭제) */
    @Transactional
    public void delete(UUID userId) {
        User user = userRepository.findById(Objects.requireNonNull(userId))
            .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + userId));
        user.setDeletedAt(LocalDateTime.now());
        user.setSuspendedAt(null);
        userRepository.save(user);
    }

    private AdminMemberDto toDto(User u) {
        AdminMemberDto d = new AdminMemberDto();
        d.setId(u.getId());
        d.setEmail(u.getEmail());
        d.setName(u.getName());
        d.setProvider(u.getProvider());
        d.setSubscription(u.getSubscription());
        d.setCreatedAt(u.getCreatedAt());
        d.setSuspendedAt(u.getSuspendedAt());
        d.setDeletedAt(u.getDeletedAt());
        return d;
    }
}
