-- ============================================
-- V2: subscriptions 테이블 생성
-- ============================================
-- 사용자 구독 정보를 저장하는 테이블

CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- 사용자 관계
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- 구독 정보
    plan_type VARCHAR(20) NOT NULL,             -- 플랜 유형 (free, basic, pro, unlimited)
    status VARCHAR(20) DEFAULT 'active',         -- 상태 (active, expired, cancelled)
    
    -- 사용량 제한
    tryon_limit INT,                             -- 월 Try-On 제한 횟수
    tryon_used INT DEFAULT 0,                    -- 사용한 Try-On 횟수
    
    -- 기간 정보
    started_at TIMESTAMP,                        -- 구독 시작일
    expires_at TIMESTAMP,                        -- 구독 만료일
    
    -- 결제 정보
    payment_id UUID,                             -- 결제 ID (FK, payments 테이블 참조)
    
    -- 자동 갱신
    auto_renew BOOLEAN DEFAULT false,            -- 자동 갱신 여부
    
    -- 취소 정보
    cancelled_at TIMESTAMP,                      -- 취소일시
    
    -- 타임스탬프
    created_at TIMESTAMP DEFAULT NOW()
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_expires_at ON subscriptions(expires_at);



