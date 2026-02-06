-- users 테이블에 소셜 로그인 관련 컬럼 추가
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;
ALTER TABLE users ADD COLUMN provider VARCHAR(20) DEFAULT 'LOCAL';
ALTER TABLE users ADD COLUMN provider_id VARCHAR(255);

-- provider_id에 인덱스 추가 (조회 성능 향상)
CREATE INDEX idx_users_provider_id ON users(provider_id);
