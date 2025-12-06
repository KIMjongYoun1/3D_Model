# 빠른 시작 가이드

프로젝트 개발을 시작하기 위한 핵심 정보를 정리한 문서입니다.

## 필수 설치 항목

### 공통 필수
- **Node.js**: v22.x 이상
- **npm**: v10.x 이상  
- **Java**: 17 이상
- **Python**: 3.12
- **Docker Desktop**: 최신 버전
- **Git**: 최신 버전

### 설치 확인
```bash
# Windows
node --version && npm --version && java -version && py -3.12 --version && mvn --version && docker --version

# macOS
node --version && npm --version && java -version && python3.12 --version && mvn --version && docker --version
```

## 초기 설정 (5분)

### 1. 저장소 클론
```bash
git clone <repository-url>
cd 3D_Model
```

### 2. Frontend 의존성 설치
```bash
npm install
```

### 3. Python 가상환경 설정

**Windows:**
```powershell
py -3.12 -m venv venv
.\venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
```

**macOS:**
```bash
python3.12 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

### 4. Docker 컨테이너 실행
```bash
docker-compose up -d postgres redis
```

### 5. 환경 변수 설정
`.env.local` 파일 생성:
```env
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/virtual_tryon
REDIS_URL=redis://localhost:6379/0
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## 개발 서버 실행

### Frontend
```bash
npm run dev
# http://localhost:3000
```

### Backend Python
```bash
# 가상환경 활성화 후
uvicorn backend-python.main:app --reload
# http://localhost:8000
```

### Backend Java
```bash
cd backend-java
mvn spring-boot:run
# http://localhost:8080
```

## 오류 발생 시

### AI 활용 (Cursor 표준)
1. 오류 메시지를 Cursor 채팅에 복사
2. "이 오류 해결 방법" 요청
3. 제안된 해결 방법 적용

### 일반적인 오류

**Python 가상환경 활성화 오류 (Windows):**
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

**포트 충돌:**
```bash
# Windows
netstat -ano | findstr :3000

# macOS
lsof -i :3000
```

## 상세 가이드

- **환경 설정**: [docs/DEVELOPMENT_SETUP.md](./docs/DEVELOPMENT_SETUP.md)
- **프로젝트 구조**: [STRUCTURE.md](./STRUCTURE.md)
- **개발 로드맵**: [planning/ROADMAP.md](./planning/ROADMAP.md)
- **시스템 아키텍처**: [technical/ARCHITECTURE.md](./technical/ARCHITECTURE.md)

## 다음 단계

1. [개발 로드맵](./planning/ROADMAP.md) 확인
2. [시스템 아키텍처](./technical/ARCHITECTURE.md) 이해
3. Phase 0부터 순차적으로 개발 진행

