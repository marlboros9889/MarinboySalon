-- URL에 포함된 & 문자를 SQLPlus 치환 변수로 오해하지 않도록 비활성화합니다.
SET DEFINE OFF;

-- 데모 로그인과 예약 테스트에 사용할 관리자/고객 계정입니다.
INSERT INTO MB_USER (ID, USERNAME, PASSWORD, NAME, EMAIL, PHONE, ROLE, SOCIAL_PROVIDER, SOCIAL_ID)
VALUES (1, 'admin', '$2a$10$/eQDkA3LfvJ4xPB3f2.Jh.EjH3uLXRHgEyGs89n1whaQmLVat.1ky', '원장 관리자', 'admin@marinboy.test', '010-0000-0000', 'ADMIN', NULL, NULL);

INSERT INTO MB_USER (ID, USERNAME, PASSWORD, NAME, EMAIL, PHONE, ROLE, SOCIAL_PROVIDER, SOCIAL_ID)
VALUES (2, 'customer', '$2a$10$jxWrQHW8tjtnleNpHzx/a.zPLaZl5gbaj2ocxVbsjhkRfQjlPhxju', '고객 사용자', 'customer@marinboy.test', '010-1111-2222', 'CUSTOMER', NULL, NULL);

INSERT INTO MB_USER (ID, USERNAME, PASSWORD, NAME, EMAIL, PHONE, ROLE, SOCIAL_PROVIDER, SOCIAL_ID)
VALUES (3, 'seoyeon', '$2a$10$jxWrQHW8tjtnleNpHzx/a.zPLaZl5gbaj2ocxVbsjhkRfQjlPhxju', '김서연', 'seoyeon@example.com', '010-1234-5678', 'CUSTOMER', NULL, NULL);

-- 고객 화면에 보여줄 헤어/뷰티 시술 메뉴 샘플입니다.
INSERT INTO MB_SERVICE_ITEM (ID, NAME, CATEGORY, DURATION_MINUTES, PRICE, DESCRIPTION, TOP_RANK)
VALUES (1, '웨이브 펌', '펌', 120, 95000, '1인 미용실에서 진행하는 부드러운 웨이브 펌 시술입니다.', 1);

INSERT INTO MB_SERVICE_ITEM (ID, NAME, CATEGORY, DURATION_MINUTES, PRICE, DESCRIPTION, TOP_RANK)
VALUES (2, '시그니처 컷', '컷', 60, 35000, '상담을 포함한 맞춤형 커트 시술입니다.', 2);

INSERT INTO MB_SERVICE_ITEM (ID, NAME, CATEGORY, DURATION_MINUTES, PRICE, DESCRIPTION, TOP_RANK)
VALUES (3, '두피 클리닉', '클리닉', 60, 55000, '두피 상태를 점검하고 진정 케어를 진행하는 클리닉입니다.', 3);

INSERT INTO MB_SERVICE_ITEM (ID, NAME, CATEGORY, DURATION_MINUTES, PRICE, DESCRIPTION, TOP_RANK)
VALUES (4, '젤 네일 기본', '네일', 60, 40000, '손톱 케어와 기본 젤 컬러를 진행하는 네일 시술입니다.', NULL);

INSERT INTO MB_SERVICE_ITEM (ID, NAME, CATEGORY, DURATION_MINUTES, PRICE, DESCRIPTION, TOP_RANK)
VALUES (5, '꾸미기 화장', '메이크업', 80, 110000, '소개팅, 촬영, 중요한 약속 전 자연스럽게 완성하는 메이크업입니다.', NULL);

INSERT INTO MB_SERVICE_ITEM (ID, NAME, CATEGORY, DURATION_MINUTES, PRICE, DESCRIPTION, TOP_RANK)
VALUES (6, '신부 화장', '웨딩', 150, 250000, '본식 또는 웨딩 촬영을 위한 신부 헤어와 메이크업 패키지입니다.', NULL);

-- 메뉴별 대표 이미지와 추가 갤러리 이미지 샘플입니다.
INSERT INTO MB_SERVICE_IMAGE (SERVICE_ID, IMAGE_URL, IMAGE_TYPE, SORT_ORDER)
VALUES (1, 'https://images.unsplash.com/photo-1633681926022-84c23e8cb2d6?auto=format&fit=crop&w=900&q=80', 'REPRESENTATIVE', 1);

INSERT INTO MB_SERVICE_IMAGE (SERVICE_ID, IMAGE_URL, IMAGE_TYPE, SORT_ORDER)
VALUES (1, 'https://images.unsplash.com/photo-1523263685509-57c1d050d19b?auto=format&fit=crop&w=900&q=80', 'DETAIL', 1);

INSERT INTO MB_SERVICE_IMAGE (SERVICE_ID, IMAGE_URL, IMAGE_TYPE, SORT_ORDER)
VALUES (2, 'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?auto=format&fit=crop&w=900&q=80', 'REPRESENTATIVE', 1);

INSERT INTO MB_SERVICE_IMAGE (SERVICE_ID, IMAGE_URL, IMAGE_TYPE, SORT_ORDER)
VALUES (3, 'https://images.unsplash.com/photo-1560066984-138dadb4c035?auto=format&fit=crop&w=900&q=80', 'REPRESENTATIVE', 1);

INSERT INTO MB_SERVICE_IMAGE (SERVICE_ID, IMAGE_URL, IMAGE_TYPE, SORT_ORDER)
VALUES (4, 'https://images.unsplash.com/photo-1604654894610-df63bc536371?auto=format&fit=crop&w=900&q=80', 'REPRESENTATIVE', 1);

INSERT INTO MB_SERVICE_IMAGE (SERVICE_ID, IMAGE_URL, IMAGE_TYPE, SORT_ORDER)
VALUES (5, 'https://images.unsplash.com/photo-1487412947147-5cebf100ffc2?auto=format&fit=crop&w=900&q=80', 'REPRESENTATIVE', 1);

INSERT INTO MB_SERVICE_IMAGE (SERVICE_ID, IMAGE_URL, IMAGE_TYPE, SORT_ORDER)
VALUES (6, 'https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=900&q=80', 'REPRESENTATIVE', 1);

-- 예약 상태 흐름 확인에 사용할 샘플 예약입니다.
INSERT INTO MB_RESERVATION (
    ID,
    SERVICE_ID,
    CUSTOMER_ID,
    CUSTOMER_NAME,
    CUSTOMER_EMAIL,
    CUSTOMER_PHONE,
    RESERVATION_DATE_TIME,
    NO_SHOW_POLICY_AGREED,
    STATUS,
    MEMO
) VALUES (
    1,
    2,
    3,
    '김서연',
    'seoyeon@example.com',
    '010-1234-5678',
    SYSTIMESTAMP + INTERVAL '1' DAY,
    1,
    'CONFIRMED',
    '옆머리는 자연스럽게 정리 요청'
);

COMMIT;
