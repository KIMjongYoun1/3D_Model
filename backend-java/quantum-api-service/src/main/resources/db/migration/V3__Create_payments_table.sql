-- ============================================
-- V3: payments 테이블 생성
-- ============================================
-- 결제 정보를 저장하는 테이블

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- 사용자 관계
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- 구독 관계
    subscription_id UUID REFERENCES subscriptions(id),
    
    -- 결제 정보
    payment_method VARCHAR(50) NOT NULL,         -- 결제 수단 (card, bank_transfer, etc.)
    amount BIGINT NOT NULL,                      -- 결제 금액 (원 단위)
    status VARCHAR(20) DEFAULT 'pending',        -- 결제 상태 (pending, completed, failed, cancelled)
    
    -- PG사 정보 (시뮬레이션 모드)
    pg_provider VARCHAR(20) DEFAULT 'simulation', -- PG사 (simulation: 시뮬레이션 모드)
    pg_transaction_id VARCHAR(100),              -- 거래 ID (시뮬레이션용)
    pg_response TEXT,                            -- 응답 데이터 (시뮬레이션 응답 저장)
    
    -- 타임스탬프
    created_at TIMESTAMP DEFAULT NOW(),          -- 결제 요청일시
    updated_at TIMESTAMP,                        -- 수정일시
    completed_at TIMESTAMP,                      -- 결제 완료일시
    cancelled_at TIMESTAMP                       -- 결제 취소일시
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_pg_transaction_id ON payments(pg_transaction_id);
CREATE INDEX IF NOT EXISTS idx_payments_subscription_id ON payments(subscription_id);

