package com.virtualtryon.core.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DART 공시 목록(list.json) 응답 형식
 */
@Entity
@Table(name = "knowledge_dart")
public class KnowledgeDart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "corp_code", nullable = false, length = 8)
    private String corpCode;

    @Column(name = "corp_name")
    private String corpName;

    @Column(name = "rcept_no", nullable = false, unique = true, length = 20)
    private String rceptNo;

    @Column(name = "rcept_dt", length = 8)
    private String rceptDt;

    @Column(name = "report_nm")
    private String reportNm;

    @Column(name = "flr_nm", length = 100)
    private String flrNm;

    @Column(name = "rm", length = 500)
    private String rm;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCorpCode() { return corpCode; }
    public void setCorpCode(String corpCode) { this.corpCode = corpCode; }

    public String getCorpName() { return corpName; }
    public void setCorpName(String corpName) { this.corpName = corpName; }

    public String getRceptNo() { return rceptNo; }
    public void setRceptNo(String rceptNo) { this.rceptNo = rceptNo; }

    public String getRceptDt() { return rceptDt; }
    public void setRceptDt(String rceptDt) { this.rceptDt = rceptDt; }

    public String getReportNm() { return reportNm; }
    public void setReportNm(String reportNm) { this.reportNm = reportNm; }

    public String getFlrNm() { return flrNm; }
    public void setFlrNm(String flrNm) { this.flrNm = flrNm; }

    public String getRm() { return rm; }
    public void setRm(String rm) { this.rm = rm; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
