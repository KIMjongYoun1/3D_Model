-- ============================================
-- V18: terms에 category, required 컬럼 추가
-- ============================================
-- category: SIGNUP(가입), PAYMENT(결제) - 어느 화면에서 사용하는지
-- required: 필수(true) / 선택(false) - 해당 화면에서 필수 동의 여부

ALTER TABLE terms ADD COLUMN IF NOT EXISTS category VARCHAR(20) DEFAULT 'SIGNUP';
ALTER TABLE terms ADD COLUMN IF NOT EXISTS required BOOLEAN DEFAULT true;

-- 기존 데이터: 가입용은 SIGNUP, 결제용은 PAYMENT
UPDATE terms SET category = 'PAYMENT' WHERE type = 'SUBSCRIPTION_TERMS';
UPDATE terms SET category = 'SIGNUP' WHERE category IS NULL OR category = '';

ALTER TABLE terms ALTER COLUMN category SET NOT NULL;
ALTER TABLE terms ALTER COLUMN required SET NOT NULL;

COMMENT ON COLUMN terms.category IS 'SIGNUP(가입), PAYMENT(결제)';
COMMENT ON COLUMN terms.required IS '필수(true) / 선택(false)';
