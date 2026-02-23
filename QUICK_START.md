# 빠른 시작 가이드 (Quick Start)

Quantum Studio 프로젝트 개발을 시작하기 위한 핵심 정보를 정리한 문서입니다. 본 가이드는 **Java 21**, **Python 3.12**, **Next.js** 기반의 멀티 백엔드 환경에 최적화되어 있습니다.

> **최종 업데이트**: 2026-02-10 — Admin AI 서버 분리, 6개 프로세스 구조, Ollama/Gemini LLM 연동

---

## 🛠 필수 설치 항목

### 공통 필수
- **Node.js**: v22.x 이상 (LTS 권장)
- **Java**: **21** (안정화된 최신 LTS 표준)
- **Python**: **3.10 이상** (3.12 권장). 기동 시 `python3` 명령어 사용.
- **Docker Desktop**: 최신 버전 (PostgreSQL, Redis 실행용)

### 선택 (Admin AI)
- **Ollama**: 로컬 LLM 실행 (`brew install ollama` → `ollama pull llama3.2` → `ollama serve`)
- Ollama가 없으면 `GEMINI_API_KEY` 환경변수 설정 시 Gemini API로 자동 폴백

---

## ⚙️ 초기 설정

### 1. 환경 변수 설정
루트 폴더에 `.env` 파일을 생성합니다. `.env.example`을 참고하세요.

**DB 2개 구분** — 같은 이름을 두 번 쓰면 나중 값만 적용되므로, 아래처럼 변수명을 구분해 두세요.
| 변수 | 용도 | 사용하는 앱 |
|------|------|-------------|
| `DATABASE_URL` | quantum_service (JDBC) | Java Service WAS, Admin WAS |
| `AI_DATABASE_URL` | quantum_ai (SQLAlchemy) | Python AI Engine |
| `SERVICE_DATABASE_URL` | quantum_service (SQLAlchemy, 읽기 전용) | Python AI, Admin AI Server |

```env
# Database (2개 분리, 변수명 구분)
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
LAW_API_OC=your_law_api_oc

# Social Auth (Naver)
NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret
NAVER_REDIRECT_URI=http://localhost:3000/api/auth/callback/naver
NEXT_PUBLIC_NAVER_CLIENT_ID=your_naver_client_id
```

### 2. DB 생성 (최초 1회)
**방법 A — DBeaver에서 수동 생성**  
기존 PostgreSQL 연결(postgres DB)에서 SQL 스크립트 창을 열고 아래 두 개만 실행해도 됩니다.

```sql
CREATE DATABASE quantum_service
    OWNER model_dev
    ENCODING 'UTF8'
    LC_COLLATE 'en_US.UTF-8'
    LC_CTYPE 'en_US.UTF-8'
    TEMPLATE template0;

CREATE DATABASE quantum_ai
    OWNER model_dev
    ENCODING 'UTF8'
    LC_COLLATE 'en_US.UTF-8'
    LC_CTYPE 'en_US.UTF-8'
    TEMPLATE template0;
```

**방법 B — 스크립트로 한 번에**
```bash
psql -U model_dev -d postgres -f scripts/init_databases.sql
```

- **테이블은 만들지 않아도 됩니다.** Service WAS 기동 시 Flyway가 `quantum_service`에, Python AI 기동 시 Alembic이 `quantum_ai`에 테이블을 자동 생성합니다.
- UUID 확장(`uuid-ossp`)도 각 앱 기동 시 마이그레이션에서 자동 활성화됩니다.

### 3. 의존성 설치
```bash
# Frontend Studio
cd frontend-studio && npm install

# Frontend Admin
cd frontend-admin && npm install

# Python AI Engine (Studio용) — venv는 반드시 backend-python 안에 생성
cd backend-python
python3 -m venv venv
source venv/bin/activate   # Windows: venv\Scripts\activate
pip install -r requirements.txt

# Python Admin AI Server — venv는 반드시 backend-admin-ai 안에 생성
cd backend-admin-ai
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

**Python 기동 시 주의:** `source venv/bin/activate`와 `uvicorn`은 **해당 프로젝트 디렉터리(backend-python 또는 backend-admin-ai)에서** 실행해야 합니다. 루트나 다른 폴더에서 하면 `venv`를 찾지 못하거나 `uvicorn`/`app.main`을 찾지 못합니다.

---

## 🚀 개발 서버 실행

### 기동 순서 규칙 (반드시 준수)

```
[순서 1] Java Service WAS (:8080)
    │     Flyway V1~V8 → quantum_service DB에 테이블 생성
    │     (users, admin_users, payments, projects, knowledge_base ...)
    │     이 서버가 먼저 떠야 DB 스키마가 만들어짐
    ▼
[순서 2] Java Admin WAS (:8081)
    │     같은 quantum_service DB 접속 (Flyway 비활성화, JPA validate만)
    │     admin_users 테이블로 관리자 전용 인증
    ▼
[순서 3] Python AI Engine (:8000) — Studio용
    │     Alembic → quantum_ai DB에 테이블 생성
    │     + quantum_service의 knowledge_base를 읽기 전용 연결
    │     순서 1이 완료되어 knowledge_base 테이블이 있어야 정상 연결
    ▼
[순서 4] Admin AI Server (:8002) — NEW
    │     quantum_service DB 읽기 전용 (분석/조회만)
    │     Ollama(Llama 3.2) 또는 Gemini 폴백 LLM 연동
    │     순서 1이 완료되어 테이블이 있어야 정상 연결
    ▼
[순서 5] Studio Frontend (:3000)
    │     백엔드 API가 먼저 떠 있어야 데이터 로드 가능
    ▼
[순서 6] Admin Frontend (:3001)
          Admin WAS + Admin AI Server가 먼저 떠 있어야 기능 정상 동작
```

**반드시 지켜야 하는 것:**
- Java Service WAS(순서 1)는 **무조건 가장 먼저** 기동 — Flyway V1~V8이 여기서 모든 테이블 생성
- Java Admin WAS(순서 2)는 Service WAS **기동 완료 후** 기동 — Flyway 비활성화 상태이지만 JPA validate가 테이블 존재를 확인함
- Python AI Engine(순서 3)은 **Java 기동 완료 후** 기동 — knowledge_base 테이블이 없으면 연결 에러
- Admin AI Server(순서 4)는 **Java 기동 완료 후** 기동 — quantum_service 테이블을 읽기 전용으로 사용

**순서 무관한 것:**
- Frontend(순서 5, 6)는 서로 순서 상관없음, 백엔드만 떠 있으면 됨
- 순서 3과 4는 서로 독립적 (병렬 기동 가능)

---

### 한 번에 전체 기동 (스크립트)

프로젝트 루트에서 **한 스크립트**로 프론트엔드·백엔드 전체를 기동합니다.  
로그는 `.run/logs/` 에 저장됩니다.

```bash
# 전체 기동 (루트에서 실행)
./start.sh

# 기동 + 로그 실시간 스트리밍 (한 터미널에서 기동과 로그 확인)
./start.sh --tail

# 종료
./stop.sh

# 로그만 별도 확인 (기동 후 다른 터미널에서)
./scripts/tail-logs.sh           # 전체 로그
./scripts/tail-logs.sh service    # Service WAS만
```

- **필수**: Java, Node, Python, PostgreSQL 준비 후 실행.  
- **최초 실행 시**: `frontend-studio`, `frontend-admin` 에서 `npm install`,  
  `backend-python`, `backend-admin-ai` 에서 `venv` 생성 및 `pip install -r requirements.txt` 선행.

**특정 서비스만 기동/에러 확인 (포그라운드)**  
한 개만 터미널에서 직접 띄워서 로그를 보려면:

```bash
./scripts/start-one.sh service    # Java Service WAS (8080)
./scripts/start-one.sh admin      # Java Admin WAS (8081)
./scripts/start-one.sh python-ai  # Python AI (8000)
./scripts/start-one.sh admin-ai    # Admin AI (8002)
./scripts/start-one.sh studio-fe   # Studio Frontend (3000)
./scripts/start-one.sh admin-fe    # Admin Frontend (3001)
```

- 인자 없이 실행하면 사용 가능한 서비스 목록이 출력됩니다.
- Java가 안 뜨면 `./scripts/start-one.sh service` 로 실행해 보며 콘솔 에러를 확인하세요.

---

### 전체 기동 명령어 (터미널 6개)

#### [터미널 1] Java Service WAS — 가장 먼저
```bash
cd backend-java/quantum-api-service
../mvnw spring-boot:run
```
- Flyway가 `quantum_service` DB에 V1~V8 자동 마이그레이션 (users, admin_users, payments, projects, knowledge_base 등)
- `Started ServiceApplication` 로그가 나오면 기동 완료
- 확인: http://localhost:8080

#### [터미널 2] Java Admin WAS — 터미널 1 기동 완료 후
```bash
cd backend-java/quantum-api-admin
../mvnw spring-boot:run
```
- 동일한 `quantum_service` DB 접속 (Flyway 비활성화, JPA validate만 수행)
- `admin_users` 테이블로 관리자 전용 인증 (일반 사용자 JWT로 접근 불가)
- Admin 로그인: `POST /api/admin/auth/login`, 계정 생성: `POST /api/admin/auth/register`
- `Started AdminApplication` 로그가 나오면 기동 완료
- 확인: http://localhost:8081

#### [터미널 3] Python AI Engine — 터미널 1 기동 완료 후
```bash
cd backend-python
source venv/bin/activate
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
- **위 세 줄은 한 번에 순서대로.** `venv`가 없으면 먼저 `python3 -m venv venv` 후 `pip install -r requirements.txt` 실행.
- Alembic이 `quantum_ai` DB에 테이블 자동 생성
- `quantum_service` DB의 knowledge_base를 읽기 전용 연결
- 확인: http://localhost:8000/docs (Swagger UI)

#### [터미널 4] Admin AI Server — 터미널 1 기동 완료 후 (NEW)
```bash
# (선택) Ollama가 미실행 시: ollama serve  (별도 터미널에서)
cd backend-admin-ai
source venv/bin/activate
uvicorn app.main:app --host 0.0.0.0 --port 8002 --reload
```
- **venv 없으면:** `python3 -m venv venv` → `source venv/bin/activate` → `pip install -r requirements.txt` 후 위 uvicorn 실행.
- `quantum_service` DB 읽기 전용 (결제, 사용자, 지식 데이터 분석)
- Ollama(Llama 3.2) 우선 사용, 연결 실패 시 Gemini API로 자동 폴백
- Ollama/Gemini 모두 없으면 안내 메시지 반환 (서버는 정상 기동됨)
- 확인: http://localhost:8002/health
- Swagger: http://localhost:8002/docs

#### [터미널 5] Studio Frontend — 백엔드 기동 후
```bash
cd frontend-studio
npm run dev
```
- 확인: http://localhost:3000

#### [터미널 6] Admin Frontend — 백엔드 기동 후
```bash
cd frontend-admin
npm run dev
```
- 확인: http://localhost:3001
- AI 어시스턴트 페이지: http://localhost:3001/ai

---

### 최초 기동 후 DBeaver 확인 체크리스트

**quantum_service DB (8개 테이블)**
- `users` — id, email, password_hash, name, provider, provider_id, mobile, refresh_token, subscription, created_at, updated_at, deleted_at
- `admin_users` — id, email, password_hash, name, role(SUPER_ADMIN/ADMIN/OPERATOR), is_active, created_at, updated_at, last_login_at
- `subscriptions` — id, user_id, plan_type, status, tryon_limit, tryon_used, ...
- `payments` — id, user_id, payment_method, amount, status, pg_provider, updated_at, ...
- `projects` — id, user_id, name, description, main_category, sub_category, status, created_at, updated_at
- `knowledge_base` — id, category, title, content, source_url, source_type, is_active, created_at, updated_at
- `flyway_schema_history` — Flyway 마이그레이션 이력 (version 1~8 확인)

**quantum_ai DB (3개 테이블, 데이터 시각화 전용)**
- `visualization_data` — id, user_id, data_type, raw_data, mapping_data, category, model_used, processing_time_ms, ...
- `correlation_rules` — id, category, keywords, strength, label, is_active (기본 데이터 4건 확인)
- `alembic_version` — Alembic 마이그레이션 이력 (version_num = 005 확인)

---

### 평소 개발 시 (터미널 3개만)

Admin 관련 작업이 아닌 경우 아래 3개만 기동하면 Studio가 정상 동작합니다.

```bash
# [터미널 1] Java Service WAS — 항상 먼저
cd backend-java/quantum-api-service && ../mvnw spring-boot:run

# [터미널 2] Python AI Engine — 터미널 1 기동 완료 후
cd backend-python && source venv/bin/activate && uvicorn app.main:app --port 8000 --reload

# [터미널 3] Studio Frontend — 백엔드 기동 후
cd frontend-studio && npm run dev
```

### Admin 전용 개발 시 (터미널 4개)

Admin 페이지 + AI 프롬프트 기능 개발 시:

```bash
# [터미널 1] Java Service WAS — 항상 먼저
cd backend-java/quantum-api-service && ../mvnw spring-boot:run

# [터미널 2] Java Admin WAS — 터미널 1 기동 완료 후
cd backend-java/quantum-api-admin && ../mvnw spring-boot:run

# [터미널 3] Admin AI Server — 터미널 1 기동 완료 후
cd backend-admin-ai && source venv/bin/activate && uvicorn app.main:app --port 8002 --reload

# [터미널 4] Admin Frontend — 백엔드 기동 후
cd frontend-admin && npm run dev
```

---

### Python 기동 시 `venv` / `uvicorn` 오류일 때

- **`source: no such file or directory: venv/bin/activate`**  
  → `venv`는 **해당 Python 프로젝트 폴더 안**에 있어야 합니다. 루트가 아니라 `backend-python` 또는 `backend-admin-ai`로 이동한 뒤 사용하세요.

- **`uvicorn: command not found`**  
  → 가상환경이 활성화된 상태에서 `uvicorn`이 없으면, 해당 폴더에서 `pip install -r requirements.txt`를 한 번 더 실행하세요.

**Python AI Engine (8000) — 프로젝트 루트에서 실행:**
```bash
cd backend-python
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

**Admin AI (8002) — 프로젝트 루트에서 실행:**
```bash
cd backend-admin-ai
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8002 --reload
```

(이미 `venv`가 있고 의존성도 설치했다면, `cd` → `source venv/bin/activate` → `uvicorn` 세 줄만 반복하면 됩니다. **Windows**에서는 `venv\Scripts\activate` 사용.)

- **`alembic_version` / UniqueViolation 마이그레이션 실패**  
  Python AI(8000) 기동 시 `alembic_version already exists` 또는 `duplicate key` 로그가 나와도 **앱은 기동됩니다**.  
  스키마가 이미 최신이면 무시해도 됩니다.  
  수동으로 마이그레이션만 맞추고 싶다면:
  ```bash
  cd backend-python && source venv/bin/activate && alembic upgrade head
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
- `quantum_service` 테이블 변경: Flyway SQL 파일 추가 (Java, quantum-api-service에서만 관리)
- `quantum_ai` 테이블 변경: Alembic 리비전 추가 (Python)
- 절대로 반대쪽 마이그레이션 도구로 다른 DB의 스키마를 변경하지 마세요.
- Admin WAS에서는 Flyway가 비활성화 상태이므로, 모든 마이그레이션은 Service WAS에서 관리합니다.

### 5. 인증 분리 (사용자 vs 관리자)
- **일반 사용자**: `users` 테이블 → Service WAS `AuthController` (`/api/v1/auth/**`) → JWT type="user"
- **관리자**: `admin_users` 테이블 → Admin WAS `AdminAuthController` (`/api/admin/auth/**`) → JWT type="admin"
- 일반 사용자 JWT로는 Admin WAS API에 접근할 수 없습니다 (403 Forbidden).
- Admin 계정 최초 생성: `POST http://localhost:8081/api/admin/auth/register` (body: email, password, name, role)

---

## 🔗 상세 문서 링크
- [프로젝트 개요](./docs/PROJECT_OVERVIEW.md)
- [디자인 시스템](./docs/FRONTEND_DESIGN_SYSTEM.md)
- [소셜 로그인 설정](./docs/SOCIAL_AUTH_SETUP.md)
- [개발 가이드](./docs/DEVELOPMENT_GUIDE.md)
- [DB 관리](./docs/README_DB.md)
- [AI 에이전트 라우팅](./docs/AI_AGENT_ROUTING.md)
- [보안 가이드 (JWT, HTTPS, XSS)](./docs/SECURITY.md)
