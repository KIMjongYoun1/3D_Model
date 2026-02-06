# 🗄️ 데이터베이스 관리 (Database Management)

Quantum Studio의 데이터베이스 스키마와 관리 방법을 설명합니다.

---

## 📂 마이그레이션 구조
실제 마이그레이션 파일은 각 백엔드 디렉토리에서 관리됩니다.

- **Java (backend-java)**: `src/main/resources/db/migration/` (Flyway)
  - 인증, 결제, 구독 등 핵심 비즈니스 데이터 관리.
- **Python (backend-python)**: `alembic/versions/` (Alembic)
  - 3D 시각화 프로젝트, 노드, AI 분석 로그 데이터 관리.

---

## 🚀 테이블 생성 및 관리

### 1. Java 백엔드 (Flyway)
Spring Boot 애플리케이션이 시작될 때 `V{version}__description.sql` 형식의 파일을 읽어 자동으로 마이그레이션을 수행합니다.

### 2. Python 백엔드 (Alembic)
터미널에서 다음 명령어를 실행하여 수동으로 마이그레이션을 수행합니다.
```bash
cd backend-python
alembic upgrade head
```

---

## 🔍 DB 도구 추천
- **Database Client 2 (VS Code Extension)**: IDE 내에서 직접 DB를 관리할 수 있는 강력한 도구입니다.
- **DBeaver**: 범용 데이터베이스 관리 도구입니다.

---

## ⚠️ 주의사항
- **JDBC URL**: Java와 Python의 DB 연결 URL 형식이 다르므로 `.env` 설정 시 주의가 필요합니다.
- **공통 스키마**: 두 백엔드가 동일한 DB를 공유하므로, 테이블 생성 시 충돌이 발생하지 않도록 마이그레이션 관리 주체를 명확히 해야 합니다.

---

## 📚 관련 문서
- [ERD 설계](./design/ERD.md)
- [빠른 시작 가이드](../QUICK_START.md)
- [개발 가이드](./DEVELOPMENT_GUIDE.md)
