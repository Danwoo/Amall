# Amall 선물 기능 DB 스키마 적용 가이드

## 📋 개요

Phase 9-3에서 구현된 선물 기능을 사용하기 위해 데이터베이스 스키마를 확장해야 합니다.

---

## 🗂️ 변경 사항

### 1. CART 테이블 확장
- **IS_GIFT** (CHAR(1)): 선물 여부 ('Y' or 'N')
- **GIFT_TO_MEMBER_ID** (VARCHAR2(50)): 선물 받는 사람 ID
- **GIFT_MESSAGE** (VARCHAR2(500)): 선물 메시지

### 2. 새 테이블 추가
- **ORDERS**: 주문 관리 (일반 구매 + 선물)
- **ORDER_ITEM**: 주문 상세 (주문별 상품 목록)
- **GIFT_DELIVERY_REQUEST**: 선물 배송지 입력 요청 (프라이버시 보호)

---

## 🔧 적용 방법

### 방법 1: SQL Developer 사용

1. **Oracle SQL Developer** 실행
2. 데이터베이스 연결 (Amall DB)
3. `src/main/resources/db/gift-feature-schema.sql` 파일 열기
4. **전체 스크립트 실행** (F5)
5. 결과 확인

### 방법 2: SQL*Plus 사용

```bash
# SQL*Plus 접속
sqlplus 사용자명/비밀번호@데이터베이스

# SQL 파일 실행
@/path/to/AmallProject/src/main/resources/db/gift-feature-schema.sql

# 종료
EXIT;
```

---

## ✅ 적용 확인

### 1. CART 테이블 컬럼 확인

```sql
DESC CART;

-- 결과에 다음 컬럼들이 있어야 함:
-- IS_GIFT            CHAR(1)
-- GIFT_TO_MEMBER_ID  VARCHAR2(50)
-- GIFT_MESSAGE       VARCHAR2(500)
```

### 2. 새 테이블 확인

```sql
-- 테이블 목록 조회
SELECT table_name FROM user_tables
WHERE table_name IN ('ORDERS', 'ORDER_ITEM', 'GIFT_DELIVERY_REQUEST');

-- 결과:
-- ORDERS
-- ORDER_ITEM
-- GIFT_DELIVERY_REQUEST
```

### 3. 시퀀스 확인

```sql
SELECT sequence_name FROM user_sequences
WHERE sequence_name IN ('SEQ_ORDER_ITEM', 'SEQ_GIFT_DELIVERY_REQUEST');

-- 결과:
-- SEQ_ORDER_ITEM
-- SEQ_GIFT_DELIVERY_REQUEST
```

---

## 🧪 테스트 데이터 (선택 사항)

스키마 적용 후 테스트하려면 아래 SQL을 실행하세요:

```sql
-- 1. 테스트 회원 생성 (커플)
INSERT INTO MEMBER (MEMBER_ID, MEMBER_NAME, MEMBER_PWD, MEMBER_EMAIL, MEMBER_PHONE,
                    MEMBER_SEX, MEMBER_MATCH_ID, MEMBER_HASH)
VALUES ('testuser1', '테스트1', '$2a$10$...', 'test1@test.com', '01012345678',
        1, 'testuser2', 'hash1');

INSERT INTO MEMBER (MEMBER_ID, MEMBER_NAME, MEMBER_PWD, MEMBER_EMAIL, MEMBER_PHONE,
                    MEMBER_SEX, MEMBER_MATCH_ID, MEMBER_HASH)
VALUES ('testuser2', '테스트2', '$2a$10$...', 'test2@test.com', '01087654321',
        2, 'testuser1', 'hash2');

-- 2. 위시리스트 추가 (testuser2의 위시리스트)
INSERT INTO WISHLIST (WISHLIST_ID, WISHLIST_REGDATE, MEMBER_ID, PROD_NUM)
VALUES (99991, TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI'), 'testuser2', 1);

-- 3. 선물 장바구니 추가 (testuser1 → testuser2)
INSERT INTO CART (CART_ID, CART_QUANTITY, CART_REG_DATE, MEMBER_ID, PROD_NUM,
                  IS_GIFT, GIFT_TO_MEMBER_ID, GIFT_MESSAGE)
VALUES (99992, 1, TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'),
        'testuser1', 1, 'Y', 'testuser2', '생일 축하해! 🎁');

COMMIT;
```

---

## 🔄 롤백 (필요 시)

스키마 변경을 되돌리려면:

```sql
-- 외래 키 제약 조건 때문에 순서 중요!
DROP TABLE GIFT_DELIVERY_REQUEST;
DROP TABLE ORDER_ITEM;
DROP TABLE ORDERS;

DROP SEQUENCE SEQ_ORDER_ITEM;
DROP SEQUENCE SEQ_GIFT_DELIVERY_REQUEST;

-- CART 테이블 컬럼 삭제
ALTER TABLE CART DROP COLUMN IS_GIFT;
ALTER TABLE CART DROP COLUMN GIFT_TO_MEMBER_ID;
ALTER TABLE CART DROP COLUMN GIFT_MESSAGE;

COMMIT;
```

---

## ⚠️ 주의사항

### 1. 백업 필수
```sql
-- 테이블 백업 (중요!)
CREATE TABLE CART_BACKUP AS SELECT * FROM CART;
```

### 2. 프로덕션 환경
- **운영 DB에 직접 적용하지 마세요!**
- 먼저 개발/테스트 환경에서 검증 필요
- 점검 시간을 공지하고 적용

### 3. 기존 데이터
- CART 테이블에 기존 데이터가 있어도 안전 (기본값: IS_GIFT='N')
- NULL이 허용되는 컬럼만 추가되므로 문제없음

---

## 📊 스키마 다이어그램

```
MEMBER
  └─ MEMBER_ID (PK)
       ├─ CART.MEMBER_ID (FK)
       ├─ CART.GIFT_TO_MEMBER_ID (FK, 선물 받는 사람)
       ├─ WISHLIST.MEMBER_ID (FK)
       ├─ ORDERS.MEMBER_ID (FK, 주문자)
       ├─ ORDERS.GIFT_FROM_MEMBER_ID (FK, 선물 보낸 사람)
       └─ GIFT_DELIVERY_REQUEST.TO_MEMBER_ID (FK)

PRODUCT
  └─ PROD_NUM (PK)
       ├─ CART.PROD_NUM (FK)
       ├─ WISHLIST.PROD_NUM (FK)
       └─ ORDER_ITEM.PROD_NUM (FK)

ORDERS
  └─ ORDER_ID (PK)
       ├─ ORDER_ITEM.ORDER_ID (FK)
       └─ GIFT_DELIVERY_REQUEST.ORDER_ID (FK)
```

---

## 🚀 적용 후 할 일

1. ✅ 애플리케이션 재시작
2. ✅ API 테스트
   - POST /api/cart/{memberId}/gift-items
   - GET /api/wishlist/{memberId}/partner
3. ✅ Frontend 테스트
   - 위시리스트 페이지 접속
   - 선물하기 버튼 클릭
   - 장바구니 추가 확인

---

## 📞 문제 발생 시

### ORA-00904: invalid identifier
- 컬럼명 오타 확인
- 대소문자 확인 (Oracle은 대문자로 저장)

### ORA-02264: name already used by an existing constraint
- 제약 조건 이름 중복
- 롤백 후 다시 적용

### ORA-01400: cannot insert NULL
- NOT NULL 컬럼에 NULL 입력 시도
- 기본값 확인

---

## ✅ 체크리스트

- [ ] DB 백업 완료
- [ ] gift-feature-schema.sql 실행 완료
- [ ] CART 테이블 컬럼 추가 확인
- [ ] ORDERS, ORDER_ITEM, GIFT_DELIVERY_REQUEST 테이블 생성 확인
- [ ] 시퀀스 생성 확인
- [ ] 인덱스 생성 확인
- [ ] 테스트 데이터 입력 (선택)
- [ ] 애플리케이션 재시작
- [ ] API 테스트 완료
- [ ] Frontend 동작 확인

---

완료되면 Phase 9-3 선물 기능을 실제로 사용할 수 있습니다! 🎉
