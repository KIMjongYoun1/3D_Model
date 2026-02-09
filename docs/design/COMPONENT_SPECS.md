# 📐 컴포넌트 스펙 문서 (Quantum Studio)

Quantum Studio의 UI는 **Apple-Clean White** 스타일과 **Bento Grid** 레이아웃을 지향합니다.

> **최종 업데이트**: 2026-02-09 — 멀티 프론트엔드 구조 반영

---

## 📁 컴포넌트 위치

두 프론트엔드 모듈에서 동일한 디자인 시스템을 유지합니다.

| 모듈 | 경로 |
| :--- | :--- |
| Studio | `frontend-studio/components/ui/` |
| Admin | `frontend-admin/components/ui/` |

---

## 🎨 Button 컴포넌트 스펙

### Variants (타입)
- **Primary**: Blue-600 배경, White 텍스트 (핵심 액션)
- **Secondary**: White 배경, Blue-600 보더/텍스트 (보조 액션)
- **Ghost**: 배경 없음, Gray-600 텍스트 (최소 액션)
- **Naver**: #03C75A 배경, White 텍스트 (네이버 로그인 전용)
- **Kakao**: #FEE500 배경, Black 텍스트 (카카오 로그인 전용)

### 스타일
- **Border Radius**: 12px (rounded-xl)
- **Font**: Inter/Pretendard, SemiBold (600)
- **Shadow**: Subtle shadow on hover

---

## 🃏 Card 컴포넌트 스펙

### Variants
- **Bento**: White 배경, 1px Gray-100 보더, 둥근 모서리 (24px)
- **Glass**: White/40 배경, Backdrop Blur (20px), 투명 보더
- **Interactive**: Hover 시 살짝 떠오르는 효과 (Scale 1.02)

### 스타일
- **Border Radius**: 24px (rounded-3xl)
- **Padding**: 24px (p-6)
- **Shadow**: Soft shadow (shadow-sm)

---

## 📝 Input 컴포넌트 스펙

### 지원 타입
- `text`: 일반 텍스트 입력
- `password`: 비밀번호 입력 (마스킹)
- `textarea`: 대용량 텍스트 입력 (Neural Input 패널 등에서 사용)

### 스타일
- **Background**: Gray-50
- **Border**: 1px solid Gray-200 (Focus 시 Blue-500)
- **Border Radius**: 12px (rounded-xl)
- **Padding**: 12px 16px

---

## 🪟 Modal 컴포넌트 스펙

### 특징
- 전역 팝업 시스템
- Backdrop blur 적용
- 애니메이션 진입/퇴장 효과

---

## 🏗️ Layout 스펙

### Bento Grid
- **Gap**: 24px (6rem)
- **Columns**: Desktop 기준 4열 / 12열 그리드 혼용
- **Container**: Max-width 1280px (7xl), 중앙 정렬

### Typography
- **Heading**: Bold (700), Gray-900
- **Body**: Regular (400), Gray-600
- **Accent**: SemiBold (600), Blue-600

---

## 🖼️ Studio 전용 컴포넌트

Studio(`frontend-studio`)에만 존재하는 특수 컴포넌트입니다.

| 컴포넌트 | 경로 | 역할 |
| :--- | :--- | :--- |
| `QuantumCanvas` | `frontend-studio/components/QuantumCanvas.tsx` | Three.js 기반 3D 시각화 캔버스 |
| `ERDDiagram` | `frontend-studio/components/ERDDiagram.tsx` | 2D 관계도 다이어그램 |
| `DraggableWindow` | `frontend-studio/components/shared/DraggableWindow.tsx` | GPU 가속 드래그 팝업 |
| `Onboarding` | `frontend-studio/components/studio/Onboarding.tsx` | 비회원 온보딩 가이드 |

---

*이 스펙은 `frontend-studio/components/ui/` 및 `frontend-admin/components/ui/`에 구현된 실제 코드를 기준으로 작성되었습니다.*
