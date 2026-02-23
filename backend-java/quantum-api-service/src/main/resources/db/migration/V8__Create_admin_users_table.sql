-- ============================================
-- V8: admin_users 테이블 생성
-- ============================================
-- 관리자 전용 사용자 테이블 (일반 사용자 users 테이블과 완전 분리)
-- Admin WAS 전용 인증에 사용

CREATE TABLE IF NOT EXISTS admin_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- 인증 정보
    email VARCHAR(255) UNIQUE NOT NULL,          -- 관리자 이메일 (로그인 ID)
    password_hash VARCHAR(255) NOT NULL,         -- 비밀번호 해시 (BCrypt)

    -- 관리자 정보
    name VARCHAR(100) NOT NULL,                  -- 관리자 이름
    role VARCHAR(30) DEFAULT 'ADMIN',            -- 역할 (SUPER_ADMIN, ADMIN, OPERATOR)
    is_active BOOLEAN DEFAULT TRUE,              -- 활성 상태

    -- 타임스탬프
    created_at TIMESTAMP DEFAULT NOW(),          -- 생성일시
    updated_at TIMESTAMP,                        -- 수정일시
    last_login_at TIMESTAMP                      -- 마지막 로그인 일시
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_admin_users_email ON admin_users(email);
CREATE INDEX IF NOT EXISTS idx_admin_users_role ON admin_users(role);
CREATE INDEX IF NOT EXISTS idx_admin_users_active ON admin_users(is_active) WHERE is_active = TRUE;
