"""
Try-On 스키마 (Pydantic)
- API 요청/응답 데이터 검증
"""
from pydantic import BaseModel, Field
from typing import Optional
from uuid import UUID
from datetime import datetime


class TryOnRequest(BaseModel):
    """
    Try-On 요청 스키마
    
    역할:
    - 클라이언트에서 전송하는 Try-On 요청 데이터 검증
    """
    person_image: str = Field(..., description="사람 이미지 URL 또는 base64")
    garment_image: str = Field(..., description="의상 이미지 URL 또는 base64")
    avatar_id: Optional[UUID] = Field(None, description="아바타 ID (선택)")


class TryOnResponse(BaseModel):
    """
    Try-On 응답 스키마
    
    역할:
    - 서버에서 클라이언트로 반환하는 Try-On 결과 데이터
    """
    id: UUID
    user_id: UUID
    avatar_id: Optional[UUID] = None
    garment_id: Optional[UUID] = None
    result_image_url: Optional[str] = None
    thumbnail_url: Optional[str] = None
    processing_time: Optional[int] = None
    status: str
    is_favorite: bool = False
    created_at: datetime
    
    class Config:
        from_attributes = True  # SQLAlchemy 모델에서 자동 변환

