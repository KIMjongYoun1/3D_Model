# 📐 컴포넌트 스펙 문서

> **목적**: Cursor에서 생성한 컴포넌트의 스타일 스펙을 Figma 디자인에 적용하기  
> **작성일**: 2025.12.06

---

## 🎨 Button 컴포넌트 스펙

### 크기 (Size)
- **Small**: 96px × 32px
  - Padding: 12px 24px
  - Font: 14px / SemiBold
- **Medium**: 120px × 40px (기본)
  - Padding: 16px 32px
  - Font: 16px / SemiBold
- **Large**: 160px × 48px
  - Padding: 24px 48px
  - Font: 18px / SemiBold

### Variants (타입)
- **Primary**
  - Background: #6366F1 (primary-500)
  - Text: #FFFFFF (white)
  - Hover: #4F46E5 (primary-600)
  - Focus Ring: #6366F1
- **Secondary**
  - Background: #EC4899 (secondary-500)
  - Text: #FFFFFF (white)
  - Hover: #DB2777 (secondary-600)
  - Focus Ring: #EC4899
- **Outline**
  - Background: transparent
  - Text: #6366F1 (primary-500)
  - Border: 2px solid #6366F1
  - Hover: #F3F4F6 (gray-50 background)
  - Focus Ring: #6366F1

### 스타일
- Border Radius: 8px (rounded-lg)
- Font Weight: 600 (SemiBold)
- Transition: colors
- Disabled: #D1D5DB (gray-300) background

### Figma 작업 가이드
1. 사각형 생성 (120 × 40px)
2. 색상 적용 (Primary: #6366F1)
3. 텍스트 추가 ("Button", 16px, SemiBold, 흰색)
4. Border Radius: 8px
5. Component로 변환
6. Variants 생성:
   - `Type`: Primary, Secondary, Outline
   - `Size`: Small, Medium, Large

---

## 📝 Input 컴포넌트 스펙

### 크기
- **기본**: 320px × 48px (width는 부모에 따라 조정 가능)
- Padding: 12px 16px (상하 12px, 좌우 16px)

### 스타일
- Background: #FFFFFF (white)
- Border: 1px solid #E5E7EB (gray-200)
- Border Radius: 8px (rounded-lg)
- Text: #111827 (gray-900), 16px, Regular
- Placeholder: #6B7280 (gray-500)
- Focus: 
  - Ring: 2px solid #6366F1
  - Border: transparent

### 상태
- **Default**: white background, gray border
- **Focus**: primary ring, transparent border
- **Error**: red border (#EF4444), red ring
- **Disabled**: gray-50 background, gray-300 border

### Label
- Font: 14px, Medium (500)
- Color: #111827 (gray-900)
- Margin Bottom: 8px
- Required indicator: red asterisk (*)

### Error Message
- Font: 14px, Regular
- Color: #EF4444 (red-500)
- Margin Top: 4px

### Figma 작업 가이드
1. 사각형 생성 (320 × 48px)
2. Border: 1px, #E5E7EB
3. Border Radius: 8px
4. Placeholder 텍스트 추가 (#6B7280)
5. Label 텍스트 추가 (선택사항)
6. Component로 변환
7. Variants 생성:
   - `State`: Default, Focus, Error, Disabled

---

## 🃏 Card 컴포넌트 스펙

### 크기
- **기본**: 너비는 부모에 따라 조정, 높이는 내용에 따라 자동
- Padding: 24px (p-6)

### 스타일
- Background: #FFFFFF (white)
- Border Radius: 8px (rounded-lg)
- Shadow: 
  - Default: shadow-md
  - Hover: shadow-lg (interactive일 때)
- Padding: 24px

### 타이포그래피
- **Title**: 20px (text-xl), SemiBold, #111827
- **Subtitle**: 14px (text-sm), Regular, #6B7280
- Margin Bottom: 16px (title/subtitle 영역)

### 상태
- **Default**: shadow-md
- **Interactive (onClick 있음)**: 
  - Cursor: pointer
  - Hover: shadow-lg

### Figma 작업 가이드
1. 사각형 생성 (예: 400 × 300px)
2. Background: white
3. Border Radius: 8px
4. Shadow: Medium shadow 적용
5. Padding: 24px 내부 여백
6. Title/Subtitle 텍스트 추가 (선택사항)
7. Component로 변환

---

## 🔐 Login 페이지 스펙

### 레이아웃
- **전체**: min-height 100vh, 중앙 정렬
- **Background**: #F9FAFB (gray-50)
- **컨테이너**: max-width 448px (28rem), 중앙 정렬, padding 좌우 16px

### 브랜드 영역
- **제목**: "Virtual Try-On"
  - Font: 30px (text-3xl), Bold, #111827
  - Margin Bottom: 8px
- **설명**: "로그인하여 서비스를 시작하세요"
  - Font: 16px, Regular, #6B7280
- **Margin Bottom**: 32px

### 로그인 카드
- **크기**: 전체 너비, 자동 높이
- **Background**: #FFFFFF (white)
- **Border Radius**: 8px (rounded-lg)
- **Shadow**: shadow-md
- **Padding**: 32px (p-8)

### 폼 영역
- **간격**: 24px (space-y-6)
- **Input 필드**: 2개 (이메일, 비밀번호)
- **Button**: 전체 너비, Large 사이즈

### 회원가입 링크
- **위치**: 카드 하단, 중앙 정렬
- **Margin Top**: 24px
- **Font**: 14px, Regular, #6B7280
- **링크 색상**: #6366F1 (primary-500), Hover: #4F46E5

### Figma 작업 가이드
1. **프레임 생성**: Desktop (1440 × 1024px)
2. **Background**: #F9FAFB
3. **중앙에 카드 배치**:
   - 카드 크기: 448px × 자동 높이
   - Background: white
   - Shadow: Medium
   - Border Radius: 8px
4. **브랜드 영역 추가** (카드 위)
5. **Input 컴포넌트 2개 배치**
6. **Button 컴포넌트 배치** (전체 너비)
7. **회원가입 링크 추가** (카드 하단)

---

## 📋 다음 단계

1. ✅ **기본 UI 컴포넌트 생성** (Button, Input, Card)
2. ✅ **Login 페이지 생성**
3. [ ] **Dashboard 페이지 생성** → 스펙 문서화
4. [ ] **Figma에서 컴포넌트 디자인** (위 스펙 참고)

---

**참고**: 이 스펙은 실제 생성된 컴포넌트 코드(`components/ui/`)를 기반으로 작성되었습니다.
