"""
매핑 스키마 (DTO)
"""
from pydantic import BaseModel, Field
from typing import Optional, Any, Union, Dict
from uuid import UUID
from datetime import datetime

class MappingBase(BaseModel):
    data_type: str = Field(..., description="데이터 유형")
    # Union[Dict, str]을 사용하여 JSON 객체와 일반 문자열 모두 허용합니다.
    raw_data: Optional[Union[Dict[str, Any], str]] = Field(None, description="원본 데이터 (JSON 또는 Text)")

class MappingCreate(MappingBase):
    pass

class MappingResponse(MappingBase):
    id: UUID
    user_id: UUID
    mapping_data: Optional[Any] = Field(None, description="3D 매핑 결과")
    result_model_url: Optional[str] = Field(None, description="생성된 3D 파일 경로")
    created_at: datetime
    
    class Config:
        from_attributes = True
