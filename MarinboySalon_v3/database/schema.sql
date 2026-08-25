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
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    CONSTRAINT fk_reservation_service FOREIGN KEY (service_id) REFERENCES service_item(id)
);

-- 요일별 영업시간을 저장합니다. 1은 월요일, 7은 일요일입니다.
CREATE TABLE IF NOT EXISTS business_hour (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    day_of_week INT NOT NULL UNIQUE,
    open_time TIME,
    close_time TIME,
    closed TINYINT(1) NOT NULL DEFAULT 0
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
