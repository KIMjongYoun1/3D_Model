# Figma 가이드

Figma를 활용한 디자인 작업 가이드입니다.

## 목차
1. [디자인 시스템](#디자인-시스템)
2. [파일 생성 및 구조](#파일-생성-및-구조)
3. [코드 기반 디자인](#코드-기반-디자인)
4. [디자인 규칙](#디자인-규칙)
5. [MCP 연동](#mcp-연동)

---

## 디자인 시스템

### 색상 팔레트

**Primary Colors:**
- Primary: #6366F1 (Indigo-500)
- Primary Dark: #4F46E5 (Indigo-600)
- Primary Light: #818CF8 (Indigo-400)

**Secondary Colors:**
- Secondary: #EC4899 (Pink-500)
- Secondary Dark: #DB2777 (Pink-600)
- Secondary Light: #F472B6 (Pink-400)

**Neutral Colors:**
- Background: #FFFFFF
- Background Secondary: #F9FAFB
- Text Primary: #111827
- Text Secondary: #6B7280
- Border: #E5E7EB

**Status Colors:**
- Success: #10B981
- Warning: #F59E0B
- Error: #EF4444
- Info: #3B82F6

### 타이포그래피

**Font Family:**
- Primary: Inter (Sans-serif)
- Code: 'Fira Code', monospace

**Font Scale:**
- Display: 48px / 56px (Bold)
- H1: 36px / 44px (Bold)
- H2: 30px / 38px (SemiBold)
- H3: 24px / 32px (SemiBold)
- H4: 20px / 28px (SemiBold)
- Body Large: 18px / 28px (Regular)
- Body: 16px / 24px (Regular)
- Body Small: 14px / 20px (Regular)
- Caption: 12px / 16px (Regular)

### 간격 시스템
4px 그리드 시스템: 4px, 8px, 12px, 16px, 20px, 24px, 32px, 40px, 48px, 64px, 80px, 96px

### Border Radius
- Small: 4px
- Medium: 8px
- Large: 12px
- XLarge: 16px

### Shadows
- sm: 0 1px 2px rgba(0,0,0,0.05)
- md: 0 4px 6px rgba(0,0,0,0.1)
- lg: 0 10px 15px rgba(0,0,0,0.1)
- xl: 0 20px 25px rgba(0,0,0,0.1)

---

## 파일 생성 및 구조

### 새 파일 만들기
1. Figma 웹 브라우저 열기 (https://figma.com)
2. 좌측 상단 **"New design file"** 클릭
3. 파일명: **"Virtual Try-On - 3D_Model"** 입력

### 프레임 설정
- **Desktop**: 1440 x 1024px
- **Tablet**: 768 x 1024px
- **Mobile**: 375 x 812px

### 페이지 구조
```
📄 Cover (표지)
📄 Design System
   ├── Colors
   ├── Typography
   ├── Spacing
   └── Components
📄 Screens
   ├── Auth (인증)
   ├── Main (메인)
   ├── Try-On (가상 착용)
   └── Avatar (아바타)
📄 Prototypes (프로토타입)
```

---

## 코드 기반 디자인

### 워크플로우
```
1. Cursor에서 컴포넌트 코드 생성
   ↓
2. 코드 스타일 정보 추출 및 문서화
   ↓
3. Figma에서 코드 스펙 기반으로 디자인
```

### 컴포넌트 스펙 문서화
각 컴포넌트의 다음 정보를 정리:
- 크기 (Width × Height)
- 색상 (Background, Text, Border)
- 타이포그래피 (Font Size, Weight)
- 간격 (Padding, Margin)
- Border Radius
- Shadows

**참고 문서:**
- `design/COMPONENT_SPECS.md`: 컴포넌트 스펙 상세
- `.cursor/rules/design_system_rules.mdc`: 디자인 시스템 규칙

### Figma 작업 순서
1. 코드 스펙 확인
2. 색상 변수 생성 (코드와 동일한 이름)
3. Text Styles 생성 (코드와 동일한 이름)
4. Component 생성 및 Variants 설정
5. Auto Layout 적용

---

## 디자인 규칙

### 기본 규칙
1. **코드 스펙 우선**: 모든 디자인은 코드 스펙을 기반으로 생성
2. **변수 사용**: 색상, 간격, 타이포그래피는 변수로 정의
3. **하드코딩 금지**: 변수만 사용
4. **4px 그리드**: 모든 간격은 4px 배수

### Component 규칙
- 모든 재사용 요소는 Component로 생성
- Variants 사용: Type, Size 등 다양한 상태 관리
- Auto Layout 사용: 간격을 정확히 적용
- Constraints 설정: 반응형 동작 고려

### Button Component 예시
**Variants:**
- Type: Primary, Secondary, Outline
- Size: Small, Medium, Large

**스펙:**
- Small: 96px × 32px, Padding: 12px 24px
- Medium: 120px × 40px, Padding: 16px 32px
- Large: 160px × 48px, Padding: 24px 48px
- Border Radius: 8px

---

## MCP 연동

### Cursor에서 Figma 디자인 가져오기

**기본 사용법:**
```
Figma 파일 [FILE_KEY]에서 노드 [NODE_ID]의 디자인 컨텍스트를 가져와줘
```

**디자인을 코드로 변환:**
```
Figma 디자인을 React/Next.js 컴포넌트로 변환해줘
- TypeScript 사용
- TailwindCSS 스타일링
- Figma에서 가져온 색상과 간격 적용
```

**MCP 설정 방법:**
- [MCP_GUIDE.md](../guides/MCP_GUIDE.md) 참고

### Figma MCP 도구
- `get_design_context`: 디자인 컨텍스트 가져오기
- `get_screenshot`: 스크린샷 가져오기
- `get_metadata`: 메타데이터 가져오기

**주의사항:**
- Figma 데스크톱 앱이 실행되어 있어야 함
- 파일 접근 권한이 필요함
- 노드 ID는 URL에서 `node-id=` 뒤의 값 사용

---

## 참고 문서
- [COMPONENT_SPECS.md](./COMPONENT_SPECS.md): 컴포넌트 스펙 상세
- [MCP_GUIDE.md](../guides/MCP_GUIDE.md): MCP 설정 및 사용법
- [WORKFLOW_DESIGN_TO_DOCS.md](../guides/WORKFLOW_DESIGN_TO_DOCS.md): 전체 워크플로우

