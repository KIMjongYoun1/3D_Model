# 🛠️ 개발 가이드 (Development Guide)

Quantum Studio의 핵심 기술 구조와 개발 시 준수해야 할 패턴을 설명합니다.

> **최종 업데이트**: 2026-02-09 — 멀티 모듈 구조, 카테고리 시스템, AI 티어링 반영

---

## 📁 프로젝트 구조 개요

```
3D_Model/
├── frontend-studio/        # 사용자향 3D 스튜디오 (Next.js, Port 3000)
├── frontend-admin/         # 관리자 대시보드 (Next.js, Port 3001)
├── backend-java/           # Java 백엔드 (Spring Boot, Port 8080)
│   ├── quantum-core/       #   공통 모듈 (엔티티, 보안, 리포지토리)
│   ├── quantum-api-service/#   사용자 API (인증, 결제, 프로젝트)
│   └── quantum-api-admin/  #   관리자 API (지식 베이스, 외부 API)
├── backend-python/         # Python 백엔드 (FastAPI, Port 8000)
└── docs/                   # 프로젝트 문서
```

---

## 🔐 인증 및 보안 (Auth & Security)

### 1. Java Backend (Spring Security + JWT)
- **JWT 발급**: `quantum-api-service`의 `AuthService.java`에서 로그인 성공 시 액세스 토큰을 발급합니다.
- **JWT 검증**: `quantum-core`의 `JwtAuthenticationFilter`에서 모든 요청의 헤더를 검사합니다.
- **비밀번호**: `quantum-core`의 `PasswordService`가 `BCryptPasswordEncoder`를 사용하여 해싱 저장합니다.
- **보안 설정**: `quantum-core`의 `SecurityConfig`에서 CORS, 필터 체인, 인증 제외 경로 등을 관리합니다.

### 2. 소셜 로그인 (Naver)
- **흐름**: Frontend(Code 발급) → Java Backend(Token/Profile 획득) → JWT 발급.
- **설정**: `application.yml` 및 `.env`의 `NAVER_CLIENT_ID` 등을 참조합니다.
- **구현**: `quantum-api-service`의 `NaverAuthService.java`에서 네이버 API 통신을 처리합니다.

---

## 🤖 AI 및 데이터 매핑 (AI & Mapping)

### 1. Python Backend (FastAPI + AI 에이전트 티어링)
- **AI 에이전트**: `backend-python/app/services/ai_agent_service.py`에서 비정형 데이터를 분석하여 JSON 구조로 변환합니다.
- **카테고리 시스템**: `backend-python/app/core/categories.py`에서 데이터 성격별 카테고리(`FINANCE_TAX`, `INFRA_ARCHITECTURE` 등)와 권장 모델 티어를 정의합니다.
- **매핑 엔진**: `backend-python/app/services/mapping_service.py`의 `MappingOrchestrator`가 분석된 데이터를 3D 노드(`pos`, `label`, `value`)로 변환하여 DB에 저장합니다.

### 2. AI 모델 티어링 (3단계)
| 티어 | 모델 | 호출 방식 | 역할 |
| :--- | :--- | :--- | :--- |
| **Tier 1 (Local)** | TinyLlama 1.1B | Transformers | 최후 폴백, 단순 텍스트 처리 |
| **Tier 2 (Flash/Pro - Local)** | Llama 3.2 | Ollama API | 복잡한 논리 추론 (로컬 우선 시도) |
| **Tier 3 (Flash/Pro - Cloud)** | Gemini 1.5 Flash / Pro | Google GenAI | Ollama 실패 시 클라우드 폴백 |

- 모든 요청은 **Ollama(Llama 3.2) → Gemini(Cloud) → TinyLlama(Local)** 순서로 폴백합니다.
- Pro 티어는 구글 검색 Grounding을 활용하여 전문 용어의 근거(references)를 제공합니다.

### 3. 지식 베이스(RAG) 연동
- 분석 시점에 로컬 DB(`KnowledgeBase` 테이블)에서 관련 도메인 지식을 조회하여 프롬프트에 주입합니다.
- `frontend-admin`의 `/knowledge` 페이지에서 관리자가 지식 항목을 관리합니다.
- `quantum-api-admin`의 외부 API 서비스(`BokEcosApiService`, `DartApiService`, `LawApiService`)를 통해 공공 데이터를 자동 수집합니다.

---

## 🎨 프론트엔드 개발 패턴 (Frontend Patterns)

### 1. 3D 시각화 (Three.js)
- **Canvas**: `frontend-studio/components/QuantumCanvas.tsx`가 전체 3D 렌더링을 담당합니다.
- **Interaction**: 노드 클릭 시 `frontend-studio/components/shared/DraggableWindow.tsx`가 활성화되며 상세 정보를 표시합니다.
- **2D/3D 전환**: `frontend-studio/components/ERDDiagram.tsx`를 통해 2D 관계도 모드를 지원합니다.

### 2. 공통 UI 컴포넌트 (`components/ui/`)
- **Button**: `primary`, `secondary`, `naver`, `kakao` 등의 variant 제공.
- **Card**: `bento`, `glass` 등 디자인 테마 제공.
- **Input**: `text`, `password`, `textarea` 타입 지원.
- **Modal**: 전역 팝업 시스템.
- 두 프론트엔드 모듈(`frontend-studio`, `frontend-admin`)에서 동일한 컴포넌트 세트를 유지합니다.

### 3. 온보딩 시스템
- 비로그인 사용자 접근 시 `frontend-studio/components/studio/Onboarding.tsx`가 자동 표시됩니다.
- `localStorage`의 `accessToken` 존재 여부로 인증 상태를 판단합니다.

---

## 🗄️ 데이터베이스 관리 (Database)

### 1. 멀티 마이그레이션
- **Java**: `quantum-api-service/src/main/resources/db/migration/` (Flyway)
  - V1: users 테이블
  - V2: subscriptions 테이블
  - V3: payments 테이블
  - V4: social auth 컬럼
  - V5: refresh token 컬럼
  - V6: knowledge_base 테이블
- **Python**: `backend-python/alembic/versions/` (Alembic)
  - 001: users 테이블
  - 002: AI models 테이블
  - 003: visualization 테이블
  - 004: correlation_rules 테이블

### 2. 엔티티 구조 (`quantum-core`)
- `User`: 사용자 (이메일, 소셜 로그인, 구독 플랜)
- `Payment`: 결제 내역
- `Project`: 프로젝트 (카테고리, 설명)
- `Knowledge`: 지식 베이스 항목 (카테고리, 출처, 활성 여부)

### 3. JDBC URL 주의사항
- Java 환경에서는 `jdbc:postgresql://` 형식을 사용해야 합니다.

---

## 🔗 관련 문서
- [빠른 시작 가이드](../QUICK_START.md)
- [프로젝트 개요](./PROJECT_OVERVIEW.md)
- [소셜 로그인 가이드](./SOCIAL_AUTH_SETUP.md)
- [AI 에이전트 라우팅](./AI_AGENT_ROUTING.md)
- [아키텍처 V2](./ARCHITECTURE_V2.md)
