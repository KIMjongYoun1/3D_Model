-- ============================================
-- V20: terms에 is_active 컬럼 추가 (노출 설정)
-- ============================================
-- is_active: true=Studio에 노출, false=관리자만 보임(미노출)

ALTER TABLE terms ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;
UPDATE terms SET is_active = true WHERE is_active IS NULL;
ALTER TABLE terms ALTER COLUMN is_active SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_terms_is_active ON terms(is_active);

COMMENT ON COLUMN terms.is_active IS '노출 여부. false면 가입/결제 화면에 미표시';
