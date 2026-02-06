# 📐 컴포넌트 스펙 문서 (Quantum Studio)

Quantum Studio의 UI는 **Apple-Clean White** 스타일과 **Bento Grid** 레이아웃을 지향합니다.

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

### 스타일
- **Background**: Gray-50
- **Border**: 1px solid Gray-200 (Focus 시 Blue-500)
- **Border Radius**: 12px (rounded-xl)
- **Padding**: 12px 16px

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

*이 스펙은 `components/ui/`에 구현된 실제 코드를 기준으로 작성되었습니다.*
