-- ============================================
-- V15: plan_config 테이블 생성
-- ============================================
-- 요금제/플랜 설정 - DB에서 자유롭게 추가·수정 가능

CREATE TABLE IF NOT EXISTS plan_config (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- 플랜 식별
    plan_code VARCHAR(30) NOT NULL UNIQUE,       -- free, pro, enterprise 등 (코드 참조용)
    plan_name VARCHAR(100) NOT NULL,             -- 표시명 (예: Pro Plan)

    -- 요금
    price_monthly BIGINT NOT NULL DEFAULT 0,      -- 월 요금 (원, 0=무료)

    -- 사용량 제한
    token_limit INT,                             -- 월 토큰 한도 (NULL=무제한)

    -- 부가 정보
    description TEXT,                            -- 플랜 설명
    features JSONB,                              -- 기능 목록 (["AI 무제한", "데이터 영구보관"] 등)

    -- 활성화
    is_active BOOLEAN NOT NULL DEFAULT true,     -- 노출/판매 여부
    sort_order INT NOT NULL DEFAULT 0,           -- 정렬 순서 (작을수록 먼저)

    -- 타임스탬프
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_plan_config_plan_code ON plan_config(plan_code);
CREATE INDEX IF NOT EXISTS idx_plan_config_is_active ON plan_config(is_active);
CREATE INDEX IF NOT EXISTS idx_plan_config_sort_order ON plan_config(sort_order);

COMMENT ON TABLE plan_config IS '요금제/플랜 설정 - 서비스 추가·변경 시 DB에서 관리';
