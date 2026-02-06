# 빠른 시작 가이드 (Quick Start)

Quantum Studio 프로젝트 개발을 시작하기 위한 핵심 정보를 정리한 문서입니다. 본 가이드는 **Java 21**, **Python 3.12**, **Next.js** 기반의 멀티 백엔드 환경에 최적화되어 있습니다.

---

## 🛠 필수 설치 항목

### 공통 필수
- **Node.js**: v22.x 이상 (LTS 권장)
- **Java**: **21** (안정화된 최신 LTS 표준)
- **Python**: **3.12** (3.13은 일부 라이브러리 미지원으로 3.12 권장)
- **Docker Desktop**: 최신 버전 (PostgreSQL, Redis 실행용)

---

## ⚙️ 초기 설정 (5분)

### 1. 환경 변수 설정
루트 폴더에 `.env` 파일을 생성하고 다음 내용을 입력합니다.
```env
# Database (PostgreSQL)
DATABASE_URL=jdbc:postgresql://localhost:5432/postgres
DB_HOST=localhost
DB_USER=model_dev
DB_PASSWORD=dev1234

# API Keys
GEMINI_API_KEY=your_gemini_api_key

# Social Auth (Naver)
NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret
NAVER_REDIRECT_URI=http://localhost:3000/api/auth/callback/naver
NEXT_PUBLIC_NAVER_CLIENT_ID=your_naver_client_id
```

### 2. 의존성 설치 및 DB 초기화
```bash
# Frontend
npm install

# Docker (DB/Redis)
docker-compose up -d

# Python 가상환경 및 의존성
python3.12 -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt

# DB Migration
cd backend-python && alembic upgrade head
# Java Migration은 실행 시 Flyway가 자동 수행
```

---

## 🚀 개발 서버 실행

### 1. Frontend (Next.js)
```bash
npm run dev  # http://localhost:3000
```

### 2. Backend Python (FastAPI)
```bash
# 가상환경 활성화 상태
uvicorn backend-python.app.main:app --host 0.0.0.0 --port 8000 --reload
```

### 3. Backend Java (Spring Boot)
```bash
cd backend-java
./mvnw spring-boot:run  # http://localhost:8080
```

---

## 🏛 프로젝트 가이드라인

### 1. Java 백엔드 (Lombok 미사용)
공유 개발 편의를 위해 **Lombok을 사용하지 않습니다.** Getter/Setter/Constructor는 IDE 기능을 사용하여 수동으로 생성하세요.

### 2. 프론트엔드 (디자인 시스템)
모든 UI는 `components/ui/`에 정의된 공통 컴포넌트(`Button`, `Card`, `Input`, `Modal`)를 최우선으로 사용합니다.

### 3. React 임포트
린트 에러 방지를 위해 모든 `.tsx` 파일 상단에 `import React from 'react';`를 포함하세요.

---

## 🔗 상세 문서 링크
- [프로젝트 개요](./docs/PROJECT_OVERVIEW.md)
- [디자인 시스템](./docs/FRONTEND_DESIGN_SYSTEM.md)
- [소셜 로그인 설정](./docs/SOCIAL_AUTH_SETUP.md)
- [개발 가이드](./docs/DEVELOPMENT_GUIDE.md)
