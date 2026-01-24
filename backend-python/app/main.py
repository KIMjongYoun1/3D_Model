"""
Virtual Try-On Python Backend
FastAPI 메인 애플리케이션

역할:
- AI 모델 연동 및 이미지 처리
- Try-On 파이프라인 실행
- 비동기 작업 큐 관리 (Celery)
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    애플리케이션 생명주기 관리
    
    역할:
    - FastAPI 애플리케이션의 시작/종료 시점에 실행할 작업 정의
    - startup: 애플리케이션 시작 시 실행 (마이그레이션, 초기화 등)
    - shutdown: 애플리케이션 종료 시 실행 (정리 작업 등)
    
    동작 원리:
    1. FastAPI 시작 시 lifespan 함수 실행
    2. yield 이전: startup 작업 실행
    3. yield 이후: shutdown 작업 실행 (애플리케이션 종료 시)
    
    @asynccontextmanager:
    - 비동기 컨텍스트 매니저로 동작
    - yield를 기준으로 startup/shutdown 구분
    """
    # ===================
    # Startup: 애플리케이션 시작 시 실행
    # ===================
    
    # 데이터베이스 마이그레이션 자동 실행
    # - Alembic을 사용하여 데이터베이스 스키마를 최신 상태로 업데이트
    # - 애플리케이션 시작 시 자동으로 실행되어 수동 작업 불필요
    try:
        from alembic.config import Config
        from alembic import command
        import os
        
        # Alembic 설정 파일 경로 확인
        # - backend-python/alembic.ini 파일 경로 생성
        # - os.path.join(): 운영체제에 맞는 경로 구분자 사용
        alembic_ini_path = os.path.join(os.path.dirname(__file__), "..", "alembic.ini")
        
        if os.path.exists(alembic_ini_path):
            # Alembic 설정 파일 로드
            alembic_cfg = Config(alembic_ini_path)
            
            # 마이그레이션 실행
            # - command.upgrade(): 최신 버전까지 마이그레이션 실행
            # - "head": 최신 마이그레이션 버전
            # - 실행 기록을 확인하여 이미 실행된 마이그레이션은 스킵
            command.upgrade(alembic_cfg, "head")
            print("✅ 데이터베이스 마이그레이션 완료")
        else:
            # Alembic 설정 파일이 없는 경우
            # - 초기 설정이 안 된 상태
            print("⚠️  alembic.ini 파일을 찾을 수 없습니다. Alembic 초기화가 필요합니다.")
    except Exception as e:
        # 마이그레이션 실행 중 오류 발생
        # - 데이터베이스 연결 실패
        # - 마이그레이션 파일 오류
        # - 수동 실행 안내
        print(f"⚠️  마이그레이션 실행 중 오류: {e}")
        print("   수동으로 실행: alembic upgrade head")
    
    # yield: startup과 shutdown의 경계
    # - yield 이전: startup 작업
    # - yield 이후: shutdown 작업 (애플리케이션 종료 시 실행)
    yield
    
    # ===================
    # Shutdown: 애플리케이션 종료 시 실행
    # ===================
    
    # 정리 작업 (필요 시)
    # - 데이터베이스 연결 종료
    # - 리소스 해제
    # - 로그 정리 등


# FastAPI 애플리케이션 인스턴스 생성
# - title: API 문서에 표시될 제목
# - description: API 설명
# - version: API 버전
# - lifespan: 애플리케이션 생명주기 관리 (마이그레이션 자동 실행)
app = FastAPI(
    title="Virtual Try-On API",
    description="AI 기반 Virtual Try-On 서비스 Python Backend",
    version="0.1.0",
    lifespan=lifespan
)

# CORS (Cross-Origin Resource Sharing) 미들웨어 설정
# - Frontend(Next.js)에서 API 호출 시 CORS 오류 방지
# - allow_origins: 허용할 출처 (Frontend URL)
# - allow_credentials: 쿠키/인증 정보 포함 허용
# - allow_methods: 허용할 HTTP 메서드 (* = 모든 메서드)
# - allow_headers: 허용할 HTTP 헤더 (* = 모든 헤더)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],  # Frontend URL
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 보안 미들웨어 추가
# - 위변조/탈취 방지
from app.core.middleware import (
    SecurityHeadersMiddleware,
    RateLimitMiddleware,
    LoginRateLimitMiddleware
)

# 보안 헤더 미들웨어 (XSS, 클릭재킹 방지)
app.add_middleware(SecurityHeadersMiddleware)

# Rate Limiting 미들웨어 (DDoS 방지)
app.add_middleware(RateLimitMiddleware, requests_per_minute=60)

# AI API Rate Limiting 미들웨어 (무차별 호출 방지)
# ⚠️ 주의: 로그인 Rate Limiting은 Java Backend에 있음
app.add_middleware(LoginRateLimitMiddleware, max_attempts=10, lockout_minutes=15)

@app.get("/")
async def root():
    """
    루트 엔드포인트
    - API 서버 상태 확인용
    """
    return {"message": "Virtual Try-On Python Backend", "status": "running"}

@app.get("/health")
async def health():
    """
    헬스 체크 엔드포인트
    - 서버 상태 모니터링용
    - 로드밸런서나 헬스체크 도구에서 사용
    """
    return {"status": "healthy"}

# API 라우터 등록
from app.api.v1 import (
    tryon_router, 
    garments_router, 
    avatars_router, 
    visualizations_router
)

app.include_router(tryon_router, prefix="/api/v1")
app.include_router(garments_router, prefix="/api/v1")
app.include_router(avatars_router, prefix="/api/v1")
app.include_router(visualizations_router, prefix="/api/v1")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)

