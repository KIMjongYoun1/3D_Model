"""
TryOn 스키마 (DTO: Data Transfer Object)
- 가상 피팅(Try-On) 프로세스의 요청과 결과 데이터를 관리합니다.
"""
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any
from uuid import UUID
from datetime import datetime

# ==========================================
# 1. TryOnRequest: 피팅 실행 요청용 DTO
# ==========================================
class TryOnRequest(BaseModel):
    """
    [입력 DTO] 사용자가 가상 피팅을 시도할 때 백엔드로 보내는 요청 규격입니다.
    - 자바의 RequestBody와 동일합니다.
    - 어떤 '사람(아바타)'에게 어떤 '옷(의류)'을 입힐지 ID로 지정합니다.
    """
    avatar_id: UUID = Field(..., description="피팅을 적용할 아바타의 고유 ID")
    garment_id: UUID = Field(..., description="입혀보고자 하는 의류의 고유 ID")

# ==========================================
# 2. TryOnResponse: 피팅 결과 응답용 DTO
# ==========================================
class TryOnResponse(BaseModel):
    """
    [출력 DTO] AI 처리가 완료된 최종 피팅 결과를 클라이언트에게 전달하는 규격입니다.
    - 합성된 결과 이미지의 URL과 AI 모델의 메타데이터를 포함합니다.
    """
    id: UUID = Field(..., description="피팅 결과 로그의 고유 ID")
    user_id: UUID = Field(..., description="요청을 수행한 사용자 ID")
    avatar_id: UUID = Field(..., description="사용된 아바타 ID")
    garment_id: UUID = Field(..., description="사용된 의류 ID")
    result_image_url: str = Field(..., description="AI가 생성한 최종 피팅 완료 이미지 URL")
    ai_metadata: Optional[Dict[str, Any]] = Field(None, description="AI 모델 정보, 처리 시간 등 기술적 메타데이터")
    created_at: datetime = Field(..., description="피팅 수행 및 결과 생성 일시")

    class Config:
        """
        엔티티 자동 변환 설정
        - DB 엔티티 객체를 Response DTO로 즉시 변환하기 위한 필수 설정입니다.
        """
        from_attributes = True
