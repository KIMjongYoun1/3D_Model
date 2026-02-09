package com.virtualtryon.core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 시각화 프로젝트 엔티티
 * 
 * 역할:
 * - 사용자의 시각화 프로젝트 정보를 저장 (카테고리, 이름 등)
 * - Java Backend에서 비즈니스 라이프사이클 관리
 */
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "main_category")
    private String mainCategory;

    @Column(name = "sub_category")
    private String subCategory;

    @Column(length = 20)
    private String status = "active";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Project() {
    }

    public Project(UUID userId, String name, String mainCategory, String subCategory) {
        this.userId = userId;
        this.name = name;
        this.mainCategory = mainCategory;
        this.subCategory = subCategory;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMainCategory() { return mainCategory; }
    public void setMainCategory(String mainCategory) { this.mainCategory = mainCategory; }

    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder Pattern (Lombok 대체)
    public static ProjectBuilder builder() {
        return new ProjectBuilder();
    }

    public static class ProjectBuilder {
        private UUID userId;
        private String name;
        private String description;
        private String mainCategory;
        private String subCategory;
        private String status = "active";

        public ProjectBuilder userId(UUID userId) { this.userId = userId; return this; }
        public ProjectBuilder name(String name) { this.name = name; return this; }
        public ProjectBuilder description(String description) { this.description = description; return this; }
        public ProjectBuilder mainCategory(String mainCategory) { this.mainCategory = mainCategory; return this; }
        public ProjectBuilder subCategory(String subCategory) { this.subCategory = subCategory; return this; }
        public ProjectBuilder status(String status) { this.status = status; return this; }

        public Project build() {
            Project project = new Project();
            project.setUserId(userId);
            project.setName(name);
            project.setDescription(description);
            project.setMainCategory(mainCategory);
            project.setSubCategory(subCategory);
            project.setStatus(status);
            return project;
        }
    }
}
