# ⚡ 빠른 시작: 테이블 생성하기

## 🎯 목표
`database/schema.sql` 파일을 사용하여 데이터베이스 테이블을 생성합니다.

---

## 📋 방법 1: Cursor Database Client 2에서 실행 (가장 쉬움) ⭐

### 1단계: 확장 프로그램 설치
1. `Cmd+Shift+X` (확장 프로그램 열기)
2. "Database Client 2" 검색
3. 설치

### 2단계: 데이터베이스 연결
1. 왼쪽 사이드바 "Database" 아이콘 클릭
2. "+" 버튼 클릭
3. 연결 정보 입력 (자동 설정됨):
   - Name: Virtual Try-On PostgreSQL
   - Type: PostgreSQL
   - Host: localhost
   - Port: 5432
   - Database: virtual_tryon
   - Username: postgres
   - Password: postgres
4. "Test Connection" → "Save"

### 3단계: SQL 실행
1. `database/schema.sql` 파일 열기
2. **전체 선택** (`Cmd+A` 또는 `Ctrl+A`)
3. **실행** (`Cmd+E` 또는 `Ctrl+E`)
4. 또는 우클릭 → "Run Selected Query"

### 4단계: 확인
- 왼쪽 트리에서 "Tables" 폴더 확장
- 생성된 테이블 목록 확인:
  - ✅ users
  - ✅ subscriptions
  - ✅ payments
  - ✅ avatars
  - ✅ garments
  - ✅ tryon_results
  - ✅ job_queue

---

## 📋 방법 2: 특정 테이블만 생성

### 예시: users 테이블만 생성

1. `database/schema.sql` 파일 열기
2. `CREATE TABLE users (...)` 부분만 선택
3. `Cmd+E` 실행

또는 Database Client 2 쿼리 창에서:
```sql
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    profile_image VARCHAR(500),
    subscription VARCHAR(20) DEFAULT 'free',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
```
→ `Cmd+E` 실행

---

## 📋 방법 3: 터미널에서 실행

```bash
# 전체 스키마 실행
psql -U postgres -d virtual_tryon -f database/schema.sql

# 특정 테이블만 실행
psql -U postgres -d virtual_tryon -c "CREATE TABLE users (...);"
```

---

## ✅ 실행 확인

### Database Client 2에서 확인
1. 왼쪽 트리에서 데이터베이스 확장
2. "Tables" 폴더 클릭
3. 테이블 목록 확인

### SQL로 확인
```sql
-- 모든 테이블 목록
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public'
ORDER BY table_name;
```

---

## ⚠️ 주의사항

1. **이미 테이블이 있는 경우**
   - `CREATE TABLE IF NOT EXISTS` 사용 (에러 방지)
   - 또는 기존 테이블 삭제 후 재생성

2. **백엔드 개발 시**
   - 실제 개발에서는 Flyway/Alembic 마이그레이션 사용
   - 이 파일은 참고 및 수동 생성용

3. **연결 오류 시**
   - PostgreSQL 서비스 실행 확인: `brew services list | grep postgresql`
   - 데이터베이스 존재 확인: `psql -U postgres -l | grep virtual_tryon`

---

## 🎉 완료!

테이블이 생성되었습니다. 이제 Database Client 2에서 테이블을 클릭하여 데이터를 확인할 수 있습니다!

---

*더 자세한 내용은 [README.md](./README.md)를 참고하세요.*





