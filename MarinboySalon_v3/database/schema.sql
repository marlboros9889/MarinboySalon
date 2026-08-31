-- v1, v2, v3가 함께 사용하는 새 데이터베이스입니다.
CREATE DATABASE IF NOT EXISTS marinboy_salon
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE marinboy_salon;

-- 회원 정보와 권한을 저장합니다.
CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 고객에게 보여줄 시술 항목을 저장합니다.
CREATE TABLE IF NOT EXISTS service_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price INT NOT NULL,
    duration_minutes INT NOT NULL,
    description VARCHAR(500),
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_service_item_price CHECK (price >= 0),
    CONSTRAINT ck_service_item_duration CHECK (duration_minutes > 0 AND MOD(duration_minutes, 30) = 0)
);

-- 메뉴별 이미지를 표시 순서대로 최대 4장까지 저장합니다.
CREATE TABLE IF NOT EXISTS service_item_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_item_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    display_order TINYINT NOT NULL,
    CONSTRAINT fk_service_item_image_service FOREIGN KEY (service_item_id) REFERENCES service_item(id),
    CONSTRAINT uk_service_item_image_order UNIQUE (service_item_id, display_order),
    CONSTRAINT ck_service_item_image_order CHECK (display_order BETWEEN 0 AND 3)
);

-- 회원이 선택한 시술과 예약 시간을 저장합니다.
CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    reservation_start DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    request_memo VARCHAR(500),
    calendar_event_id VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES user_account(id),
    CONSTRAINT fk_reservation_service FOREIGN KEY (service_id) REFERENCES service_item(id),
    -- 예약 상태 철자를 한 종류로 고정해 CANCELED/CANCELLED 같은 데이터 오류를 막습니다.
    CONSTRAINT ck_reservation_status CHECK (status IN ('REQUESTED', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    INDEX idx_reservation_status_start (status, reservation_start)
);

-- 예약 생성 시 같은 날짜·30분 슬롯만 트랜잭션으로 직렬화합니다.
CREATE TABLE IF NOT EXISTS reservation_slot_lock (
    reservation_date DATE NOT NULL,
    slot_time TIME NOT NULL,
    locked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (reservation_date, slot_time)
);

-- 요일별 영업시간을 저장합니다. 1은 월요일, 7은 일요일입니다.
CREATE TABLE IF NOT EXISTS business_hour (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    day_of_week INT NOT NULL UNIQUE,
    open_time TIME,
    close_time TIME,
    closed TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT ck_business_hour_day CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT ck_business_hour_time CHECK (
        closed = 1 OR (open_time IS NOT NULL AND close_time IS NOT NULL AND open_time < close_time)
    )
);

-- 날짜별 임시 휴무일을 저장합니다.
CREATE TABLE IF NOT EXISTS holiday (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    holiday_date DATE NOT NULL UNIQUE,
    reason VARCHAR(200),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 소셜 제공자 계정을 일반 회원과 연결합니다.
CREATE TABLE IF NOT EXISTS user_social_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_user_id VARCHAR(150) NOT NULL,
    provider_email VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_social_account_user FOREIGN KEY (user_id) REFERENCES user_account(id),
    CONSTRAINT uk_social_provider_user UNIQUE (provider, provider_user_id)
);
