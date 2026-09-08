USE marinboy_salon;

-- 예약·리뷰 이력을 보존한 회원 탈퇴를 위해 탈퇴 시각을 기록합니다.
ALTER TABLE user_account
    ADD COLUMN IF NOT EXISTS deleted_at DATETIME NULL;

-- 관리자만 관리하는 기간형 할인 이벤트입니다. 한 시점에는 한 이벤트만 적용하도록 서비스에서 기간 중복을 막습니다.
CREATE TABLE IF NOT EXISTS discount_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    discount_rate DECIMAL(5,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_discount_event_period CHECK (start_date <= end_date),
    CONSTRAINT ck_discount_event_rate CHECK (discount_rate > 0 AND discount_rate <= 100)
);

-- 예약 시점의 금액을 고정해 이후 메뉴 가격·이벤트 변경이 과거 예약에 영향을 주지 않게 합니다.
ALTER TABLE reservation
    ADD COLUMN IF NOT EXISTS original_price INT NULL,
    ADD COLUMN IF NOT EXISTS discount_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS discount_amount INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS final_price INT NULL,
    ADD COLUMN IF NOT EXISTS discount_event_id BIGINT NULL;
