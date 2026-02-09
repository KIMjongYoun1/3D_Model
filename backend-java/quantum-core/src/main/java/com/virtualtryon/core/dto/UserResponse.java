package com.virtualtryon.core.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 응답 DTO
 * 
 * ⚠️ Lombok 제거 버전
 */
public class UserResponse {
    
    private UUID id;
    private String email;
    private String name;
    private String profileImage;
    private String subscription;
    private LocalDateTime createdAt;

    public UserResponse() {}

    public UserResponse(UUID id, String email, String name, String profileImage, String subscription, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
        this.subscription = subscription;
        this.createdAt = createdAt;
    }

    // Getter & Setter
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder 패턴 시뮬레이션
    public static class UserResponseBuilder {
        private UUID id;
        private String email;
        private String name;
        private String profileImage;
        private String subscription;
        private LocalDateTime createdAt;

        public UserResponseBuilder id(UUID id) { this.id = id; return this; }
        public UserResponseBuilder email(String email) { this.email = email; return this; }
        public UserResponseBuilder name(String name) { this.name = name; return this; }
        public UserResponseBuilder profileImage(String profileImage) { this.profileImage = profileImage; return this; }
        public UserResponseBuilder subscription(String subscription) { this.subscription = subscription; return this; }
        public UserResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public UserResponse build() {
            return new UserResponse(id, email, name, profileImage, subscription, createdAt);
        }
    }

    public static UserResponseBuilder builder() {
        return new UserResponseBuilder();
    }
}
