"""
애플리케이션 설정 관리
- 환경 변수 로드
- 데이터베이스 연결 설정
- JWT 설정
- 외부 API 설정
"""
from pydantic_settings import BaseSettings
from pydantic import Field
from typing import Optional


class Settings(BaseSettings):
    """
    애플리케이션 설정 클래스
    - 개발 초기 단계이므로 필수값 제약을 완화합니다.
    """
    
    # ===================
    # 애플리케이션 기본 설정
    # ===================
    app_name: str = "QuantumViz API"
    app_version: str = "0.1.0"
    debug: bool = True # 개발 모드
    
    # ===================
    # 데이터베이스 및 인프라 (기본값 제공)
    # ===================
    database_url: str = "postgresql+psycopg://model_dev:dev1234@localhost:5432/postgres"
    
    @property
    def sqlalchemy_database_url(self) -> str:
        """
        SQLAlchemy용 데이터베이스 URL 반환
        - Java용 jdbc: 형식이 들어오면 파이썬용으로 변환
        """
        url = self.database_url
        if url.startswith("jdbc:postgresql://"):
            url = url.replace("jdbc:postgresql://", "postgresql+psycopg://")
        elif url.startswith("postgresql://") and not url.startswith("postgresql+psycopg://"):
            url = url.replace("postgresql://", "postgresql+psycopg://")
        return url

    redis_url: str = "redis://localhost:6379/0"
    
    # 보안 및 AI 키 (선택사항으로 변경)
    jwt_secret: Optional[str] = "temp-secret-key-for-dev"
    gemini_api_key: Optional[str] = None
    
    # ===================
    # 선택적 설정 (Optional)
    # ===================
    jwt_algorithm: str = "HS256"
    jwt_expire_minutes: int = 60
    storage_type: str = "local"
    storage_path: str = "./storage"
    ai_server_url: str = "http://localhost:8000"
    
    # 프론트엔드 연동용
    next_public_api_url: Optional[str] = None
    next_public_python_api_url: Optional[str] = None

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = False
        extra = "ignore" 


# 전역 설정 인스턴스
settings = Settings()
