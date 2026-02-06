# 빠른 시작 가이드 (Quick Start)

프로젝트 개발을 시작하기 위한 핵심 정보를 정리한 문서입니다. 본 가이드는 **Java 21**, **Python 3.12**, **Next.js** 기반의 멀티 백엔드 환경에 최적화되어 있습니다.

## 필수 설치 항목

### 공통 필수
- **Node.js**: v22.x 이상 (LTS 권장)
- **npm**: v10.x 이상
- **Java**: **21** (안정화된 최신 표준)
- **Python**: **3.12** (3.13은 일부 라이브러리 미지원으로 3.12 권장)
- **Docker Desktop**: 최신 버전 (PostgreSQL, Redis 실행용)
- **Git**: 최신 버전

### 설치 확인
```bash
# Windows
node --version && npm --version && java -version && py -3.12 --version && mvn --version && docker --version

# macOS
node --version && npm --version && java -version && python3.12 --version && mvn --version && docker --version
```

---

## 초기 설정 (5분)

### 1. 저장소 클론
```bash
git clone <repository-url>
cd 3D_Model
```

### 2. 환경 변수 설정
루트 폴더에 `.env` 파일을 생성하고 다음 내용을 입력합니다. (Git에 노출되지 않도록 주의)
```env
# Database
DATABASE_URL=postgresql://model_dev:dev1234@localhost:5432/postgres
DB_USER=model_dev
DB_PASSWORD=dev1234

# API Keys
GEMINI_API_KEY=your_gemini_api_key

# Social Auth (Naver)
NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret
NAVER_REDIRECT_URI=http://localhost:3000/api/auth/callback/naver
```

### 3. Frontend 의존성 설치
```bash
npm install
```

### 4. Python 가상환경 및 DB 마이그레이션
```bash
# 가상환경 생성 및 활성화 (macOS 기준)
python3.12 -m venv venv
source venv/bin/activate

# 의존성 설치
pip install -r requirements.txt

# DB 마이그레이션 (Alembic)
cd backend-python
alembic upgrade head
```

### 5. Java 백엔드 설정 (Lombok 미사용)
본 프로젝트는 공유 개발 편의를 위해 **Lombok을 사용하지 않습니다.**
- IDE(Cursor/VSCode)에서 `Java Extension Pack` 설치 권장
- `Command + Shift + P` -> `Java: Clean Java Language Server Workspace` 실행하여 Classpath 동기화

---

## 개발 서버 실행

### 1. Docker 컨테이너 (DB/Redis)
```bash
docker-compose up -d postgres redis
```

### 2. Frontend (Next.js)
```bash
npm run dev
# 접속 주소: http://localhost:3000
```

### 3. Backend Python (FastAPI - AI/Mapping)
```bash
# 가상환경 활성화 상태에서 루트 폴더 기준
uvicorn backend-python.app.main:app --host 0.0.0.0 --port 8000 --reload
```

### 4. Backend Java (Spring Boot - Auth/Payment)
```bash
cd backend-java
./mvnw spring-boot:run
# 접속 주소: http://localhost:8080
```

---

## IDE 최적화 설정 (Cursor/VSCode)

프로젝트 루트의 `.vscode` 폴더에 공유 설정이 포함되어 있습니다.
- **Run and Debug**: `F5` 키를 눌러 `VirtualTryOnApplication (Java Backend)`를 즉시 실행 가능합니다.
- **자동 빌드**: 파일 저장 시 자바 프로젝트가 자동으로 빌드됩니다.

---

## 주요 문제 해결 (Troubleshooting)

- **Java Classpath 에러**: `Clean Java Language Server Workspace` 명령을 실행하세요.
- **Python Tokenizers 빌드 에러**: Python 3.13 대신 **3.12**를 사용 중인지 확인하세요.
- **DB 연결 에러**: `.env` 파일의 `DB_HOST`가 `localhost`로 설정되어 있는지 확인하세요.

---

## 상세 문서 링크
- [프로젝트 개요](./docs/PROJECT_OVERVIEW.md)
- [디자인 시스템](./docs/FRONTEND_DESIGN_SYSTEM.md)
- [소셜 로그인 설정](./docs/SOCIAL_AUTH_SETUP.md)
