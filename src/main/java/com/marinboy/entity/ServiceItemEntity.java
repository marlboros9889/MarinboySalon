package com.marinboy.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** MB_SERVICE_ITEM 테이블을 JPA 도메인 모델로 연결합니다. */
@Entity
@Table(name = "MB_SERVICE_ITEM")
public class ServiceItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;
    @Column(name = "CATEGORY", nullable = false, length = 30)
    private String category;
    @Column(name = "DURATION_MINUTES", nullable = false)
    private Integer durationMinutes;
    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @Column(name = "DESCRIPTION", nullable = false, length = 255)
    private String description;
    @Column(name = "TOP_RANK")
    private Integer topRank;
    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "UPDATED_AT", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected ServiceItemEntity() { }

    /** 서비스 계층에서 검증한 값으로 생성하거나 변경합니다. */
    public ServiceItemEntity(String name, String category, Integer durationMinutes, BigDecimal price, String description, Integer topRank) {
        change(name, category, durationMinutes, price, description, topRank);
    }

    public void change(String name, String category, Integer durationMinutes, BigDecimal price, String description, Integer topRank) {
        this.name = name;
        this.category = category;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.description = description;
        this.topRank = topRank;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public BigDecimal getPrice() { return price; }
    public String getDescription() { return description; }
    public Integer getTopRank() { return topRank; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
