package com.virtualtryon.admin.dto.dashboard;

import java.util.List;

/** 관리자 대시보드 매출 통계 DTO */
public class AdminDashboardDto {

    private Long totalRevenue;
    private Long previousTotalRevenue;
    private String period; // week, month, quarter, half
    private List<RevenueByMonth> revenueByMonth;
    private List<RevenueByPlan> revenueByPlan;
    /** 기간별 시계열 (막대: 당기, 선: 전기) */
    private List<RevenueTimeSeriesItem> timeSeries;

    public AdminDashboardDto() {}

    public Long getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Long totalRevenue) { this.totalRevenue = totalRevenue; }
    public Long getPreviousTotalRevenue() { return previousTotalRevenue; }
    public void setPreviousTotalRevenue(Long previousTotalRevenue) { this.previousTotalRevenue = previousTotalRevenue; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public List<RevenueByMonth> getRevenueByMonth() { return revenueByMonth; }
    public void setRevenueByMonth(List<RevenueByMonth> revenueByMonth) { this.revenueByMonth = revenueByMonth; }
    public List<RevenueByPlan> getRevenueByPlan() { return revenueByPlan; }
    public void setRevenueByPlan(List<RevenueByPlan> revenueByPlan) { this.revenueByPlan = revenueByPlan; }
    public List<RevenueTimeSeriesItem> getTimeSeries() { return timeSeries; }
    public void setTimeSeries(List<RevenueTimeSeriesItem> timeSeries) { this.timeSeries = timeSeries; }

    /** 기간별 매출 (당기 vs 전기) */
    public static class RevenueTimeSeriesItem {
        private String periodKey;
        private String label;
        private Long revenue;
        private Long previousRevenue;
        private java.util.List<PlanRevenueInPeriod> byPlan;

        public RevenueTimeSeriesItem() {}
        public RevenueTimeSeriesItem(String periodKey, String label, Long revenue, Long previousRevenue) {
            this.periodKey = periodKey;
            this.label = label;
            this.revenue = revenue;
            this.previousRevenue = previousRevenue;
        }
        public String getPeriodKey() { return periodKey; }
        public void setPeriodKey(String periodKey) { this.periodKey = periodKey; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public Long getRevenue() { return revenue; }
        public void setRevenue(Long revenue) { this.revenue = revenue; }
        public Long getPreviousRevenue() { return previousRevenue; }
        public void setPreviousRevenue(Long previousRevenue) { this.previousRevenue = previousRevenue; }
        public java.util.List<PlanRevenueInPeriod> getByPlan() { return byPlan; }
        public void setByPlan(java.util.List<PlanRevenueInPeriod> byPlan) { this.byPlan = byPlan; }
    }

    /** 기간 내 플랜별 매출 */
    public static class PlanRevenueInPeriod {
        private String planId;
        private Long revenue;

        public PlanRevenueInPeriod() {}
        public PlanRevenueInPeriod(String planId, Long revenue) {
            this.planId = planId;
            this.revenue = revenue;
        }
        public String getPlanId() { return planId; }
        public void setPlanId(String planId) { this.planId = planId; }
        public Long getRevenue() { return revenue; }
        public void setRevenue(Long revenue) { this.revenue = revenue; }
    }

    public static class RevenueByMonth {
        private int year;
        private int month;
        private Long revenue;

        public RevenueByMonth() {}
        public RevenueByMonth(int year, int month, Long revenue) {
            this.year = year;
            this.month = month;
            this.revenue = revenue;
        }
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        public int getMonth() { return month; }
        public void setMonth(int month) { this.month = month; }
        public Long getRevenue() { return revenue; }
        public void setRevenue(Long revenue) { this.revenue = revenue; }
    }

    public static class RevenueByPlan {
        private String planId;
        private Long revenue;
        private Long count;

        public RevenueByPlan() {}
        public RevenueByPlan(String planId, Long revenue, Long count) {
            this.planId = planId;
            this.revenue = revenue;
            this.count = count;
        }
        public String getPlanId() { return planId; }
        public void setPlanId(String planId) { this.planId = planId; }
        public Long getRevenue() { return revenue; }
        public void setRevenue(Long revenue) { this.revenue = revenue; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }
}
