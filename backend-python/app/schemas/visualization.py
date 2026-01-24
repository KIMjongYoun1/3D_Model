"""
Visualization 스키마 (DTO)
- 추상 데이터 시각화 요청 및 응답을 위한 데이터 규격입니다.
"""
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any
from uuid import UUID
from datetime import datetime

class VisualizationBase(BaseModel):
    data_type: str = Field(..., description="데이터 유형 (예: json_log, software_arch)")
    raw_data: Optional[Dict[str, Any]] = Field(None, description="시각화할 원본 JSON 데이터")

class VisualizationCreate(VisualizationBase):
    pass

class VisualizationResponse(VisualizationBase):
    id: UUID
    user_id: UUID
    mapping_data: Optional[Dict[str, Any]] = Field(None, description="3D 매핑 결과 데이터")
    model_url: Optional[str] = Field(None, description="생성된 3D 파일 경로")
    created_at: datetime
    
    class Config:
        from_attributes = True

