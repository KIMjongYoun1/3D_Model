-- 조문 본문 저장용 컬럼 추가
-- content: 기존 요약 메타 ([법률] 법령명 (공포일자: ...))
-- article_body: 조문 본문 (제1조, 제2조...)

-- knowledge_base: RAG/AI 지식 베이스
ALTER TABLE knowledge_base
    ADD COLUMN IF NOT EXISTS article_body TEXT;

COMMENT ON COLUMN knowledge_base.article_body IS '법령 조문 본문. content는 요약, article_body는 상세 조문.';

-- knowledge_law: 법령 상세 테이블
ALTER TABLE knowledge_law
    ADD COLUMN IF NOT EXISTS article_body TEXT;

COMMENT ON COLUMN knowledge_law.article_body IS '법령 조문 본문. content는 요약, article_body는 상세 조문.';
