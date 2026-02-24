package com.virtualtryon.service.service;

import com.virtualtryon.core.entity.Payment;
import com.virtualtryon.core.entity.PlanConfig;
import com.virtualtryon.core.entity.User;
import com.virtualtryon.core.payment.PgApprovalRequest;
import com.virtualtryon.core.payment.PgApprovalResponse;
import com.virtualtryon.core.payment.PgCancelRequest;
import com.virtualtryon.core.payment.PgClient;
import com.virtualtryon.core.entity.Subscription;
import com.virtualtryon.core.repository.PaymentRepository;
import com.virtualtryon.core.repository.PlanConfigRepository;
import com.virtualtryon.core.repository.SubscriptionRepository;
import com.virtualtryon.core.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** 결제 서비스 - plan_config 검증, 주문 생성, PG 승인 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final int SUBSCRIPTION_MONTHS = 1;

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PlanConfigRepository planConfigRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PgClient pgClient;
    private final TermsService termsService;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository,
                          PlanConfigRepository planConfigRepository, SubscriptionRepository subscriptionRepository,
                          PgClient pgClient, TermsService termsService) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.planConfigRepository = planConfigRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.pgClient = pgClient;
        this.termsService = termsService;
    }

    /** 플랜 검증 후 주문 생성. orderId·paymentKey 반환용 Payment 저장. 필수 결제 약관 동의 검증 */
    @Transactional
    public Payment createPaymentRequest(UUID userId, String planId, String paymentMethod, Long amount, List<UUID> agreedTermIds) {
        String planCode = planId != null && !planId.isBlank() ? planId : "pro";
        PlanConfig plan = planConfigRepository.findByPlanCode(planCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 플랜입니다: " + planCode));
        if (Boolean.FALSE.equals(plan.getIsActive())) {
            throw new IllegalArgumentException("판매 중인 플랜이 아닙니다: " + planCode);
        }
        Long expectedPrice = plan.getPriceMonthly() != null ? plan.getPriceMonthly() : 0L;
        if (!Objects.equals(expectedPrice, amount)) {
            throw new IllegalArgumentException("플랜 요금이 일치하지 않습니다. 기대값: " + expectedPrice + ", 요청: " + amount);
        }
        if (expectedPrice == 0) {
            throw new IllegalArgumentException("무료 플랜은 결제가 필요하지 않습니다.");
        }
        List<UUID> requiredPaymentIds = termsService.getRequiredPaymentTermIds();
        if (!requiredPaymentIds.isEmpty()) {
            List<UUID> agreed = agreedTermIds != null ? agreedTermIds.stream().filter(Objects::nonNull).distinct().toList() : List.of();
            for (UUID requiredId : requiredPaymentIds) {
                if (!agreed.contains(requiredId)) {
                    throw new IllegalArgumentException("결제를 진행하려면 필수 약관에 모두 동의해 주세요.");
                }
            }
        }
        Payment payment = new Payment(userId, paymentMethod, amount);
        payment.setPlanId(planCode);
        payment.setStatus("pending");
        payment.setPgProvider("simulation");
        payment.setPgTransactionId(generateTransactionId());
        return paymentRepository.save(payment);
    }

    /**
     * PG 승인 처리.
     * PG 승인 후 우리 처리(Subscription, User) 중 오류 시: PG 취소 호출 → 트랜잭션 롤백 → 없던 일로.
     */
    @Transactional
    public Payment confirmPayment(UUID userId, UUID orderId, String paymentKey, Long amount) {
        Payment payment = paymentRepository.findById(Objects.requireNonNull(orderId, "orderId must not be null"))
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다."));
        if (!payment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 결제만 승인할 수 있습니다.");
        }
        if (!"pending".equals(payment.getStatus())) {
            throw new IllegalArgumentException("이미 처리된 결제입니다.");
        }
        if (!Objects.equals(payment.getAmount(), amount)) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }
        if (paymentKey == null || !payment.getPgTransactionId().equals(paymentKey)) {
            throw new IllegalArgumentException("결제 정보가 올바르지 않습니다.");
        }
        PgApprovalResponse pgResponse = pgClient.approve(new PgApprovalRequest(
                orderId, paymentKey, amount, payment.getPaymentMethod()));
        if (pgResponse.success()) {
            try {
                User user = userRepository.findById(Objects.requireNonNull(userId, "userId must not be null"))
                        .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

                String planCode = payment.getPlanId() != null ? payment.getPlanId() : "pro";
                PlanConfig plan = planConfigRepository.findByPlanCode(planCode).orElse(null);

                LocalDateTime completedAt = LocalDateTime.now();
                Subscription subscription = new Subscription(userId, planCode);
                subscription.setStatus("active");
                subscription.setStartedAt(completedAt);
                subscription.setExpiresAt(completedAt.plusMonths(SUBSCRIPTION_MONTHS));
                subscription.setPaymentId(payment.getId());
                if (plan != null && plan.getTokenLimit() != null) {
                    subscription.setTryonLimit(plan.getTokenLimit());
                }
                subscription = subscriptionRepository.save(subscription);

                payment.setStatus("completed");
                payment.setCompletedAt(completedAt);
                payment.setPgResponse(pgResponse.rawResponse());
                payment.setSubscriptionId(subscription.getId());
                paymentRepository.save(payment);

                user.setSubscription(planCode);
                userRepository.save(user);
            } catch (Exception e) {
                try {
                    pgClient.cancel(new PgCancelRequest(orderId, paymentKey, amount));
                } catch (Exception cancelEx) {
                    log.error("PG 취소 실패. orderId={}, paymentKey={} - 수동 처리 필요", orderId, paymentKey, cancelEx);
                }
                throw e;
            }
        } else {
            payment.setStatus("failed");
            payment.setPgResponse(pgResponse.rawResponse());
            paymentRepository.save(payment);
        }
        return payment;
    }

    /** 결제 강제 성공 처리 (테스트/관리용). Subscription 미존재 시 생성 */
    @Transactional
    public Payment forceSuccess(UUID paymentId) {
        Payment payment = paymentRepository.findById(Objects.requireNonNull(paymentId, "paymentId must not be null"))
                .orElseThrow(() -> new RuntimeException("결제를 찾을 수 없습니다."));
        payment.setStatus("completed");
        LocalDateTime completedAt = LocalDateTime.now();
        payment.setCompletedAt(completedAt);
        payment.setPgResponse(String.format(
                "{\"status\":\"success\",\"message\":\"결제가 강제로 성공 처리되었습니다.\",\"transaction_id\":\"%s\"}",
                payment.getPgTransactionId()));

        if (payment.getSubscriptionId() == null) {
            UUID userId = Objects.requireNonNull(payment.getUserId(), "payment.userId must not be null");
            String planCode = payment.getPlanId() != null ? payment.getPlanId() : "pro";
            PlanConfig plan = planConfigRepository.findByPlanCode(planCode).orElse(null);

            Subscription subscription = new Subscription(userId, planCode);
            subscription.setStatus("active");
            subscription.setStartedAt(completedAt);
            subscription.setExpiresAt(completedAt.plusMonths(SUBSCRIPTION_MONTHS));
            subscription.setPaymentId(Objects.requireNonNull(payment.getId(), "payment.id must not be null"));
            if (plan != null && plan.getTokenLimit() != null) {
                subscription.setTryonLimit(plan.getTokenLimit());
            }
            subscription = subscriptionRepository.save(subscription);
            payment.setSubscriptionId(subscription.getId());

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("결제에 연결된 사용자를 찾을 수 없습니다: " + userId));
            user.setSubscription(planCode);
            userRepository.save(user);
        }
        return paymentRepository.save(payment);
    }

    /** 결제 강제 실패 처리 (테스트/관리용) */
    @Transactional
    public Payment forceFailure(UUID paymentId) {
        Payment payment = paymentRepository.findById(Objects.requireNonNull(paymentId, "paymentId must not be null"))
                .orElseThrow(() -> new RuntimeException("결제를 찾을 수 없습니다."));
        payment.setStatus("failed");
        payment.setPgResponse(String.format(
                "{\"status\":\"failed\",\"message\":\"결제가 강제로 실패 처리되었습니다.\",\"transaction_id\":\"%s\"}",
                payment.getPgTransactionId()));
        return paymentRepository.save(payment);
    }

    /** 사용자별 결제 이력 조회 (최신순) */
    public List<Payment> getUserPayments(UUID userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(Objects.requireNonNull(userId, "userId must not be null"));
    }

    /** 결제 단건 조회 */
    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(Objects.requireNonNull(paymentId, "paymentId must not be null"))
                .orElseThrow(() -> new RuntimeException("결제를 찾을 수 없습니다."));
    }

    /** 거래번호 생성 규칙. PG 연동 시 별도 규칙으로 교체 가능 */
    private static String generateTransactionId() {
        return "SIM_" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
