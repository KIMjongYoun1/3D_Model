package com.virtualtryon.admin.service;

import com.virtualtryon.admin.dto.dashboard.AdminDashboardDto;
import com.virtualtryon.core.entity.Payment;
import com.virtualtryon.core.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/** 관리자 대시보드 서비스 */
@Service
public class AdminDashboardService {

    private final PaymentRepository paymentRepository;

    public AdminDashboardService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /** 매출 통계 조회 (period: week, month, quarter, half) */
    public AdminDashboardDto getRevenueStats(String period) {
        String p = (period != null && !period.isBlank()) ? period.toLowerCase() : "month";
        if (!Set.of("week", "month", "quarter", "half").contains(p)) {
            p = "month";
        }

        AdminDashboardDto dto = new AdminDashboardDto();
        dto.setPeriod(p);

        List<Payment> completed = paymentRepository.findAllCompletedWithCompletedAt();
        Long total = completed.stream().mapToLong(Payment::getAmount).sum();
        dto.setTotalRevenue(total != null ? total : 0L);

        // 기존 월별/플랜별 (호환)
        Map<String, Long> byMonth = completed.stream()
                .collect(Collectors.groupingBy(
                        pay -> pay.getCompletedAt().getYear() + "-" + String.format("%02d", pay.getCompletedAt().getMonthValue()),
                        Collectors.summingLong(Payment::getAmount)
                ));

        List<AdminDashboardDto.RevenueByMonth> revenueByMonth = byMonth.entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split("-");
                    return new AdminDashboardDto.RevenueByMonth(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            e.getValue()
                    );
                })
                .sorted((a, b) -> {
                    int c = Integer.compare(b.getYear(), a.getYear());
                    return c != 0 ? c : Integer.compare(b.getMonth(), a.getMonth());
                })
                .limit(12)
                .toList();
        dto.setRevenueByMonth(revenueByMonth);

        Map<String, List<Payment>> byPlan = completed.stream()
                .collect(Collectors.groupingBy(pay -> pay.getPlanId() != null ? pay.getPlanId() : "unknown"));

        List<AdminDashboardDto.RevenueByPlan> revenueByPlan = byPlan.entrySet().stream()
                .map(e -> new AdminDashboardDto.RevenueByPlan(
                        e.getKey(),
                        e.getValue().stream().mapToLong(Payment::getAmount).sum(),
                        (long) e.getValue().size()
                ))
                .sorted((a, b) -> Long.compare(b.getRevenue(), a.getRevenue()))
                .toList();
        dto.setRevenueByPlan(revenueByPlan);

        // 시계열 (당기 vs 전기) + 구독별
        List<AdminDashboardDto.RevenueTimeSeriesItem> timeSeries = buildTimeSeries(completed, p);
        dto.setTimeSeries(timeSeries);

        long prevTotal = timeSeries.stream()
                .mapToLong(i -> i.getPreviousRevenue() != null ? i.getPreviousRevenue() : 0L)
                .sum();
        dto.setPreviousTotalRevenue(prevTotal);

        return dto;
    }

    private List<AdminDashboardDto.RevenueTimeSeriesItem> buildTimeSeries(List<Payment> completed, String period) {
        Map<String, Long> revenueByKey = new HashMap<>();
        Map<String, Map<String, Long>> byPlanByKey = new HashMap<>();

        for (Payment p : completed) {
            LocalDateTime at = Objects.requireNonNull(p.getCompletedAt(), "Payment completedAt must not be null");
            Long amount = Objects.requireNonNull(p.getAmount(), "Payment amount must not be null");
            String planId = p.getPlanId() != null ? p.getPlanId() : "unknown";

            String key = toPeriodKey(at, period);
            BiFunction<Long, Long, Long> safeSum = (a, b) -> (a != null ? a : 0L) + (b != null ? b : 0L);
            revenueByKey.merge(key, amount, safeSum);
            byPlanByKey
                    .computeIfAbsent(key, k -> new HashMap<>())
                    .merge(planId, amount, safeSum);
        }

        List<String> orderedKeys = getOrderedPeriodKeys(period);
        List<AdminDashboardDto.RevenueTimeSeriesItem> result = new ArrayList<>();

        for (int i = 0; i < orderedKeys.size(); i++) {
            String key = orderedKeys.get(i);
            String prevKey = i > 0 ? orderedKeys.get(i - 1) : null;
            Long rev = revenueByKey.getOrDefault(key, 0L);
            Long prevRev = prevKey != null ? revenueByKey.getOrDefault(prevKey, 0L) : 0L;

            List<AdminDashboardDto.PlanRevenueInPeriod> byPlan = new ArrayList<>();
            Map<String, Long> planMap = byPlanByKey.get(key);
            if (planMap != null) {
                planMap.forEach((planId, amount) ->
                        byPlan.add(new AdminDashboardDto.PlanRevenueInPeriod(planId, amount)));
                byPlan.sort((a, b) -> Long.compare(b.getRevenue(), a.getRevenue()));
            }

            AdminDashboardDto.RevenueTimeSeriesItem item = new AdminDashboardDto.RevenueTimeSeriesItem(
                    key, toPeriodLabel(key, period), rev, prevRev
            );
            item.setByPlan(byPlan);
            result.add(item);
        }

        return result;
    }

    private String toPeriodKey(LocalDateTime at, String period) {
        int y = at.getYear();
        switch (period) {
            case "week":
                int w = at.get(WeekFields.ISO.weekOfYear());
                return y + "-W" + String.format("%02d", w);
            case "quarter":
                int q = at.get(IsoFields.QUARTER_OF_YEAR);
                return y + "-Q" + q;
            case "half":
                int h = at.getMonthValue() <= 6 ? 1 : 2;
                return y + "-H" + h;
            default: // month
                return y + "-" + String.format("%02d", at.getMonthValue());
        }
    }

    private String toPeriodLabel(String key, String period) {
        if (key == null) return "";
        String[] parts = key.split("-");
        if (parts.length < 2) return key;
        int y = Integer.parseInt(parts[0]);
        switch (period) {
            case "week":
                return y + "년 " + parts[1];
            case "quarter":
                return y + "년 Q" + parts[1].replace("Q", "");
            case "half":
                return y + "년 " + ("1".equals(parts[1].replace("H", "")) ? "상반기" : "하반기");
            default:
                int m = Integer.parseInt(parts[1]);
                return y + "년 " + m + "월";
        }
    }

    private List<String> getOrderedPeriodKeys(String period) {
        LocalDateTime now = LocalDateTime.now();
        List<String> keys = new ArrayList<>();
        int count = period.equals("week") ? 12 : (period.equals("half") ? 4 : 6);

        for (int i = 0; i < count; i++) {
            keys.add(toPeriodKey(now, period));
            now = prevPeriod(now, period);
        }
        Collections.reverse(keys); // 오래된 순 (차트 왼쪽→오른쪽)
        return keys;
    }

    private LocalDateTime prevPeriod(LocalDateTime at, String period) {
        switch (period) {
            case "week":
                return at.minusWeeks(1);
            case "quarter":
                return at.minusMonths(3);
            case "half":
                return at.minusMonths(6);
            default:
                return at.minusMonths(1);
        }
    }
}
