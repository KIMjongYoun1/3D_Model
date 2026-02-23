package com.virtualtryon.core.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * DART 공시대상 회사 고유번호 (corpCode.json 수집)
 * list.json 검색 시 corp_code 사용 시 3개월 제한 없음
 */
@Entity
@Table(name = "dart_corp_code")
public class DartCorpCode {

    @Id
    @Column(name = "corp_code", length = 8)
    private String corpCode;

    @Column(name = "corp_name")
    private String corpName;

    @Column(name = "corp_name_eng")
    private String corpNameEng;

    @Column(name = "stock_code", length = 6)
    private String stockCode;

    @Column(name = "modify_date", length = 8)
    private String modifyDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public DartCorpCode() {}

    public String getCorpCode() { return corpCode; }
    public void setCorpCode(String corpCode) { this.corpCode = corpCode; }

    public String getCorpName() { return corpName; }
    public void setCorpName(String corpName) { this.corpName = corpName; }

    public String getCorpNameEng() { return corpNameEng; }
    public void setCorpNameEng(String corpNameEng) { this.corpNameEng = corpNameEng; }

    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }

    public String getModifyDate() { return modifyDate; }
    public void setModifyDate(String modifyDate) { this.modifyDate = modifyDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
