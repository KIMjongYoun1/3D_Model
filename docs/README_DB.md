# 🗄️ 데이터베이스 디렉토리

데이터베이스 스키마 참고 자료를 관리하는 디렉토리입니다.

---

## 📂 디렉토리 구조

```
database/
├── schema.sql          # 전체 스키마 정의 (참고용)
└── README.md          # 이 파일
```

> **참고**: 실제 마이그레이션 파일은 각 백엔드 디렉토리에 있습니다:
> - **Java**: `backend-java/src/main/resources/db/migration/` (Flyway)
> - **Python**: `backend-python/alembic/versions/` (Alembic)

---

## 🚀 테이블 생성 방법

### 방법 1: Cursor Database Client 2에서 실행 (권장) ⭐

1. **Database Client 2 확장 프로그램 설치**
   - `Cmd+Shift+X` → "Database Client 2" 검색 → 설치

2. **데이터베이스 연결**
   - 왼쪽 사이드바 "Database" 아이콘 클릭
   - "+" 버튼으로 연결 추가 (자동 설정됨)

3. **SQL 파일 실행**
   - `database/schema.sql` 파일 열기
   - 실행할 SQL 선택 (예: `CREATE TABLE users ...`)
   - `Cmd+E` (macOS) 또는 `Ctrl+E` (Windows) 실행
   - 또는 우클릭 → "Run Selected Query"

4. **전체 스키마 실행**
   - `schema.sql` 파일 전체 선택 (`Cmd+A`)
   - `Cmd+E` 실행
   - 또는 Database Client 2에서 "New Query" → 파일 내용 붙여넣기 → 실행

### 방법 2: 터미널에서 psql 실행

```bash
# 전체 스키마 실행
psql -U postgres -d virtual_tryon -f database/schema.sql

# 특정 테이블만 실행
psql -U postgres -d virtual_tryon -c "CREATE TABLE users (...);"
```

### 방법 3: Database Client 2 쿼리 창에서 실행

1. Database Client 2에서 연결 선택
2. "New Query" 클릭
3. SQL 입력:
   ```sql
   CREATE TABLE users (
       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
       email VARCHAR(255) UNIQUE NOT NULL,
       ...
   );
   ```
4. `Cmd+E` 또는 실행 버튼 클릭

---

## 📋 스키마 파일 설명

### `schema.sql`
- 전체 데이터베이스 스키마 정의
- 모든 테이블, 인덱스, 제약조건 포함
- **참고용**: 실제 개발에서는 Flyway/Alembic 마이그레이션 사용

### 실행 순서
1. UUID 확장 활성화
2. users 테이블 생성
3. subscriptions 테이블 생성
4. payments 테이블 생성
5. avatars 테이블 생성
6. garments 테이블 생성
7. tryon_results 테이블 생성
8. job_queue 테이블 생성

---

## ⚠️ 주의사항

### 백엔드 개발 시
- ❌ 직접 SQL 파일 실행하지 않음
- ✅ **Python**: Alembic 마이그레이션 사용 (`backend-python/alembic/versions/`)
- ✅ **Java**: Flyway 마이그레이션 사용 (`backend-java/src/main/resources/db/migration/`)

### 이 디렉토리의 용도
- ✅ 스키마 참고 및 확인
- ✅ 수동 테이블 생성 (초기 설정 시)
- ✅ 데이터베이스 구조 이해

### 실제 마이그레이션 위치
- **Java Flyway**: `backend-java/src/main/resources/db/migration/`
  - 파일명 형식: `V{version}__{description}.sql` (예: `V1__Create_users_table.sql`)
  - Spring Boot 시작 시 자동 실행
  
- **Python Alembic**: `backend-python/alembic/versions/`
  - 파일명 형식: `{revision}_{description}.py` (예: `a1b2c3d4_create_users_table.py`)
  - 수동 실행: `alembic upgrade head`

---

## 🔍 테이블 확인 방법

### Database Client 2에서
1. 왼쪽 트리에서 데이터베이스 확장
2. "Tables" 폴더 클릭
3. 생성된 테이블 목록 확인
4. 테이블 더블클릭하여 데이터 확인

### SQL로 확인
```sql
-- 모든 테이블 목록
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public'
ORDER BY table_name;

-- 특정 테이블 구조 확인
\d users  -- psql 명령어
```

---

## 📚 관련 문서

- [Cursor DB 설정 가이드](../docs/CURSOR_DB_SETUP.md) - DB 툴 설정 방법
- [백엔드 DB 접근 방법](../docs/BACKEND_DB_ACCESS.md) - ORM 사용 방법
- [빠른 시작 가이드](./QUICK_START.md) - 테이블 생성 빠른 가이드

---

*이 디렉토리는 데이터베이스 스키마를 참고하고 관리하는 용도입니다. 실제 마이그레이션은 각 백엔드 디렉토리에서 관리합니다.*
