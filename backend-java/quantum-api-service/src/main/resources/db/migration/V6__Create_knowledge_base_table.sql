-- V6__Create_knowledge_base_table.sql
-- 지식 베이스(RAG) 정보를 저장하기 위한 테이블 생성

CREATE TABLE IF NOT EXISTS knowledge_base (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    source_url VARCHAR(500),
    source_type VARCHAR(50) DEFAULT 'manual',    -- 출처 유형 (manual, bok_ecos, fss_dart, law_api)
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_knowledge_category ON knowledge_base(category);
CREATE INDEX IF NOT EXISTS idx_knowledge_source_type ON knowledge_base(source_type);
CREATE INDEX IF NOT EXISTS idx_knowledge_is_active ON knowledge_base(is_active) WHERE is_active = TRUE;
