# 🎨 QuantumViz Frontend Design System & Studio Architecture

이 문서는 QuantumViz의 프론트엔드 디자인 철학, 스튜디오 구조 및 각 디자인 안건(Option)별 구현 상세를 기록합니다.

> **최종 업데이트**: 2026-02-09 — 멀티 프론트엔드 구조(`frontend-studio`, `frontend-admin`) 반영

---

## 📁 프론트엔드 모듈 구조

프로젝트 리팩토링을 통해 단일 프론트엔드에서 **2개의 독립 프론트엔드 모듈**로 분리되었습니다.

| 모듈 | 경로 | 포트 | 역할 |
| :--- | :--- | :--- | :--- |
| **Studio** | `frontend-studio/` | 3000 | 사용자향 3D 시각화 스튜디오 |
| **Admin** | `frontend-admin/` | 3001 | 관리자 대시보드 (지식 베이스 관리) |

### 공통 기술 스택
- Next.js 14.2.0 (App Router)
- React 18.3.0
- Tailwind CSS
- TypeScript

### Studio 전용 의존성
- @react-three/fiber 8.16.0 (3D 렌더링)
- @react-three/drei 9.105.0 (3D 유틸리티)
- Three.js 0.160.0
- Zustand 4.5.0 (상태 관리)
- @tanstack/react-query 5.28.0

---

## 🏗️ Core Architecture: Spatial Studio Workspace
QuantumViz는 2026년형 **공간 컴퓨팅(Spatial Computing)** UI를 지향하며, 3D 캔버스를 전체 배경으로 하고 데이터 도식을 하단에 배치한 개방형 구조를 채택하고 있습니다.

### 핵심 레이어 구조 (Layered Architecture)
1. **Layer 1 (Background)**: 3D Quantum Canvas (White Theme / Spatial Offset)
2. **Layer 2 (Floating Bottom)**: Horizontal Schematic Bar (Bento Scroll Style)
3. **Layer 3 (Global)**: Interaction & Popup Layer (Z-Index Workspace)
4. **Layer 4 (Right Drawer)**: Neural Input Panel (Swipe-in Interface)

---

## 💎 디자인 안건 1: 전면 개방형 스페이셜 스튜디오 (Full-Spatial Studio) - [완성형 적용]

### 1. 개념 및 특징
*   **해결 과제**: 화면 분할로 인한 공간 협소함 및 데이터 가둠 현상 해결.
*   **디자인 철학**: "무한한 캔버스와 유연한 레이어". 3D 캔버스를 전체 배경으로 하고, UI 요소들은 투명한 유리판(Overlay)처럼 배치.
*   **주요 기술**: 
    *   **Full-Window Rendering**: 3D 캔버스가 브라우저 전체 영역을 사용하여 공간감 극대화.
    *   **Bottom-Sheet Overlay**: 하단 다이어그램 바를 3D 시점에 영향을 주지 않는 순수 오버레이 방식으로 구현.
    *   **Contextual Popups**: 3D 노드 클릭 시 월드 좌표를 스크린 좌표로 변환하여 노드 바로 옆에 정보창 생성.
    *   **Ultra-Bold Typography**: 3D 텍스트 두께 900, 외곽선 0.2로 화이트 배경 시인성 확보.
    *   **GPU Accelerated Drag**: `translate3d` 및 DOM 직접 조작으로 지연 없는 드래그 구현.

### 2. 구현 코드 상세 (Implementation)

#### A. 메인 레이아웃 구조 (`frontend-studio/app/studio/page.tsx`)
```tsx
<div className="flex-1 flex w-full overflow-hidden relative bg-white">
  {/* [Layer 1] 3D 캔버스: 전체 배경 고정 */}
  <div className="absolute inset-0 z-10">
    <QuantumCanvas centerOffset={[0, 0, 0]} {...props} />
  </div>

  {/* [Layer 2] 하단 다이어그램 바: 3D 뷰 위에 떠 있는 바텀 시트 */}
  <div className="absolute bottom-0 left-0 right-0 h-[380px] z-30 transition-all">
    <div className="w-full h-full bg-white/40 backdrop-blur-3xl rounded-t-[3.5rem]">
      {/* 검색 및 ERD 다이어그램 */}
    </div>
  </div>

  {/* [Layer 3] 글로벌 팝업: 데이터-해석-근거 3단 분리 레이아웃 */}
  <div className="absolute inset-0 z-50 pointer-events-none">
    {openNodes.map(node => <DraggableWindow {...props} />)}
  </div>
</div>
```

#### B. 3D 울트라 볼드 스타일 (`frontend-studio/components/QuantumCanvas.tsx`)
```tsx
<Text 
  fontSize={1.4} 
  color="#1e3a8a" 
  fontWeight={900} 
  outlineWidth={0.2} 
  outlineColor="#ffffff"
>
  {node.label}
</Text>
```

#### C. 2D/3D 모드 전환
스튜디오 서브 헤더에서 `vizMode` 토글을 통해 2D(ERDDiagram) / 3D(QuantumCanvas) 뷰를 실시간 전환할 수 있습니다.

---

## 📂 컴포넌트 파일 구조

### frontend-studio/components/
```
components/
├── common/
│   ├── Footer.tsx          # 공통 푸터
│   └── Header.tsx          # 공통 헤더 (인증 상태 반영)
├── shared/
│   └── DraggableWindow.tsx # GPU 가속 드래그 팝업
├── studio/
│   └── Onboarding.tsx      # 비회원 온보딩 가이드
├── ui/                     # 디자인 시스템 컴포넌트
│   ├── Button.tsx
│   ├── Card.tsx
│   ├── Input.tsx
│   └── Modal.tsx
├── ERDDiagram.tsx          # 2D 관계도 다이어그램
└── QuantumCanvas.tsx       # 3D 시각화 캔버스 (Three.js)
```

### frontend-admin/components/
```
components/
├── common/
│   ├── Footer.tsx
│   └── Header.tsx
└── ui/
    ├── Button.tsx
    ├── Card.tsx
    ├── Input.tsx
    └── Modal.tsx
```

---

## 📂 대기 중인 디자인 안건 (Future Options)

### 디자인 안건 2: 사이드 스와이프 모드 (Side Swipe)
*   **특징**: 우측 영역을 프롬프트 입력 전용으로 사용하며, 필요 시에만 스와이프하여 확장.

### 디자인 안건 3: 다이내믹 스플릿 (Dynamic Split)
*   **특징**: 사용자의 시선에 따라 3D와 2D 영역의 비중을 실시간 조절.

---

## 📈 발전 방향 (Roadmap)
1. **WebXR 대응**: 하단바를 손목 메뉴(Wrist UI) 형태로 변환하여 VR 확장.
2. **Adaptive Typography**: 카메라 거리에 따라 텍스트 두께를 동적으로 조절.
