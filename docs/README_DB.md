# 🗄️ 데이터베이스 관리 (Database Management)

Quantum Studio의 데이터베이스 스키마와 관리 방법을 설명합니다.

> **최종 업데이트**: 2026-02-09 — 2개 DB 물리 분리 구조 적용

---

## 📂 데이터베이스 분리 구조

서비스 역할에 따라 **2개의 독립 PostgreSQL 데이터베이스**를 사용합니다.

| DB 이름 | 마이그레이션 | 소유 서비스 | 역할 |
| :--- | :--- | :--- | :--- |
| **quantum_service** | Flyway (Java) | Service WAS, Admin WAS | 인증, 결제, 프로젝트, 지식 베이스 |
| **quantum_ai** | Alembic (Python) | Python AI Engine | 시각화, 상관관계, 가상 피팅 |

### 테이블 소유권

**quantum_service** (Flyway 전담)
- `users` — 사용자 정보
- `subscriptions` — 구독 플랜
- `payments` — 결제 내역
- `projects` — 프로젝트
- `knowledge_base` — 지식 베이스 (RAG 원본 데이터)

**quantum_ai** (Alembic 전담)
- `visualization_data` — 3D 시각화 매핑 데이터
- `correlation_rules` — 노드 간 상관관계 규칙
- `avatars` — 사용자 아바타
- `garments` — 의류 데이터
- `tryon_results` — 가상 피팅 결과

### Cross-DB 접근

- Python AI Engine은 `quantum_service`의 `knowledge_base` 테이블을 **읽기 전용**으로 접근합니다.
- `user_id` 참조는 FK가 아닌 **application 레벨**에서 보장됩니다 (cross-DB FK 불가).

---

## 📂 마이그레이션 구조

- **Java (Flyway)**: `backend-java/quantum-api-service/src/main/resources/db/migration/`
  - V1~V6: users, subscriptions, payments, social_auth, refresh_token, knowledge_base
- **Python (Alembic)**: `backend-python/alembic/versions/`
  - 001: UUID 확장 활성화 (users 테이블은 Flyway가 담당)
  - 002: avatars, garments, tryon_results
  - 003: visualization_data
  - 004: correlation_rules

---

## 🚀 DB 초기화 및 테이블 생성

### 1. DB 생성 (최초 1회)
DBeaver에서 기존 PostgreSQL 연결(postgres)에 접속 후 다음 스크립트를 실행합니다.

```sql
-- scripts/init_databases.sql
CREATE DATABASE quantum_service OWNER model_dev ENCODING 'UTF8' TEMPLATE template0;
CREATE DATABASE quantum_ai OWNER model_dev ENCODING 'UTF8' TEMPLATE template0;
```

각 DB에 접속하여 UUID 확장을 활성화합니다.
```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

### 2. DBeaver 연결 등록
생성 후 DBeaver에 새 연결 2개를 등록하세요:
- **quantum_service**: Host=localhost, Port=5432, Database=quantum_service, User=model_dev
- **quantum_ai**: Host=localhost, Port=5432, Database=quantum_ai, User=model_dev

### 3. 테이블 생성 (자동)
- **Java**: Service WAS 또는 Admin WAS 기동 시 Flyway가 `quantum_service`에 자동 마이그레이션
- **Python**: AI Engine 기동 시 Alembic이 `quantum_ai`에 자동 마이그레이션

---

## ⚠️ 주의사항
- **스키마 소유권**: `quantum_service`의 스키마 변경은 반드시 Flyway(Java)에서만 수행합니다. Python은 읽기 전용입니다.
- **JDBC URL 형식**: Java는 `jdbc:postgresql://`, Python은 `postgresql+psycopg://` 형식을 사용합니다.
- **커넥션 풀**: Python의 Service DB 연결은 `pool_size=3`으로 작게 설정되어 있습니다 (읽기 전용이므로).

---

## 📚 관련 문서
- [ERD 설계](./design/ERD.md)
- [빠른 시작 가이드](../QUICK_START.md)
- [개발 가이드](./DEVELOPMENT_GUIDE.md)
