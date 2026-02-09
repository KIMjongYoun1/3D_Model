-- ============================================
-- Quantum Studio DB 초기화 스크립트
-- ============================================
-- 사용법: DBeaver에서 기존 PostgreSQL 연결(postgres DB)에 접속 후 이 스크립트 실행
--
-- 실행 후 DBeaver에 새 연결 2개를 등록하세요:
--   1. quantum_service (Port 5432, User: model_dev, Password: dev1234)
--   2. quantum_ai      (Port 5432, User: model_dev, Password: dev1234)
-- ============================================

-- 1. quantum_service: Java 전용 (인증, 결제, 프로젝트, 지식 베이스)
--    마이그레이션: Flyway (Java 기동 시 자동 실행)
CREATE DATABASE quantum_service
    OWNER model_dev
    ENCODING 'UTF8'
    LC_COLLATE 'en_US.UTF-8'
    LC_CTYPE 'en_US.UTF-8'
    TEMPLATE template0;

-- 2. quantum_ai: Python 전용 (시각화, 상관관계, 가상 피팅)
--    마이그레이션: Alembic (Python 기동 시 자동 실행)
CREATE DATABASE quantum_ai
    OWNER model_dev
    ENCODING 'UTF8'
    LC_COLLATE 'en_US.UTF-8'
    LC_CTYPE 'en_US.UTF-8'
    TEMPLATE template0;

-- 3. UUID 확장 활성화 (각 DB에서 별도 실행 필요)
-- ※ DBeaver에서 quantum_service DB로 전환 후 실행:
--    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- ※ DBeaver에서 quantum_ai DB로 전환 후 실행:
--    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 확인: 생성된 DB 목록 조회
-- ============================================
SELECT datname FROM pg_database WHERE datname IN ('quantum_service', 'quantum_ai');
