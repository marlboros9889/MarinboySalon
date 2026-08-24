package com.marinboy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.ibatis.type.LocalDateTimeTypeHandler;
import java.time.LocalDateTime;
import java.util.List;

/** 예약 신청, 예약 조회, 예약 가능 시간 응답에 공통으로 사용하는 DTO입니다. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationDto {
    // 저장된 예약의 기본 키와 선택한 시술 메뉴 ID입니다.
    private Long id;
    private Long serviceId;
    // 예약 목록에 함께 표시할 시술명과 카테고리입니다.
    private String serviceName;
    private String serviceCategory;
    // 예약 시간 중복 계산에 사용하는 시술 소요 시간입니다.
    private Integer durationMinutes;
    // 예약자 확인과 연락에 사용하는 고객 정보입니다.
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    // 고객이 선택한 실제 예약 일시입니다.
    private LocalDateTime reservationDateTime;
    // 노쇼·취소 정책 동의 여부입니다.
    private Boolean noShowPolicyAgreed;
    // REQUESTED, CONFIRMED 등 현재 예약 처리 상태입니다.
    private String status;
    // 고객이 입력한 요청 사항입니다.
    private String memo;
    // 특정 날짜에 고객이 선택할 수 있는 예약 시간 목록입니다.
    private List<LocalDateTime> availableSlots;

    // MyBatis 매핑과 JSON 변환에 사용하는 getter/setter입니다.
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getServiceCategory() { return serviceCategory; }
    public void setServiceCategory(String serviceCategory) { this.serviceCategory = serviceCategory; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public LocalDateTime getReservationDateTime() { return reservationDateTime; }
    public void setReservationDateTime(LocalDateTime reservationDateTime) { this.reservationDateTime = reservationDateTime; }
    public Boolean getNoShowPolicyAgreed() { return noShowPolicyAgreed; }
    public void setNoShowPolicyAgreed(Boolean noShowPolicyAgreed) { this.noShowPolicyAgreed = noShowPolicyAgreed; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public List<LocalDateTime> getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(List<LocalDateTime> availableSlots) { this.availableSlots = availableSlots; }
}
