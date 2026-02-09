# 빠른 시작 가이드 (Quick Start)

Quantum Studio 프로젝트 개발을 시작하기 위한 핵심 정보를 정리한 문서입니다. 본 가이드는 **Java 21**, **Python 3.12**, **Next.js** 기반의 멀티 백엔드 환경에 최적화되어 있습니다.

> **최종 업데이트**: 2026-02-09 — 2개 DB 분리 구조, 멀티 프론트엔드 반영

---

## 🛠 필수 설치 항목

### 공통 필수
- **Node.js**: v22.x 이상 (LTS 권장)
- **Java**: **21** (안정화된 최신 LTS 표준)
- **Python**: **3.12** (3.13은 일부 라이브러리 미지원으로 3.12 권장)
- **Docker Desktop**: 최신 버전 (PostgreSQL, Redis 실행용)

---

## ⚙️ 초기 설정

### 1. 환경 변수 설정
루트 폴더에 `.env` 파일을 생성합니다. `.env.example`을 참고하세요.
```env
# Database (2개 분리)
DATABASE_URL=jdbc:postgresql://localhost:5432/quantum_service
AI_DATABASE_URL=postgresql+psycopg://model_dev:dev1234@localhost:5432/quantum_ai
SERVICE_DATABASE_URL=postgresql+psycopg://model_dev:dev1234@localhost:5432/quantum_service
DB_USER=model_dev
DB_PASSWORD=dev1234

# API Keys
GEMINI_API_KEY=your_gemini_api_key
JWT_SECRET=your-super-secret-key-at-least-32-chars
JWT_EXPIRE_MINUTES=60

# External Knowledge APIs
BOK_ECOS_API_KEY=your_bok_ecos_api_key
FSS_DART_API_KEY=your_fss_dart_api_key

# Social Auth (Naver)
NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret
NAVER_REDIRECT_URI=http://localhost:3000/api/auth/callback/naver
NEXT_PUBLIC_NAVER_CLIENT_ID=your_naver_client_id
```

### 2. DB 생성 (최초 1회)
DBeaver에서 기존 PostgreSQL 연결에 접속 후 스크립트를 실행합니다.
```bash
# 또는 psql로 직접 실행
psql -U model_dev -d postgres -f scripts/init_databases.sql
```
UUID 확장(`uuid-ossp`)은 별도로 실행할 필요 없습니다. Java 기동 시 Flyway V1이 `quantum_service`에, Python 기동 시 Alembic 001이 `quantum_ai`에 자동으로 활성화합니다.

### 3. 의존성 설치
```bash
# Frontend Studio
cd frontend-studio && npm install

# Frontend Admin
cd frontend-admin && npm install

# Python 가상환경 및 의존성
cd backend-python
python3.12 -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
```

---

## 🚀 개발 서버 실행 (5개 프로세스, 터미널 5개)

> **기동 순서가 중요합니다.** Java Service WAS가 먼저 기동되어야 Flyway가 quantum_service DB에 테이블을 생성하고, 이후 Python이 해당 테이블(knowledge_base)을 읽을 수 있습니다.

### [터미널 1] Java Service WAS (Port 8080) — 가장 먼저 기동
```bash
cd backend-java/quantum-api-service
../mvnw spring-boot:run
# → Flyway가 quantum_service DB에 V1~V7 테이블 자동 생성
# → 확인: DBeaver에서 quantum_service에 users, payments, projects, knowledge_base 등 7개 테이블 확인
```

### [터미널 2] Java Admin WAS (Port 8081)
```bash
cd backend-java/quantum-api-admin
../mvnw spring-boot:run
# → 동일한 quantum_service DB 접속 (지식 베이스 관리, 외부 API 연동)
# → 확인: http://localhost:8081 응답 확인
```

### [터미널 3] Python AI Engine (Port 8000)
```bash
cd backend-python
source venv/bin/activate  # Windows: venv\Scripts\activate
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
# → Alembic이 quantum_ai DB에 visualization_data, correlation_rules 등 테이블 자동 생성
# → quantum_service DB의 knowledge_base를 읽기 전용으로 연결
# → 확인: DBeaver에서 quantum_ai에 5개 테이블 확인
# → 확인: http://localhost:8000/docs 에서 Swagger UI 접속
```

### [터미널 4] Studio Frontend (Port 3000)
```bash
cd frontend-studio
npm run dev
# → http://localhost:3000 에서 Studio 접속
```

### [터미널 5] Admin Frontend (Port 3001)
```bash
cd frontend-admin
npm run dev
# → http://localhost:3001 에서 Admin 대시보드 접속
```

### 기동 확인 체크리스트

최초 기동 후 DBeaver에서 다음을 확인하세요:

**quantum_service DB (7개 테이블)**
- `users` — 사용자 (id, email, password_hash, name, provider, ...)
- `subscriptions` — 구독 플랜
- `payments` — 결제 (updated_at 컬럼 포함 확인)
- `projects` — 프로젝트 (main_category, sub_category, status 컬럼 확인)
- `knowledge_base` — 지식 베이스 (source_type 컬럼 포함 확인)
- `flyway_schema_history` — Flyway 마이그레이션 이력 (V1~V7)

**quantum_ai DB (5개 테이블)**
- `visualization_data` — 3D 시각화 매핑 (category, model_used, processing_time_ms 컬럼 확인)
- `correlation_rules` — 상관관계 규칙 (기본 데이터 4건 확인)
- `avatars` — 아바타
- `garments` — 의류
- `tryon_results` — 가상 피팅 결과
- `alembic_version` — Alembic 마이그레이션 이력

### 평소 개발 시 (3개만 기동)

Admin 작업이 아닌 경우 터미널 1, 3, 4만 기동하면 됩니다:
```bash
# 터미널 1: Java Service WAS
cd backend-java/quantum-api-service && ../mvnw spring-boot:run

# 터미널 2: Python AI Engine
cd backend-python && source venv/bin/activate && uvicorn app.main:app --port 8000 --reload

# 터미널 3: Studio Frontend
cd frontend-studio && npm run dev
```

---

## 🏛 프로젝트 가이드라인

### 1. Java 백엔드 (Lombok 미사용)
공유 개발 편의를 위해 **Lombok을 사용하지 않습니다.** Getter/Setter/Constructor는 IDE 기능을 사용하여 수동으로 생성하세요.

### 2. 프론트엔드 (디자인 시스템)
모든 UI는 `components/ui/`에 정의된 공통 컴포넌트(`Button`, `Card`, `Input`, `Modal`)를 최우선으로 사용합니다.

### 3. React 임포트
린트 에러 방지를 위해 모든 `.tsx` 파일 상단에 `import React from 'react';`를 포함하세요.

### 4. DB 스키마 변경 규칙
- `quantum_service` 테이블 변경: Flyway SQL 파일 추가 (Java)
- `quantum_ai` 테이블 변경: Alembic 리비전 추가 (Python)
- 절대로 반대쪽 마이그레이션 도구로 다른 DB의 스키마를 변경하지 마세요.

---

## 🔗 상세 문서 링크
- [프로젝트 개요](./docs/PROJECT_OVERVIEW.md)
- [디자인 시스템](./docs/FRONTEND_DESIGN_SYSTEM.md)
- [소셜 로그인 설정](./docs/SOCIAL_AUTH_SETUP.md)
- [개발 가이드](./docs/DEVELOPMENT_GUIDE.md)
- [DB 관리](./docs/README_DB.md)
- [AI 에이전트 라우팅](./docs/AI_AGENT_ROUTING.md)
