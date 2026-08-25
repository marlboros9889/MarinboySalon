USE marinboy_salon;

-- 화면과 예약 기능을 바로 확인할 수 있는 기본 시술 항목입니다.
INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '디자인 컷', 50000, 60, '두상과 모질을 고려한 1:1 디자인 컷', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '디자인 컷');

INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '전체 염색', 150000, 120, '톤 상담을 포함한 전체 컬러 시술', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '전체 염색');

INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '디자인 펌', 180000, 150, '손질 방법까지 안내하는 맞춤 펌', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '디자인 펌');

-- 월요일부터 토요일은 영업하고 일요일은 쉽니다.
INSERT INTO business_hour (day_of_week, open_time, close_time, closed) VALUES
(1, '10:00:00', '19:00:00', 0),
(2, '10:00:00', '19:00:00', 0),
(3, '10:00:00', '19:00:00', 0),
(4, '10:00:00', '19:00:00', 0),
(5, '10:00:00', '19:00:00', 0),
(6, '10:00:00', '17:00:00', 0),
(7, NULL, NULL, 1)
ON DUPLICATE KEY UPDATE day_of_week = VALUES(day_of_week);
