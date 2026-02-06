# 🛠️ 개발 환경 가이드

## 📋 Cursor에서 데이터베이스 시각화 (DBeaver처럼)

### ⭐ 추천: Database Client 2 확장 프로그램

**설치 방법:**
1. Cursor에서 `Cmd+Shift+X` (확장 프로그램 열기)
2. "Database Client 2" 검색
3. `cweijan.vscode-database-client2` 설치

**사용 방법:**
1. 왼쪽 사이드바에서 "Database" 아이콘 클릭
2. "+" 버튼으로 연결 추가 (자동 설정되어 있음)
3. 테이블 트리에서 데이터 확인
4. SQL 파일에서 쿼리 실행 (`Cmd+E`)

**자세한 설정 방법:** [Cursor DB 설정 가이드](./docs/CURSOR_DB_SETUP.md)

---

## 📋 백엔드에서 데이터베이스 접근

### Python Backend (SQLAlchemy ORM)
- `backend-python/app/models/` - SQLAlchemy 모델 정의
- `backend-python/app/core/database.py` - DB 연결 및 세션 관리
- ORM을 통해 쿼리 실행 (직접 SQL 작성 불필요)

### Java Backend (JPA/Hibernate)
- `backend-java/src/main/java/com/virtualtryon/entity/` - JPA 엔티티 정의
- `backend-java/src/main/java/com/virtualtryon/repository/` - Repository 인터페이스
- JPA를 통해 쿼리 실행 (직접 SQL 작성 불필요)

### 데이터베이스 마이그레이션
- **Java**: Flyway (`src/main/resources/db/migration/`)
- **Python**: Alembic (`alembic/versions/`)

---

## 📋 SQL 스키마 파일 (초기 설정용)

```bash
# psql로 실행
psql -d virtual_tryon -f scripts/db_schema.sql

# 또는 Cursor 터미널에서
psql -U postgres -d virtual_tryon < scripts/db_schema.sql
```

---

## 🚀 서버 실행

### Python Backend
```bash
# 가상환경 활성화
source venv/bin/activate  # macOS/Linux
# 또는
.\venv\Scripts\Activate.ps1  # Windows

# 서버 실행
cd backend-python
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### Java Backend
```bash
cd backend-java
mvn spring-boot:run
```

### Frontend
```bash
npm run dev
```

---

## 📝 코드 주석 규칙

모든 코드에는 다음 규칙으로 주석을 작성합니다:

### Python
- 모듈/클래스: docstring ("""...""")
- 함수: docstring + 주요 로직 인라인 주석
- 복잡한 로직: 인라인 주석으로 설명

### Java
- 클래스: JavaDoc (/** ... */)
- 메서드: JavaDoc + 주요 로직 인라인 주석
- 복잡한 로직: 인라인 주석으로 설명

### 설정 파일
- 각 설정 항목에 주석으로 용도 설명

---

## 🔍 주요 파일 위치

- **Python Backend**: `backend-python/app/`
  - `models/` - SQLAlchemy 모델
  - `core/database.py` - DB 연결
- **Java Backend**: `backend-java/src/main/java/com/virtualtryon/`
  - `entity/` - JPA 엔티티
  - `repository/` - Repository 인터페이스
- **Frontend**: `app/`
- **DB 스키마 (참고용)**: `database/schema.sql`
- **DB 마이그레이션**:
  - Java: `backend-java/src/main/resources/db/migration/`
  - Python: `backend-python/alembic/versions/`

