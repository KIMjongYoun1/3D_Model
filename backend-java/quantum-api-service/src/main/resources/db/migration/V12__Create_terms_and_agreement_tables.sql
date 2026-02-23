-- ============================================
-- V12: terms, user_terms_agreement 테이블
-- ============================================
-- 약관 버전 관리 및 사용자별 동의 이력 저장

-- 1. terms: 이용약관, 개인정보처리방침 등
CREATE TABLE IF NOT EXISTS terms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type VARCHAR(50) NOT NULL,           -- TERMS_OF_SERVICE, PRIVACY_POLICY
    version VARCHAR(20) NOT NULL,        -- 1.0, 2024.01 등
    title VARCHAR(200) NOT NULL,          -- 표시용 제목
    content TEXT NOT NULL,               -- 약관 전문 (HTML/텍스트)
    effective_at TIMESTAMP NOT NULL,     -- 시행일
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_terms_type_version ON terms(type, version);
CREATE INDEX IF NOT EXISTS idx_terms_type ON terms(type);
CREATE INDEX IF NOT EXISTS idx_terms_effective_at ON terms(effective_at);

COMMENT ON TABLE terms IS '약관 버전 관리. 이용약관, 개인정보처리방침 등.';
COMMENT ON COLUMN terms.type IS 'TERMS_OF_SERVICE(이용약관), PRIVACY_POLICY(개인정보처리방침)';

-- 2. user_terms_agreement: 사용자별 약관 동의 이력
CREATE TABLE IF NOT EXISTS user_terms_agreement (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    terms_id UUID NOT NULL REFERENCES terms(id) ON DELETE CASCADE,
    agreed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ip_address VARCHAR(45),              -- IPv6 대비
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_terms_agreement_unique ON user_terms_agreement(user_id, terms_id);
CREATE INDEX IF NOT EXISTS idx_user_terms_agreement_user ON user_terms_agreement(user_id);
CREATE INDEX IF NOT EXISTS idx_user_terms_agreement_terms ON user_terms_agreement(terms_id);

COMMENT ON TABLE user_terms_agreement IS '사용자별 약관 동의 이력. terms 버전별 저장.';
