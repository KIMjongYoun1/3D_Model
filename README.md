# 🚀 Quantum Studio

**Universal 3D Visualization Platform** — 비정형 데이터를 3D 공간에 지능적으로 도식화하고 분석하는 풀스택 시각화 플랫폼

---

## 📌 프로젝트 소개

JSON, 로그, 문서(PDF/Excel) 등 복잡한 데이터를 AI가 분석하여 시각적 노드와 관계로 변환하고, Three.js 기반 3D 캔버스에 실시간 렌더링합니다. 멀티 백엔드(Java + Python) 구조로 인증·결제·AI 매핑·관리자 운영을 분리하여 운영합니다.

---

## 🛠 기술 스택

| 구분 | 기술 |
| :--- | :--- |
| **Frontend** | Next.js 14, TypeScript, Three.js, React Three Fiber, Tailwind CSS |
| **Backend** | Java 21, Spring Boot 3.2, Python 3.12, FastAPI |
| **Database** | PostgreSQL, Redis |
| **인증** | JWT (Access/Refresh), Spring Security, OAuth2 (네이버) |
| **인프라** | Docker, Flyway, Alembic |

---

## ✨ 주요 기능

### 사용자 서비스

#### 인증
- 네이버 소셜 로그인 전용 (이메일 회원가입 없음)
- JWT Access/Refresh 토큰, 갱신 API

#### 결제·구독
- **결제 시뮬레이션**: PG 연동 전 테스트용. `success-rate` 설정으로 성공/실패 시뮬레이션
- **플랜별 구독**: free, pro 등 plan_config 기반. 관리자가 등록한 **노출 중** 플랜만 결제 화면에 표시
- **결제 플로우**: 로그인 → 플랜 선택 → 약관 동의(agreedTermIds) → 결제 확인 → 구독 활성화
- **비로그인 결제 시도** 시 로그인 페이지로 리다이렉트
- **구독 해지 신청**: 당월 말까지 이용 가능, 만료 후 자동 종료
- **마이페이지**: Active Plan 카드에 구독 상태 배지 표시
  - `정상` (초록): 활성 구독
  - `해지 신청됨 (만료일까지 이용 가능)` (노랑): 해지 신청 후 만료 전

#### 약관
- 가입용(SIGNUP) / 결제용(PAYMENT), 필수/선택 구분
- 현재 적용 약관 + **이전 버전 전체 조회** (법령 준수)
- 관리자가 `is_active=false`로 설정한 약관은 화면에 미노출

#### 3D 시각화
- 데이터 매핑 AI, 노드-링크 3D 렌더링, 프로젝트 CRUD

---

### 관리자 운영

#### 회원·거래·구독
- **회원관리**: 목록/상세, 정지/해제, 탈퇴(소프트 삭제)
- **거래관리**: 결제 목록/상세, 취소. **날짜 범위 필터**(fromDate, toDate)로 기간별 조회
- **구독관리**: 구독 목록/상세, 취소

#### 플랜·약관
- **플랜관리**: 새 플랜 등록(`POST /plans`), 수정, **노출 토글**(목록에서 배지 클릭 시 즉시 전환). 노출 중인 플랜만 Studio 결제 화면에 표시
- **약관관리**: CRUD, **노출 설정**(is_active), **새 버전 등록**(기존 약관 복사 후 version·effectiveAt 지정)

#### 매출 대시보드
- 총매출, 월별/플랜별 통계
- **기간별 차트**: 주별·월별·분기별·반기별 선택
- **당기 vs 전기**: 막대(당기) + 선(전기) ComposedChart
- **구독별 세부**: 툴팁에 플랜별 매출 표시

#### 지식 베이스
- BOK/DART/LAW 공공 API 수집·관리

### AI / 매핑
- **하이브리드 AI**: Ollama(Local) → Gemini(Cloud) 폴백
- **데이터 매핑**: 비정형 데이터 → 3D 노드 자동 변환

### 보안
- **Open Redirect 방지**: redirect 파라미터 검증 (authRedirect)
- **References URL 검증**: 외부 링크 http/https만 허용 (safeUrl, url_sanitizer)

---

## 🏗 아키텍처

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ frontend-studio │  │ frontend-admin   │  │  Admin AI       │
│ (Next.js 3000)  │  │ (Next.js 3001)   │  │  (8002)         │
└────────┬────────┘  └────────┬────────┘  └────────┬────────┘
         │                    │                     │
         ▼                    ▼                     ▼
┌────────────────────────────────────────────────────────────────────────────┐
│  quantum-api-service (8080)  │  quantum-api-admin (8081)  │  Python (8000)  │
│  인증·결제·프로젝트           │  회원·거래·구독·플랜·약관   │  AI 매핑 엔진   │
└────────────────────────────────────────────────────────────────────────────┘
         │                    │                     │
         └────────────────────┴─────────────────────┘
                              ▼
                    PostgreSQL · Redis
```

---

## 🚀 빠른 시작

### 필수 환경
- Node.js 22+, Java 21, Python 3.12, Docker

### 1. 환경 변수
```env
# .env
DATABASE_URL=jdbc:postgresql://localhost:5432/postgres
DB_USER=model_dev
DB_PASSWORD=dev1234
GEMINI_API_KEY=your_key
NAVER_CLIENT_ID=...
NAVER_CLIENT_SECRET=...
```

### 2. 실행
```bash
# DB/Redis
docker-compose up -d

# 전체 기동 (권장) - Java 빌드, npm install, 6개 서비스 순차 기동
./start.sh --tail

# 또는 개별 실행
cd frontend-studio && npm install && npm run dev    # http://localhost:3000
cd frontend-admin && npm install && npm run dev    # http://localhost:3001
cd backend-java && ./mvnw spring-boot:run -pl quantum-api-service   # 8080
cd backend-java && ./mvnw spring-boot:run -pl quantum-api-admin    # 8081
```

### 3. 테스트 실행
```bash
# 프론트엔드
cd frontend-admin && npm run test
cd frontend-studio && npm run test

# Python 백엔드 (pytest)
cd backend-python && ./run_tests.sh
```

---

## 📂 프로젝트 구조

```
3D_Model/
├── frontend-studio/     # 사용자 3D 스튜디오 (Port 3000)
├── frontend-admin/      # 관리자 대시보드 (Port 3001)
├── backend-java/
│   ├── quantum-core/    # 공통 엔티티, DTO, 리포지토리
│   ├── quantum-api-service/   # 사용자 API (인증, 결제, 프로젝트)
│   └── quantum-api-admin/     # 관리자 API (회원, 거래, 구독, 플랜, 약관)
├── backend-python/      # AI 매핑 엔진 (Port 8000)
└── docs/                # 상세 문서
```

---

## 📚 문서

| 문서 | 설명 |
| :--- | :--- |
| [프로젝트 개요](./docs/PROJECT_OVERVIEW.md) | 아키텍처, 기술 스택 상세 |
| [적용 내용 요약](./docs/IMPLEMENTATION_SUMMARY.md) | 최근 구현 기능 요약 (구독 상태, 대시보드, 플랜/약관 등) |
| [API 명세서](./docs/API_SPECIFICATION.md) | REST API 엔드포인트 |
| [관리자 수동 작업](./docs/ADMIN_MANUAL_OPERATIONS.md) | Admin 운영 가이드 |
| [개발 가이드](./docs/DEVELOPMENT_GUIDE.md) | 인증, DB, AI 연동 |
| [보안 가이드](./docs/SECURITY.md) | Redirect·URL 검증, HTTPS, JWT |
| [빠른 시작](./QUICK_START.md) | 상세 설치·실행 가이드 |
