package com.virtualtryon.core.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AdminUser 엔티티 (관리자)
 * 
 * 일반 사용자(users)와 완전히 분리된 관리자 전용 테이블
 * Admin WAS(:8081) 인증에 사용
 * 
 * ⚠️ Lombok 제거 버전: Getter, Setter, Constructor 직접 구현
 */
@Entity
@Table(name = "admin_users")
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 30)
    private String role = "ADMIN";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // 기본 생성자
    public AdminUser() {}

    // 전체 생성자
    public AdminUser(String email, String name, String role) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.isActive = true;
    }

    // Getter & Setter
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    // Builder 패턴
    public static class AdminUserBuilder {
        private String email;
        private String name;
        private String role = "ADMIN";

        public AdminUserBuilder email(String email) { this.email = email; return this; }
        public AdminUserBuilder name(String name) { this.name = name; return this; }
        public AdminUserBuilder role(String role) { this.role = role; return this; }

        public AdminUser build() {
            return new AdminUser(email, name, role);
        }
    }

    public static AdminUserBuilder builder() {
        return new AdminUserBuilder();
    }
}
