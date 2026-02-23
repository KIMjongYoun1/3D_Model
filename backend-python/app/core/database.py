"""
데이터베이스 연결 및 세션 관리 (2개 DB 분리 구조)

1. AI DB (quantum_ai): Python 전용, R/W
   - visualization_data, correlation_rules
   - Alembic 마이그레이션 대상

2. Service DB (quantum_service): Java 전용, Python에서는 READ ONLY
   - knowledge_base (RAG 조회용)
   - 스키마 관리는 Java Flyway가 담당
"""
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from app.core.config import settings

# ===================
# [AI DB] 메인 엔진 (R/W)
# ===================
# visualization_data, correlation_rules 등 Python 소유 테이블 접근용
engine = create_engine(
    settings.sqlalchemy_database_url,
    echo=True,
    pool_pre_ping=True,
    pool_size=10,
    max_overflow=20
)

SessionLocal = sessionmaker(
    autocommit=False,
    autoflush=False,
    bind=engine
)

# ===================
# [Service DB] 읽기 전용 엔진
# ===================
# knowledge_base (RAG) 조회 전용
# - Java Flyway가 스키마를 관리하므로 Python은 읽기만 수행
# - pool_size를 작게 설정 (읽기 전용이므로 커넥션을 적게 유지)
service_engine = create_engine(
    settings.sqlalchemy_service_database_url,
    echo=True,
    pool_pre_ping=True,
    pool_size=3,
    max_overflow=5
)

ServiceSessionLocal = sessionmaker(
    autocommit=False,
    autoflush=False,
    bind=service_engine
)

# ===================
# Base 클래스
# ===================
Base = declarative_base()


def get_db():
    """
    AI DB 세션 의존성 주입 (R/W)
    - visualization_data, correlation_rules 등 접근용
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def get_service_db():
    """
    Service DB 세션 의존성 주입 (READ ONLY)
    - knowledge_base RAG 조회 전용
    - Java Flyway가 관리하는 테이블에 대한 읽기 접근
    """
    db = ServiceSessionLocal()
    try:
        yield db
    finally:
        db.close()
