"""
quantum_service DB 읽기 전용 연결

- Admin AI 서버는 분석/조회만 수행 (쓰기 없음)
- 스키마 관리는 Java Flyway가 담당
- 테이블: users, payments, subscriptions, projects, knowledge_base, admin_users
"""
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker
from app.core.config import settings

engine = create_engine(
    settings.sqlalchemy_service_url,
    echo=settings.debug,
    pool_pre_ping=True,
    pool_size=5,
    max_overflow=10,
)

SessionLocal = sessionmaker(
    autocommit=False,
    autoflush=False,
    bind=engine,
)


def get_db():
    """quantum_service DB 세션 의존성 주입 (READ ONLY)"""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def execute_query(db, query: str, params: dict | None = None) -> list[dict]:
    """
    Raw SQL 쿼리 실행 (읽기 전용)
    SELECT 쿼리만 허용합니다.
    """
    normalized = query.strip().upper()
    if not normalized.startswith("SELECT"):
        raise ValueError("읽기 전용: SELECT 쿼리만 허용됩니다.")

    result = db.execute(text(query), params or {})
    columns = result.keys()
    return [dict(zip(columns, row)) for row in result.fetchall()]
