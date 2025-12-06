# 🎨 Figma 디자인 가이드

> **프로젝트**: Virtual Try-On 서비스  
> **기술 스택**: Next.js 14, TypeScript, TailwindCSS, Three.js  
> **작성일**: 2025.12.06

---

## 📋 디자인 시스템

### 색상 팔레트

#### Primary Colors
```
Primary: #6366F1 (Indigo-500)
Primary Dark: #4F46E5 (Indigo-600)
Primary Light: #818CF8 (Indigo-400)
```

#### Secondary Colors
```
Secondary: #EC4899 (Pink-500)
Secondary Dark: #DB2777 (Pink-600)
Secondary Light: #F472B6 (Pink-400)
```

#### Neutral Colors
```
Background: #FFFFFF (White)
Background Secondary: #F9FAFB (Gray-50)
Text Primary: #111827 (Gray-900)
Text Secondary: #6B7280 (Gray-500)
Border: #E5E7EB (Gray-200)
```

#### Status Colors
```
Success: #10B981 (Green-500)
Warning: #F59E0B (Amber-500)
Error: #EF4444 (Red-500)
Info: #3B82F6 (Blue-500)
```

### 타이포그래피

#### Font Family
```
Primary: Inter (Sans-serif)
Code: 'Fira Code', monospace
```

#### Font Sizes
```
Display: 48px / 56px (Bold)
H1: 36px / 44px (Bold)
H2: 30px / 38px (SemiBold)
H3: 24px / 32px (SemiBold)
H4: 20px / 28px (SemiBold)
Body Large: 18px / 28px (Regular)
Body: 16px / 24px (Regular)
Body Small: 14px / 20px (Regular)
Caption: 12px / 16px (Regular)
```

### 간격 (Spacing)

```
4px, 8px, 12px, 16px, 20px, 24px, 32px, 40px, 48px, 64px, 80px, 96px
```

### Border Radius

```
Small: 4px
Medium: 8px
Large: 12px
XLarge: 16px
Full: 9999px
```

### Shadows

```
Small: 0 1px 2px 0 rgba(0, 0, 0, 0.05)
Medium: 0 4px 6px -1px rgba(0, 0, 0, 0.1)
Large: 0 10px 15px -3px rgba(0, 0, 0, 0.1)
XLarge: 0 20px 25px -5px rgba(0, 0, 0, 0.1)
```

---

## 📱 주요 화면 목록

### 1. 인증 화면

#### 1.1 로그인 페이지 (`/login`)
**레이아웃:**
- 중앙 정렬 카드 레이아웃
- 이메일/비밀번호 입력 필드
- 로그인 버튼
- 회원가입 링크
- 소셜 로그인 (선택)

**컴포넌트:**
- Input (이메일, 비밀번호)
- Button (Primary)
- Link (회원가입)
- Divider (소셜 로그인 구분선)

#### 1.2 회원가입 페이지 (`/register`)
**레이아웃:**
- 중앙 정렬 카드 레이아웃
- 이메일, 비밀번호, 비밀번호 확인, 이름 입력 필드
- 회원가입 버튼
- 로그인 링크
- 약관 동의 체크박스

**컴포넌트:**
- Input (이메일, 비밀번호, 비밀번호 확인, 이름)
- Button (Primary)
- Checkbox (약관 동의)
- Link (로그인)

---

### 2. 메인 화면

#### 2.1 대시보드 (`/dashboard`)
**레이아웃:**
- 헤더 (로고, 네비게이션, 프로필)
- 사이드바 (메뉴)
- 메인 콘텐츠 영역
  - 환영 메시지
  - 빠른 액션 카드
  - 최근 Try-On 결과 갤러리
  - 통계 (선택)

**컴포넌트:**
- Header (Navigation)
- Sidebar (Menu)
- Card (빠른 액션, 결과 미리보기)
- Image Grid (갤러리)

#### 2.2 메인 페이지 (`/`)
**레이아웃:**
- 히어로 섹션 (서비스 소개)
- 주요 기능 소개
- 사용 방법 안내
- CTA 버튼 (시작하기)

**컴포넌트:**
- Hero Section
- Feature Cards
- CTA Button

---

### 3. Try-On 화면

#### 3.1 의상 업로드 페이지 (`/tryon/upload`)
**레이아웃:**
- 헤더
- 업로드 영역 (드래그 앤 드롭)
- 업로드된 의상 미리보기
- 의상 목록 (이미 업로드된 것들)
- Try-On 시작 버튼

**컴포넌트:**
- FileUpload (드래그 앤 드롭)
- ImagePreview
- ImageGrid (의상 목록)
- Button (Primary, Secondary)

#### 3.2 Try-On 처리 중 페이지 (`/tryon/processing`)
**레이아웃:**
- 진행 상태 표시
- 로딩 애니메이션
- 예상 소요 시간
- 취소 버튼

**컴포넌트:**
- ProgressBar
- LoadingSpinner
- Button (Secondary - 취소)

#### 3.3 Try-On 결과 페이지 (`/tryon/result`)
**레이아웃:**
- 원본 이미지와 결과 이미지 비교
- 결과 이미지 확대 보기
- 다운로드 버튼
- 공유 버튼
- 다시 시도 버튼
- 다른 의상 선택 버튼

**컴포넌트:**
- ImageComparison (Before/After)
- ImageViewer (확대 보기)
- Button (Primary, Secondary, Icon)
- ShareButtons (SNS 공유)

---

### 4. 아바타 화면

#### 4.1 아바타 설정 페이지 (`/avatar/setup`)
**레이아웃:**
- 얼굴 사진 업로드 영역
- 체형 설정 (키, 체중, 체형)
- 아바타 미리보기
- 저장 버튼

**컴포넌트:**
- FileUpload (얼굴 사진)
- Input (키, 체중)
- Select (체형)
- AvatarPreview
- Button (Primary)

#### 4.2 3D 아바타 뷰어 (`/avatar/viewer`)
**레이아웃:**
- 3D 뷰어 캔버스 (Three.js)
- 컨트롤 패널 (회전, 줌, 각도 선택)
- 의상 적용 버튼
- 캡처 버튼 (스크린샷)

**컴포넌트:**
- Canvas3D (Three.js)
- ControlPanel (회전, 줌)
- AngleSelector (각도 선택)
- Button (Icon)

---

### 5. 갤러리 화면

#### 5.1 결과 갤러리 (`/gallery`)
**레이아웃:**
- 그리드 레이아웃 (이미지 갤러리)
- 필터 (날짜, 의상 카테고리)
- 정렬 옵션
- 이미지 클릭 시 상세 보기

**컴포넌트:**
- ImageGrid (Masonry 또는 Grid)
- FilterDropdown
- SortDropdown
- ImageModal (상세 보기)

---

### 6. 설정 화면

#### 6.1 프로필 설정 (`/settings/profile`)
**레이아웃:**
- 프로필 이미지 업로드
- 이름, 이메일 수정
- 비밀번호 변경
- 저장 버튼

**컴포넌트:**
- AvatarUpload
- Input (이름, 이메일)
- Button (Primary)

#### 6.2 구독 설정 (`/settings/subscription`)
**레이아웃:**
- 현재 구독 플랜 표시
- 플랜 비교 테이블
- 업그레이드/다운그레이드 버튼
- 결제 내역

**컴포넌트:**
- PlanCard
- ComparisonTable
- Button (Primary, Secondary)

---

## 🧩 공통 컴포넌트

### 버튼 (Button)
```
Primary: Indigo 배경, 흰색 텍스트
Secondary: 회색 배경, Indigo 텍스트
Ghost: 투명 배경, Indigo 텍스트
Icon: 아이콘만 표시
```

### 입력 필드 (Input)
```
Default: 회색 테두리
Focus: Indigo 테두리
Error: 빨간색 테두리 + 에러 메시지
Disabled: 회색 배경
```

### 카드 (Card)
```
Default: 흰색 배경, 그림자
Hover: 그림자 증가
Clickable: 커서 포인터
```

### 모달 (Modal)
```
배경: 반투명 검정 오버레이
내용: 중앙 정렬 카드
닫기: X 버튼 또는 배경 클릭
```

### 로딩 (Loading)
```
Spinner: 회전 애니메이션
Skeleton: 콘텐츠 로딩 중 스켈레톤
Progress: 진행률 표시
```

---

## 📐 레이아웃 규칙

### 그리드 시스템
```
Desktop: 12컬럼 그리드
Tablet: 8컬럼 그리드
Mobile: 4컬럼 그리드
Gutter: 24px (Desktop), 16px (Tablet/Mobile)
```

### 브레이크포인트
```
Mobile: 0-767px
Tablet: 768-1023px
Desktop: 1024px+
```

### 컨테이너 최대 너비
```
Small: 640px
Medium: 768px
Large: 1024px
XLarge: 1280px
Full: 100%
```

---

## 🎯 Figma 파일 구조 제안

```
Virtual Try-On Design System
│
├── 🎨 Design System
│   ├── Colors
│   ├── Typography
│   ├── Spacing
│   ├── Shadows
│   └── Components (기본 컴포넌트)
│       ├── Buttons
│       ├── Inputs
│       ├── Cards
│       ├── Modals
│       └── Loading
│
├── 📱 Pages
│   ├── Auth
│   │   ├── Login
│   │   └── Register
│   ├── Main
│   │   ├── Home
│   │   └── Dashboard
│   ├── Try-On
│   │   ├── Upload
│   │   ├── Processing
│   │   └── Result
│   ├── Avatar
│   │   ├── Setup
│   │   └── Viewer
│   ├── Gallery
│   └── Settings
│
└── 🧩 Components
    ├── Layout
    │   ├── Header
    │   ├── Sidebar
    │   └── Footer
    ├── Features
    │   ├── ImageUpload
    │   ├── ImageComparison
    │   ├── AvatarPreview
    │   └── 3DViewer
    └── Shared
        ├── Navigation
        ├── UserMenu
        └── ShareButtons
```

---

## 🚀 Figma 디자인 시작 가이드

### Step 1: Figma 파일 생성
1. Figma 새 파일 생성
2. 파일명: "Virtual Try-On Design System"
3. 프레임 크기: Desktop (1440x1024), Tablet (768x1024), Mobile (375x812)

### Step 2: 디자인 시스템 구축
1. **Colors 페이지 생성**
   - Primary, Secondary, Neutral, Status 색상 팔레트 추가
   - 각 색상에 변수명 지정 (예: `primary-500`)

2. **Typography 페이지 생성**
   - Inter 폰트 설치
   - 텍스트 스타일 정의 (H1, H2, Body 등)

3. **Components 페이지 생성**
   - 기본 컴포넌트들 (Button, Input, Card 등) 생성
   - Variants 설정 (Primary, Secondary 등)

### Step 3: 주요 화면 디자인
1. **인증 화면**부터 시작
   - 로그인 페이지
   - 회원가입 페이지

2. **메인 화면**
   - 홈 페이지
   - 대시보드

3. **Try-On 화면**
   - 업로드 페이지
   - 결과 페이지

4. **아바타 화면**
   - 설정 페이지
   - 3D 뷰어 (플레이스홀더)

### Step 4: 프로토타이핑
1. 각 화면 간 연결 설정
2. 인터랙션 추가 (클릭, 호버 등)
3. 사용자 플로우 테스트

---

## 💡 디자인 팁

### 1. 모바일 퍼스트
- 모바일 화면부터 디자인하고 확장
- 터치 친화적인 버튼 크기 (최소 44x44px)

### 2. 접근성
- 색상 대비 비율 4.5:1 이상
- 포커스 상태 명확히 표시
- 에러 메시지 명확히 표시

### 3. 성능 고려
- 이미지 최적화 (WebP 형식)
- 로딩 상태 명확히 표시
- 스켈레톤 UI 사용

### 4. 3D 뷰어 고려사항
- Three.js 캔버스 영역 명확히 표시
- 컨트롤 버튼 직관적으로 배치
- 각도 선택 UI 명확히

---

## 📚 참고 자료

- [TailwindCSS 색상 팔레트](https://tailwindcss.com/docs/customizing-colors)
- [Inter 폰트](https://rsms.me/inter/)
- [Three.js 예제](https://threejs.org/examples/)
- [Next.js App Router](https://nextjs.org/docs/app)

---

## 🔗 다음 단계

1. ✅ Figma 파일 생성 및 디자인 시스템 구축
2. ✅ 주요 화면 디자인
3. ✅ 프로토타이핑
4. ✅ Cursor에서 Figma MCP로 디자인 가져오기
5. ✅ React 컴포넌트로 변환

