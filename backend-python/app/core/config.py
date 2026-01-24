"""
애플리케이션 설정 관리
- 환경 변수 로드
- 데이터베이스 연결 설정
- JWT 설정
- 외부 API 설정
"""
from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """
    애플리케이션 설정 클래스
    - pydantic-settings를 사용하여 환경 변수 자동 로드
    - .env 파일 또는 환경 변수에서 값 읽기
    """
    
    # ===================
    # 애플리케이션 기본 설정
    # ===================
    app_name: str = "Virtual Try-On API"
    app_version: str = "0.1.0"
    debug: bool = False
    
    # ===================
    # 데이터베이스 설정
    # ===================
    # PostgreSQL 연결 URL
    # 형식: postgresql+psycopg://사용자명:비밀번호@호스트:포트/데이터베이스명
    # (psycopg2 대신 현대적인 psycopg (v3) 드라이버를 사용하도록 지정합니다)
    database_url: str = "postgresql+psycopg://model_dev:dev1234@localhost:5432/postgres"
    
    # ===================
    # Redis 설정
    # ===================
    # Redis 연결 URL (Celery 브로커/백엔드용)
    # 형식: redis://호스트:포트/데이터베이스번호
    redis_url: str = "redis://localhost:6379/0"
    
    # ===================
    # JWT 인증 설정
    # ===================
    # JWT 비밀키 (프로덕션에서는 반드시 변경 필요)
    jwt_secret: str = "your-super-secret-key-change-this-in-production"
    jwt_algorithm: str = "HS256"  # JWT 알고리즘
    jwt_expire_minutes: int = 60  # 토큰 만료 시간 (분)
    
    # ===================
    # 스토리지 설정 (로컬 파일 시스템 우선)
    # ===================
    # 로컬 파일 시스템 사용 (기본값)
    storage_type: str = "local"  # local: 로컬 파일 시스템, s3: S3 호환 스토리지
    storage_path: str = "./storage"  # 로컬 저장 경로
    
    # S3 호환 스토리지 (선택사항 - 프로덕션에서 사용)
    s3_endpoint: Optional[str] = None
    s3_access_key: Optional[str] = None
    s3_secret_key: Optional[str] = None
    s3_bucket: Optional[str] = None
    
    # ===================
    # AI 서비스 설정
    # ===================
    ai_server_url: str = "http://localhost:8000"
    huggingface_token: Optional[str] = None
    
    # ===================
    # Java Backend API URL
    # ===================
    java_api_url: str = "http://localhost:8080"
    
    class Config:
        """
        Pydantic 설정
        - env_file: .env 파일에서 환경 변수 로드
        - case_sensitive: 환경 변수 대소문자 구분 여부
        """
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = False


# 전역 설정 인스턴스
# 다른 모듈에서 from app.core.config import settings로 사용
settings = Settings()

