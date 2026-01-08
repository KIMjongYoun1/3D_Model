"""
Avatar 스키마 (Pydantic)
- API 요청/응답 데이터 검증
"""
from pydantic import BaseModel, Field
from typing import Optional
from uuid import UUID
from datetime import datetime


class AvatarCreate(BaseModel):
    """
    아바타 생성 요청 스키마
    """
    name: str = Field(default="My Avatar", description="아바타 이름")
    face_image_url: str = Field(..., description="얼굴 이미지 URL")
    body_height: Optional[int] = Field(None, description="키 (cm)")
    body_weight: Optional[int] = Field(None, description="몸무게 (kg)")
    body_type: Optional[str] = Field(None, description="체형 (slim, regular, athletic)")


class AvatarResponse(BaseModel):
    """
    아바타 응답 스키마
    """
    id: UUID
    user_id: UUID
    name: str
    face_image_url: str
    mesh_data_url: Optional[str] = None
    body_height: Optional[int] = None
    body_weight: Optional[int] = None
    body_type: Optional[str] = None
    is_default: bool
    created_at: datetime
    updated_at: Optional[datetime] = None
    
    class Config:
        from_attributes = True  # SQLAlchemy 모델에서 자동 변환

