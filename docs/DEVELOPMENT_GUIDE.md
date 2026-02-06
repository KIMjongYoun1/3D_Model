# 🛠️ 개발 가이드 (Development Guide)

Quantum Studio의 핵심 기술 구조와 개발 시 준수해야 할 패턴을 설명합니다.

---

## 🔐 인증 및 보안 (Auth & Security)

### 1. Java Backend (Spring Security + JWT)
- **JWT 발급**: `AuthService.java`에서 로그인 성공 시 액세스 토큰을 발급합니다.
- **JWT 검증**: `JwtAuthenticationFilter`에서 모든 요청의 헤더를 검사합니다.
- **비밀번호**: `BCryptPasswordEncoder`를 사용하여 해싱 저장합니다.

### 2. 소셜 로그인 (Naver)
- **흐름**: Frontend(Code 발급) -> Java Backend(Token/Profile 획득) -> JWT 발급.
- **설정**: `application.yml` 및 `.env`의 `NAVER_CLIENT_ID` 등을 참조합니다.

---

## 🤖 AI 및 데이터 매핑 (AI & Mapping)

### 1. Python Backend (FastAPI + Gemini)
- **AI 에이전트**: `ai_agent_service.py`에서 비정형 데이터를 분석하여 JSON 구조로 변환합니다.
- **매핑 엔진**: 분석된 데이터를 3D 노드(`pos`, `label`, `value`)로 변환하여 DB에 저장합니다.

### 2. 하이브리드 라우팅
- 비용 절감을 위해 간단한 분석은 로컬 모델(준비 중)로, 복잡한 분석은 Gemini API로 라우팅합니다.

---

## 🎨 프론트엔드 개발 패턴 (Frontend Patterns)

### 1. 3D 시각화 (Three.js)
- **Canvas**: `QuantumCanvas.tsx`가 전체 3D 렌더링을 담당합니다.
- **Interaction**: 노드 클릭 시 `DraggableWindow`가 활성화되며 상세 정보를 표시합니다.

### 2. 공통 UI 컴포넌트 (`components/ui/`)
- **Button**: `primary`, `secondary`, `naver`, `kakao` 등의 variant 제공.
- **Card**: `bento`, `glass` 등 디자인 테마 제공.
- **Modal**: 전역 팝업 시스템.

---

## 🗄️ 데이터베이스 관리 (Database)

### 1. 멀티 마이그레이션
- **Java**: `src/main/resources/db/migration` (Flyway) - 인증, 결제 테이블 관리.
- **Python**: `alembic/versions` (Alembic) - 시각화 데이터, AI 로그 테이블 관리.

### 2. JDBC URL 주의사항
- Java 환경에서는 `jdbc:postgresql://` 형식을 사용해야 합니다.

---

## 🔗 관련 문서
- [빠른 시작 가이드](../QUICK_START.md)
- [프로젝트 개요](./PROJECT_OVERVIEW.md)
- [소셜 로그인 가이드](./SOCIAL_AUTH_SETUP.md)
