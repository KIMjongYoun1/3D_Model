# 📑 Quantum Studio API 및 메소드 명세서

Quantum Studio는 멀티 백엔드 아키텍처를 채택하고 있으며, 각 백엔드의 역할에 따라 API와 내부 메소드가 분리되어 있습니다. 본 문서는 각 기능의 상세 명세와 내부 동작 원리를 기록합니다.

---

## 🟢 Java Backend (Auth & Business)
**Base URL**: `http://localhost:8080`
**역할**: 사용자 인증, 권한 관리, 결제 및 핵심 비즈니스 로직

### 1. API 명세 (Controllers)

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

### 2. 주요 메소드 명세 (Services)

#### `AuthService` (인증 핵심 로직)
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

#### `NaverAuthService` (네이버 연동)
- **`loginWithNaver(code, state)`**:
  - 네이버 API를 통해 Access Token 획득.
  - 사용자 프로필(이메일, 이름, 프로필 이미지) 조회.
  - 기존 사용자면 정보 업데이트, 신규면 자동 가입 처리.

#### `PaymentService` (결제 시뮬레이션)
- **`createPayment(userId, subId, method, amount)`**:
  - 결제 요청 정보를 `pending` 상태로 저장.
  - `success-rate` 설정값에 따라 성공/실패 시뮬레이션 수행.

---

## 🔵 Python Backend (AI & Visualization)
**Base URL**: `http://localhost:8000`
**역할**: 비정형 데이터 분석, AI 에이전트, 3D 매핑 엔진

### 1. API 명세 (Routers)

#### [매핑 API] - `MappingRouter`
| Method | Endpoint | Description | Request Body / Params | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/mapping` | 데이터 3D 변환 | `data_type, raw_data, options` | `MappingResponse` |
| `POST` | `/api/v1/mapping/upload` | 파일 기반 분석 | `file (UploadFile)` | `MappingResponse` |
| `GET` | `/api/v1/mapping` | 히스토리 조회 | - | `List[MappingResponse]` |

### 2. 주요 메소드 명세 (Services)

#### `MappingOrchestrator` (3D 변환 엔진)
- **`process_data_to_3d(data_type, raw_data, options)`**:
  - 데이터 성격(JSON, Text, File)에 따른 시각화 전략 결정.
  - `settlement`(정산), `diagram`(관계도), `monolith`(통합) 모드 지원.
- **`_analyze_local_correlations(db, nodes)`**:
  - DB에 정의된 `CorrelationRule`을 바탕으로 노드 간 숨겨진 관계 분석.
  - 키워드 매칭 강도에 따라 연결선(Link)의 굵기 결정.

#### `AIAgentService` (AI 분석 엔진)
- **`analyze_document(text, options)`**:
  - Gemini API(Cloud) 또는 TinyLlama(Local)를 사용하여 텍스트 분석.
  - **Grounding**: 구글 검색을 통해 전문 용어의 근거(`references`)와 URL 제공.
  - 분석 결과를 `summary`, `keywords`, `relations` 형태의 JSON으로 구조화.

#### `DocumentProcessor` (파일 처리)
- **`extract_data(content, filename)`**:
  - PDF, Excel, TXT 파일에서 텍스트 데이터 추출.
  - 대용량 문서의 경우 지능형 청크 분할(Chunking) 수행.

---

## 🏛 공통 사항
- **보안**: 모든 API는 `JwtAuthenticationFilter`(Java) 및 보안 미들웨어(Python)를 통해 검증됩니다.
- **문서화**: Python 백엔드는 `/docs` 경로에서 Swagger UI를 통해 실시간 테스트가 가능합니다.

---

*본 명세서는 작업 완료 시마다 최신 상태로 업데이트됩니다.*
