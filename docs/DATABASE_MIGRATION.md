# 🔄 데이터베이스 마이그레이션 (Migration) 개념

데이터베이스 마이그레이션이 무엇인지, 왜 필요한지 설명합니다.

---

## 🤔 마이그레이션이란?

**마이그레이션(Migration)** = 데이터베이스 스키마(구조)를 **점진적으로 변경**하는 방법

### 간단한 비유
- **집을 짓는 것**: 처음부터 모든 방을 한 번에 설계하고 짓기
- **마이그레이션**: 방을 하나씩 추가하거나 수정하기
  - 1단계: 거실 만들기
  - 2단계: 침실 추가하기
  - 3단계: 주방 확장하기
  - 4단계: 화장실 문 수리하기

---

## 📊 왜 마이그레이션이 필요한가?

### 문제 상황: 직접 SQL 실행의 한계

#### ❌ 나쁜 방법: 직접 SQL 실행
```sql
-- 개발자가 직접 실행
CREATE TABLE users (...);
CREATE TABLE subscriptions (...);
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
```

**문제점:**
1. **버전 관리 어려움**: 누가 언제 무엇을 변경했는지 추적 불가
2. **팀 협업 어려움**: 개발자마다 다른 스키마 상태
3. **롤백 불가능**: 실수로 잘못된 변경 시 되돌리기 어려움
4. **프로덕션 배포 위험**: 개발 환경과 프로덕션 환경 불일치

#### ✅ 좋은 방법: 마이그레이션 사용
```sql
-- V1__Create_users_table.sql
CREATE TABLE users (...);

-- V2__Create_subscriptions_table.sql
CREATE TABLE subscriptions (...);

-- V3__Add_phone_to_users.sql
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
```

**장점:**
1. **버전 관리**: Git으로 변경 이력 추적
2. **일관성**: 모든 환경에서 동일한 스키마
3. **롤백 가능**: 이전 버전으로 되돌리기 가능
4. **자동화**: 배포 시 자동 실행

---

## 🔄 마이그레이션 작동 원리

### 1. 마이그레이션 파일 생성

#### Java (Flyway)
```sql
-- backend-java/src/main/resources/db/migration/V1__Create_users_table.sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    ...
);
```

#### Python (Alembic)
```python
# backend-python/alembic/versions/a1b2c3d4_create_users_table.py
def upgrade():
    op.create_table('users',
        sa.Column('id', sa.UUID(), primary_key=True),
        sa.Column('email', sa.String(255), unique=True, nullable=False),
        ...
    )
```

### 2. 마이그레이션 실행

#### Java (Flyway) - 자동 실행
```bash
# Spring Boot 시작 시 자동으로 실행
mvn spring-boot:run

# Flyway가 자동으로:
# 1. 마이그레이션 파일 목록 확인
# 2. 아직 실행되지 않은 마이그레이션 찾기
# 3. 순서대로 실행
# 4. 실행 기록 저장 (flyway_schema_history 테이블)
```

#### Python (Alembic) - 수동 실행
```bash
# 마이그레이션 생성
alembic revision --autogenerate -m "create users table"

# 마이그레이션 실행
alembic upgrade head

# 마이그레이션 롤백
alembic downgrade -1
```

### 3. 실행 기록 관리

마이그레이션 도구는 실행 기록을 데이터베이스에 저장합니다:

```sql
-- Flyway가 자동 생성하는 테이블
SELECT * FROM flyway_schema_history;

-- 결과:
-- installed_rank | version | description          | type | installed_on
-- 1              | 1       | Create users table   | SQL  | 2025-12-26
-- 2              | 2       | Add phone column     | SQL  | 2025-12-27
```

이 기록을 통해:
- ✅ 이미 실행된 마이그레이션은 다시 실행하지 않음
- ✅ 새로운 마이그레이션만 실행
- ✅ 실행 순서 보장

---

## 📋 마이그레이션 예시 시나리오

### 시나리오: 사용자 테이블에 전화번호 추가하기

#### 1단계: 마이그레이션 파일 생성

**Java (Flyway)**
```sql
-- V3__Add_phone_to_users.sql
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
CREATE INDEX idx_users_phone ON users(phone);
```

**Python (Alembic)**
```bash
alembic revision --autogenerate -m "add phone to users"
```

생성된 파일:
```python
def upgrade():
    op.add_column('users', sa.Column('phone', sa.String(20)))
    op.create_index('idx_users_phone', 'users', ['phone'])

def downgrade():
    op.drop_index('idx_users_phone', 'users')
    op.drop_column('users', 'phone')
```

#### 2단계: 마이그레이션 실행

**Java**: Spring Boot 재시작 시 자동 실행
```bash
mvn spring-boot:run
# Flyway가 V3 마이그레이션 자동 실행
```

**Python**: 수동 실행
```bash
alembic upgrade head
# V3 마이그레이션 실행
```

#### 3단계: 확인

```sql
-- 테이블 구조 확인
\d users

-- 결과: phone 컬럼이 추가됨
```

#### 4단계: 문제 발생 시 롤백

**Python (Alembic)**
```bash
# 이전 버전으로 롤백
alembic downgrade -1
# phone 컬럼 제거됨
```

**Java (Flyway)**
```sql
-- Flyway는 자동 롤백을 지원하지 않으므로
-- 수동으로 롤백 마이그레이션 파일 생성 필요
-- V4__Remove_phone_from_users.sql
ALTER TABLE users DROP COLUMN phone;
```

---

## 🆚 마이그레이션 vs 직접 SQL

### 직접 SQL 실행 (`database/schema.sql`)

**용도:**
- ✅ 초기 프로젝트 설정
- ✅ 스키마 참고 및 확인
- ✅ 수동 테이블 생성 (개발 환경)

**특징:**
- 한 번만 실행
- 버전 관리 어려움
- 변경 사항 추적 불가

### 마이그레이션 (Flyway/Alembic)

**용도:**
- ✅ 프로덕션 배포
- ✅ 팀 협업
- ✅ 스키마 변경 이력 관리

**특징:**
- 점진적 변경
- 버전 관리 가능
- 실행 기록 자동 관리
- 롤백 가능

---

## 📂 프로젝트에서의 마이그레이션 구조

```
프로젝트/
├── database/
│   └── schema.sql          # 참고용 (수동 실행)
│
├── backend-java/
│   └── src/main/resources/
│       └── db/migration/   # ✅ Flyway 마이그레이션
│           ├── V1__Create_users.sql
│           ├── V2__Create_subscriptions.sql
│           └── V3__Add_phone_to_users.sql
│
└── backend-python/
    └── alembic/
        └── versions/       # ✅ Alembic 마이그레이션
            ├── a1b2c3d4_create_users.py
            ├── e5f6g7h8_create_subscriptions.py
            └── i9j0k1l2_add_phone_to_users.py
```

---

## 🎯 마이그레이션 사용 시나리오

### 시나리오 1: 새 기능 개발

1. **엔티티/모델 수정**
   ```java
   // User.java에 phone 필드 추가
   private String phone;
   ```

2. **마이그레이션 생성**
   ```bash
   # Python
   alembic revision --autogenerate -m "add phone to users"
   
   # Java
   # V3__Add_phone_to_users.sql 파일 직접 생성
   ```

3. **마이그레이션 실행**
   ```bash
   # Python
   alembic upgrade head
   
   # Java
   mvn spring-boot:run  # 자동 실행
   ```

4. **코드 커밋**
   ```bash
   git add .
   git commit -m "feat: add phone column to users"
   ```

### 시나리오 2: 팀원이 코드 받기

1. **코드 받기**
   ```bash
   git pull
   ```

2. **마이그레이션 자동 실행**
   ```bash
   # Java: Spring Boot 시작 시 자동
   mvn spring-boot:run
   
   # Python: 수동 실행
   alembic upgrade head
   ```

3. **결과**: 모든 팀원이 동일한 스키마 상태

---

## ⚠️ 주의사항

### 1. 마이그레이션 파일은 절대 수정하지 않기

```sql
-- ❌ 나쁜 예: 이미 실행된 마이그레이션 수정
-- V1__Create_users.sql (이미 실행됨)
CREATE TABLE users (...);  -- 수정하면 안 됨!

-- ✅ 좋은 예: 새 마이그레이션 생성
-- V2__Modify_users_table.sql
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
```

### 2. 마이그레이션 순서 중요

```sql
-- V1__Create_users.sql
CREATE TABLE users (...);

-- V2__Create_subscriptions.sql
CREATE TABLE subscriptions (
    user_id UUID REFERENCES users(id)  -- V1이 먼저 실행되어야 함
);
```

### 3. 프로덕션 배포 시 주의

- ✅ 백업 먼저 수행
- ✅ 테스트 환경에서 먼저 검증
- ✅ 롤백 계획 준비

---

## 📚 요약

### 마이그레이션이란?
- 데이터베이스 스키마를 **점진적으로 변경**하는 방법
- **버전 관리**가 가능한 데이터베이스 변경 이력

### 왜 필요한가?
- ✅ 팀 협업: 모든 개발자가 동일한 스키마
- ✅ 버전 관리: 변경 이력 추적
- ✅ 롤백 가능: 문제 발생 시 되돌리기
- ✅ 자동화: 배포 시 자동 실행

### 어떻게 사용하는가?
- **Java**: Flyway (`backend-java/src/main/resources/db/migration/`)
  - ✅ **자동 실행**: Spring Boot 시작 시 자동으로 체크하고 실행
- **Python**: Alembic (`backend-python/alembic/versions/`)
  - ❌ **수동 실행**: `alembic upgrade head` 명령어 필요

### 자동 실행 원리
- **Java (Flyway)**: 
  - Spring Boot 시작 시 `flyway_schema_history` 테이블 확인
  - 실행되지 않은 마이그레이션 파일 자동 실행
  - 실행 기록 저장하여 중복 실행 방지
  
- **Python (Alembic)**:
  - 수동으로 `alembic upgrade head` 실행
  - 자동화하려면 FastAPI startup 이벤트에 추가

> **자세한 내용**: [마이그레이션 자동 실행 원리](./DATABASE_MIGRATION_AUTO.md)

---

*마이그레이션은 데이터베이스 스키마를 코드처럼 관리하는 방법입니다!*

