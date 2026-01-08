"""
Garment 스키마 (Pydantic)
- API 요청/응답 데이터 검증
"""
from pydantic import BaseModel, Field
from typing import Optional
from uuid import UUID
from datetime import datetime


class GarmentCreate(BaseModel):
    """
    의상 생성 요청 스키마
    """
    name: Optional[str] = Field(None, description="의상 이름")
    image_url: str = Field(..., description="의상 이미지 URL")
    category: str = Field(..., description="카테고리 (top, bottom, dress)")


class GarmentResponse(BaseModel):
    """
    의상 응답 스키마
    """
    id: UUID
    user_id: UUID
    name: Optional[str] = None
    original_url: str
    segmented_url: Optional[str] = None
    category: Optional[str] = None
    color: Optional[str] = None
    status: str
    created_at: datetime
    
    class Config:
        from_attributes = True  # SQLAlchemy 모델에서 자동 변환

