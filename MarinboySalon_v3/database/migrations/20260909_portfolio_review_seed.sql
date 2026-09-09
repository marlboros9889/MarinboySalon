USE marinboy_salon;

-- 포트폴리오 메인 화면에서 순환할 샘플 후기입니다.
-- 완료 예약과 연결해 후기 1건당 예약 1건이라는 서비스 규칙을 지킵니다.
START TRANSACTION;

CREATE TEMPORARY TABLE portfolio_review_seed (
    sequence_no INT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    customer_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    rating TINYINT NOT NULL,
    content VARCHAR(500) NOT NULL
);

INSERT INTO portfolio_review_seed (sequence_no, email, customer_name, phone, rating, content) VALUES
(1, 'review.customer01@marinboy.demo', '김하늘', '010-9000-0001', 5, '상담부터 시술까지 차분하게 설명해 주셔서 편하게 맡길 수 있었어요.'),
(2, 'review.customer02@marinboy.demo', '이서윤', '010-9000-0002', 5, '원하던 분위기를 정확히 이해해 주셨고 손질도 정말 편해졌어요.'),
(3, 'review.customer03@marinboy.demo', '박지민', '010-9000-0003', 4, '조용한 공간에서 꼼꼼하게 케어받아 기분 좋게 쉬고 왔습니다.'),
(4, 'review.customer04@marinboy.demo', '최유진', '010-9000-0004', 5, '모발 상태를 세심하게 봐주시고 홈케어 방법도 알려주셨어요.'),
(5, 'review.customer05@marinboy.demo', '정다은', '010-9000-0005', 5, '자연스러운 컬러가 마음에 들어요. 다음에도 다시 예약할게요.'),
(6, 'review.customer06@marinboy.demo', '한소희', '010-9000-0006', 4, '시술 과정이 편안했고 원하는 길이와 질감으로 잘 정리됐어요.'),
(7, 'review.customer07@marinboy.demo', '윤서아', '010-9000-0007', 5, '얼굴형에 맞는 스타일을 추천해 주셔서 만족도가 높았습니다.'),
(8, 'review.customer08@marinboy.demo', '강민지', '010-9000-0008', 5, '예약 시간에 맞춰 바로 진행돼서 여유롭게 시술받았어요.'),
(9, 'review.customer09@marinboy.demo', '송예린', '010-9000-0009', 4, '손상된 부분을 고려해서 진행해 주셔서 머릿결이 한결 부드러워졌어요.'),
(10, 'review.customer10@marinboy.demo', '오지아', '010-9000-0010', 5, '디자이너님이 처음부터 끝까지 직접 봐주셔서 믿음이 갔어요.'),
(11, 'review.customer11@marinboy.demo', '문채원', '010-9000-0011', 5, '과하지 않으면서 분위기 있는 스타일이라 매일 만족하고 있어요.'),
(12, 'review.customer12@marinboy.demo', '서아린', '010-9000-0012', 4, '상담 내용을 꼼꼼히 반영해 주셔서 원하는 느낌과 잘 맞았습니다.'),
(13, 'review.customer13@marinboy.demo', '권나연', '010-9000-0013', 5, '샴푸부터 마무리까지 세심해서 프라이빗한 케어를 받은 느낌이에요.'),
(14, 'review.customer14@marinboy.demo', '임수아', '010-9000-0014', 5, '스타일 유지 방법을 쉽게 알려주셔서 집에서도 손질하기 좋아요.'),
(15, 'review.customer15@marinboy.demo', '배유나', '010-9000-0015', 4, '부담스럽지 않은 추천 덕분에 새로운 스타일에 도전할 수 있었어요.'),
(16, 'review.customer16@marinboy.demo', '신혜원', '010-9000-0016', 5, '공간도 깔끔하고 시술 결과도 기대 이상이라 만족합니다.'),
(17, 'review.customer17@marinboy.demo', '장서진', '010-9000-0017', 5, '모발 고민을 잘 들어주시고 딱 필요한 케어만 안내해 주셨어요.'),
(18, 'review.customer18@marinboy.demo', '노유진', '010-9000-0018', 4, '편안한 분위기에서 충분히 상담하고 시술받을 수 있어 좋았습니다.'),
(19, 'review.customer19@marinboy.demo', '홍지우', '010-9000-0019', 5, '디테일하게 다듬어 주셔서 시간이 지나도 스타일이 예쁘게 유지돼요.'),
(20, 'review.customer20@marinboy.demo', '조민서', '010-9000-0020', 5, '다음 예약까지 기다려질 만큼 결과와 응대 모두 만족스러웠어요.');

-- 샘플 계정은 실제 로그인 계정과 구분되는 demo 이메일만 사용합니다.
INSERT INTO user_account (email, password, name, phone, role, created_at)
SELECT seed.email, '$2a$10$7EqJtq98hPqEX7fNZaFWoO6Z0ZkmA6K0VdXJkOUmYlYH7GBhphTui', seed.customer_name, seed.phone, 'CUSTOMER',
       DATE_SUB('2026-09-08 09:00:00', INTERVAL seed.sequence_no DAY)
FROM portfolio_review_seed seed
WHERE NOT EXISTS (SELECT 1 FROM user_account account WHERE account.email = seed.email);

-- 현재 활성 메뉴 하나를 사용해 과거 완료 예약을 만들고, 재실행 때는 같은 메모를 찾습니다.
INSERT INTO reservation (user_id, service_id, reservation_start, status, request_memo, original_price, discount_rate, discount_amount, final_price, created_at)
SELECT account.id, menu.id, DATE_SUB('2026-09-08 14:00:00', INTERVAL seed.sequence_no DAY), 'COMPLETED',
       CONCAT('[PORTFOLIO REVIEW ', LPAD(seed.sequence_no, 2, '0'), ']'), menu.price, 0, 0, menu.price,
       DATE_SUB('2026-09-08 09:00:00', INTERVAL seed.sequence_no DAY)
FROM portfolio_review_seed seed
INNER JOIN user_account account ON account.email = seed.email
INNER JOIN (SELECT id, price FROM service_item WHERE active = 1 ORDER BY id LIMIT 1) menu
WHERE NOT EXISTS (
    SELECT 1 FROM reservation saved_reservation
    WHERE saved_reservation.request_memo = CONCAT('[PORTFOLIO REVIEW ', LPAD(seed.sequence_no, 2, '0'), ']')
);

INSERT INTO review (reservation_id, user_id, rating, content, created_at)
SELECT reservation.id, account.id, seed.rating, seed.content,
       DATE_SUB('2026-09-08 18:00:00', INTERVAL seed.sequence_no DAY)
FROM portfolio_review_seed seed
INNER JOIN user_account account ON account.email = seed.email
INNER JOIN reservation ON reservation.user_id = account.id
    AND reservation.request_memo = CONCAT('[PORTFOLIO REVIEW ', LPAD(seed.sequence_no, 2, '0'), ']')
WHERE NOT EXISTS (SELECT 1 FROM review saved_review WHERE saved_review.reservation_id = reservation.id);

COMMIT;
