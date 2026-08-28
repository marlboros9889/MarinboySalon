USE marinboy_salon;

CREATE TABLE IF NOT EXISTS review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    rating TINYINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_reservation FOREIGN KEY (reservation_id) REFERENCES reservation(id),
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES user_account(id),
    CONSTRAINT ck_review_rating CHECK (rating BETWEEN 1 AND 5)
);
