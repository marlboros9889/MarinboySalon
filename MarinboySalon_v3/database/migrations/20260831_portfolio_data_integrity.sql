USE marinboy_salon;

-- 1. 아래 조회 결과가 모두 0건인지 확인한 뒤 제약을 적용합니다.
SELECT id, price, duration_minutes
FROM service_item
WHERE price < 0
   OR duration_minutes <= 0
   OR MOD(duration_minutes, 30) <> 0;

SELECT id, status
FROM reservation
WHERE status NOT IN ('REQUESTED', 'CONFIRMED', 'COMPLETED', 'CANCELLED');

SELECT id, day_of_week, open_time, close_time, closed
FROM business_hour
WHERE day_of_week NOT BETWEEN 1 AND 7
   OR (closed = 0 AND (open_time IS NULL OR close_time IS NULL OR open_time >= close_time));

-- 2. 이미 적용된 데이터베이스에서도 다시 실행할 수 있도록 없는 제약만 추가합니다.
SET @schema_name = DATABASE();

SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE constraint_schema = @schema_name
      AND table_name = 'service_item'
      AND constraint_name = 'ck_service_item_price'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE service_item ADD CONSTRAINT ck_service_item_price CHECK (price >= 0)',
    'SELECT ''ck_service_item_price already exists'' AS migration_message');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE constraint_schema = @schema_name
      AND table_name = 'service_item'
      AND constraint_name = 'ck_service_item_duration'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE service_item ADD CONSTRAINT ck_service_item_duration CHECK (duration_minutes > 0 AND MOD(duration_minutes, 30) = 0)',
    'SELECT ''ck_service_item_duration already exists'' AS migration_message');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE constraint_schema = @schema_name
      AND table_name = 'reservation'
      AND constraint_name = 'ck_reservation_status'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE reservation ADD CONSTRAINT ck_reservation_status CHECK (status IN (''REQUESTED'', ''CONFIRMED'', ''COMPLETED'', ''CANCELLED''))',
    'SELECT ''ck_reservation_status already exists'' AS migration_message');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE constraint_schema = @schema_name
      AND table_name = 'business_hour'
      AND constraint_name = 'ck_business_hour_day'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE business_hour ADD CONSTRAINT ck_business_hour_day CHECK (day_of_week BETWEEN 1 AND 7)',
    'SELECT ''ck_business_hour_day already exists'' AS migration_message');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE constraint_schema = @schema_name
      AND table_name = 'business_hour'
      AND constraint_name = 'ck_business_hour_time'
);
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE business_hour ADD CONSTRAINT ck_business_hour_time CHECK (closed = 1 OR (open_time IS NOT NULL AND close_time IS NOT NULL AND open_time < close_time))',
    'SELECT ''ck_business_hour_time already exists'' AS migration_message');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

-- 3. 예약 겹침 조회에 필요한 인덱스도 없는 경우에만 추가합니다.
SET @index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'reservation'
      AND index_name = 'idx_reservation_status_start'
);
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_reservation_status_start ON reservation (status, reservation_start)',
    'SELECT ''idx_reservation_status_start already exists'' AS migration_message');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;
