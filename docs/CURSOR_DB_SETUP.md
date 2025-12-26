# 🗄️ Cursor에서 데이터베이스 시각화 설정 가이드

Cursor(VS Code 기반)에서 DBeaver처럼 데이터베이스를 시각적으로 관리하는 방법입니다.

> **참고**: 백엔드에서는 ORM을 사용하여 DB 접근
> - **Python**: SQLAlchemy ORM (`app/models/`, `app/core/database.py`)
> - **Java**: JPA/Hibernate (`entity/`, `repository/`)
> - DB 툴은 **시각화 및 관리 목적**으로만 사용

---

## 🎯 추천 확장 프로그램

### 옵션 1: Database Client 2 (가장 추천) ⭐

**특징:**
- 테이블, 데이터, 스키마를 트리 형태로 시각화
- SQL 쿼리 실행 및 결과 표시
- 데이터 편집 가능
- 무료

**설치 방법:**
1. Cursor에서 `Cmd+Shift+X` (macOS) 또는 `Ctrl+Shift+X` (Windows)로 확장 프로그램 열기
2. "Database Client 2" 검색
3. `cweijan.vscode-database-client2` 설치

**사용 방법:**
1. 왼쪽 사이드바에서 "Database" 아이콘 클릭
2. "+" 버튼 클릭하여 연결 추가
3. 다음 정보 입력:
   - **Name**: Virtual Try-On PostgreSQL
   - **Type**: PostgreSQL
   - **Host**: localhost
   - **Port**: 5432
   - **Database**: virtual_tryon
   - **Username**: postgres
   - **Password**: postgres
4. "Test Connection" 클릭하여 연결 확인
5. "Save" 클릭

**기능:**
- 📊 테이블 목록 트리 뷰
- 📝 데이터 조회 및 편집
- 🔍 SQL 쿼리 실행
- 📋 스키마 정보 확인

---

### 옵션 2: SQLTools

**특징:**
- SQL 쿼리 실행 및 결과 표시
- 여러 데이터베이스 지원
- 확장 가능한 구조

**설치 방법:**
1. "SQLTools" 검색하여 설치 (`mtxr.sqltools`)
2. "SQLTools PostgreSQL" 드라이버 설치 (`mtxr.sqltools-driver-pg`)

**사용 방법:**
1. 왼쪽 사이드바에서 "SQLTools" 아이콘 클릭
2. "Add New Connection" 클릭
3. PostgreSQL 선택
4. 연결 정보 입력 후 저장

---

### 옵션 3: PostgreSQL (Chris Kolkman)

**특징:**
- PostgreSQL 전용
- 간단한 쿼리 실행

**설치 방법:**
1. "PostgreSQL" 검색하여 설치 (`ckolkman.vscode-postgres`)

---

## ⚙️ 자동 설정

프로젝트에 `.vscode/settings.json` 파일이 있어서, 확장 프로그램 설치 후 자동으로 연결 설정이 적용됩니다.

**연결 정보:**
- Host: `localhost`
- Port: `5432`
- Database: `virtual_tryon`
- Username: `postgres`
- Password: `postgres`

---

## 🚀 빠른 시작

### 1단계: 확장 프로그램 설치

```bash
# Cursor에서 확장 프로그램 탭 열기 (Cmd+Shift+X)
# "Database Client 2" 검색 후 설치
```

또는 Cursor 명령 팔레트에서:
1. `Cmd+Shift+P` (macOS) 또는 `Ctrl+Shift+P` (Windows)
2. "Extensions: Install Extensions" 입력
3. "Database Client 2" 검색 후 설치

### 2단계: 데이터베이스 연결

1. 왼쪽 사이드바에서 "Database" 아이콘 클릭
2. "+" 버튼 클릭
3. 연결 정보 입력 (자동 설정되어 있음)
4. "Test Connection" → "Save"

### 3단계: 데이터베이스 사용

- **테이블 보기**: 왼쪽 트리에서 테이블 클릭
- **데이터 조회**: 테이블 더블클릭 또는 우클릭 → "Show Table Data"
- **SQL 실행**: 새 SQL 파일 생성 후 쿼리 작성, 실행

---

## 📋 SQL 파일 실행 방법

### 방법 1: Database Client 2 사용
1. `.sql` 파일 열기
2. 쿼리 선택
3. 우클릭 → "Run Selected Query" 또는 `Cmd+E` (macOS) / `Ctrl+E` (Windows)

### 방법 2: SQLTools 사용
1. `.sql` 파일 열기
2. 쿼리 선택
3. 우클릭 → "Run Selected Query" 또는 `Cmd+E` / `Ctrl+E`

---

## 🔧 문제 해결

### 연결 실패 시

1. **PostgreSQL 서비스 확인**
   ```bash
   # macOS
   brew services list | grep postgresql
   
   # 서비스가 실행 중이 아니면
   brew services start postgresql@16
   ```

2. **데이터베이스 존재 확인**
   ```bash
   psql -U postgres -l | grep virtual_tryon
   
   # 없으면 생성
   createdb -U postgres virtual_tryon
   ```

3. **연결 정보 확인**
   - Host: `localhost` (127.0.0.1 아님)
   - Port: `5432`
   - Database: `virtual_tryon`
   - Username: `postgres`
   - Password: `postgres`

---

## 📚 유용한 기능

### Database Client 2
- **테이블 데이터 편집**: 테이블 우클릭 → "Edit Table Data"
- **스키마 내보내기**: 데이터베이스 우클릭 → "Export Schema"
- **데이터 내보내기**: 테이블 우클릭 → "Export Data"

### SQLTools
- **쿼리 히스토리**: 실행한 쿼리 기록
- **결과 비교**: 여러 쿼리 결과 비교
- **북마크**: 자주 사용하는 쿼리 저장

---

## 💡 추천 워크플로우

1. **Database Client 2** 설치 (가장 직관적)
2. 연결 설정 (자동으로 되어 있음)
3. 왼쪽 사이드바에서 데이터베이스 트리 탐색
4. 테이블 클릭하여 데이터 확인
5. SQL 파일에서 쿼리 작성 및 실행

---

*이제 Cursor에서 DBeaver처럼 데이터베이스를 시각적으로 관리할 수 있습니다!*

