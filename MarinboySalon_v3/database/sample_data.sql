USE marinboy_salon;

-- 제공된 스타일 이미지와 연결되는 기본 메뉴입니다.
INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '두피 클리닉', 90000, 60, '두피 상태를 확인하고 진정과 보습을 돕는 맞춤 케어', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '두피 클리닉');

INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '롱 레이어드 컷', 70000, 90, '얼굴선을 부드럽게 감싸는 긴 기장 레이어드 디자인', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '롱 레이어드 컷');

INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '롱 웨이브 펌', 180000, 150, '긴 머리에 자연스러운 볼륨과 흐름을 더하는 웨이브', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '롱 웨이브 펌');

INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '미니멀 웨이브 펌', 150000, 120, '간결한 실루엣과 손질 편의성을 살린 웨이브', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '미니멀 웨이브 펌');

INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '신부 메이크업', 250000, 180, '예식 분위기와 드레스에 맞춘 메이크업과 헤어 연출', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '신부 메이크업');

INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '내추럴 웨이브 펌', 170000, 150, '일상에서 편하게 손질하는 자연스러운 웨이브', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '내추럴 웨이브 펌');

-- 각 메뉴에는 표시 순서대로 최대 4장의 이미지를 연결합니다.
INSERT INTO service_item_image (service_item_id, image_url, display_order)
SELECT item.id, image.image_url, image.display_order
FROM service_item item
JOIN (
    SELECT '두피 클리닉' AS service_name, '/images/services/scalp-care-1.jpg' AS image_url, 0 AS display_order
    UNION ALL SELECT '두피 클리닉', '/images/services/scalp-care-2.jpg', 1
    UNION ALL SELECT '두피 클리닉', '/images/services/scalp-care-3.jpg', 2
    UNION ALL SELECT '두피 클리닉', '/images/services/scalp-care-4.jpg', 3
    UNION ALL SELECT '롱 레이어드 컷', '/images/services/long-layered-1.jpg', 0
    UNION ALL SELECT '롱 레이어드 컷', '/images/services/long-layered-2.jpg', 1
    UNION ALL SELECT '롱 레이어드 컷', '/images/services/long-layered-3.jpg', 2
    UNION ALL SELECT '롱 레이어드 컷', '/images/services/long-layered-4.jpg', 3
    UNION ALL SELECT '롱 웨이브 펌', '/images/services/long-wave-1.jpg', 0
    UNION ALL SELECT '롱 웨이브 펌', '/images/services/long-wave-2.jpg', 1
    UNION ALL SELECT '롱 웨이브 펌', '/images/services/long-wave-3.jpg', 2
    UNION ALL SELECT '롱 웨이브 펌', '/images/services/long-wave-4.jpg', 3
    UNION ALL SELECT '미니멀 웨이브 펌', '/images/services/minimal-wave-1.jpg', 0
    UNION ALL SELECT '미니멀 웨이브 펌', '/images/services/minimal-wave-2.jpg', 1
    UNION ALL SELECT '미니멀 웨이브 펌', '/images/services/minimal-wave-3.jpg', 2
    UNION ALL SELECT '미니멀 웨이브 펌', '/images/services/minimal-wave-4.jpg', 3
    UNION ALL SELECT '신부 메이크업', '/images/services/bridal-makeup-1.jpg', 0
    UNION ALL SELECT '신부 메이크업', '/images/services/bridal-makeup-2.jpg', 1
    UNION ALL SELECT '신부 메이크업', '/images/services/bridal-makeup-3.jpg', 2
    UNION ALL SELECT '신부 메이크업', '/images/services/bridal-makeup-4.jpg', 3
    UNION ALL SELECT '내추럴 웨이브 펌', '/images/services/natural-wave-1.jpg', 0
    UNION ALL SELECT '내추럴 웨이브 펌', '/images/services/natural-wave-2.jpg', 1
    UNION ALL SELECT '내추럴 웨이브 펌', '/images/services/natural-wave-3.jpg', 2
    UNION ALL SELECT '내추럴 웨이브 펌', '/images/services/natural-wave-4.jpg', 3
) image ON image.service_name = item.name
WHERE NOT EXISTS (
    SELECT 1
    FROM service_item_image saved_image
    WHERE saved_image.service_item_id = item.id
      AND saved_image.display_order = image.display_order
);

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
