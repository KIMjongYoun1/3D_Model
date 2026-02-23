"""
Admin AI 서버 설정 관리
- quantum_service DB 읽기 전용 연결
- Ollama / Gemini LLM 설정
"""
from pydantic_settings import BaseSettings
from pydantic import Field
from typing import Optional


class Settings(BaseSettings):
    """Admin AI 서버 설정"""

    # ===================
    # 애플리케이션 기본 설정
    # ===================
    app_name: str = "Admin AI Server"
    app_version: str = "0.1.0"
    debug: bool = True

    # ===================
    # 데이터베이스 (quantum_service 읽기 전용)
    # ===================
    service_database_url: str = "postgresql+psycopg://model_dev:dev1234@localhost:5432/quantum_service"

    @property
    def sqlalchemy_service_url(self) -> str:
        """SQLAlchemy용 Service DB URL (읽기 전용)"""
        url = self.service_database_url
        if url.startswith("jdbc:postgresql://"):
            url = url.replace("jdbc:postgresql://", "postgresql+psycopg://")
        elif url.startswith("postgresql://") and not url.startswith("postgresql+psycopg://"):
            url = url.replace("postgresql://", "postgresql+psycopg://")
        return url

    # ===================
    # LLM 설정
    # ===================
    ollama_base_url: str = "http://localhost:11434"
    ollama_model: str = "llama3.2"
    gemini_api_key: Optional[str] = None

    # ===================
    # CORS
    # ===================
    admin_frontend_url: str = "http://localhost:3001"

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = False
        extra = "ignore"


settings = Settings()
