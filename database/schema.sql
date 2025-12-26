-- ============================================
-- Virtual Try-On 데이터베이스 스키마
-- ============================================
-- 이 파일은 데이터베이스 스키마를 정의합니다.
-- Cursor의 Database Client 2에서 실행하거나 psql로 실행할 수 있습니다.
--
-- 사용 방법:
-- 1. Cursor Database Client 2 (권장):
--    - database/schema.sql 파일 열기
--    - 실행할 SQL 선택 (또는 전체 선택)
--    - Cmd+E (macOS) 또는 Ctrl+E (Windows) 실행
--
-- 2. psql 터미널:
--    psql -U postgres -d virtual_tryon -f database/schema.sql
--
-- 3. Database Client 2 쿼리 창:
--    - New Query 클릭
--    - SQL 입력 후 실행
-- ============================================

-- UUID 확장 활성화 (UUID 생성 함수 사용)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 1. users 테이블 (사용자)
-- ============================================
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

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_subscription ON users(subscription);

-- ============================================
-- 2. subscriptions 테이블 (구독)
-- ============================================
CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_type VARCHAR(20) NOT NULL,  -- free, basic, pro, unlimited
    status VARCHAR(20) DEFAULT 'active',  -- active, expired, cancelled
    tryon_limit INT,  -- 월 Try-On 제한
    tryon_used INT DEFAULT 0,  -- 사용량
    started_at TIMESTAMP,
    expires_at TIMESTAMP,
    payment_id UUID,  -- 결제 ID (FK)
    auto_renew BOOLEAN DEFAULT false,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON subscriptions(status);

-- ============================================
-- 3. payments 테이블 (결제) ⭐ 신규
-- ============================================
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subscription_id UUID REFERENCES subscriptions(id),
    payment_method VARCHAR(50) NOT NULL,  -- card, bank_transfer
    amount BIGINT NOT NULL,  -- 결제 금액 (원)
    status VARCHAR(20) DEFAULT 'pending',  -- pending, completed, failed, cancelled
    pg_provider VARCHAR(20) NOT NULL,  -- toss, iamport
    pg_transaction_id VARCHAR(100),  -- PG사 거래 ID
    pg_response JSONB,  -- PG사 응답 데이터
    created_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_pg_transaction_id ON payments(pg_transaction_id);

-- ============================================
-- 4. avatars 테이블 (아바타)
-- ============================================
CREATE TABLE IF NOT EXISTS avatars (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) DEFAULT 'My Avatar',
    face_image_url VARCHAR(500) NOT NULL,
    mesh_data_url VARCHAR(500),
    body_height INT,
    body_weight INT,
    body_type VARCHAR(20),  -- slim, regular, athletic
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_avatars_user_id ON avatars(user_id);
CREATE INDEX IF NOT EXISTS idx_avatars_is_default ON avatars(user_id, is_default);

-- ============================================
-- 5. garments 테이블 (의상)
-- ============================================
CREATE TABLE IF NOT EXISTS garments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(200),
    original_url VARCHAR(500) NOT NULL,
    segmented_url VARCHAR(500),
    category VARCHAR(50),  -- top, bottom, dress
    color VARCHAR(50),
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_garments_user_id ON garments(user_id);
CREATE INDEX IF NOT EXISTS idx_garments_category ON garments(category);
CREATE INDEX IF NOT EXISTS idx_garments_status ON garments(status);

-- ============================================
-- 6. tryon_results 테이블 (Try-On 결과)
-- ============================================
CREATE TABLE IF NOT EXISTS tryon_results (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    avatar_id UUID REFERENCES avatars(id) ON DELETE SET NULL,
    garment_id UUID REFERENCES garments(id) ON DELETE SET NULL,
    result_image_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    processing_time INT,  -- 처리 시간 (ms)
    status VARCHAR(20) DEFAULT 'pending',
    is_favorite BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_results_user_id ON tryon_results(user_id);
CREATE INDEX IF NOT EXISTS idx_results_status ON tryon_results(status);
CREATE INDEX IF NOT EXISTS idx_results_favorite ON tryon_results(user_id, is_favorite);

-- ============================================
-- 7. job_queue 테이블 (작업 큐)
-- ============================================
CREATE TABLE IF NOT EXISTS job_queue (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',  -- pending, processing, completed, failed, cancelled
    input_data JSONB,
    output_data JSONB,
    error_message TEXT,
    priority INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_jobs_user_id ON job_queue(user_id);
CREATE INDEX IF NOT EXISTS idx_jobs_status ON job_queue(status);

-- ============================================
-- 완료 메시지
-- ============================================
DO $$
BEGIN
    RAISE NOTICE '✅ 데이터베이스 스키마 생성 완료';
END $$;

