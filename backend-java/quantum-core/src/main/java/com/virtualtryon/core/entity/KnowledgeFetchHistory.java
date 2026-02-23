package com.virtualtryon.core.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 지식 수집(API 호출) 히스토리
 *
 * "언제 무엇을 불러왔는지"만 저장. 실제 지식 내용은 knowledge_base에 항목별·카테고리별로 저장.
 */
@Entity
@Table(name = "knowledge_fetch_history")
public class KnowledgeFetchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source_type", nullable = false, length = 50)
    private String sourceType;

    @Column(nullable = false, length = 20)
    private String status = "RUNNING";

    @Column(name = "item_count")
    private Integer itemCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "params_json", columnDefinition = "TEXT")
    private String paramsJson;

    @CreationTimestamp
    @Column(name = "fetched_at", nullable = false, updatable = false)
    private LocalDateTime fetchedAt;

    public KnowledgeFetchHistory() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getParamsJson() { return paramsJson; }
    public void setParamsJson(String paramsJson) { this.paramsJson = paramsJson; }

    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }
}
