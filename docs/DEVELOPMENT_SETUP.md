# 개발 환경 설정 가이드

Windows와 macOS에서 공용으로 개발할 수 있도록 환경을 설정합니다.

> **참고**: 이 문서는 `SETUP_GUIDE.md`, `SETUP_CHECKLIST.md`, `SETUP_STATUS.md`, `PYTHON_DOWNGRADE_GUIDE.md`를 통합한 문서입니다.

## 필수 요구사항

### 공통 필수 설치 항목
- **Node.js**: v22.x 이상
- **npm**: v10.x 이상
- **Java**: 17 이상
- **Python**: 3.12
- **Docker Desktop**: 최신 버전
- **Git**: 최신 버전

---

## Windows 환경 설정

### 1. Node.js 설치

**다운로드:**
- https://nodejs.org/
- LTS 버전 다운로드 및 설치

**설치 확인:**
```powershell
node --version
npm --version
```

### 2. Java 설치

**다운로드:**
- https://adoptium.net/
- Java 17 LTS 다운로드 및 설치

**설치 확인:**
```powershell
java -version
```

### 3. Python 3.12 설치

**다운로드:**
- https://www.python.org/downloads/release/python-31211/
- "Windows installer (64-bit)" 다운로드

**설치 시 주의:**
- ✅ "Add Python to PATH" 체크 필수

**설치 확인:**
```powershell
py -3.12 --version
```

**가상환경 생성:**
```powershell
py -3.12 -m venv venv
.\venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
python -m pip install -r requirements.txt
```

### 4. Maven 설치

**다운로드:**
- https://maven.apache.org/download.cgi
- Binary zip archive 다운로드

**환경 변수 설정:**
1. Windows 검색에서 "환경 변수" 검색
2. "시스템 환경 변수 편집" → "환경 변수"
3. "시스템 변수"에서 "새로 만들기":
   - 변수 이름: `MAVEN_HOME`
   - 변수 값: `C:\Program Files\Apache\maven` (실제 경로)
4. "Path" 변수 편집 → `%MAVEN_HOME%\bin` 추가

**설치 확인:**
```powershell
mvn --version
```

### 5. Docker Desktop 설치

**다운로드:**
- https://www.docker.com/products/docker-desktop/
- Docker Desktop for Windows 다운로드 및 설치

**설치 후:**
- Docker Desktop 실행
- WSL 2 백엔드 사용 (권장)

**설치 확인:**
```powershell
docker --version
docker-compose --version
```

---

## macOS 환경 설정

### 1. Node.js 설치

**Homebrew 사용:**
```bash
brew install node
```

**또는 공식 사이트:**
- https://nodejs.org/
- LTS 버전 다운로드 및 설치

**설치 확인:**
```bash
node --version
npm --version
```

### 2. Java 설치

**Homebrew 사용:**
```bash
brew install openjdk@17
```

**환경 변수 설정 (.zshrc 또는 .bash_profile):**
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH
```

**설치 확인:**
```bash
java -version
```

### 3. Python 3.12 설치

**Homebrew 사용:**
```bash
brew install python@3.12
```

**또는 공식 사이트:**
- https://www.python.org/downloads/release/python-31211/
- macOS installer 다운로드 및 설치

**설치 확인:**
```bash
python3.12 --version
```

**가상환경 생성:**
```bash
python3.12 -m venv venv
source venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt
```

### 4. Maven 설치

**Homebrew 사용:**
```bash
brew install maven
```

**설치 확인:**
```bash
mvn --version
```

### 5. Docker Desktop 설치

**Homebrew 사용:**
```bash
brew install --cask docker
```

**또는 공식 사이트:**
- https://www.docker.com/products/docker-desktop/
- Docker Desktop for Mac 다운로드 및 설치

**설치 후:**
- Docker Desktop 실행

**설치 확인:**
```bash
docker --version
docker-compose --version
```

---

## 프로젝트 초기 설정

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

---

## 개발 서버 실행

### Frontend (Next.js)
```bash
npm run dev
```
- http://localhost:3000

### Backend Python (FastAPI)
```bash
# 가상환경 활성화 후
uvicorn backend-python.main:app --reload
```
- http://localhost:8000

### Backend Java (Spring Boot)
```bash
cd backend-java
mvn spring-boot:run
```
- http://localhost:8080

---

## 오류 발생 시 대응 방법

### AI 활용 (Cursor 표준)

**Cursor AI 사용:**
1. 오류 메시지를 Cursor 채팅에 복사
2. "이 오류를 해결하는 방법 알려줘" 요청
3. 제안된 해결 방법 적용

**예시:**
```
오류: ModuleNotFoundError: No module named 'psycopg'
→ Cursor: "psycopg 설치 오류 해결 방법"
```

### 일반적인 오류 해결

#### Python 가상환경 활성화 오류 (Windows)
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

#### Maven 인식 안 됨
- 환경 변수 `MAVEN_HOME` 확인
- PATH에 `%MAVEN_HOME%\bin` 추가 확인
- 터미널 재시작

#### Docker 실행 오류
- Docker Desktop이 실행 중인지 확인
- WSL 2 설치 확인 (Windows)
- Docker Desktop 재시작

#### 포트 충돌
```bash
# 포트 사용 중인 프로세스 확인
# Windows
netstat -ano | findstr :3000

# macOS
lsof -i :3000
```

---

## 설치 확인 스크립트

### Windows (PowerShell)
```powershell
Write-Host "=== 개발 환경 확인 ===" -ForegroundColor Cyan
node --version
npm --version
java -version
py -3.12 --version
mvn --version
docker --version
docker-compose --version
```

### macOS (Bash/Zsh)
```bash
echo "=== 개발 환경 확인 ==="
node --version
npm --version
java -version
python3.12 --version
mvn --version
docker --version
docker-compose --version
```

---

## 환경 점검 체크리스트

### 설치 확인 명령어

**Windows:**
```powershell
node --version      # v22.x 이상
npm --version      # v10.x 이상
java -version       # 17 이상
py -3.12 --version  # 3.12.x
mvn --version       # 3.9.x 이상
docker --version    # 최신 버전
```

**macOS:**
```bash
node --version      # v22.x 이상
npm --version       # v10.x 이상
java -version       # 17 이상
python3.12 --version # 3.12.x
mvn --version       # 3.9.x 이상
docker --version    # 최신 버전
```

### Python 가상환경 확인

**Windows:**
```powershell
.\venv\Scripts\Activate.ps1
python -m pip list
```

**macOS:**
```bash
source venv/bin/activate
pip list
```

### Docker 컨테이너 확인

```bash
docker ps
docker-compose ps
```

---

## 참고사항

- 모든 설치 항목은 최신 LTS 버전 사용 권장
- Python은 3.12 버전 사용 (3.14는 일부 패키지 호환성 문제)
- Docker Desktop은 WSL 2 백엔드 사용 권장 (Windows)
- 환경 변수 설정 후 터미널 재시작 필요
- Python 가상환경은 프로젝트별로 독립적으로 관리

