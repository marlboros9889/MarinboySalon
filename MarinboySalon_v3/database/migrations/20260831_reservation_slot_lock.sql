USE marinboy_salon;

-- 예약 생성 중 같은 날짜·30분 슬롯만 잠그기 위한 보조 테이블입니다.
-- 잠금 행은 재사용하므로 요청이 늘어도 요일 전체를 직렬화하지 않습니다.
CREATE TABLE IF NOT EXISTS reservation_slot_lock (
    reservation_date DATE NOT NULL,
    slot_time TIME NOT NULL,
    locked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (reservation_date, slot_time)
);
