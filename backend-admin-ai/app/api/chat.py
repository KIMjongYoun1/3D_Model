"""
Admin AI Chat API
POST /api/admin-ai/chat - 자연어 프롬프트를 받아 분석 결과 반환
"""
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.services.chat_service import ChatService

router = APIRouter()


VALID_CATEGORIES = {"payment_analysis", "user_statistics", "knowledge_query", "general"}


class ChatRequest(BaseModel):
    """채팅 요청"""
    prompt: str
    category: str  # 필수: payment_analysis | user_statistics | knowledge_query | general
    context: str | None = None  # 추가 컨텍스트 (선택)


class ChatResponse(BaseModel):
    """채팅 응답"""
    answer: str
    intent: str
    data_summary: str | None = None
    model_used: str


@router.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest, db: Session = Depends(get_db)):
    """
    자연어 프롬프트를 받아 처리합니다.

    - 프롬프트 의도 분류 (결제 분석, 사용자 통계, 지식 조회, 일반 질문)
    - 의도에 따라 quantum_service DB 조회
    - 조회 결과 + 프롬프트를 LLM에 전달
    - LLM 응답 반환

    예시:
    - "1월 결제 내역 요약해줘"
    - "현재 활성 구독자 몇 명이야?"
    - "부가가치세법 관련 지식 정리해줘"
    - "이번 주 신규 가입자 트렌드 분석"
    """
    if not request.prompt.strip():
        raise HTTPException(status_code=400, detail="프롬프트를 입력해주세요.")
    if not request.category or request.category not in VALID_CATEGORIES:
        raise HTTPException(status_code=400, detail="카테고리를 선택해주세요.")

    chat_service = ChatService(db)
    result = await chat_service.process_prompt(request.prompt, request.context, request.category)

    return ChatResponse(
        answer=result["answer"],
        intent=result["intent"],
        data_summary=result.get("data_summary"),
        model_used=result["model_used"],
    )


@router.get("/intents")
async def get_supported_intents():
    """지원하는 프롬프트 의도 목록"""
    return {
        "intents": [
            {"id": "payment_analysis", "label": "결제 분석", "examples": ["1월 결제 내역 요약해줘", "이번 달 매출 분석"]},
            {"id": "user_statistics", "label": "사용자 통계", "examples": ["현재 활성 사용자 수", "이번 주 신규 가입자"]},
            {"id": "knowledge_query", "label": "지식 조회", "examples": ["부가가치세법 정리해줘", "세금 관련 지식 검색"]},
            {"id": "general", "label": "일반 질문", "examples": ["시스템 상태 알려줘", "도움말"]},
        ]
    }
