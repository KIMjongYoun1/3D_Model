# 🎨 Quantum Studio UI/UX 명세서

Quantum Studio의 프론트엔드는 **Apple-Clean White** 테마와 **Spatial Computing** UI를 지향하며, Next.js 14 App Router와 Tailwind CSS를 기반으로 구축되었습니다.

> **최종 업데이트**: 2026-02-09 — 멀티 프론트엔드 구조 및 Admin 페이지 반영

---

## 🏗️ 디자인 시스템 핵심 규격

### 1. 컬러 팔레트 (Color Palette)
- **Primary**: `Blue-600 (#2563EB)` - 핵심 액션 및 브랜드 컬러
- **Background**: `White (#FFFFFF)` / `Slate-50 (#F8FAFC)` - 깨끗한 공간감 강조
- **Surface**: `White/40` + `Backdrop Blur (3xl)` - 유리 질감(Glassmorphism)의 오버레이 레이어
- **Text**: `Slate-900` (Heading), `Slate-500` (Body), `Slate-400` (Caption)

### 2. 컴포넌트 시스템 (`frontend-studio/components/ui/`, `frontend-admin/components/ui/`)

두 프론트엔드 모듈에서 동일한 디자인 시스템 컴포넌트를 공유합니다.

#### [Button]
- **Variants**: `primary`, `secondary`, `outline`, `ghost`, `naver`, `kakao`
- **Style**: `rounded-full`, `font-black`, `tracking-widest`, `uppercase`
- **Interaction**: Hover 시 그림자 강화 및 색상 변화

#### [Card]
- **Variants**: 
  - `default`: 흰색 배경, 둥근 모서리 (2.5rem)
  - `bento`: 유리 질감, 호버 시 스케일 업 (1.02x), 둥근 모서리 (3rem)
  - `glass`: 투명도 높은 배경, 강한 블러 처리, 둥근 모서리 (3.5rem)

#### [Input]
- **Types**: `text`, `password`, `textarea`
- **Style**: `bg-slate-50`, `rounded-xl`, `border-slate-200`, `focus:ring-blue-500`

---

## 📱 주요 페이지 명세

### Frontend-Studio (Port 3000)

#### 1. 메인 스튜디오 (`/studio`)
데이터 시각화의 핵심 공간으로, 4단계 레이어 구조를 가집니다.
- **Layer 1 (Background)**: 3D Quantum Canvas (Three.js) - 전체 화면 렌더링, **2D/3D 모드 전환** 지원
- **Layer 2 (Bottom Sheet)**: 다이어그램 바 - 노드 검색 및 2D 관계도(ERDDiagram) 표시
- **Layer 3 (Overlay)**: 
  - **Neural Input**: 우측 슬라이드 인 패널. 파일 업로드(PDF/TXT/Excel/CSV) 및 텍스트 입력 지원
  - **Draggable Windows**: 3D 노드 클릭 시 나타나는 플로팅 정보창
- **Sub Header**: 2D/3D 토글, Auto Focus 토글, Diagram 토글, New Mapping 버튼
- **카테고리 선택**: Main Category(GENERAL, FINANCE, INFRA, LOGISTICS) → Sub Category 2단 선택

#### 2. 마이페이지 (`/mypage`)
사용자 프로필 및 구독 상태를 관리합니다.
- **Auth Guard**: 비로그인 시 접근 제한 및 로그인 유도 팝업 표시
- **Profile Card**: 로그인 수단(Naver/Email), 이메일, 이름 정보 표시
- **Subscription**: 현재 플랜 정보 및 업그레이드 버튼 제공

#### 3. 결제 페이지 (`/payment`)
구독 플랜 선택 및 시뮬레이션 결제를 진행합니다.
- **Pricing Cards**: Free, Basic, Pro 플랜 비교
- **Most Popular Badge**: 추천 플랜(Pro)에 시각적 강조 효과 적용

#### 4. 로그인 페이지 (`/(auth)/login`)
이메일 및 네이버 소셜 로그인을 지원합니다.

### Frontend-Admin (Port 3001)

#### 1. 관리자 대시보드 (`/`)
시스템 현황 및 관리 기능 진입점.

#### 2. 지식 베이스 관리 (`/knowledge`)
AI 분석에 활용되는 도메인별 지식 데이터를 관리합니다.
- 지식 항목 CRUD (생성, 조회, 수정, 삭제)
- 카테고리별 분류 및 검색

---

## 🛠️ UI 인터랙션 및 상태 관리

### 1. 인증 상태 동기화
- `localStorage`의 `accessToken` 존재 여부로 로그인 판단
- `auth-change` 커스텀 이벤트 및 `storage` 이벤트를 통해 헤더와 페이지 간 로그인 상태 실시간 동기화
- 비회원 접근 시 Onboarding 컴포넌트 자동 표시

### 2. 3D 인터랙션
- **Auto Focus**: 새로운 데이터 로드 시 카메라가 노드 그룹 중앙으로 자동 이동 (토글 가능)
- **Node Selection**: 3D 노드 클릭 시 해당 노드 정보를 담은 `DraggableWindow` 생성 및 최상단 배치
- **2D/3D 전환**: 서브 헤더의 토글을 통해 QuantumCanvas(3D) ↔ ERDDiagram(2D) 실시간 전환

### 3. 반응형 레이아웃
- **Desktop**: 3D 캔버스와 사이드 패널 동시 활용
- **Mobile**: 헤더 메뉴 간소화 및 사이드 패널 전체 화면 전환

---

## 🔗 관련 문서
- [API 명세서](./API_SPECIFICATION.md)
- [디자인 시스템 상세](./FRONTEND_DESIGN_SYSTEM.md)
- [프로젝트 개요](./PROJECT_OVERVIEW.md)
- [컴포넌트 스펙](./design/COMPONENT_SPECS.md)
