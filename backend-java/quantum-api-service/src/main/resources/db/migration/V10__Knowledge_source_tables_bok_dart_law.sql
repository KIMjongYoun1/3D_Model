-- ============================================
-- V10: 지식 소스별 테이블 분리 (한국은행, DART, 법령)
-- 응답 형식에 맞춰 컬럼 구성 → 지식 활용 + 상세 목록 조회
-- ============================================

-- ---------------------------------------------------------------------------
-- 1. DART 기업코드 (corpCode.json 수집용)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS dart_corp_code (
    corp_code VARCHAR(8) PRIMARY KEY,        -- 고유번호 8자리
    corp_name VARCHAR(255),                  -- 정식명칭
    corp_name_eng VARCHAR(255),              -- 영문 정식명칭
    stock_code VARCHAR(6),                   -- 종목코드 6자리 (상장사)
    modify_date VARCHAR(8),                  -- 최종변경일자 YYYYMMDD
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_dart_corp_name ON dart_corp_code(corp_name);
CREATE INDEX IF NOT EXISTS idx_dart_corp_stock ON dart_corp_code(stock_code) WHERE stock_code IS NOT NULL AND stock_code != '';

COMMENT ON TABLE dart_corp_code IS 'DART 공시대상 회사 고유번호. corpCode.json 수집 후 list.json 검색 시 사용(3개월 제한 회피).';


-- ---------------------------------------------------------------------------
-- 2. 한국은행 ECOS (StatisticSearch 응답 형식)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS knowledge_bok (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    stat_code VARCHAR(20) NOT NULL,           -- 722Y001 등
    stat_name VARCHAR(255),                   -- 1.3.1. 한국은행 기준금리 및 여수신금리
    item_code1 VARCHAR(20),
    item_name1 VARCHAR(255),                  -- 한국은행 기준금리
    item_code2 VARCHAR(20),
    item_name2 VARCHAR(255),
    time VARCHAR(8) NOT NULL,                 -- YYYYMMDD
    data_value VARCHAR(50) NOT NULL,          -- 3.5
    unit_name VARCHAR(20),                   -- 연%
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(stat_code, item_code1, time)
);

CREATE INDEX IF NOT EXISTS idx_knowledge_bok_time ON knowledge_bok(time DESC);
CREATE INDEX IF NOT EXISTS idx_knowledge_bok_stat ON knowledge_bok(stat_code);

COMMENT ON TABLE knowledge_bok IS '한국은행 ECOS API 응답 형식. 기준금리 등 경제 지표.';


-- ---------------------------------------------------------------------------
-- 3. DART 공시 목록 (list.json 응답 형식)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS knowledge_dart (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    corp_code VARCHAR(8) NOT NULL,            -- dart_corp_code 참조
    corp_name VARCHAR(255),
    rcept_no VARCHAR(20) NOT NULL UNIQUE,     -- 접수번호 (항목 식별)
    rcept_dt VARCHAR(8),                      -- 접수일자 YYYYMMDD
    report_nm VARCHAR(255),                  -- 보고서명
    flr_nm VARCHAR(100),                      -- 공시 제출인명
    rm VARCHAR(500),                          -- 비고
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_knowledge_dart_corp ON knowledge_dart(corp_code);
CREATE INDEX IF NOT EXISTS idx_knowledge_dart_rcept_dt ON knowledge_dart(rcept_dt DESC);

COMMENT ON TABLE knowledge_dart IS 'DART 공시 목록(list.json) 응답 형식. 상세 목록/지식 활용.';


-- ---------------------------------------------------------------------------
-- 4. 법령 (국가법령정보센터 lawSearch 응답 형식)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS knowledge_law (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mst VARCHAR(20) NOT NULL UNIQUE,         -- 법령일련번호 (항목 식별)
    law_name_ko VARCHAR(255) NOT NULL,       -- 법령명한글
    law_type VARCHAR(50),                     -- 법령구분명: 법률, 대통령령, 부령
    dept_name VARCHAR(100),                  -- 소관부처명
    proclamation_no VARCHAR(30),             -- 공포번호
    proclamation_date VARCHAR(8),             -- 공포일자 YYYYMMDD
    enforce_date VARCHAR(8),                  -- 시행일자 YYYYMMDD
    law_id VARCHAR(20),                       -- 법령ID
    content TEXT,                             -- 요약/상세(조문 등)
    source_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_knowledge_law_name ON knowledge_law(law_name_ko);
CREATE INDEX IF NOT EXISTS idx_knowledge_law_type ON knowledge_law(law_type);
CREATE INDEX IF NOT EXISTS idx_knowledge_law_date ON knowledge_law(proclamation_date DESC);

COMMENT ON TABLE knowledge_law IS '국가법령정보센터 API 응답 형식. 법령별 상세 목록/지식 활용.';
