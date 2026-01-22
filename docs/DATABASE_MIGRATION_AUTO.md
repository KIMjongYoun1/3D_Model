# 🤖 마이그레이션 자동 실행 원리

프레임워크가 어떻게 데이터베이스를 자동으로 체크하고 생성하는지 설명합니다.

---

## 🎯 핵심 개념

**네, 맞습니다!** 프레임워크가 애플리케이션 시작 시 자동으로:
1. ✅ 데이터베이스 스키마 상태를 **체크**
2. ✅ 마이그레이션 파일과 비교
3. ✅ 실행되지 않은 마이그레이션을 **자동 실행**

---

## 🔄 Java (Flyway) - 자동 실행

### 작동 원리

#### 1. Spring Boot 시작 시

```java
@SpringBootApplication
public class VirtualTryOnApplication {
    public static void main(String[] args) {
        SpringApplication.run(VirtualTryOnApplication.class, args);
        // ↑ 이 시점에 Flyway가 자동 실행됨
    }
}
```

#### 2. Flyway가 자동으로 수행하는 작업

```java
// 내부적으로 이런 작업을 수행 (의사코드)
1. flyway_schema_history 테이블 확인
   - 없으면 생성
   - 있으면 실행 기록 조회

2. db/migration/ 디렉토리에서 마이그레이션 파일 스캔
   - V1__Create_users.sql
   - V2__Create_subscriptions.sql
   - V3__Add_phone_to_users.sql

3. 실행 기록과 비교
   - flyway_schema_history에 V1, V2가 있음
   - V3는 없음 → 새로 실행해야 함

4. V3 마이그레이션 자동 실행
   ALTER TABLE users ADD COLUMN phone VARCHAR(20);

5. 실행 기록 저장
   INSERT INTO flyway_schema_history (version, description, ...)
   VALUES ('3', 'Add phone to users', ...);
```

### 실제 동작 예시

#### 초기 개발 환경 (데이터베이스가 비어있음)

```bash
# 1. 빈 데이터베이스
psql -d virtual_tryon
> \dt
# 결과: 테이블 없음

# 2. Spring Boot 시작
mvn spring-boot:run

# 3. Flyway가 자동으로:
#    - flyway_schema_history 테이블 생성
#    - V1__Create_users.sql 실행 → users 테이블 생성
#    - V2__Create_subscriptions.sql 실행 → subscriptions 테이블 생성
#    - 실행 기록 저장

# 4. 결과 확인
psql -d virtual_tryon
> \dt
# 결과:
# - flyway_schema_history (Flyway가 생성)
# - users (V1 마이그레이션으로 생성)
# - subscriptions (V2 마이그레이션으로 생성)
```

#### 기존 데이터베이스에 새 마이그레이션 추가

```bash
# 1. 현재 상태
# - users 테이블 존재
# - subscriptions 테이블 존재
# - flyway_schema_history에 V1, V2 기록

# 2. 새 마이그레이션 파일 추가
# V3__Add_phone_to_users.sql 생성

# 3. Spring Boot 재시작
mvn spring-boot:run

# 4. Flyway가 자동으로:
#    - flyway_schema_history 확인
#    - V1, V2는 이미 실행됨 (스킵)
#    - V3는 새로 발견 → 자동 실행
#    - ALTER TABLE users ADD COLUMN phone 실행
#    - 실행 기록 저장

# 5. 결과
# - users 테이블에 phone 컬럼 추가됨
```

### 설정 확인

```yaml
# backend-java/src/main/resources/application.yml
spring:
  flyway:
    enabled: true                    # Flyway 활성화
    locations: classpath:db/migration  # 마이그레이션 파일 위치
    baseline-on-migrate: true        # 기존 DB에 baseline 적용
```

---

## 🐍 Python (Alembic) - 수동 실행

### 작동 원리

Python은 **자동 실행이 아닌 수동 실행**입니다.

#### 1. Alembic 초기화

```bash
cd backend-python
alembic init alembic
```

#### 2. 마이그레이션 생성

```bash
# 모델 변경 후
alembic revision --autogenerate -m "create users table"
# → alembic/versions/a1b2c3d4_create_users_table.py 생성
```

#### 3. 수동 실행 필요

```bash
# 마이그레이션 실행 (수동)
alembic upgrade head

# 또는 특정 버전까지
alembic upgrade +1  # 다음 버전으로
alembic downgrade -1  # 이전 버전으로
```

### 자동화 방법 (선택사항)

FastAPI 시작 시 자동 실행하려면:

```python
# backend-python/app/main.py
from alembic.config import Config
from alembic import command

@app.on_event("startup")
async def startup_event():
    """애플리케이션 시작 시 마이그레이션 자동 실행"""
    alembic_cfg = Config("alembic.ini")
    command.upgrade(alembic_cfg, "head")
    print("✅ 데이터베이스 마이그레이션 완료")
```

---

## 📊 비교: Flyway vs Alembic

| 특징 | Flyway (Java) | Alembic (Python) |
|------|---------------|------------------|
| **실행 방식** | ✅ 자동 (Spring Boot 시작 시) | ❌ 수동 (`alembic upgrade head`) |
| **파일 형식** | SQL 파일 | Python 파일 |
| **실행 기록** | `flyway_schema_history` 테이블 | `alembic_version` 테이블 |
| **롤백** | 수동 마이그레이션 파일 필요 | `downgrade()` 함수 자동 생성 |

---

## 🔍 실제 동작 시나리오

### 시나리오 1: 처음 프로젝트 시작 (빈 데이터베이스)

#### Java (Flyway)
```bash
# 1. 빈 데이터베이스
createdb virtual_tryon

# 2. 마이그레이션 파일 준비
backend-java/src/main/resources/db/migration/
├── V1__Create_users.sql
└── V2__Create_subscriptions.sql

# 3. Spring Boot 시작
mvn spring-boot:run

# 4. Flyway가 자동으로:
#    ✅ flyway_schema_history 테이블 생성
#    ✅ V1 실행 → users 테이블 생성
#    ✅ V2 실행 → subscriptions 테이블 생성
#    ✅ 실행 기록 저장

# 결과: 모든 테이블이 자동으로 생성됨!
```

#### Python (Alembic)
```bash
# 1. 빈 데이터베이스
createdb virtual_tryon

# 2. 마이그레이션 파일 준비
alembic revision --autogenerate -m "create users table"

# 3. 수동 실행 필요
alembic upgrade head

# 결과: 테이블 생성됨
```

### 시나리오 2: 새 기능 추가 (기존 데이터베이스에 컬럼 추가)

#### Java (Flyway)
```bash
# 1. 새 마이그레이션 파일 생성
# V3__Add_phone_to_users.sql

# 2. Spring Boot 재시작
mvn spring-boot:run

# 3. Flyway가 자동으로:
#    ✅ flyway_schema_history 확인
#    ✅ V1, V2는 이미 실행됨 (스킵)
#    ✅ V3는 새로 발견 → 자동 실행
#    ✅ phone 컬럼 추가

# 결과: 자동으로 변경사항 적용됨!
```

#### Python (Alembic)
```bash
# 1. 모델 수정 (User에 phone 필드 추가)
# app/models/user.py

# 2. 마이그레이션 생성
alembic revision --autogenerate -m "add phone to users"

# 3. 수동 실행
alembic upgrade head

# 결과: phone 컬럼 추가됨
```

---

## ⚙️ Flyway 실행 기록 테이블

Flyway가 자동으로 생성하는 테이블:

```sql
-- flyway_schema_history 테이블 구조
CREATE TABLE flyway_schema_history (
    installed_rank INT PRIMARY KEY,
    version VARCHAR(50),
    description VARCHAR(200),
    type VARCHAR(20),
    script VARCHAR(1000),
    checksum INT,
    installed_by VARCHAR(100),
    installed_on TIMESTAMP,
    execution_time INT,
    success BOOLEAN
);

-- 실행 기록 예시
SELECT * FROM flyway_schema_history;

-- 결과:
-- installed_rank | version | description          | success | installed_on
-- 1              | 1       | Create users table   | true    | 2025-12-26
-- 2              | 2       | Create subscriptions | true    | 2025-12-26
-- 3              | 3       | Add phone to users   | true    | 2025-12-27
```

이 테이블을 통해:
- ✅ 어떤 마이그레이션이 실행되었는지 확인
- ✅ 실행 순서 보장
- ✅ 중복 실행 방지

---

## 🎯 요약

### Java (Flyway) - 자동 실행 ✅
1. Spring Boot 시작 시 자동 실행
2. `flyway_schema_history` 테이블로 실행 기록 관리
3. 실행되지 않은 마이그레이션만 자동 실행
4. **개발자가 별도 작업 불필요**

### Python (Alembic) - 수동 실행
1. `alembic upgrade head` 명령어로 수동 실행
2. 자동화하려면 FastAPI startup 이벤트에 추가
3. **개발자가 직접 실행 필요**

### 공통점
- ✅ 실행 기록 관리 (중복 실행 방지)
- ✅ 순서 보장 (버전 번호로)
- ✅ 롤백 가능

---

## 💡 실전 팁

### Java에서 마이그레이션 비활성화 (필요 시)

```yaml
# application.yml
spring:
  flyway:
    enabled: false  # 마이그레이션 비활성화
```

### Python에서 자동 실행 설정

```python
# app/main.py
from alembic.config import Config
from alembic import command

@app.on_event("startup")
async def startup_event():
    """마이그레이션 자동 실행"""
    alembic_cfg = Config("alembic.ini")
    command.upgrade(alembic_cfg, "head")
```

---

*Flyway는 자동으로, Alembic은 수동으로 실행됩니다!*




