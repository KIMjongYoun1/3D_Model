from logging.config import fileConfig
from sqlalchemy import engine_from_config
from sqlalchemy import pool
from alembic import context
import os
import sys

# 프로젝트 루트 경로 추가
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))

# 설정 파일 경로
config = context.config

# 환경 변수에서 데이터베이스 URL 가져오기
from app.core.config import settings
config.set_main_option("sqlalchemy.url", settings.database_url)

# 로깅 설정
if config.config_file_name is not None:
    fileConfig(config.config_file_name)

# Base 및 모델 import
from app.core.database import Base
from app.models.user import User  # 모델 import (자동 생성용)

# target_metadata: 모든 모델의 메타데이터
target_metadata = Base.metadata

def run_migrations_offline() -> None:
    """오프라인 모드 마이그레이션"""
    url = config.get_main_option("sqlalchemy.url")
    context.configure(
        url=url,
        target_metadata=target_metadata,
        literal_binds=True,
        dialect_opts={"paramstyle": "named"},
    )

    with context.begin_transaction():
        context.run_migrations()


def run_migrations_online() -> None:
    """온라인 모드 마이그레이션"""
    connectable = engine_from_config(
        config.get_section(config.config_ini_section, {}),
        prefix="sqlalchemy.",
        poolclass=pool.NullPool,
    )

    with connectable.connect() as connection:
        context.configure(
            connection=connection, target_metadata=target_metadata
        )

        with context.begin_transaction():
            context.run_migrations()


if context.is_offline_mode():
    run_migrations_offline()
else:
    run_migrations_online()





