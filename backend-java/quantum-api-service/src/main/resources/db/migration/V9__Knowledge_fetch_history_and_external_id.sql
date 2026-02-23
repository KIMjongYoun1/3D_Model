-- ============================================
-- V9: 불러오기 히스토리 + 지식 항목별 구분 저장
-- ============================================
-- 1) knowledge_base: API에서 받은 항목을 "항목별·카테고리별"로 구분해 저장하기 위해 external_id 추가
--    - source_type + external_id 로 동일 항목 식별 → upsert 가능 (중복 수집 방지)
-- 2) knowledge_fetch_history: 언제 무엇을 불러왔는지 히스토리만 저장 (진짜 지식과 분리)

-- 1. knowledge_base에 external_id 컬럼 추가 (nullable: 기존 데이터 호환)
ALTER TABLE knowledge_base
  ADD COLUMN IF NOT EXISTS external_id VARCHAR(255);

COMMENT ON COLUMN knowledge_base.external_id IS '외부 API 항목 식별자. source_type과 조합해 동일 항목 upsert 시 사용';

-- source_type + external_id 조합 유니크 (둘 다 있을 때만, null 허용)
CREATE UNIQUE INDEX IF NOT EXISTS idx_knowledge_source_external
  ON knowledge_base (source_type, external_id)
  WHERE external_id IS NOT NULL AND source_type IS NOT NULL;

-- 2. 불러오기 히스토리 테이블 (진짜 지식과 분리)
CREATE TABLE IF NOT EXISTS knowledge_fetch_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_type VARCHAR(50) NOT NULL,           -- bok_ecos, fss_dart, law_api
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING', -- RUNNING, SUCCESS, FAILED
    item_count INTEGER DEFAULT 0,               -- 저장/갱신된 항목 수
    error_message TEXT,                         -- 실패 시 메시지
    params_json TEXT,                           -- 요청 파라미터 (예: lawName, corpName)
    fetched_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fetch_history_source ON knowledge_fetch_history(source_type);
CREATE INDEX IF NOT EXISTS idx_fetch_history_fetched_at ON knowledge_fetch_history(fetched_at DESC);

COMMENT ON TABLE knowledge_fetch_history IS '지식 수집(API 호출) 히스토리. 실제 지식 내용은 knowledge_base에 항목별·카테고리별 저장.';
