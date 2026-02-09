-- users 테이블에 refresh_token 컬럼 추가
ALTER TABLE users ADD COLUMN refresh_token VARCHAR(255);

-- 토큰 조회를 위한 인덱스 추가
CREATE INDEX idx_users_refresh_token ON users(refresh_token);
