# 🎨 Virtual Try-On 서비스 기획안 (초안)

> **버전**: v0.1 (초안)  
> **작성일**: 2025.11.30  
> **개발 규모**: 1인 ~ 3인  
> **예상 월 유지비**: 약 30만원

---

## 📋 프로젝트 개요

### 서비스명 (가칭)
- **FitMe** / **TryOn3D** / **VirtualFit**

### 핵심 가치
> "옷을 사기 전에 내 모습으로 미리 입어본다"

### 주요 기능
1. **의상 촬영 → 3D 모델링**: 옷 사진을 찍으면 AI가 분석하여 착용 이미지 생성
2. **개인화 마네킹**: 사용자 얼굴이 반영된 3D 아바타에 옷 적용
3. **결과물 저장/공유**: 착용 시뮬레이션 결과를 저장하고 SNS 공유

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
- [시스템 아키텍처](./docs/technical/ARCHITECTURE.md)
- [AI 모델 선정](./docs/technical/AI_MODELS.md)
- [AI 모듈 연동](./docs/technical/AI_INTEGRATION.md)
- [보안 가이드](./docs/technical/SECURITY.md)

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
- [프로젝트 구조](./docs/STRUCTURE.md)
- [Git Ignore 가이드](./docs/GIT_IGNORE_GUIDE.md)

---

## 🔄 서비스 플로우 (Big Picture)

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           사용자 플로우                                   │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  [1] 얼굴 등록       [2] 옷 사진 촬영      [3] AI 합성        [4] 결과물  │
│  ┌─────────┐        ┌─────────┐         ┌─────────┐       ┌─────────┐   │
│  │ 📸 얼굴 │  ──▶   │ 👗 옷   │   ──▶   │ 🤖 AI   │ ──▶   │ 🖼️ 착용 │   │
│  │  사진   │        │  사진   │         │  처리   │       │  이미지 │   │
│  └─────────┘        └─────────┘         └─────────┘       └─────────┘   │
│       │                  │                  │                  │        │
│       ▼                  ▼                  ▼                  ▼        │
│  Face Mesh 생성   의상 세그멘테이션   Virtual Try-On     3D 렌더링      │
│  + 3D 아바타 생성  + 텍스처 추출      모델 적용         + 공유/저장     │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

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
2. [시스템 아키텍처](./docs/technical/ARCHITECTURE.md)에서 기술 스택 확인
3. [개발 로드맵](./docs/planning/ROADMAP.md)의 Phase 1부터 진행
4. [ERD](./docs/design/ERD.md)를 참고하여 DB 설계

---

## 📌 주요 결정 사항 (TBD)

- [ ] 서비스명 최종 결정
- [ ] AI 모델 최종 선정 (IDM-VTON vs OOTDiffusion)
- [ ] 클라우드 인프라 선정 (AWS vs GCP vs 국내)
- [ ] 수익 모델 결정 (B2C 구독 vs B2B API)

---

## 📞 문의 & 협업

- 프로젝트 관리: Notion / GitHub Projects
- 커뮤니케이션: Slack / Discord

---

*이 문서는 초안이며, 개발 진행에 따라 지속적으로 업데이트됩니다.*


