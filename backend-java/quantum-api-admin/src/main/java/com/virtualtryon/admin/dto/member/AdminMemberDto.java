package com.virtualtryon.admin.dto.member;

import java.time.LocalDateTime;
import java.util.UUID;

/** 관리자 회원 목록/상세 DTO */
public class AdminMemberDto {

    private UUID id;
    private String email;
    private String name;
    private String provider;
    private String subscription;
    private LocalDateTime createdAt;
    private LocalDateTime suspendedAt;
    private LocalDateTime deletedAt;
    private Integer paymentCount;
    private Integer subscriptionCount;

    public AdminMemberDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getSubscription() { return subscription; }
    public void setSubscription(String subscription) { this.subscription = subscription; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getSuspendedAt() { return suspendedAt; }
    public void setSuspendedAt(LocalDateTime suspendedAt) { this.suspendedAt = suspendedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public Integer getPaymentCount() { return paymentCount; }
    public void setPaymentCount(Integer paymentCount) { this.paymentCount = paymentCount; }
    public Integer getSubscriptionCount() { return subscriptionCount; }
    public void setSubscriptionCount(Integer subscriptionCount) { this.subscriptionCount = subscriptionCount; }
}
