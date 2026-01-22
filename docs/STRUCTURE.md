# 📁 프로젝트 디렉토리 구조

> 문서 정리 완료일: 2026.01.22

---

## 📂 디렉토리 구조

```
3D_Model/
│
├── 📁 docs/                  # 모든 문서 (개발 파일과 분리)
│   ├── 📋 planning/          # 기획 문서
│   │   ├── README.md        # 프로젝트 개요
│   │   ├── ROADMAP.md       # 개발 로드맵
│   │   └── VISUALIZATION_EXPANSION.md # 시각화 확장 계획 ⭐
│   │
│   ├── 🏗️ technical/        # 기술 문서
│   │   ├── ARCHITECTURE.md  # 시스템 아키텍처 (통합 3D 엔진)
│   │   ├── AI_MODELS.md     # AI 모델 선정
│   │   ├── AI_INTEGRATION.md # AI 모듈 연동
│   │   ├── SECURITY.md      # 보안 가이드
│   │   └── JWT_CLAIM_VALIDATION_PATTERN.md # 보안 패턴 ⭐
│   │
│   ├── 🛠️ guides/           # 개발 가이드
│   │   ├── MCP_GUIDE.md     # MCP 설정 및 사용법
│   │   ├── NOTION_GUIDE.md  # Notion 가이드
│   │   ├── WORKFLOW_DESIGN_TO_DOCS.md  # 워크플로우
│   │   ├── CURSOR_GUIDE.md  # Cursor 가이드
│   │   └── CURSOR_TOOLS_INTEGRATION.md # 도구 통합
│   │
│   ├── 🔧 tools/            # 도구
│   │   └── TOOLS.md         # 사용 도구
│   │
│   ├── 📊 design/           # 설계 문서
│   │   ├── ERD.md           # 데이터베이스 설계
│   │   ├── FIGMA_GUIDE.md   # Figma 가이드
│   │   └── COMPONENT_SPECS.md # 컴포넌트 스펙
│   │
│   ├── 📄 QUICK_START.md    # 빠른 시작 가이드
│   ├── 📄 DEVELOPMENT_SETUP.md # 개발 환경 설정
│   ├── 📄 STRUCTURE.md      # 프로젝트 구조 (이 문서)
│   ├── 📄 GIT_IGNORE_GUIDE.md # Git ignore 가이드
│   ├── 📄 README.md         # 문서 디렉토리 안내
│   └── 🐍 upload_to_notion.py # Notion 업로드 스크립트
│
├── 🚫 excluded/             # 제외 문서 (포트폴리오용)
│   ├── PROJECT_REALITY_CHECK.md
│   ├── PROJECT_ASSESSMENT.md
│   ├── COST.md
│   └── UserThink.md
│
├── 📁 app/                  # Next.js 앱 디렉토리
├── 📁 components/           # React 컴포넌트
├── 📁 backend-python/       # Python 백엔드
├── 📁 backend-java/         # Java 백엔드
│
├── 📄 README.md             # 프로젝트 메인 README
├── 📄 package.json          # Node.js 의존성
├── 📄 requirements.txt      # Python 의존성
├── 📄 docker-compose.yml    # Docker Compose 설정
└── 📄 .gitignore           # Git ignore 설정
```

---

## 📋 카테고리별 문서 목록

### 📋 기획 문서 (docs/planning/)
- **README.md**: 프로젝트 개요, 서비스 소개, 주요 기능
- **ROADMAP.md**: 개발 로드맵, 마일스톤, 단계별 계획
- **VISUALIZATION_EXPANSION.md**: 실물 및 추상 데이터 시각화 확장 전략 ⭐

### 🏗️ 기술 문서 (docs/technical/)
- **ARCHITECTURE.md**: 시스템 아키텍처, 기술 스택, 데이터 플로우
- **AI_MODELS.md**: AI 모델 선정 및 비교, 파이프라인 구성
- **AI_INTEGRATION.md**: AI 모듈 연동 가이드 (준비 중)
- **SECURITY.md**: 보안 가이드, 인증/인가, API 보안
- **JWT_CLAIM_VALIDATION_PATTERN.md**: JWT 클레임 검증 보안 패턴

### 🛠️ 개발 가이드 (docs/guides/)
- **MCP_GUIDE.md**: Figma & Notion MCP 설정 및 사용법
- **NOTION_GUIDE.md**: Notion 문서 관리 가이드
- **WORKFLOW_DESIGN_TO_DOCS.md**: 디자인→개발→문서화 워크플로우
- **CURSOR_GUIDE.md**: Cursor IDE 요금제 & 활용 가이드
- **CURSOR_TOOLS_INTEGRATION.md**: Cursor 도구 연동 & 협업 가이드

### 🔧 도구 (docs/tools/)
- **TOOLS.md**: 사용 도구 & 협업 환경

### 📊 설계 문서 (docs/design/)
- **ERD.md**: 데이터베이스 설계, 테이블 구조, 관계 정의
- **FIGMA_GUIDE.md**: Figma 디자인 시스템 및 작업 가이드
- **COMPONENT_SPECS.md**: 컴포넌트 스펙 문서

### 🚫 제외 문서 (excluded/)
포트폴리오용으로 제외된 문서들:
- **PROJECT_REALITY_CHECK.md**: 부정적 평가, 실패 확률 등
- **PROJECT_ASSESSMENT.md**: 개발자 프로필, 개인적 평가
- **COST.md**: 비용 산정
- **UserThink.md**: 개인 기록

---

## 🔗 문서 간 링크

모든 문서의 상대 경로가 업데이트되어 있습니다:
- `planning/README.md`에서 다른 문서로의 링크는 상대 경로로 설정됨
- 루트 `README.md`는 각 카테고리로의 링크 제공

---

## 📤 Notion 업로드

### 자동 업로드 (스크립트)
```bash
cd docs
python upload_to_notion.py
```

### 수동 업로드
[Notion 가이드](./guides/NOTION_GUIDE.md) 참고

---

*이 구조는 프로젝트 진행에 따라 업데이트됩니다.*
