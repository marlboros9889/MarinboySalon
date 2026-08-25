USE marinboy_salon;

-- 요일별 영업시간과 정기 휴무 여부를 저장합니다.
CREATE TABLE business_hour (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    day_of_week INT NOT NULL UNIQUE,
    open_time TIME,
    close_time TIME,
    closed TINYINT(1) NOT NULL DEFAULT 0
);

-- 날짜별 임시 휴무일과 사유를 저장합니다.
CREATE TABLE holiday (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    holiday_date DATE NOT NULL UNIQUE,
    reason VARCHAR(200),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 월요일부터 토요일까지 기본 영업시간을 등록하고 일요일은 휴무로 등록합니다.
INSERT INTO business_hour (day_of_week, open_time, close_time, closed) VALUES
(1, '10:00:00', '19:00:00', 0),
(2, '10:00:00', '19:00:00', 0),
(3, '10:00:00', '19:00:00', 0),
(4, '10:00:00', '19:00:00', 0),
(5, '10:00:00', '19:00:00', 0),
(6, '10:00:00', '17:00:00', 0),
(7, NULL, NULL, 1);

-- 관리자 계정은 일반 회원가입 후 아래 이메일 조건으로 권한을 변경합니다.
-- 실제 가입 이메일에 맞게 WHERE 값을 확인한 뒤 한 번만 실행합니다.
UPDATE user_account
SET role = 'ADMIN'
WHERE email = 'owner@marinboy.com';
