package com.virtualtryon.core.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 국가법령정보센터 lawSearch 응답 형식
 */
@Entity
@Table(name = "knowledge_law")
public class KnowledgeLaw {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "mst", nullable = false, unique = true, length = 20)
    private String mst;

    @Column(name = "law_name_ko", nullable = false)
    private String lawNameKo;

    @Column(name = "law_type", length = 50)
    private String lawType;

    @Column(name = "dept_name", length = 100)
    private String deptName;

    @Column(name = "proclamation_no", length = 30)
    private String proclamationNo;

    @Column(name = "proclamation_date", length = 8)
    private String proclamationDate;

    @Column(name = "enforce_date", length = 8)
    private String enforceDate;

    @Column(name = "law_id", length = 20)
    private String lawId;

    @Column(columnDefinition = "TEXT")
    private String content;

    /** 법령 조문 본문 (content는 요약, article_body는 상세 조문) */
    @Column(name = "article_body", columnDefinition = "TEXT")
    private String articleBody;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getMst() { return mst; }
    public void setMst(String mst) { this.mst = mst; }

    public String getLawNameKo() { return lawNameKo; }
    public void setLawNameKo(String lawNameKo) { this.lawNameKo = lawNameKo; }

    public String getLawType() { return lawType; }
    public void setLawType(String lawType) { this.lawType = lawType; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public String getProclamationNo() { return proclamationNo; }
    public void setProclamationNo(String proclamationNo) { this.proclamationNo = proclamationNo; }

    public String getProclamationDate() { return proclamationDate; }
    public void setProclamationDate(String proclamationDate) { this.proclamationDate = proclamationDate; }

    public String getEnforceDate() { return enforceDate; }
    public void setEnforceDate(String enforceDate) { this.enforceDate = enforceDate; }

    public String getLawId() { return lawId; }
    public void setLawId(String lawId) { this.lawId = lawId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getArticleBody() { return articleBody; }
    public void setArticleBody(String articleBody) { this.articleBody = articleBody; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
