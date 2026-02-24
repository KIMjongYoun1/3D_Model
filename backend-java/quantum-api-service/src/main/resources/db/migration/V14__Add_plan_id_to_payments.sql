-- ============================================
-- V14: payments 테이블에 plan_id 컬럼 추가
-- ============================================
-- 구독 플랜 식별용 (free, pro, enterprise)

ALTER TABLE payments ADD COLUMN IF NOT EXISTS plan_id VARCHAR(20);
COMMENT ON COLUMN payments.plan_id IS '구독 플랜 (free, pro, enterprise)';
