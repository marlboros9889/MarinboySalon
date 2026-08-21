package com.marinboy.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 관리자가 정한 요일별 영업 여부와 시작·종료 시간을 보관합니다. */
@Entity
@Table(name = "MB_BUSINESS_HOUR")
public class BusinessHourEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DAY_OF_WEEK", nullable = false, unique = true)
    private Integer dayOfWeek;
    @Column(name = "IS_OPEN", nullable = false)
    private Boolean open;
    @Column(name = "OPEN_TIME", nullable = false, length = 5)
    private String openTime;
    @Column(name = "CLOSE_TIME", nullable = false, length = 5)
    private String closeTime;
    @Column(name = "UPDATED_AT", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected BusinessHourEntity() { }

    public Long getId() { return id; }
    public Integer getDayOfWeek() { return dayOfWeek; }
    public Boolean getOpen() { return open; }
    public String getOpenTime() { return openTime; }
    public String getCloseTime() { return closeTime; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
