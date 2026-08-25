-- v1, v2, v3가 함께 사용할 새 데이터베이스를 생성합니다.
-- 같은 이름이 이미 있으면 오류를 내서 기존 DB를 실수로 재사용하지 않게 합니다.
CREATE DATABASE marinboy_salon
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE marinboy_salon;

-- 회원 정보와 관리자 권한을 저장합니다.
CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 고객에게 보여줄 시술 메뉴를 저장합니다.
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
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_user
        FOREIGN KEY (user_id) REFERENCES user_account(id),
    CONSTRAINT fk_reservation_service
        FOREIGN KEY (service_id) REFERENCES service_item(id)
);

CREATE INDEX idx_reservation_start ON reservation (reservation_start);
CREATE INDEX idx_reservation_user ON reservation (user_id);
