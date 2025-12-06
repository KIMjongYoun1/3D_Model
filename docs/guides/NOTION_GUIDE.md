# Notion 가이드

Notion을 활용한 프로젝트 문서 관리 가이드입니다.

## 목차
1. [문서 업로드](#문서-업로드)
2. [페이지 공유](#페이지-공유)
3. [DEV 스타일 설정](#dev-스타일-설정)
4. [Notion AI 활용](#notion-ai-활용)
5. [MCP 연동](#mcp-연동)

---

## 문서 업로드

### 자동 업로드 (스크립트)
```bash
python upload_to_notion.py
```

**필수 설정:**
- `.env` 파일에 `NOTION_API_KEY` 설정
- `pip install notion-client`

### 수동 업로드

**업로드할 문서 목록:**
- 기획 문서: `README.md`, `ROADMAP.md`
- 기술 문서: `ARCHITECTURE.md`, `AI_MODELS.md`, `SECURITY.md`
- 개발 가이드: `MCP_SETUP.md`, `WORKFLOW_DESIGN_TO_DOCS.md`, `CURSOR_GUIDE.md` 등
- 설계 문서: `ERD.md`

**제외할 문서:**
- `PROJECT_REALITY_CHECK.md`
- `PROJECT_ASSESSMENT.md`
- `COST.md`
- `UserThink.md`

### Notion 페이지 구조
```
3D_Model (메인 페이지)
├── 📋 기획 문서
├── 🏗️ 기술 문서
├── 🛠️ 개발 가이드
├── 🔧 도구
└── 📊 설계 문서
```

---

## 페이지 공유

### 공개 링크 생성
1. Notion에서 "3D_Model" 페이지 열기
2. 우측 상단 **"Share"** 버튼 클릭
3. **"Share to web"** 토글 활성화
4. 링크 복사 및 공유

**권한 설정:**
- **Allow comments**: 댓글 허용 여부
- **Allow duplicate as template**: 템플릿으로 복사 허용
- **Search engine indexing**: 검색 엔진 노출 (일반적으로 비활성화)

### 특정 사용자 초대
1. Share 메뉴에서 **"Add people"** 클릭
2. 이메일 주소 입력
3. 권한 선택: Can view / Can edit / Full access

---

## DEV 스타일 설정

### 와이드 레이아웃 설정
1. Notion에서 "3D_Model" 메인 페이지 열기
2. 우측 상단 "..." 메뉴 클릭
3. **"Full width"** 또는 **"Wide"** 선택

### Quick Navigation 구조
```markdown
# 🚀 Virtual Try-On Service

## 📋 Quick Navigation

### 📖 Planning & Overview
- 프로젝트 개요
- 개발 로드맵
- 서비스 플로우

### 🏗️ Architecture & Design
- 시스템 아키텍처
- AI 모델 선정
- 데이터베이스 설계

### 🛠️ Development
- 개발 환경 설정
- MCP 설정
- Cursor 가이드
```

---

## Notion AI 활용

### 기본 사용법
- **단축키**: `Space` 키 또는 `/ai` 입력
- **또는**: 페이지 상단 우측 "Ask AI" 버튼 클릭

### 문서 정리 요청 예시
```
이 프로젝트 문서들을 카테고리별로 잘 정리해줘.
- 기획 문서, 기술 문서, 개발 가이드, 도구, 설계 문서로 구분
- 각 문서의 핵심 내용을 요약해서 정리
- 중복 내용이 있으면 통합해줘
```

### 활용 팁
- **문서 요약**: 긴 문서를 간단히 요약
- **구조 정리**: 문서를 카테고리별로 자동 정리
- **내용 개선**: 문서의 가독성과 구조 개선 제안
- **체크리스트 생성**: 작업 항목을 체크리스트로 변환

**주의사항:**
- Notion AI는 유료 플랜에서만 사용 가능 (Plus 이상)
- 무료 플랜에서는 제한적으로 사용 가능
- 대용량 문서는 여러 번에 나눠서 요청

---

## MCP 연동

### Cursor에서 Notion MCP 사용
**기본 사용법:**
```
Notion의 "3D_Model" 페이지에 있는 문서들을 정리해줘:
1. 각 카테고리별 문서 목록 확인
2. 문서 간 중복 내용 찾기
3. 개선 제안사항 작성
4. 프로젝트 구조 개요 페이지 생성
```

**MCP 설정 방법:**
- [MCP_GUIDE.md](./MCP_GUIDE.md) 참고

---

## 참고 문서
- [MCP_GUIDE.md](./MCP_GUIDE.md): MCP 설정 및 사용법
- [WORKFLOW_DESIGN_TO_DOCS.md](./WORKFLOW_DESIGN_TO_DOCS.md): 전체 워크플로우

