USE marinboy_salon;

-- 기존 예약이 참조하는 1~3번 메뉴 ID는 유지한 채 새 메뉴 정보로 바꿉니다.
UPDATE service_item
SET name = '두피 클리닉',
    price = 90000,
    duration_minutes = 60,
    description = '두피 상태를 확인하고 진정과 보습을 돕는 맞춤 케어',
    active = 1
WHERE id = 1;

UPDATE service_item
SET name = '롱 레이어드 컷',
    price = 70000,
    duration_minutes = 90,
    description = '얼굴선을 부드럽게 감싸는 긴 기장 레이어드 디자인',
    active = 1
WHERE id = 2;

UPDATE service_item
SET name = '롱 웨이브 펌',
    price = 180000,
    duration_minutes = 150,
    description = '긴 머리에 자연스러운 볼륨과 흐름을 더하는 웨이브',
    active = 1
WHERE id = 3;

INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '미니멀 웨이브 펌', 150000, 120, '간결한 실루엣과 손질 편의성을 살린 웨이브', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '미니멀 웨이브 펌');

INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '신부 메이크업', 250000, 180, '예식 분위기와 드레스에 맞춘 메이크업과 헤어 연출', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '신부 메이크업');

INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '내추럴 웨이브 펌', 170000, 150, '일상에서 편하게 손질하는 자연스러운 웨이브', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '내추럴 웨이브 펌');

CREATE TABLE IF NOT EXISTS service_item_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_item_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    display_order TINYINT NOT NULL,
    CONSTRAINT fk_service_item_image_service FOREIGN KEY (service_item_id) REFERENCES service_item(id),
    CONSTRAINT uk_service_item_image_order UNIQUE (service_item_id, display_order),
    CONSTRAINT ck_service_item_image_order CHECK (display_order BETWEEN 0 AND 3)
);

-- 이 마이그레이션이 다시 실행되어도 같은 메뉴 이미지가 중복되지 않게 교체합니다.
DELETE saved_image
FROM service_item_image saved_image
JOIN service_item item ON item.id = saved_image.service_item_id
WHERE item.name IN (
    '두피 클리닉',
    '롱 레이어드 컷',
    '롱 웨이브 펌',
    '미니멀 웨이브 펌',
    '신부 메이크업',
    '내추럴 웨이브 펌'
);

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
) image ON image.service_name = item.name;
