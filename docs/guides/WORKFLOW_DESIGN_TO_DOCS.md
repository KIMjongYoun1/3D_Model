# 🎨 디자인부터 개발 문서화까지 워크플로우

> **버전**: v1.0  
> **작성일**: 2025.01.XX  
> **대상**: Figma MCP + Notion MCP를 활용한 전체 개발 프로세스

---

## 📋 개요

이 문서는 **Figma MCP**와 **Notion MCP**를 활용하여 디자인부터 개발, 문서화까지의 전체 워크플로우를 설명합니다.

```
Figma 디자인 → Cursor 개발 → Notion 문서화
     ↓              ↓              ↓
  MCP 연결      코드 생성      자동 문서화
```

---

## 🔄 전체 워크플로우

### Phase 1: 디자인 (Figma)

#### 1.1 Figma에서 UI 디자인

```
✅ Figma에서 화면 디자인
   ├── 컴포넌트 설계
   ├── 레이아웃 구성
   ├── 색상/타이포그래피 정의
   └── 프로토타이핑
```

#### 1.2 Cursor에서 Figma 디자인 가져오기

**Cursor 채팅 예시:**

```
"Figma에서 현재 프로젝트의 메인 페이지 디자인 가져와줘"
"Figma 디자인 컴포넌트 정보를 보여줘"
"Figma에서 버튼 컴포넌트 스타일 정보 가져와줘"
```

**결과:**
- Figma 디자인 정보가 Cursor에 로드됨
- 컴포넌트 구조, 색상, 간격 등 정보 확인 가능

---

### Phase 2: 개발 (Cursor + Figma MCP)

#### 2.1 디자인 기반 컴포넌트 생성

**Cursor 채팅 예시:**

```
"이 Figma 디자인을 React 컴포넌트로 만들어줘.
- Next.js 14 App Router 사용
- TypeScript 사용
- TailwindCSS로 스타일링
- Figma에서 가져온 색상과 간격 적용"
```

**생성되는 코드 구조:**

```typescript
// components/ui/Button.tsx
export const Button = ({ children, variant, ...props }) => {
  // Figma 디자인 기반 스타일 적용
  return <button className={...}>{children}</button>
}
```

#### 2.2 컴포넌트 스타일링

**Cursor 채팅 예시:**

```
"Figma 디자인에서 가져온 색상 팔레트를 TailwindCSS config에 추가해줘"
"Figma에서 정의한 간격(spacing)을 TailwindCSS에 적용해줘"
```

#### 2.3 페이지 구성

**Cursor 채팅 예시:**

```
"Figma 디자인을 기반으로 메인 페이지를 만들어줘.
- 컴포넌트 재사용
- 반응형 디자인 적용
- Figma 프로토타입과 동일한 레이아웃"
```

#### 2.4 API 개발

**Cursor 채팅 예시:**

```
"이 컴포넌트에 필요한 FastAPI 엔드포인트를 만들어줘.
- 사용자 인증 필요
- Pydantic 스키마 포함
- 에러 핸들링 추가"
```

---

### Phase 3: 문서화 (Notion MCP)

#### 3.1 컴포넌트 문서화

**Cursor 채팅 예시:**

```
"이 React 컴포넌트를 Notion에 문서화해줘.
- 컴포넌트 설명
- Props 설명
- 사용 예시
- 스타일 가이드"
```

**Notion에 생성되는 문서 구조:**

```markdown
# Button 컴포넌트

## 개요
Figma 디자인을 기반으로 제작된 버튼 컴포넌트입니다.

## Props
- variant: 'primary' | 'secondary'
- size: 'sm' | 'md' | 'lg'
- ...

## 사용 예시
\`\`\`tsx
<Button variant="primary">클릭</Button>
\`\`\`
```

#### 3.2 API 문서화

**Cursor 채팅 예시:**

```
"이 FastAPI 엔드포인트를 Notion에 API 문서로 작성해줘.
- 엔드포인트 설명
- 요청/응답 스키마
- 예시 코드
- 에러 코드"
```

#### 3.3 개발 가이드 작성

**Cursor 채팅 예시:**

```
"프로젝트 개발 가이드를 Notion에 작성해줘.
- 프로젝트 구조
- 개발 환경 설정
- 코딩 컨벤션
- 배포 가이드"
```

---

## 🎯 실제 사용 시나리오

### 시나리오 1: 로그인 페이지 개발

#### Step 1: Figma 디자인 확인

```
Cursor: "Figma에서 로그인 페이지 디자인 가져와줘"
```

#### Step 2: 컴포넌트 개발

```
Cursor: "Figma 디자인을 기반으로 로그인 페이지 컴포넌트 만들어줘.
- 이메일/비밀번호 입력 필드
- 로그인 버튼
- 에러 메시지 표시
- TailwindCSS 스타일링"
```

#### Step 3: API 연동

```
Cursor: "로그인 API 엔드포인트 만들어줘.
- FastAPI
- JWT 토큰 발급
- 비밀번호 검증"
```

#### Step 4: 문서화

```
Cursor: "로그인 기능을 Notion에 문서화해줘.
- 사용자 플로우
- API 명세
- 컴포넌트 설명
- 테스트 방법"
```

---

### 시나리오 2: Virtual Try-On 메인 페이지

#### Step 1: Figma 디자인 분석

```
Cursor: "Figma에서 Virtual Try-On 메인 페이지 디자인 정보 가져와줘"
```

#### Step 2: 레이아웃 구성

```
Cursor: "Figma 디자인을 기반으로 메인 페이지 레이아웃 만들어줘.
- 헤더 컴포넌트
- 이미지 업로드 섹션
- 결과 표시 영역
- Three.js 3D 뷰어 통합"
```

#### Step 3: 기능 구현

```
Cursor: "이미지 업로드 기능 구현해줘.
- 파일 선택
- 미리보기
- FastAPI로 업로드
- 진행 상태 표시"
```

#### Step 4: 통합 문서화

```
Cursor: "Virtual Try-On 메인 페이지를 Notion에 문서화해줘.
- 전체 플로우 설명
- 컴포넌트 구조
- API 연동 방법
- 사용자 가이드"
```

---

## 🔧 MCP 활용 팁

### Figma MCP 활용

1. **디자인 정보 가져오기**
   ```
   "Figma에서 [컴포넌트명] 디자인 정보 가져와줘"
   ```

2. **스타일 추출**
   ```
   "Figma 디자인에서 색상, 폰트, 간격 정보 추출해줘"
   ```

3. **디자인 시스템 확인**
   ```
   "Figma에서 디자인 시스템 컴포넌트 목록 보여줘"
   ```

### Notion MCP 활용

1. **문서 자동 생성**
   ```
   "이 코드를 Notion 페이지로 문서화해줘"
   ```

2. **기존 문서 업데이트**
   ```
   "Notion의 [페이지명] 문서에 이 내용 추가해줘"
   ```

3. **문서 구조 생성**
   ```
   "프로젝트 문서 구조를 Notion에 만들어줘"
   ```

---

## 📊 워크플로우 체크리스트

### 디자인 단계

- [ ] Figma에서 UI 디자인 완료
- [ ] 컴포넌트 구조 정의
- [ ] 디자인 시스템 정리
- [ ] Figma MCP 연결 확인
- [ ] Cursor에서 디자인 정보 가져오기 테스트

### 개발 단계

- [ ] Figma 디자인 기반 컴포넌트 생성
- [ ] 스타일링 적용 (TailwindCSS)
- [ ] 페이지 구성
- [ ] API 개발
- [ ] 테스트 작성

### 문서화 단계

- [ ] Notion MCP 연결 확인
- [ ] 컴포넌트 문서 작성
- [ ] API 문서 작성
- [ ] 개발 가이드 작성
- [ ] 사용자 가이드 작성

---

## 🚀 고급 활용

### 1. 디자인 변경 시 자동 업데이트

```
1. Figma에서 디자인 수정
2. Cursor: "Figma 디자인 변경사항 확인해줘"
3. Cursor: "변경된 디자인에 맞게 컴포넌트 업데이트해줘"
4. Cursor: "Notion 문서도 업데이트해줘"
```

### 2. 일괄 문서화

```
Cursor: "프로젝트의 모든 컴포넌트를 Notion에 문서화해줘.
- components/ui 폴더의 모든 컴포넌트
- 각 컴포넌트별 사용법과 예시 포함"
```

### 3. 디자인-코드 일관성 검증

```
Cursor: "Figma 디자인과 현재 코드를 비교해서 차이점 알려줘"
```

---

## 📝 템플릿

### 컴포넌트 개발 템플릿

```
"Figma에서 [컴포넌트명] 디자인 가져와서
Next.js + TypeScript + TailwindCSS로 만들어줘.
- Props 타입 정의
- 반응형 디자인
- 접근성 고려"
```

### API 개발 템플릿

```
"[기능명] API 엔드포인트 만들어줘.
- FastAPI
- 인증 필요
- Pydantic 스키마
- 에러 핸들링
- Swagger 문서 포함"
```

### 문서화 템플릿

```
"이 [컴포넌트/API]를 Notion에 문서화해줘.
- 개요
- 사용 방법
- 예시 코드
- 주의사항"
```

---

## ⚠️ 주의사항

1. **Figma MCP**
   - 데스크톱 앱이 실행 중이어야 함
   - Access Token이 유효해야 함
   - 디자인 파일이 공유되어 있어야 함

2. **Notion MCP**
   - Integration이 페이지에 연결되어 있어야 함
   - 적절한 권한이 설정되어 있어야 함
   - 페이지 ID를 정확히 지정해야 함

3. **보안**
   - Access Token은 `.env`에 저장
   - Git에 커밋하지 않기
   - `.gitignore` 확인

---

## 📚 관련 문서

- [MCP_SETUP.md](./MCP_SETUP.md) - MCP 설정 가이드
- [CURSOR_TOOLS_INTEGRATION.md](./CURSOR_TOOLS_INTEGRATION.md) - 도구 통합 가이드
- [ARCHITECTURE.md](./ARCHITECTURE.md) - 시스템 아키텍처

---

*이 문서는 워크플로우 개선 시 업데이트됩니다.*



