# 📑 Quantum Studio API 및 메소드 명세서

Quantum Studio는 멀티 백엔드 아키텍처를 채택하고 있으며, 각 백엔드의 역할에 따라 API와 내부 메소드가 분리되어 있습니다. 본 문서는 각 기능의 상세 명세와 내부 동작 원리를 기록합니다.

> **최종 업데이트**: 2026-02-09 — Java 멀티 모듈 구조(`quantum-core`, `quantum-api-service`, `quantum-api-admin`) 반영

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

#### [관리자 API] - `AdminController`
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| - | 지식 베이스 관리 | 도메인 지식 CRUD |
| - | 외부 API 연동 | 법제처, DART, 한국은행 ECOS 데이터 수집 |

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

## 🏛 공통 사항
- **보안**: 모든 API는 `JwtAuthenticationFilter`(`quantum-core`) 및 보안 미들웨어(Python)를 통해 검증됩니다.
- **문서화**: Python 백엔드는 `/docs` 경로에서 Swagger UI를 통해 실시간 테스트가 가능합니다.
- **DB 마이그레이션**:
  - Java: `quantum-api-service/src/main/resources/db/migration/` (Flyway, V1~V6)
  - Python: `alembic/versions/` (Alembic, 001~004)

---

*본 명세서는 작업 완료 시마다 최신 상태로 업데이트됩니다.*
