-- 포트폴리오 검증용: 고객 20명, 시술 20개, 과거 시술 이력 20건을 반복 실행 가능하게 생성합니다.
DECLARE
    v_id NUMBER;
BEGIN
    FOR i IN 1..20 LOOP
        v_id := 100 + i;

        MERGE INTO MB_USER U
        USING (SELECT v_id AS ID FROM DUAL) V ON (U.ID = V.ID)
        WHEN MATCHED THEN UPDATE SET U.NAME = 'Customer ' || LPAD(i, 2, '0'), U.EMAIL = 'customer' || LPAD(i, 2, '0') || '@marinboy.test', U.PHONE = '010-9000-' || LPAD(i, 4, '0'), U.ROLE = 'CUSTOMER', U.UPDATED_AT = SYSTIMESTAMP
        WHEN NOT MATCHED THEN INSERT (ID, USERNAME, PASSWORD, NAME, EMAIL, PHONE, ROLE)
        VALUES (v_id, 'customer' || LPAD(i, 2, '0'), '$2a$10$jxWrQHW8tjtnleNpHzx/a.zPLaZl5gbaj2ocxVbsjhkRfQjlPhxju', 'Customer ' || LPAD(i, 2, '0'), 'customer' || LPAD(i, 2, '0') || '@marinboy.test', '010-9000-' || LPAD(i, 4, '0'), 'CUSTOMER');

        MERGE INTO MB_SERVICE_ITEM S
        USING (SELECT v_id AS ID FROM DUAL) V ON (S.ID = V.ID)
        WHEN MATCHED THEN UPDATE SET S.NAME = 'Portfolio Style ' || LPAD(i, 2, '0'), S.CATEGORY = CASE WHEN MOD(i, 2) = 0 THEN 'CUT' ELSE 'PERM' END, S.DURATION_MINUTES = 60 + MOD(i, 3) * 30, S.PRICE = 40000 + i * 5000, S.DESCRIPTION = 'Portfolio sample service ' || LPAD(i, 2, '0'), S.UPDATED_AT = SYSTIMESTAMP
        WHEN NOT MATCHED THEN INSERT (ID, NAME, CATEGORY, DURATION_MINUTES, PRICE, DESCRIPTION)
        VALUES (v_id, 'Portfolio Style ' || LPAD(i, 2, '0'), CASE WHEN MOD(i, 2) = 0 THEN 'CUT' ELSE 'PERM' END, 60 + MOD(i, 3) * 30, 40000 + i * 5000, 'Portfolio sample service ' || LPAD(i, 2, '0'));

        MERGE INTO MB_RESERVATION R
        USING (SELECT v_id AS ID FROM DUAL) V ON (R.ID = V.ID)
        WHEN MATCHED THEN UPDATE SET R.SERVICE_ID = v_id, R.CUSTOMER_ID = v_id, R.CUSTOMER_NAME = 'Customer ' || LPAD(i, 2, '0'), R.CUSTOMER_EMAIL = 'customer' || LPAD(i, 2, '0') || '@marinboy.test', R.CUSTOMER_PHONE = '010-9000-' || LPAD(i, 4, '0'), R.RESERVATION_DATE_TIME = SYSTIMESTAMP - NUMTODSINTERVAL(i * 7, 'DAY'), R.STATUS = CASE WHEN MOD(i, 5) = 0 THEN 'CANCELED' ELSE 'COMPLETED' END, R.MEMO = 'Portfolio history sample', R.UPDATED_AT = SYSTIMESTAMP
        WHEN NOT MATCHED THEN INSERT (ID, SERVICE_ID, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, CUSTOMER_PHONE, RESERVATION_DATE_TIME, NO_SHOW_POLICY_AGREED, STATUS, MEMO)
        VALUES (v_id, v_id, v_id, 'Customer ' || LPAD(i, 2, '0'), 'customer' || LPAD(i, 2, '0') || '@marinboy.test', '010-9000-' || LPAD(i, 4, '0'), SYSTIMESTAMP - NUMTODSINTERVAL(i * 7, 'DAY'), 1, CASE WHEN MOD(i, 5) = 0 THEN 'CANCELED' ELSE 'COMPLETED' END, 'Portfolio history sample');
    END LOOP;
    COMMIT;
END;
/
