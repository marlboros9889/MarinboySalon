-- 새 프로젝트 공통 데이터베이스에 학습용 메뉴를 등록합니다.
USE marinboy_salon;

-- 서비스 메뉴가 비어 있을 때만 학습용 기본 메뉴를 등록합니다.
INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '남성 커트', 20000, 30, '두상과 모질을 고려한 기본 커트', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '남성 커트');

INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '다운펌', 40000, 60, '옆머리를 자연스럽게 정리하는 시술', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '다운펌');

INSERT INTO service_item (name, price, duration_minutes, description, active)
SELECT '디자인 펌', 80000, 120, '상담 후 스타일을 정하는 디자인 펌', 1
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE name = '디자인 펌');
