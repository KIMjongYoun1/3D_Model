# 🛠️ 로컬 개발 환경 구축 가이드 (Docker 제외)

> **목적**: Docker 없이 로컬에서 개발 환경 구축  
> **대상**: macOS / Windows  
> **작성일**: 2025.12.06

---

## ✅ 필수 설치 항목 확인

### 1. Node.js & npm
```bash
node --version   # v22.x 이상 권장
npm --version    # v10.x 이상 권장
```

**설치 방법 (macOS)**:
```bash
brew install node
```

**설치 방법 (Windows)**:
- https://nodejs.org/ 에서 LTS 버전 다운로드

### 2. Python 3.12+
```bash
python3 --version  # 3.12 이상 권장
```

**설치 방법 (macOS)**:
```bash
brew install python@3.12
```

**설치 방법 (Windows)**:
- https://www.python.org/downloads/ 에서 Python 3.12 다운로드
- 설치 시 "Add Python to PATH" 체크 필수

### 3. Java 17+
```bash
java -version  # 17 이상 권장
```

**설치 방법 (macOS)**:
```bash
brew install openjdk@17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

**설치 방법 (Windows)**:
- https://adoptium.net/ 에서 Java 17 LTS 다운로드

### 4. Maven
```bash
mvn --version  # 3.9.x 이상 권장
```

**설치 방법 (macOS)**:
```bash
brew install maven
```

**설치 방법 (Windows)**:
- https://maven.apache.org/download.cgi 에서 다운로드
- 환경 변수 `MAVEN_HOME` 설정 필요

### 5. PostgreSQL (로컬 설치)

**macOS**:
```bash
brew install postgresql@16
brew services start postgresql@16

# 데이터베이스 생성
createdb virtual_tryon
psql -d virtual_tryon -c "CREATE USER postgres WITH PASSWORD 'postgres';"
```

**Windows**:
- https://www.postgresql.org/download/windows/ 에서 설치
- 설치 후 pgAdmin 또는 psql로 데이터베이스 생성:
```sql
CREATE DATABASE virtual_tryon;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE virtual_tryon TO postgres;
```

### 6. Redis (로컬 설치)

**macOS**:
```bash
brew install redis
brew services start redis

# 확인
redis-cli ping  # PONG 응답 확인
```

**Windows**:
- https://github.com/microsoftarchive/redis/releases 에서 다운로드
- 또는 WSL2 사용 권장

---

## 🚀 프로젝트 초기 설정

### 1. 저장소 클론 (이미 있는 경우 생략)
```bash
cd /Users/ryankim/3D_Model/3D_Model
```

### 2. Frontend 의존성 설치
```bash
npm install
```

### 3. Python 가상환경 설정

**macOS/Linux**:
```bash
python3 -m venv venv
source venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt
```

**Windows**:
```powershell
py -3.12 -m venv venv
.\venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
python -m pip install -r requirements.txt
```

### 4. Java 프로젝트 빌드
```bash
cd backend-java
mvn clean install
cd ..
```

### 5. 환경 변수 설정

`env.example` 파일을 복사하여 `.env.local` 생성:
```bash
cp env.example .env.local
```

`.env.local` 파일 수정:
```env
# Database
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/virtual_tryon

# Redis
REDIS_URL=redis://localhost:6379/0

# JWT Secret (프로덕션에서는 반드시 변경)
JWT_SECRET=your-super-secret-key-change-this-in-production

# Payment (토스페이먼츠 테스트 키)
TOSS_PAYMENTS_SECRET_KEY=test_sk_xxx
TOSS_PAYMENTS_CLIENT_KEY=test_ck_xxx
```

---

## 🗄️ 데이터베이스 초기화

### PostgreSQL 스키마 생성

`backend-java/src/main/resources/db/migration/` 또는 `backend-python/alembic/` 에서 마이그레이션 실행:

**Java (Flyway - 자동 실행)**:
```bash
cd backend-java
mvn spring-boot:run  # Flyway가 자동으로 마이그레이션 실행
```

**Python (Alembic)**:
```bash
source venv/bin/activate
cd backend-python
alembic upgrade head
```

---

## 🏃 개발 서버 실행

### 1. PostgreSQL & Redis 실행 확인

**macOS**:
```bash
# PostgreSQL
brew services start postgresql@16

# Redis
brew services start redis

# 확인
psql -d virtual_tryon -c "SELECT 1;"  # 연결 확인
redis-cli ping  # PONG 확인
```

### 2. Java Backend 실행
```bash
cd backend-java
mvn spring-boot:run
# http://localhost:8080
```

### 3. Python Backend 실행
```bash
source venv/bin/activate
cd backend-python
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
# http://localhost:8000
```

### 4. Frontend 실행
```bash
npm run dev
# http://localhost:3000
```

---

## 📋 개발 환경 체크리스트

### 설치 확인
- [ ] Node.js v22.x 이상
- [ ] Python 3.12 이상
- [ ] Java 17 이상
- [ ] Maven 3.9.x 이상
- [ ] PostgreSQL 16 설치 및 실행
- [ ] Redis 설치 및 실행

### 프로젝트 설정
- [ ] `npm install` 완료
- [ ] Python 가상환경 생성 및 패키지 설치
- [ ] Java 프로젝트 빌드 성공
- [ ] `.env.local` 파일 생성 및 설정

### 서비스 실행
- [ ] PostgreSQL 연결 확인
- [ ] Redis 연결 확인
- [ ] Java Backend 실행 (포트 8080)
- [ ] Python Backend 실행 (포트 8000)
- [ ] Frontend 실행 (포트 3000)

---

## 🔧 문제 해결

### PostgreSQL 연결 오류
```bash
# PostgreSQL 서비스 확인
brew services list | grep postgresql

# 재시작
brew services restart postgresql@16

# 데이터베이스 확인
psql -l | grep virtual_tryon
```

### Redis 연결 오류
```bash
# Redis 서비스 확인
brew services list | grep redis

# 재시작
brew services restart redis

# 연결 테스트
redis-cli ping
```

### Python 가상환경 활성화 오류 (Windows)
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 포트 충돌
```bash
# 포트 사용 중인 프로세스 확인 (macOS)
lsof -i :3000
lsof -i :8000
lsof -i :8080

# 프로세스 종료
kill -9 <PID>
```

---

## 📚 다음 단계

1. [서비스 아키텍처 문서](./SERVICE_ARCHITECTURE.md) 확인
2. [ERD 문서](./design/ERD.md) 확인
3. [로드맵](./planning/ROADMAP.md) 확인
4. 개발 시작!

---

*이 가이드는 Docker 없이 로컬 개발 환경을 구축하는 방법을 안내합니다.*




