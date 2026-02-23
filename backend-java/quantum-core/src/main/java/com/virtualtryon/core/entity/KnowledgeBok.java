package com.virtualtryon.core.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 한국은행 ECOS StatisticSearch 응답 형식
 */
@Entity
@Table(name = "knowledge_bok")
public class KnowledgeBok {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "stat_code", nullable = false, length = 20)
    private String statCode;

    @Column(name = "stat_name")
    private String statName;

    @Column(name = "item_code1", length = 20)
    private String itemCode1;

    @Column(name = "item_name1")
    private String itemName1;

    @Column(name = "item_code2", length = 20)
    private String itemCode2;

    @Column(name = "item_name2")
    private String itemName2;

    @Column(name = "time", nullable = false, length = 8)
    private String time;

    @Column(name = "data_value", nullable = false, length = 50)
    private String dataValue;

    @Column(name = "unit_name", length = 20)
    private String unitName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getStatCode() { return statCode; }
    public void setStatCode(String statCode) { this.statCode = statCode; }

    public String getStatName() { return statName; }
    public void setStatName(String statName) { this.statName = statName; }

    public String getItemCode1() { return itemCode1; }
    public void setItemCode1(String itemCode1) { this.itemCode1 = itemCode1; }

    public String getItemName1() { return itemName1; }
    public void setItemName1(String itemName1) { this.itemName1 = itemName1; }

    public String getItemCode2() { return itemCode2; }
    public void setItemCode2(String itemCode2) { this.itemCode2 = itemCode2; }

    public String getItemName2() { return itemName2; }
    public void setItemName2(String itemName2) { this.itemName2 = itemName2; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDataValue() { return dataValue; }
    public void setDataValue(String dataValue) { this.dataValue = dataValue; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
