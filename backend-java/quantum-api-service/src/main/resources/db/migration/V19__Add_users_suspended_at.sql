-- ============================================
-- V19: users에 suspended_at 컬럼 추가 (회원 정지용)
-- ============================================
ALTER TABLE users ADD COLUMN IF NOT EXISTS suspended_at TIMESTAMP;
COMMENT ON COLUMN users.suspended_at IS '정지 시각. NULL이면 활성, 값이 있으면 정지';
