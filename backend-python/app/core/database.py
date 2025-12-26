"""
데이터베이스 연결 및 세션 관리
- SQLAlchemy 엔진 및 세션 생성
- 의존성 주입을 위한 DB 세션 제공
"""
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from app.core.config import settings

# ===================
# SQLAlchemy 엔진 생성
# ===================
# 
# 엔진(Engine)의 역할:
# - 데이터베이스와의 연결을 관리
# - 연결 풀(Connection Pool) 관리
# - SQL 쿼리 실행
# 
# 주요 설정:
# - database_url: PostgreSQL 연결 URL
# - echo: SQL 쿼리 로그 출력 (개발 환경에서 디버깅용)
# - pool_pre_ping: 연결 유효성 검사 (연결 끊김 방지)
# - pool_size: 기본 연결 풀 크기 (동시 연결 수)
# - max_overflow: 추가 연결 수 (pool_size 초과 시)
# 
# 연결 풀 동작:
# - pool_size=10: 기본 10개 연결 유지
# - max_overflow=20: 최대 30개까지 연결 가능 (10 + 20)
# - 사용 후 연결 반환 (재사용)
engine = create_engine(
    settings.database_url,  # PostgreSQL 연결 URL
    echo=True,               # 개발 환경에서 SQL 쿼리 로그 출력 (프로덕션에서는 False)
    pool_pre_ping=True,     # 연결 전 유효성 검사 (연결 끊김 방지)
    pool_size=10,           # 기본 연결 풀 크기 (동시에 유지할 연결 수)
    max_overflow=20         # 최대 오버플로우 연결 수 (pool_size 초과 시 추가 연결)
)

# ===================
# 세션 팩토리 생성
# ===================
# 
# 세션(Session)의 역할:
# - 데이터베이스 작업의 단위
# - 트랜잭션 관리
# - ORM 객체와 데이터베이스 간 매핑
# 
# 주요 설정:
# - autocommit=False: 자동 커밋 비활성화 (명시적 커밋 필요)
# - autoflush=False: 자동 플러시 비활성화 (명시적 플러시 필요)
# - bind=engine: 사용할 엔진 지정
# 
# 세션 사용 패턴:
# 1. 세션 생성: SessionLocal()
# 2. 작업 수행: query(), add(), commit() 등
# 3. 세션 종료: close()
SessionLocal = sessionmaker(
    autocommit=False,  # 자동 커밋 비활성화 (트랜잭션 명시적 관리)
    autoflush=False,   # 자동 플러시 비활성화 (성능 최적화)
    bind=engine        # 사용할 엔진 지정
)

# ===================
# Base 클래스
# ===================
# 
# Base의 역할:
# - 모든 ORM 모델이 상속받을 기본 클래스
# - 모델 메타데이터 관리
# - 테이블 생성 시 사용
# 
# 사용 예시:
# class User(Base):
#     __tablename__ = "users"
#     id = Column(UUID, primary_key=True)
# 
# Base.metadata.create_all(engine)  # 모든 테이블 생성
Base = declarative_base()


def get_db():
    """
    데이터베이스 세션 의존성 주입 함수
    
    ⭐ 이해 필요: FastAPI 의존성 주입 패턴 이해
    - Depends()를 사용한 의존성 주입
    - Generator 패턴 (yield 사용)
    - 자동 리소스 정리 (finally 블록)
    
    역할:
    - FastAPI의 Depends()에서 사용하여 데이터베이스 세션 제공
    - 요청마다 새로운 세션 생성
    - 요청 완료 후 자동으로 세션 종료 (리소스 누수 방지)
    
    동작 원리:
    1. 요청 시작 시: SessionLocal()로 새 세션 생성
    2. yield db: 세션을 반환하여 엔드포인트에서 사용
    3. 요청 완료 시: finally 블록에서 세션 종료
    
    Generator 패턴:
    - yield를 사용하여 값을 반환하고 일시 중지
    - finally 블록이 항상 실행되어 리소스 정리 보장
    
    사용 예시:
        @app.get("/users")
        def get_users(db: Session = Depends(get_db)):
            # db 세션 사용
            users = db.query(User).all()
            return users
            # 함수 종료 시 자동으로 db.close() 실행
    
    주의사항:
    - 세션은 요청마다 새로 생성됨 (공유하지 않음)
    - 트랜잭션은 명시적으로 commit() 또는 rollback() 필요
    """
    # 새 세션 생성
    # - SessionLocal(): 세션 팩토리에서 새 세션 인스턴스 생성
    # - 각 요청마다 독립적인 세션 사용
    db = SessionLocal()
    try:
        # 세션을 반환하여 엔드포인트에서 사용
        # - yield: Generator 패턴으로 값을 반환하고 일시 중지
        # - 엔드포인트 함수가 실행되는 동안 세션 유지
        yield db
    finally:
        # 세션 종료 (항상 실행)
        # - try 블록이 정상 종료되거나 예외가 발생해도 실행
        # - 데이터베이스 연결 반환 (연결 풀로)
        # - 리소스 누수 방지
        db.close()

