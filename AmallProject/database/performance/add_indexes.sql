-- =============================================
-- 성능 최적화: 인덱스 추가
-- Phase 5-2: Database Index Optimization
-- =============================================
-- 목적: MyBatis 쿼리 패턴 분석을 통한 누락된 인덱스 추가
-- 작성일: 2025-11-10
-- =============================================

SET SERVEROUTPUT ON;

-- =============================================
-- 1. CART 테이블 인덱스
-- =============================================
-- 목적: 회원별 장바구니 조회 성능 향상
-- 영향 쿼리: showMyCart, clearCart

DECLARE
    idx_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO idx_count
    FROM user_indexes
    WHERE index_name = 'IDX_CART_MEMBER_ID';

    IF idx_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IDX_CART_MEMBER_ID ON CART(MEMBER_ID)';
        DBMS_OUTPUT.PUT_LINE('✓ Created index: IDX_CART_MEMBER_ID');
    ELSE
        DBMS_OUTPUT.PUT_LINE('○ Index already exists: IDX_CART_MEMBER_ID');
    END IF;
END;
/

-- 목적: 장바구니 중복 체크 성능 향상
-- 영향 쿼리: findByMemberAndProduct (WHERE MEMBER_ID = ? AND PROD_NUM = ?)

DECLARE
    idx_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO idx_count
    FROM user_indexes
    WHERE index_name = 'IDX_CART_MEMBER_PROD';

    IF idx_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IDX_CART_MEMBER_PROD ON CART(MEMBER_ID, PROD_NUM)';
        DBMS_OUTPUT.PUT_LINE('✓ Created index: IDX_CART_MEMBER_PROD');
    ELSE
        DBMS_OUTPUT.PUT_LINE('○ Index already exists: IDX_CART_MEMBER_PROD');
    END IF;
END;
/

-- =============================================
-- 2. ORDERS 테이블 인덱스
-- =============================================
-- 목적: 선물 주문 조회 성능 향상
-- 영향 쿼리: findGiftOrdersReceivedByMemberId (WHERE IS_GIFT = 'Y' AND MEMBER_ID = ?)
-- 설명: 복합 인덱스는 WHERE 절의 필터링 순서를 고려하여 IS_GIFT를 선행 컬럼으로 배치

DECLARE
    idx_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO idx_count
    FROM user_indexes
    WHERE index_name = 'IDX_ORDERS_GIFT_MEMBER';

    IF idx_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IDX_ORDERS_GIFT_MEMBER ON ORDERS(IS_GIFT, MEMBER_ID)';
        DBMS_OUTPUT.PUT_LINE('✓ Created index: IDX_ORDERS_GIFT_MEMBER');
    ELSE
        DBMS_OUTPUT.PUT_LINE('○ Index already exists: IDX_ORDERS_GIFT_MEMBER');
    END IF;
END;
/

-- 목적: 주문 상태별 조회 성능 향상
-- 영향: 주문 상태 필터링 시 (향후 확장 대비)
-- 우선순위: MEDIUM

DECLARE
    idx_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO idx_count
    FROM user_indexes
    WHERE index_name = 'IDX_ORDERS_STATUS';

    IF idx_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IDX_ORDERS_STATUS ON ORDERS(ORDER_STATUS)';
        DBMS_OUTPUT.PUT_LINE('✓ Created index: IDX_ORDERS_STATUS');
    ELSE
        DBMS_OUTPUT.PUT_LINE('○ Index already exists: IDX_ORDERS_STATUS');
    END IF;
END;
/

-- 목적: 날짜 범위 주문 조회 성능 향상
-- 영향: 주문 내역 날짜별 검색 시 (향후 확장 대비)
-- 우선순위: LOW

DECLARE
    idx_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO idx_count
    FROM user_indexes
    WHERE index_name = 'IDX_ORDERS_DATE';

    IF idx_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IDX_ORDERS_DATE ON ORDERS(ORDER_DATE DESC)';
        DBMS_OUTPUT.PUT_LINE('✓ Created index: IDX_ORDERS_DATE');
    ELSE
        DBMS_OUTPUT.PUT_LINE('○ Index already exists: IDX_ORDERS_DATE');
    END IF;
END;
/

-- 목적: 회원별 주문 내역 조회와 정렬 성능 향상
-- 영향 쿼리: findOrdersByMemberId (ORDER BY O.ORDER_DATE DESC)
-- 설명: 복합 인덱스로 WHERE 절과 ORDER BY 절 모두 최적화

DECLARE
    idx_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO idx_count
    FROM user_indexes
    WHERE index_name = 'IDX_ORDERS_MEMBER_DATE';

    IF idx_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IDX_ORDERS_MEMBER_DATE ON ORDERS(MEMBER_ID, ORDER_DATE DESC)';
        DBMS_OUTPUT.PUT_LINE('✓ Created index: IDX_ORDERS_MEMBER_DATE');
    ELSE
        DBMS_OUTPUT.PUT_LINE('○ Index already exists: IDX_ORDERS_MEMBER_DATE');
    END IF;
END;
/

-- =============================================
-- 3. GIFT_DELIVERY_REQUEST 테이블 인덱스
-- =============================================
-- 목적: 주문 ID로 선물 배송 요청 조회 성능 향상
-- 영향 쿼리: findRequestByOrderId (WHERE ORDER_ID = ?)

DECLARE
    idx_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO idx_count
    FROM user_indexes
    WHERE index_name = 'IDX_GIFT_REQ_ORDER';

    IF idx_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IDX_GIFT_REQ_ORDER ON GIFT_DELIVERY_REQUEST(ORDER_ID)';
        DBMS_OUTPUT.PUT_LINE('✓ Created index: IDX_GIFT_REQ_ORDER');
    ELSE
        DBMS_OUTPUT.PUT_LINE('○ Index already exists: IDX_GIFT_REQ_ORDER');
    END IF;
END;
/

-- =============================================
-- 4. WISHLIST 테이블 인덱스
-- =============================================
-- 목적: 회원별 위시리스트 조회 성능 향상
-- 영향 쿼리: showThisIdWishList, clearWishList

DECLARE
    idx_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO idx_count
    FROM user_indexes
    WHERE index_name = 'IDX_WISHLIST_MEMBER';

    IF idx_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IDX_WISHLIST_MEMBER ON WISHLIST(MEMBER_ID)';
        DBMS_OUTPUT.PUT_LINE('✓ Created index: IDX_WISHLIST_MEMBER');
    ELSE
        DBMS_OUTPUT.PUT_LINE('○ Index already exists: IDX_WISHLIST_MEMBER');
    END IF;
END;
/

-- 목적: 위시리스트 중복 체크 성능 향상
-- 영향 쿼리: findByMemberAndProduct (WHERE MEMBER_ID = ? AND PROD_NUM = ?)

DECLARE
    idx_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO idx_count
    FROM user_indexes
    WHERE index_name = 'IDX_WISHLIST_MEMBER_PROD';

    IF idx_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IDX_WISHLIST_MEMBER_PROD ON WISHLIST(MEMBER_ID, PROD_NUM)';
        DBMS_OUTPUT.PUT_LINE('✓ Created index: IDX_WISHLIST_MEMBER_PROD');
    ELSE
        DBMS_OUTPUT.PUT_LINE('○ Index already exists: IDX_WISHLIST_MEMBER_PROD');
    END IF;
END;
/

-- =============================================
-- 5. ALARM 테이블 인덱스
-- =============================================
-- 목적: 알림 수신자 조회 성능 향상
-- 영향 쿼리: getMyAlarm (WHERE GETTER_ID = ?), deleteMyPostAlarm

DECLARE
    idx_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO idx_count
    FROM user_indexes
    WHERE index_name = 'IDX_ALARM_GETTER';

    IF idx_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IDX_ALARM_GETTER ON ALARM(GETTER_ID)';
        DBMS_OUTPUT.PUT_LINE('✓ Created index: IDX_ALARM_GETTER');
    ELSE
        DBMS_OUTPUT.PUT_LINE('○ Index already exists: IDX_ALARM_GETTER');
    END IF;
END;
/

-- 목적: 알림 발신자 조회 및 삭제 성능 향상
-- 영향 쿼리: deleteMyPostAlarm (WHERE SENDER_ID = ?)

DECLARE
    idx_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO idx_count
    FROM user_indexes
    WHERE index_name = 'IDX_ALARM_SENDER';

    IF idx_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IDX_ALARM_SENDER ON ALARM(SENDER_ID)';
        DBMS_OUTPUT.PUT_LINE('✓ Created index: IDX_ALARM_SENDER');
    ELSE
        DBMS_OUTPUT.PUT_LINE('○ Index already exists: IDX_ALARM_SENDER');
    END IF;
END;
/

-- 목적: 알림 중복 체크 성능 향상
-- 영향 쿼리: CheckSend (WHERE SENDER_ID = ? AND GETTER_ID = ?)

DECLARE
    idx_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO idx_count
    FROM user_indexes
    WHERE index_name = 'IDX_ALARM_SENDER_GETTER';

    IF idx_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IDX_ALARM_SENDER_GETTER ON ALARM(SENDER_ID, GETTER_ID)';
        DBMS_OUTPUT.PUT_LINE('✓ Created index: IDX_ALARM_SENDER_GETTER');
    ELSE
        DBMS_OUTPUT.PUT_LINE('○ Index already exists: IDX_ALARM_SENDER_GETTER');
    END IF;
END;
/

-- =============================================
-- 6. PRODUCT 테이블 인덱스 (추가 최적화)
-- =============================================
-- 목적: 판매자별 상품 조회 성능 향상
-- 영향: 판매자 페이지, 상품 관리

DECLARE
    idx_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO idx_count
    FROM user_indexes
    WHERE index_name = 'IDX_PRODUCT_SELLER';

    IF idx_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IDX_PRODUCT_SELLER ON PRODUCT(SELLER_ID)';
        DBMS_OUTPUT.PUT_LINE('✓ Created index: IDX_PRODUCT_SELLER');
    ELSE
        DBMS_OUTPUT.PUT_LINE('○ Index already exists: IDX_PRODUCT_SELLER');
    END IF;
END;
/

-- 목적: 카테고리별 상품 조회 성능 향상
-- 영향: 카테고리 페이지, 상품 검색

DECLARE
    idx_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO idx_count
    FROM user_indexes
    WHERE index_name = 'IDX_PRODUCT_CATEGORY';

    IF idx_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IDX_PRODUCT_CATEGORY ON PRODUCT(CATEGORY_ID)';
        DBMS_OUTPUT.PUT_LINE('✓ Created index: IDX_PRODUCT_CATEGORY');
    ELSE
        DBMS_OUTPUT.PUT_LINE('○ Index already exists: IDX_PRODUCT_CATEGORY');
    END IF;
END;
/

-- =============================================
-- 인덱스 생성 완료
-- =============================================

BEGIN
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('=============================================');
    DBMS_OUTPUT.PUT_LINE('인덱스 생성 작업 완료');
    DBMS_OUTPUT.PUT_LINE('=============================================');
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('다음 단계:');
    DBMS_OUTPUT.PUT_LINE('1. 인덱스 상태 확인: SELECT * FROM user_indexes WHERE table_name IN (''CART'', ''ORDERS'', ''GIFT_DELIVERY_REQUEST'', ''WISHLIST'', ''ALARM'', ''PRODUCT'');');
    DBMS_OUTPUT.PUT_LINE('2. 인덱스 통계 수집: EXEC DBMS_STATS.GATHER_SCHEMA_STATS(''C##AMALL'');');
    DBMS_OUTPUT.PUT_LINE('3. 쿼리 실행 계획 확인: EXPLAIN PLAN FOR <your_query>;');
    DBMS_OUTPUT.PUT_LINE('4. INDEX_OPTIMIZATION_GUIDE.md 문서 참조');
    DBMS_OUTPUT.PUT_LINE('');
END;
/

-- =============================================
-- 인덱스 검증 쿼리
-- =============================================

-- 생성된 인덱스 목록 조회
SELECT
    index_name,
    table_name,
    uniqueness,
    status
FROM user_indexes
WHERE table_name IN ('CART', 'ORDERS', 'ORDER_ITEM', 'GIFT_DELIVERY_REQUEST', 'WISHLIST', 'ALARM', 'PRODUCT')
ORDER BY table_name, index_name;

-- 인덱스 컬럼 상세 정보 조회
SELECT
    ic.index_name,
    ic.table_name,
    ic.column_name,
    ic.column_position,
    ic.descend
FROM user_ind_columns ic
WHERE ic.table_name IN ('CART', 'ORDERS', 'ORDER_ITEM', 'GIFT_DELIVERY_REQUEST', 'WISHLIST', 'ALARM', 'PRODUCT')
ORDER BY ic.table_name, ic.index_name, ic.column_position;

-- =============================================
-- 롤백 스크립트 (필요 시 사용)
-- =============================================
/*
-- 경고: 아래 스크립트는 생성된 모든 인덱스를 삭제합니다.
-- 실행 전 반드시 확인하세요!

DROP INDEX IDX_CART_MEMBER_ID;
DROP INDEX IDX_CART_MEMBER_PROD;
DROP INDEX IDX_ORDERS_GIFT_MEMBER;
DROP INDEX IDX_ORDERS_STATUS;
DROP INDEX IDX_ORDERS_DATE;
DROP INDEX IDX_ORDERS_MEMBER_DATE;
DROP INDEX IDX_GIFT_REQ_ORDER;
DROP INDEX IDX_WISHLIST_MEMBER;
DROP INDEX IDX_WISHLIST_MEMBER_PROD;
DROP INDEX IDX_ALARM_GETTER;
DROP INDEX IDX_ALARM_SENDER;
DROP INDEX IDX_ALARM_SENDER_GETTER;
DROP INDEX IDX_PRODUCT_SELLER;
DROP INDEX IDX_PRODUCT_CATEGORY;
*/

COMMIT;
