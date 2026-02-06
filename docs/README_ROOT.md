# 🎨 Virtual Try-On 서비스

> **버전**: v1.0  
> **최종 수정**: 2025.12.26  
> **개발 규모**: 1인 ~ 3인  
> **설계 방향**: 서비스 기능 중심 + 결제 연동

---

## 📋 프로젝트 개요

### 서비스명 (가칭)
- **FitMe** / **TryOn3D** / **VirtualFit**

### 핵심 가치
> "보이지 않는 것을 보이게, 입어보지 못한 것을 입어보게"
> (Making the invisible visible, Wearing the unworn)

### 주요 서비스 기능

#### 1. 3D 시각화 플랫폼 (Universal 3D Visualization) ⭐
- **가상 피팅**: 의상 업로드 및 AI 기반 실착 시뮬레이션 (IDM-VTON)
- **개인화 아바타**: 얼굴 메시 기반 아바타 생성 (MediaPipe)
- **데이터 시각화**: JSON/로그 등 추상적 데이터를 3D 공간에 시각화 (Three.js)
- **인터랙티브 뷰어**: 멀티 앵글 탐색 및 시나리오 캡처

#### 2. 구독 관리 서비스
- 구독 플랜 관리 (Free, Basic, Pro, Unlimited)
- 사용량 추적 및 제한
- 구독 상태 관리

#### 3. 결제 서비스 ⭐
- 결제 요청 및 검증
- PG사 연동 (토스페이먼츠, 아임포트)
- 결제 이력 관리
- 구독 자동 갱신

#### 4. Try-On 서비스
- 의상 업로드 및 관리
- AI 기반 Virtual Try-On (IDM-VTON)
- 결과 이미지 저장 및 관리

#### 5. 아바타 서비스
- 개인화 아바타 생성 (MediaPipe Face Mesh)
- 체형 파라미터 설정
- 3D 렌더링 (Three.js)

### 타겟 사용자
- 온라인 쇼핑 시 사이즈/핏 고민하는 소비자
- 의류 쇼핑몰 운영자 (B2B 연동)
- 패션 인플루언서

---

## 📁 문서 구조

모든 문서는 `docs/` 디렉토리에 정리되어 있습니다.

### 📋 기획 문서
- [프로젝트 개요](./docs/planning/README.md)
- [개발 로드맵](./docs/planning/ROADMAP.md)

### 🏗️ 기술 문서
- [서비스 아키텍처](./docs/SERVICE_ARCHITECTURE.md) ⭐ - 서비스 기능 중심 구조
- [시스템 아키텍처](./docs/technical/ARCHITECTURE.md) - 기술 스택 상세
- [AI 모델 선정](./docs/technical/AI_MODELS.md)
- [AI 모듈 연동](./docs/technical/AI_INTEGRATION.md)
- [보안 가이드](./docs/technical/SECURITY.md)
- [암호화 기준](./docs/SECURITY_ENCRYPTION.md) - 비밀번호, JWT 암호화 기준

### 🛠️ 개발 가이드
- [MCP 가이드](./docs/guides/MCP_GUIDE.md) - Figma & Notion MCP 설정 및 사용법
- [Notion 가이드](./docs/guides/NOTION_GUIDE.md) - Notion 문서 관리
- [워크플로우](./docs/guides/WORKFLOW_DESIGN_TO_DOCS.md)
- [Cursor 가이드](./docs/guides/CURSOR_GUIDE.md)
- [도구 통합](./docs/guides/CURSOR_TOOLS_INTEGRATION.md)

### 🔧 도구
- [사용 도구](./docs/tools/TOOLS.md)

### 📊 설계 문서
- [ERD (데이터베이스 설계)](./docs/design/ERD.md)
- [Figma 가이드](./docs/design/FIGMA_GUIDE.md) - 디자인 시스템 및 작업 가이드
- [컴포넌트 스펙](./docs/design/COMPONENT_SPECS.md)

### 📚 문서 관리
- [빠른 시작](./docs/QUICK_START.md)
- [개발 환경 설정](./docs/DEVELOPMENT_SETUP.md)
- [로컬 개발 환경 설정](./docs/DEVELOPMENT_SETUP_LOCAL.md) - Docker 제외
- [프로젝트 구조](./docs/STRUCTURE.md)
- [데이터베이스 마이그레이션](./docs/DATABASE_MIGRATION.md) - 마이그레이션 개념
- [백엔드 DB 접근 방법](./docs/BACKEND_DB_ACCESS.md) - ORM 사용 가이드
- [개발 가이드](./docs/DEVELOPMENT_GUIDE.md) - 핵심 기능 직접 구현 가이드
- [클래스 참조 가이드](./docs/CLASS_REFERENCE_GUIDE.md) - 기능 개발 시 클래스 확인 순서
- [프로젝트 개선 방안](./docs/PROJECT_IMPROVEMENT.md) - 프로젝트 품질 향상 방안

---

## 🔄 전체 서비스 흐름

### 사용자 여정

```
[1] 회원가입/로그인
    └─▶ 사용자 관리 서비스 (Java)
        └─▶ 비밀번호 BCrypt 해싱 저장

[2] 구독 플랜 선택
    └─▶ 구독 관리 서비스 (Java)
        └─▶ 사용량 제한 확인

[3] 결제 진행 ⭐
    └─▶ 결제 서비스 (Java)
        └─▶ PG사 연동 (토스페이먼츠/아임포트)
            └─▶ 결제 검증 및 구독 활성화

[4] 아바타 생성 (선택)
    └─▶ 아바타 서비스 (Java + Python)
        └─▶ MediaPipe Face Mesh 처리
            └─▶ 3D 아바타 생성

[5] 의상 업로드
    └─▶ 의상 관리 서비스 (Java)
        └─▶ 이미지 저장 (Cloudflare R2)

[6] Try-On 실행
    └─▶ Try-On 서비스 (Java + Python)
        ├─▶ 사용량 체크 (Usage Service)
        ├─▶ AI 처리 요청 (Python)
        │   └─▶ IDM-VTON 모델 실행
        └─▶ 결과 저장 및 반환

[7] 결과 확인 및 공유
    └─▶ 결과 이미지 다운로드/공유
```

### 서비스 아키텍처

```
Frontend (Next.js)
    │
    ├─▶ Java Backend (비즈니스 로직)
    │   ├─▶ User Service (인증/인가)
    │   ├─▶ Subscription Service (구독 관리)
    │   ├─▶ Payment Service (결제) ⭐
    │   ├─▶ Usage Service (사용량 추적)
    │   └─▶ Garment Service (의상 관리)
    │
    └─▶ Python Backend (AI 처리)
        ├─▶ Try-On Service (AI 모델 실행)
        ├─▶ Image Processing Service
        └─▶ AI Model Service
```

> **자세한 내용**: [서비스 아키텍처 문서](./docs/SERVICE_ARCHITECTURE.md)

---

## 👥 팀 구성 시나리오

### 1인 개발 (Solo)
- 풀스택 + AI 모델 연동
- MVP 집중, 3~4개월 예상

### 2인 개발
- **개발자 A**: Backend + AI Pipeline
- **개발자 B**: Frontend + 3D 렌더링

### 3인 개발
- **개발자 A**: Backend API + 인프라
- **개발자 B**: Frontend + UX
- **개발자 C**: AI/ML + 모델 최적화

---

## 🚀 Quick Start

### 빠른 시작
1. [빠른 시작 가이드](./docs/QUICK_START.md) - 5분 빠른 시작 가이드
2. [개발 환경 설정](./docs/DEVELOPMENT_SETUP.md) - Windows/macOS 환경 설정

### MCP 연결 (Figma & Notion)
1. [MCP 가이드](./docs/guides/MCP_GUIDE.md) - MCP 설정 및 사용법 (5분 빠른 시작 포함)
2. [워크플로우](./docs/guides/WORKFLOW_DESIGN_TO_DOCS.md) - 디자인→개발→문서화 워크플로우

### 프로젝트 개발
1. [도구](./docs/tools/TOOLS.md)를 참고하여 필요한 도구 설치
2. [서비스 아키텍처](./docs/SERVICE_ARCHITECTURE.md)에서 서비스 구조 확인
3. [시스템 아키텍처](./docs/technical/ARCHITECTURE.md)에서 기술 스택 확인
4. [개발 로드맵](./docs/planning/ROADMAP.md)의 Phase 1부터 진행
5. [ERD](./docs/design/ERD.md)를 참고하여 DB 설계
6. [데이터베이스 마이그레이션](./docs/DATABASE_MIGRATION.md) 이해

---

## 🛠️ 기술 스택

### Backend
- **Java (Spring Boot)**: 비즈니스 로직, 사용자 관리, 구독, **결제** ⭐
- **Python (FastAPI)**: AI 모델 연동, 이미지 처리
- **PostgreSQL**: 메인 데이터베이스
- **Redis**: 캐시 및 작업 큐

### Frontend
- **Next.js 14**: React 프레임워크
- **TypeScript**: 타입 안정성
- **Three.js**: 3D 렌더링

### 보안
- **비밀번호 암호화**: BCrypt
- **JWT 토큰**: HS256 알고리즘
- **결제 연동**: 토스페이먼츠, 아임포트

---

## 📌 주요 결정 사항

### ✅ 확정
- [x] 서비스 기능 중심 아키텍처
- [x] 결제 연동 구조 (토스페이먼츠, 아임포트)
- [x] 자동 마이그레이션 (Flyway, Alembic)
- [x] 암호화 기준 (BCrypt, JWT HS256)

### 🔄 진행 중
- [ ] 서비스명 최종 결정
- [ ] AI 모델 최종 선정 (IDM-VTON vs OOTDiffusion)
- [ ] 클라우드 인프라 선정 (AWS vs GCP vs 국내)

---

## 📞 문의 & 협업

- 프로젝트 관리: Notion / GitHub Projects
- 커뮤니케이션: Slack / Discord

---

*이 문서는 초안이며, 개발 진행에 따라 지속적으로 업데이트됩니다.*


