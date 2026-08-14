package com.marinboy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 시술 메뉴 기본 정보와 연결된 이미지 정보를 전달하는 DTO입니다. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceDto {
    // 시술 메뉴의 기본 키입니다.
    private Long id;
    // 이미지 조회 결과가 어느 시술에 속하는지 나타내는 외래 키입니다.
    private Long serviceId;
    // 고객 화면에 표시할 시술명과 분류입니다.
    private String name;
    private String category;
    // 예약 가능 시간 계산에 사용하는 시술 소요 시간(분)입니다.
    private Integer durationMinutes;
    // 고객에게 표시할 시술 가격입니다.
    private BigDecimal price;
    // 시술에 대한 상세 설명입니다.
    private String description;
    // 메인 화면 추천 메뉴의 노출 우선순위입니다.
    private Integer topRank;
    // 이달의 TOP5 헤어 순위를 계산할 때 사용하는 이번 달 유효 예약 건수입니다.
    private Integer reservationCount;
    // 대표 이미지의 웹 접근 경로와 이미지 용도입니다.
    private String imageUrl;
    private String imageType;
    // 상세 화면에 추가로 표시할 이미지 경로 목록입니다.
    private List<String> additionalImageUrls = new ArrayList<>();

    // MyBatis 매핑과 JSON 변환에 사용하는 getter/setter입니다.
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getTopRank() { return topRank; }
    public void setTopRank(Integer topRank) { this.topRank = topRank; }
    public Integer getReservationCount() { return reservationCount; }
    public void setReservationCount(Integer reservationCount) { this.reservationCount = reservationCount; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }
    public List<String> getAdditionalImageUrls() { return additionalImageUrls; }
    public void setAdditionalImageUrls(List<String> additionalImageUrls) { this.additionalImageUrls = additionalImageUrls; }
}
