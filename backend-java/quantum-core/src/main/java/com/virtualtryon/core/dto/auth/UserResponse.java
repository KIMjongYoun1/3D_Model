package com.virtualtryon.core.dto.auth;

import java.time.LocalDateTime;
import java.util.UUID;

/** 사용자 응답 DTO */
public class UserResponse {

    private UUID id;
    private String email;
    private String name;
    private String profileImage;
    private String subscription;
    private String subscriptionPlanName;
    private LocalDateTime subscriptionExpiresAt;
    /** 구독 상태: active(정상), cancelled(해지신청됨), null(무료/없음) */
    private String subscriptionStatus;
    private LocalDateTime createdAt;
    private String provider;

    public UserResponse() {}

    public UserResponse(UUID id, String email, String name, String profileImage, String subscription,
                        String subscriptionPlanName, LocalDateTime subscriptionExpiresAt,
                        String subscriptionStatus, LocalDateTime createdAt, String provider) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
        this.subscription = subscription;
        this.subscriptionPlanName = subscriptionPlanName;
        this.subscriptionExpiresAt = subscriptionExpiresAt;
        this.subscriptionStatus = subscriptionStatus;
        this.createdAt = createdAt;
        this.provider = provider;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public String getSubscription() { return subscription; }
    public void setSubscription(String subscription) { this.subscription = subscription; }
    public String getSubscriptionPlanName() { return subscriptionPlanName; }
    public void setSubscriptionPlanName(String subscriptionPlanName) { this.subscriptionPlanName = subscriptionPlanName; }
    public LocalDateTime getSubscriptionExpiresAt() { return subscriptionExpiresAt; }
    public void setSubscriptionExpiresAt(LocalDateTime subscriptionExpiresAt) { this.subscriptionExpiresAt = subscriptionExpiresAt; }
    public String getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public static UserResponseBuilder builder() {
        return new UserResponseBuilder();
    }

    public static class UserResponseBuilder {
        private UUID id;
        private String email;
        private String name;
        private String profileImage;
        private String subscription;
        private String subscriptionPlanName;
        private LocalDateTime subscriptionExpiresAt;
        private String subscriptionStatus;
        private LocalDateTime createdAt;
        private String provider;

        public UserResponseBuilder id(UUID id) { this.id = id; return this; }
        public UserResponseBuilder email(String email) { this.email = email; return this; }
        public UserResponseBuilder name(String name) { this.name = name; return this; }
        public UserResponseBuilder profileImage(String profileImage) { this.profileImage = profileImage; return this; }
        public UserResponseBuilder subscription(String subscription) { this.subscription = subscription; return this; }
        public UserResponseBuilder subscriptionPlanName(String subscriptionPlanName) { this.subscriptionPlanName = subscriptionPlanName; return this; }
        public UserResponseBuilder subscriptionExpiresAt(LocalDateTime subscriptionExpiresAt) { this.subscriptionExpiresAt = subscriptionExpiresAt; return this; }
        public UserResponseBuilder subscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; return this; }
        public UserResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public UserResponseBuilder provider(String provider) { this.provider = provider; return this; }

        public UserResponse build() {
            return new UserResponse(id, email, name, profileImage, subscription, subscriptionPlanName, subscriptionExpiresAt, subscriptionStatus, createdAt, provider);
        }
    }
}
