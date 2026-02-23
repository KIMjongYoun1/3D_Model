"""
Admin AI Server - FastAPI 앱
- 관리자 전용 AI 프롬프트 처리
- quantum_service DB 읽기 전용 분석
- Ollama(Llama 3.2) / Gemini 폴백 LLM 연동
"""
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.core.config import settings
from app.api.chat import router as chat_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    """앱 시작/종료 이벤트"""
    print(f"🚀 {settings.app_name} v{settings.app_version} starting...")
    print(f"   Ollama: {settings.ollama_base_url} (model: {settings.ollama_model})")
    print(f"   DB: quantum_service (READ ONLY)")
    yield
    print(f"🛑 {settings.app_name} shutting down...")


app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    description="Admin 전용 AI 분석 서버 - 자연어 프롬프트로 관리 업무 지원",
    lifespan=lifespan,
)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        settings.admin_frontend_url,
        "http://localhost:3001",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Routes
app.include_router(chat_router, prefix="/api/admin-ai", tags=["Admin AI Chat"])


@app.get("/health")
async def health_check():
    return {
        "status": "ok",
        "service": settings.app_name,
        "version": settings.app_version,
    }
