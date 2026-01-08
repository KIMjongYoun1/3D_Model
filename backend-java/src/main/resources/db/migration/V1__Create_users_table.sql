-- ============================================
-- V1: users 테이블 생성
-- ============================================
-- 사용자 정보를 저장하는 테이블
-- 비밀번호는 해시화되어 저장됨 (BCrypt)

-- UUID 확장 활성화 (UUID 생성 함수 사용)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- users 테이블 생성
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- 인증 정보
    email VARCHAR(255) UNIQUE NOT NULL,           -- 이메일 (로그인 ID, 유니크)
    password_hash VARCHAR(255) NOT NULL,         -- 비밀번호 해시 (BCrypt)
    
    -- 사용자 정보
    name VARCHAR(100),                            -- 사용자 이름
    profile_image VARCHAR(500),                  -- 프로필 이미지 URL
    
    -- 구독 정보
    subscription VARCHAR(20) DEFAULT 'free',     -- 구독 유형 (free, basic, pro, unlimited)
    
    -- 타임스탬프
    created_at TIMESTAMP DEFAULT NOW(),          -- 생성일시
    updated_at TIMESTAMP,                        -- 수정일시
    deleted_at TIMESTAMP                          -- 삭제일시 (소프트 삭제)
);

-- 인덱스 생성 (조회 성능 향상)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_subscription ON users(subscription);
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NULL;



