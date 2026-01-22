# 🏗️ 시스템 아키텍처 & 기술 스택

> **버전**: v0.1 (초안)  
> **최종 수정**: 2025.11.30

---

## 📐 전체 아키텍처

> **컨셉**: 실물 기물과 추상 데이터를 아우르는 통합 3D 시각화 엔진

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              클라이언트                                      │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                         Next.js (React)                               │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │  │
│  │  │   Pages     │  │ Components  │  │  Three.js   │  │   Zustand   │  │  │
│  │  │  (라우팅)    │  │   (UI)      │  │(범용3D엔진)  │  │  (상태관리)  │  │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘  │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼ REST API / WebSocket
┌─────────────────────────────────────────────────────────────────────────────┐
│                              백엔드 서버                                     │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                         FastAPI (Python)                              │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │  │
│  │  │   Auth      │  │   Upload    │  │ 3D Generator│  │   Result    │  │  │
│  │  │  (인증)      │  │  (업로드)    │  │(좌표/메시생성)│  │   (결과)    │  │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘  │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                      │                                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                          │
│  │   Celery    │  │    Redis    │  │  PostgreSQL │                          │
│  │ (비동기작업)  │  │  (캐시/큐)   │  │   (DB)      │                          │
│  └─────────────┘  └─────────────┘  └─────────────┘                          │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼ Internal API
┌─────────────────────────────────────────────────────────────────────────────┐
│                           시각화 파이프라인                                 │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                        GPU & Data Processor                          │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │  │
│  │  │  Try-On     │  │  Data Parser│  │  Face Mesh  │  │  Structure  │  │  │
│  │  │ (실물시각화)  │  │ (추상데이터)  │  │ (생체데이터)  │  │ (로직시각화)  │  │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘  │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              스토리지                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │   MinIO / Cloudflare R2 / AWS S3                                    │    │
│  │   - 원본 이미지                                                      │    │
│  │   - 처리된 이미지                                                    │    │
│  │   - 3D 메시 데이터                                                   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ 기술 스택 상세

### Frontend

| 기술 | 버전 | 용도 | 선정 이유 |
|------|------|------|-----------|
| **Next.js** | 14.x | 프레임워크 | SSR/SSG, App Router, 최적화 |
| **TypeScript** | 5.x | 언어 | 타입 안정성, 협업 효율 |
| **TailwindCSS** | 3.x | 스타일링 | 빠른 개발, 일관된 디자인 |
| **Three.js** | 0.160+ | 3D 렌더링 | 웹 3D 표준, 커뮤니티 |
| **React Three Fiber** | 8.x | Three.js 래퍼 | React 친화적 |
| **Zustand** | 4.x | 상태관리 | 경량, 간단한 API |
| **React Query** | 5.x | 서버상태 | 캐싱, 동기화 |

### Backend

| 기술 | 버전 | 용도 | 선정 이유 |
|------|------|------|-----------|
| **FastAPI** | 0.109+ | API 서버 | 비동기, 자동문서화, 빠름 |
| **Python** | 3.11+ | 언어 | AI 라이브러리 호환성 |
| **Celery** | 5.x | 작업 큐 | 비동기 AI 처리 |
| **Redis** | 7.x | 캐시/메시지 | 빠른 세션, 작업 큐 |
| **PostgreSQL** | 16.x | 메인 DB | 안정성, JSON 지원 |
| **SQLAlchemy** | 2.x | ORM | 타입힌트, 비동기 |
| **Pydantic** | 2.x | 데이터 검증 | FastAPI 통합 |

### AI/ML

| 기술 | 용도 | 라이선스 |
|------|------|----------|
| **PyTorch** | 딥러닝 프레임워크 | BSD |
| **HuggingFace Diffusers** | 모델 로딩 | Apache 2.0 |
| **IDM-VTON** | Virtual Try-On | Apache 2.0 |
| **Segment Anything (SAM)** | 의상 세그멘테이션 | Apache 2.0 |
| **MediaPipe** | 얼굴 랜드마크 | Apache 2.0 |
| **ONNX Runtime** | 모델 최적화 | MIT |

### 인프라

| 서비스 | 용도 | 대안 |
|--------|------|------|
| **Vercel** | Frontend 배포 | Netlify, Cloudflare Pages |
| **Railway** | Backend 배포 | Render, Fly.io |
| **RunPod** | GPU 서버 | Lambda Labs, Vast.ai |
| **Cloudflare R2** | 이미지 저장소 | AWS S3, MinIO |
| **Supabase** | DB (대안) | PlanetScale, Neon |

---

## 📂 프로젝트 구조 (Monorepo)

```
virtual-tryon/
│
├── 📂 apps/
│   ├── 📂 web/                      # Next.js Frontend
│   │   ├── 📂 app/                  # App Router
│   │   │   ├── 📂 (auth)/           # 인증 관련 페이지
│   │   │   ├── 📂 (main)/           # 메인 서비스 페이지
│   │   │   ├── 📂 api/              # API Routes
│   │   │   └── 📄 layout.tsx
│   │   ├── 📂 components/           # React 컴포넌트
│   │   │   ├── 📂 ui/               # 기본 UI 컴포넌트
│   │   │   ├── 📂 3d/               # Three.js 컴포넌트
│   │   │   └── 📂 features/         # 기능별 컴포넌트
│   │   ├── 📂 hooks/                # 커스텀 훅
│   │   ├── 📂 lib/                  # 유틸리티
│   │   ├── 📂 stores/               # Zustand 스토어
│   │   └── 📂 styles/               # 전역 스타일
│   │
│   └── 📂 api/                      # FastAPI Backend
│       ├── 📂 app/
│       │   ├── 📂 api/              # API 라우터
│       │   │   ├── 📂 v1/
│       │   │   │   ├── 📄 auth.py
│       │   │   │   ├── 📄 users.py
│       │   │   │   ├── 📄 avatars.py
│       │   │   │   ├── 📄 garments.py
│       │   │   │   └── 📄 tryon.py
│       │   │   └── 📄 deps.py
│       │   ├── 📂 core/             # 핵심 설정
│       │   │   ├── 📄 config.py
│       │   │   ├── 📄 security.py
│       │   │   └── 📄 database.py
│       │   ├── 📂 models/           # SQLAlchemy 모델
│       │   ├── 📂 schemas/          # Pydantic 스키마
│       │   ├── 📂 services/         # 비즈니스 로직
│       │   └── 📂 tasks/            # Celery 태스크
│       ├── 📂 tests/
│       ├── 📄 main.py
│       └── 📄 requirements.txt
│
├── 📂 packages/
│   ├── 📂 ai-pipeline/              # AI 모델 래퍼
│   │   ├── 📂 models/
│   │   │   ├── 📄 segmentation.py   # SAM 래퍼
│   │   │   ├── 📄 tryon.py          # IDM-VTON 래퍼
│   │   │   └── 📄 face_mesh.py      # MediaPipe 래퍼
│   │   ├── 📂 utils/
│   │   └── 📄 pipeline.py           # 통합 파이프라인
│   │
│   └── 📂 shared/                   # 공유 코드
│       ├── 📂 types/                # TypeScript 타입
│       └── 📂 constants/            # 공통 상수
│
├── 📂 docs/                         # 문서
│   ├── 📄 API.md
│   ├── 📄 DEPLOYMENT.md
│   └── 📄 CONTRIBUTING.md
│
├── 📂 infra/                        # 인프라 설정
│   ├── 📂 docker/
│   │   ├── 📄 Dockerfile.web
│   │   ├── 📄 Dockerfile.api
│   │   └── 📄 Dockerfile.ai
│   └── 📄 docker-compose.yml
│
├── 📄 .env.example                  # 환경변수 예시
├── 📄 .gitignore
├── 📄 pnpm-workspace.yaml           # pnpm 워크스페이스
├── 📄 turbo.json                    # Turborepo 설정
└── 📄 README.md
```

---

## 🔐 환경 변수 구성

### `.env.example`

```bash
# ===================
# App
# ===================
NODE_ENV=development
APP_NAME=VirtualTryOn
APP_URL=http://localhost:3000

# ===================
# Database
# ===================
DATABASE_URL=postgresql://user:password@localhost:5432/tryon_db

# ===================
# Redis
# ===================
REDIS_URL=redis://localhost:6379

# ===================
# Storage
# ===================
S3_ENDPOINT=https://xxx.r2.cloudflarestorage.com
S3_ACCESS_KEY=your_access_key
S3_SECRET_KEY=your_secret_key
S3_BUCKET=tryon-images

# ===================
# AI Services
# ===================
AI_SERVER_URL=http://gpu-server:8080
HUGGINGFACE_TOKEN=hf_xxx

# ===================
# Auth
# ===================
JWT_SECRET=your-super-secret-key
JWT_ALGORITHM=HS256
JWT_EXPIRE_MINUTES=60

# ===================
# External APIs
# ===================
READY_PLAYER_ME_API_KEY=xxx  # 3D 아바타 (선택사항)
```

---

## 🔄 데이터 플로우

### Try-On 프로세스

```
1. 이미지 업로드
   Client ──▶ API Server ──▶ S3 Storage
                   │
                   ▼
2. 작업 큐 등록
   API Server ──▶ Redis (Celery Queue)
                   │
                   ▼
3. AI 처리
   Celery Worker ──▶ GPU Server
        │
        ├── SAM: 의상 세그멘테이션
        ├── IDM-VTON: 착용 합성
        └── MediaPipe: 얼굴 메시 (아바타용)
                   │
                   ▼
4. 결과 저장
   GPU Server ──▶ S3 Storage
                   │
                   ▼
5. 알림 & 조회
   Redis ──▶ API Server ──▶ Client (WebSocket/Polling)
```

---

## 📊 확장성 고려사항

### 1인 개발 시
- 모노리식 구조 유지
- 단일 GPU 서버
- 동기 처리 가능

### 2~3인 개발 시
- 마이크로서비스 분리 고려
- AI 파이프라인 독립 서버
- Kubernetes 도입 검토

### 스케일업 시
- GPU Auto-scaling (RunPod Serverless)
- CDN 이미지 캐싱
- DB 읽기 복제본

---

*이 문서는 초안이며, 기술 검증 후 업데이트됩니다.*

