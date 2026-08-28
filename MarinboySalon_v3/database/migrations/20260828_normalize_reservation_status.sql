USE marinboy_salon;

-- v1, v2, v3가 같은 예약 상태를 사용하도록 과거 v3의 철자를 한 번만 정리합니다.
UPDATE reservation
SET status = 'CANCELLED'
WHERE status = 'CANCELED';
