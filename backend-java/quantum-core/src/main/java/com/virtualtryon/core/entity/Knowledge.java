package com.virtualtryon.core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 지식 베이스 엔티티
 * 
 * 역할:
 * - AI 에이전트가 참고할 "진짜 지식" (법령, 가이드라인 등) 저장
 * - Java Backend에서 데이터 관리 및 API 연동 담당
 */
@Entity
@Table(name = "knowledge_base")
public class Knowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "source_type", length = 50)
    private String sourceType = "manual";

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Knowledge() {
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

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder Pattern
    public static KnowledgeBuilder builder() {
        return new KnowledgeBuilder();
    }

    public static class KnowledgeBuilder {
        private String category;
        private String title;
        private String content;
        private String sourceUrl;
        private String sourceType = "manual";
        private boolean isActive = true;

        public KnowledgeBuilder category(String category) { this.category = category; return this; }
        public KnowledgeBuilder title(String title) { this.title = title; return this; }
        public KnowledgeBuilder content(String content) { this.content = content; return this; }
        public KnowledgeBuilder sourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; return this; }
        public KnowledgeBuilder sourceType(String sourceType) { this.sourceType = sourceType; return this; }
        public KnowledgeBuilder isActive(boolean isActive) { this.isActive = isActive; return this; }

        public Knowledge build() {
            Knowledge knowledge = new Knowledge();
            knowledge.setCategory(category);
            knowledge.setTitle(title);
            knowledge.setContent(content);
            knowledge.setSourceUrl(sourceUrl);
            knowledge.setSourceType(sourceType);
            knowledge.setActive(isActive);
            return knowledge;
        }
    }
}
