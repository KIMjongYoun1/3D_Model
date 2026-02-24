package com.virtualtryon.core.dto.plan;

import com.virtualtryon.core.entity.PlanConfig;

import java.util.List;

/**
 * 플랜/요금제 API 응답 DTO
 */
public class PlanResponse {

    private String id;
    private String planCode;
    private String planName;
    private Long priceMonthly;
    private Integer tokenLimit;
    private String description;
    private List<String> features;
    private Boolean isActive;
    private Integer sortOrder;

    public PlanResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public Long getPriceMonthly() { return priceMonthly; }
    public void setPriceMonthly(Long priceMonthly) { this.priceMonthly = priceMonthly; }

    public Integer getTokenLimit() { return tokenLimit; }
    public void setTokenLimit(Integer tokenLimit) { this.tokenLimit = tokenLimit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public static PlanResponse from(PlanConfig config) {
        PlanResponse r = new PlanResponse();
        r.setId(config.getId() != null ? config.getId().toString() : null);
        r.setPlanCode(config.getPlanCode());
        r.setPlanName(config.getPlanName());
        r.setPriceMonthly(config.getPriceMonthly());
        r.setTokenLimit(config.getTokenLimit());
        r.setDescription(config.getDescription());
        r.setFeatures(config.getFeatures());
        r.setIsActive(config.getIsActive());
        r.setSortOrder(config.getSortOrder());
        return r;
    }
}
