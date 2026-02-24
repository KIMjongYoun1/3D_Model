-- ============================================
-- V16: plan_config 초기 데이터
-- ============================================

INSERT INTO plan_config (plan_code, plan_name, price_monthly, token_limit, description, features, is_active, sort_order)
VALUES
    ('free', 'Free Plan', 0, 10,
     '기본 3D 시각화 및 제한된 AI 분석',
     '["기본 3D 시각화", "AI 분석 월 10회", "데이터 7일 보관"]'::jsonb,
     true, 1),
    ('pro', 'Pro Plan', 29000, 100,
     '무제한 3D 시각화 및 AI 분석',
     '["무제한 3D 시각화", "AI 분석 무제한", "데이터 영구 보관", "우선 기술 지원"]'::jsonb,
     true, 2),
    ('enterprise', 'Enterprise Plan', 0, NULL,
     '커스텀 AI 모델링 및 전용 인프라',
     '["커스텀 AI 모델링", "전용 인프라 구축", "SLA 보장", "24/7 매니지드 서비스"]'::jsonb,
     true, 3)
ON CONFLICT (plan_code) DO NOTHING;
