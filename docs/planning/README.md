# 🎨 Quantum Studio 서비스 기획안

Quantum Studio는 비정형 데이터를 3D 공간에 지능적으로 도식화하고 분석하는 차세대 시각화 플랫폼입니다.

---

## 📋 프로젝트 개요

### 서비스명
- **Quantum Studio** (퀀텀 스튜디오)

### 핵심 가치
> "복잡한 데이터를 한눈에 이해하는 3D 지능형 캔버스"

### 주요 기능
1. **비정형 데이터 분석**: JSON, 로그, 문서 등을 AI가 분석하여 구조화된 노드로 변환
2. **지능형 3D 매핑**: 데이터 간의 상관관계를 3D 공간에 실시간 렌더링
3. **하이브리드 AI 엔진**: 클라우드와 로컬 모델을 결합한 최적의 분석 제공

---

## 📁 문서 구조

### 📋 핵심 문서
- [PROJECT_OVERVIEW.md](../PROJECT_OVERVIEW.md): 프로젝트 개요 및 아키텍처
- [API_SPECIFICATION.md](../API_SPECIFICATION.md): API 및 주요 메소드 명세서
- [UI_SPECIFICATION.md](../UI_SPECIFICATION.md): 프론트엔드 UI/UX 명세서
- [DEVELOPMENT_GUIDE.md](../DEVELOPMENT_GUIDE.md): 핵심 개발 가이드 및 패턴
- [FRONTEND_DESIGN_SYSTEM.md](../FRONTEND_DESIGN_SYSTEM.md): 디자인 시스템 및 컴포넌트 규격
- [SOCIAL_AUTH_SETUP.md](../SOCIAL_AUTH_SETUP.md): 소셜 로그인 설정 가이드

### 📊 설계 및 로드맵
- [ROADMAP.md](./ROADMAP.md): 개발 로드맵 및 마일스톤
- [ERD.md](../design/ERD.md): 데이터베이스 설계

---

## 🏗️ 서비스 아키텍처 (Big Picture)

```text
[사용자 데이터] ──▶ [AI 분석 엔진 (Python)] ──▶ [3D 시각화 (Next.js)]
      │                                              │
      └──────▶ [인증/결제 (Java)] ◀──────────────────┘
```

---

## 📌 주요 결정 사항

- **Framework**: Next.js 14 (Frontend), Spring Boot 3.2 (Java BE), FastAPI (Python BE)
- **Database**: PostgreSQL (Main), Redis (Cache)
- **AI**: Google Gemini Pro API 연동
- **Theme**: Apple-Clean White Style

---

## 📞 문의 & 협업

- 프로젝트 관리: GitHub Issues / Projects
- 커뮤니케이션: Slack / Discord

---

*이 문서는 현재 개발 진행 상태를 반영하여 지속적으로 업데이트됩니다.*
