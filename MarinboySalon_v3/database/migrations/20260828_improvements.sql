-- 2026-08-28 개선: 예약 조회 성능
USE marinboy_salon;

CREATE INDEX idx_reservation_status_start
    ON reservation (status, reservation_start);
