# 📑 Quantum Studio API 및 메소드 명세서

Quantum Studio는 멀티 백엔드 아키텍처를 채택하고 있으며, 각 백엔드의 역할에 따라 API와 내부 메소드가 분리되어 있습니다. 본 문서는 각 기능의 상세 명세와 내부 동작 원리를 기록합니다.

> **최종 업데이트**: 2026-02-19 — 기능별 작동 여부·설계 이유 반영

---

## 📋 기능별 작동 여부 (Feature Status)

| 구분 | 기능 | 상태 | 비고 |
| :--- | :--- | :--- | :--- |
| **Java Service** | 이메일 로그인/회원가입, JWT 갱신 | ✅ | |
| | 네이버 소셜 로그인 | ✅ | |
| | 결제 시뮬레이션 | ✅ | success-rate로 시뮬레이션 |
| | 프로젝트 CRUD | ✅ | |
| **Java Admin** | 관리자 인증 (login/register/me) | ✅ | `admin_users` 테이블, 일반 JWT와 분리 |
| | 지식 베이스 CRUD, 소스별 목록/상세 | ✅ | BOK/DART/LAW 상세 테이블 지원 |
| | BOK·DART·LAW 외부 API 수집 | ✅ | corp_code 활용 시 DART 장기 검색 가능 |
| **Python AI (8000)** | 매핑 API (3D 변환, 업로드, 히스토리) | ✅ | MappingOrchestrator, AIAgentService |
| **Admin AI (8002)** | Ollama/Gemini 채팅 | ✅ | Ollama 없으면 Gemini 폴백 |
| **Frontend Studio** | 로그인·결제·마이페이지·스튜디오 | ⚠️ | 로그인 API 연동 TODO |
| **Frontend Admin** | 로그인·지식 관리·AI | ✅ | |

> `✅` 동작 | `⚠️` 부분 동작 | `❌` 미구현

---

## 🛠 주요 설계 선택 및 이유 (Design Rationale)

| 선택 | 이유 |
| :--- | :--- |
| **Service/Admin WAS 분리** | 보안(관리자 API 격리), 안정성(서비스 장애 시에도 Admin 운영 가능), 스케일 분리 |
| **인증 분리 (users vs admin_users)** | 일반 사용자 JWT로 Admin API 접근 차단, `type="admin"` 검증 |
| **지식 소스 테이블 분리 (bok/dart/law)** | 외부 API 응답 형식 그대로 저장 → 상세 조회·필터 용이, DART corp_code로 3개월 제한 우회 |
| **Admin AI 별도 서버 (8002)** | Studio AI(매핑)와 역할 분리, 관리자 전용 분석·조회에 맞춘 DB·LLM 전략 |
| **Ollama → Gemini 폴백** | 로컬 비용 절감, 오프라인 시에도 Admin AI 기동 가능 |

---

## 🟢 Java Backend (Auth & Business)
**Base URL**: `http://localhost:8080`
**역할**: 사용자 인증, 권한 관리, 결제, 프로젝트 관리 및 핵심 비즈니스 로직

### 모듈 구조

| 모듈 | 경로 | 역할 |
| :--- | :--- | :--- |
| **quantum-core** | `backend-java/quantum-core/` | 공통 엔티티, DTO, 리포지토리, 보안 설정 |
| **quantum-api-service** | `backend-java/quantum-api-service/` | 사용자향 API (인증, 결제, 프로젝트) |
| **quantum-api-admin** | `backend-java/quantum-api-admin/` | 관리자 API (지식 베이스, 외부 API 연동) |

### 1. API 명세 (quantum-api-service - Controllers)

#### [인증 API] - `AuthController`
| Method | Endpoint | Description | Request Body / Params | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | 이메일 회원가입 | `email, password, name` | `UserResponse` |
| `POST` | `/api/v1/auth/login` | 이메일 로그인 | `email, password` | `LoginResponse (Tokens)` |
| `GET` | `/api/v1/auth/naver/callback` | 네이버 로그인 콜백 | `code, state` | `LoginResponse (Tokens)` |
| `POST` | `/api/v1/auth/refresh` | 토큰 갱신 | `refreshToken` | `LoginResponse (Tokens)` |

#### [결제 API] - `PaymentController`
| Method | Endpoint | Description | Request Body / Params | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/payments/confirm` | 결제 승인 및 구독 활성화 | `paymentKey, orderId, amount` | `PaymentResponse` |

#### [프로젝트 API] - `ProjectController` (신규)
| Method | Endpoint | Description | Request Body / Params | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/projects` | 프로젝트 생성 | `name, description, mainCategory, subCategory` | `Project` |
| `GET` | `/api/projects` | 사용자 프로젝트 목록 조회 | - | `List<Project>` |
| `GET` | `/api/projects/{id}` | 프로젝트 상세 조회 | `id (UUID)` | `Project` |
| `DELETE` | `/api/projects/{id}` | 프로젝트 삭제 | `id (UUID)` | `204 No Content` |

### 2. Admin API 명세 (quantum-api-admin - Controllers)

**Base URL**: `http://localhost:8081`

#### [관리자 인증] - `AdminAuthController` (`/api/admin/auth`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/login` | 관리자 로그인 |
| `POST` | `/register` | 관리자 계정 생성 |
| `GET` | `/me` | 현재 관리자 정보 |

#### [지식 베이스] - `AdminController` (`/api/admin/knowledge`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/` | 전체 지식 목록 |
| `GET` | `/{id}` | 단일 지식 상세 |
| `POST` | `/` | 지식 직접 추가 |
| `DELETE` | `/{id}` | 지식 삭제 |
| `GET` | `/fetch-history` | 수집 히스토리 |
| `POST` | `/fetch-bok` | 한국은행 경제지표 수집 |
| `POST` | `/fetch-dart` | DART 공시 수집 (corpName 옵션) |
| `POST` | `/fetch-law` | 법령 수집 (lawName 파라미터) |
| `GET` | `/law-preview` | 법령 API 미리보기 (저장 없음) |
| `GET` | `/bok`, `/bok/{id}` | BOK 목록/상세 |
| `GET` | `/dart`, `/dart/{id}` | DART 목록/상세 |
| `GET` | `/law`, `/law/{id}` | LAW 목록/상세 |
| `GET` | `/dart/corp-codes` | DART 기업코드 목록 |

### 3. 주요 메소드 명세 (Services)

#### `AuthService` (`quantum-api-service`) — 인증 핵심 로직
- **`login(email, password)`**:
  - DB에서 사용자 조회 및 비밀번호(`BCrypt`) 검증.
  - Access Token(1시간) 및 Refresh Token(7일) 생성.
  - Refresh Token을 DB에 저장하여 세션 유지.
- **`refresh(refreshToken)`**:
  - Refresh Token 유효성 및 DB 일치 여부 확인.
  - 새로운 토큰 쌍을 발급하여 보안 강화 (Rotation 전략).
- **`register(email, password, name)`**:
  - 이메일 중복 체크 후 비밀번호 암호화 저장.
  - 기본 구독 플랜(`free`) 할당.

#### `NaverAuthService` (`quantum-api-service`) — 네이버 연동
- **`loginWithNaver(code, state)`**:
  - 네이버 API를 통해 Access Token 획득.
  - 사용자 프로필(이메일, 이름, 프로필 이미지) 조회.
  - 기존 사용자면 정보 업데이트, 신규면 자동 가입 처리.

#### `PaymentService` (`quantum-api-service`) — 결제 시뮬레이션
- **`createPayment(userId, subId, method, amount)`**:
  - 결제 요청 정보를 `pending` 상태로 저장.
  - `success-rate` 설정값에 따라 성공/실패 시뮬레이션 수행.

#### `ProjectService` (`quantum-api-service`) — 프로젝트 관리 (신규)
- **`createProject(userId, name, description, mainCategory, subCategory)`**: 프로젝트 생성
- **`getUserProjects(userId)`**: 사용자별 프로젝트 목록 조회
- **`getProject(id)`**: 프로젝트 상세 조회
- **`deleteProject(id)`**: 프로젝트 삭제

#### Admin Services (`quantum-api-admin`) — 관리 서비스 (신규)
- **`KnowledgeService`**: 지식 베이스 항목의 CRUD 및 카테고리별 관리
- **`BokEcosApiService`**: 한국은행 경제통계(ECOS) API 연동
- **`DartApiService`**: 금융감독원 전자공시(DART) API 연동
- **`LawApiService`**: 법제처 법률 API 연동

#### `JwtService` / `PasswordService` (`quantum-core`) — 공통 보안
- JWT 토큰 생성/검증 및 BCrypt 비밀번호 해싱 (모든 API 모듈에서 공유)

---

## 🔵 Python Backend (AI & Visualization)
**Base URL**: `http://localhost:8000`
**역할**: 비정형 데이터 분석, AI 에이전트, 3D 매핑 엔진

### 1. API 명세 (Routers)

#### [매핑 API] - `MappingRouter`
| Method | Endpoint | Description | Request Body / Params | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/mapping` | 데이터 3D 변환 | `data_type, raw_data, main_category, sub_category, options` | `MappingResponse` |
| `POST` | `/api/v1/mapping/upload` | 파일 기반 분석 | `file (UploadFile), main_category, sub_category` | `MappingResponse` |
| `GET` | `/api/v1/mapping` | 히스토리 조회 | - | `List[MappingResponse]` |

### 2. 주요 메소드 명세 (Services)

#### `MappingOrchestrator` (3D 변환 엔진)
- **`process_data_to_3d(data_type, raw_data, db, options)`**:
  - 데이터 성격(JSON, Text, File)에 따른 시각화 전략 결정.
  - `settlement`(정산 막대), `ai_analysis`(AI 다이어그램), `diagram`(JSON 관계도), `monolith`(통합) 모드 지원.
  - 카테고리 정보(`main_category`, `sub_category`)를 AI 에이전트에 전달.
- **`_analyze_local_correlations(db, nodes)`**:
  - DB에 정의된 `CorrelationRule`을 바탕으로 노드 간 숨겨진 관계 분석.
  - 키워드 매칭 강도에 따라 연결선(Link)의 굵기 결정.

#### `AIAgentService` (AI 분석 엔진 — 카테고리별 티어링)
- **`analyze_document(text, db, options)`**:
  - **1단계**: 카테고리 결정 — 사용자 입력(main_category/sub_category) 우선, 없으면 키워드 자동 감지
  - **2단계**: 지식 베이스(RAG) 조회 — DB에서 관련 도메인 지식을 추출하여 프롬프트에 주입
  - **3단계**: 티어별 모델 호출 — Llama 3.2(Ollama) → Gemini(Cloud) → TinyLlama(Local) 순 폴백
  - 분석 결과를 `summary`, `keywords`, `relations` 형태의 JSON으로 구조화.
  - **Grounding**: Pro 티어에서 구글 검색을 통해 전문 용어의 근거(`references`)와 URL 제공.

#### `DocumentProcessor` (파일 처리)
- **`extract_data(content, filename)`**:
  - PDF, Excel, TXT, CSV 파일에서 텍스트 데이터 추출.
  - 대용량 문서의 경우 지능형 청크 분할(Chunking) 수행.

---

## 🟣 Admin AI Server (관리자용 AI)
**Base URL**: `http://localhost:8002`
**역할**: 관리자 전용 자연어 프롬프트, quantum_service 읽기 전용 분석

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/admin-ai/chat` | 채팅 메시지 (Ollama/Gemini) |
| `GET` | `/api/admin-ai/intents` | 지원 의도 목록 |
| `GET` | `/health` | 헬스 체크 |

---

## 🏛 공통 사항
- **보안**: Java API는 `JwtAuthenticationFilter`(`quantum-core`)로 검증. Admin API는 `type="admin"` JWT 필요.
- **문서화**: Python·Admin AI는 `/docs` 경로에서 Swagger UI 제공.
- **DB 마이그레이션**:
  - Java: `quantum-api-service/.../db/migration/` (Flyway, V1~V10)
  - Python: `alembic/versions/` (Alembic, 001~004)

---

*본 명세서는 작업 완료 시마다 최신 상태로 업데이트됩니다.*
