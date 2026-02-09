-- ============================================
-- V7: projects 테이블 생성
-- ============================================
-- 사용자의 시각화 프로젝트를 관리하는 테이블

CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- 사용자 관계
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- 프로젝트 정보
    name VARCHAR(255) NOT NULL,                  -- 프로젝트명
    description TEXT,                            -- 프로젝트 설명
    main_category VARCHAR(50),                   -- 메인 카테고리 (GENERAL, FINANCE, INFRA, LOGISTICS)
    sub_category VARCHAR(50),                    -- 서브 카테고리 (DOC, TAX, SETTLEMENT 등)
    status VARCHAR(20) DEFAULT 'active',         -- 상태 (active, archived, deleted)
    
    -- 타임스탬프
    created_at TIMESTAMP DEFAULT NOW(),          -- 생성일시
    updated_at TIMESTAMP                         -- 수정일시
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_projects_user_id ON projects(user_id);
CREATE INDEX IF NOT EXISTS idx_projects_main_category ON projects(main_category);
CREATE INDEX IF NOT EXISTS idx_projects_status ON projects(status);
