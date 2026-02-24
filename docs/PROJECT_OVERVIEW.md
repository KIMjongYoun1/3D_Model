# 🚀 Quantum Studio 프로젝트 개요 (Project Overview)

Quantum Studio는 비정형 데이터를 3D 공간에 지능적으로 도식화하고 분석하는 **Universal 3D Visualization Platform**입니다.

## 🌟 핵심 가치
- **비정형 데이터의 구조화**: JSON, 로그, 문서(PDF/TXT/Excel) 등 복잡한 데이터를 AI가 분석하여 시각적 노드와 관계로 변환합니다.
- **지능형 3D 엔진**: 데이터 간의 상관관계를 GPU 가속 3D 캔버스에 실시간으로 렌더링합니다.
- **하이브리드 AI 라우팅**: 클라우드 AI(Gemini)와 로컬 모델을 결합하여 비용 효율적이고 강력한 분석을 제공합니다.

---

## 🏗 서비스 아키텍처 (Architecture)

프로젝트는 성능과 확장성을 위해 멀티 백엔드 구조를 채택하고 있습니다.

### 1. Frontend (Next.js 14+)
- **역할**: 3D 렌더링(Three.js/React Three Fiber), 사용자 인터페이스, 상태 관리.
- **디자인**: Apple-Clean White 테마, Bento Grid 레이아웃, 공통 UI 컴포넌트 시스템.

### 2. Java Backend (Spring Boot 3.x)
- **역할**: 사용자 인증(JWT/소셜 로그인), 결제 시스템, 핵심 비즈니스 로직.
- **기술 스택**: Java 21, Spring Security, JPA/Hibernate, Flyway(DB Migration), PostgreSQL.

### 3. Python Backend (FastAPI)
- **역할**: AI 에이전트 서비스, 데이터 매핑 엔진, 비정형 데이터 처리.
- **기술 스택**: Python 3.12, SQLAlchemy, Alembic(DB Migration), Google Gemini API.

---

## 🛠 기술 스택 요약 (Tech Stack)

| 구분 | 기술 |
| :--- | :--- |
| **Language** | TypeScript, Java 21, Python 3.12 |
| **Framework** | Next.js 14, Spring Boot 3.2, FastAPI |
| **Database** | PostgreSQL, Redis |
| **3D Engine** | Three.js, React Three Fiber, @react-three/drei |
| **Style** | Tailwind CSS, Framer Motion |
| **DevOps** | Docker, Docker Compose |

---

## 📂 프로젝트 구조 (Structure)

```text
3D_Model/
├── app/                # Next.js App Router (Pages)
├── components/         # React Components
│   ├── common/         # Header, Footer 등 공통 레이아웃
│   ├── ui/             # Button, Card, Modal 등 원자 단위 컴포넌트
│   ├── studio/         # 3D 스튜디오 전용 컴포넌트
│   └── shared/         # 비즈니스 로직 공유 컴포넌트
├── backend-java/       # Spring Boot 프로젝트
├── backend-python/     # FastAPI 프로젝트
├── docs/               # 프로젝트 문서 (통합 관리)
└── public/             # 정적 자산
```

---

## 📋 최근 업데이트 (2026-02-24)
- Admin 운영 기능: 회원/거래/구독/플랜/약관 관리, 매출 대시보드
- 회원 정지(`suspended_at`), 소프트 삭제(`deleted_at`)
- 결제 시 약관 동의(agreedTermIds), category(SIGNUP/PAYMENT)
- **인증**: 소셜 로그인(네이버) 전용. 이메일/회원가입 제거
- **결제 플로우**: 비로그인 결제 시도 → `/login?redirect=/payment` 리다이렉트
- **보안**: Open Redirect 방지(authRedirect), References URL 검증(safeUrl, url_sanitizer)

---

## 🔗 관련 문서
- [보안 가이드](./SECURITY.md)
- [빠른 시작 가이드](../QUICK_START.md)
- [개발 가이드](./DEVELOPMENT_GUIDE.md)
- [API 명세서](./API_SPECIFICATION.md)
- [관리자 수동 작업 목록](./ADMIN_MANUAL_OPERATIONS.md)
- [디자인 시스템](./FRONTEND_DESIGN_SYSTEM.md)
- [소셜 로그인 가이드](./SOCIAL_AUTH_SETUP.md)
