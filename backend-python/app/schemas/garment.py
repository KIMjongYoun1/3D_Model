"""
Garment 스키마 (DTO: Data Transfer Object)
- 의류 데이터의 유효성 검사 및 데이터 직렬화를 담당합니다.
- 자바의 Request/Response DTO 객체와 동일한 역할을 수행합니다.
"""
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any
from uuid import UUID
from datetime import datetime

# ==========================================
# 1. GarmentBase: 공통 필드 정의
# ==========================================
class GarmentBase(BaseModel):
    """
    [공통 필드] 의류 데이터의 기본 속성을 정의합니다.
    - 다른 모든 Garment 관련 스키마가 이 클래스를 상속받습니다.
    - 필드 정의 시 Optional을 사용하여 값이 없을 수도 있음을 명시합니다.
    """
    name: Optional[str] = Field(None, max_length=100, description="의류 이름 (예: 여름용 린넨 셔츠)")
    category: Optional[str] = Field(None, max_length=50, description="의류 카테고리 (상의, 하의, 아우터 등)")
    features: Optional[Dict[str, Any]] = Field(None, description="의류의 상세 특징 (색상, 재질 정보 등을 JSON으로 저장)")

# ==========================================
# 2. GarmentCreate: 생성 요청용 DTO
# ==========================================
class GarmentCreate(GarmentBase):
    """
    [입력 DTO] 새로운 의류 등록 시 클라이언트로부터 받는 데이터 규격입니다.
    - 자바의 @RequestBody에 매핑되는 객체와 같습니다.
    - image_url은 필수값으로 설정하여 원본 사진 없이는 등록이 불가능하게 제한합니다.
    """
    image_url: str = Field(..., description="원본 이미지 URL (S3 또는 로컬 경로)")
    # '...'는 Pydantic에서 이 필드가 필수(Required)임을 의미합니다.

# ==========================================
# 3. GarmentUpdate: 수정 요청용 DTO
# ==========================================
class GarmentUpdate(GarmentBase):
    """
    [입력 DTO] 기존 의류 정보를 수정할 때 사용하는 규격입니다.
    - GarmentBase를 상속받아 필요한 필드만 선택적으로 수정할 수 있도록 합니다.
    """
    pass # 추가적인 제약 조건 없이 Base의 필드들을 그대로 사용합니다.

# ==========================================
# 4. GarmentResponse: API 응답용 DTO
# ==========================================
class GarmentResponse(GarmentBase):
    """
    [출력 DTO] API가 클라이언트에게 데이터를 보낼 때 사용하는 규격입니다.
    - 자바의 ResponseDTO와 동일한 역할입니다.
    - 보안상 민감하지 않은 DB 정보(ID, 생성일시 등)를 포함하여 반환합니다.
    """
    id: UUID = Field(..., description="의류의 고유 식별자 (PK)")
    user_id: UUID = Field(..., description="해당 의류를 소유한 사용자의 ID")
    image_url: str = Field(..., description="원본 이미지 접근 경로")
    processed_image_url: Optional[str] = Field(None, description="AI 처리가 완료된(배경 제거 등) 이미지 경로")
    created_at: datetime = Field(..., description="의류 정보 생성 일시")
    updated_at: Optional[datetime] = Field(None, description="의류 정보 최종 수정 일시")

    class Config:
        """
        ORM 모드 설정
        - SQLAlchemy 모델 객체를 Pydantic 스키마로 자동 변환(Mapping)해주는 기능입니다.
        - 서비스 계층에서 엔티티 객체를 그대로 반환해도 이 설정 덕분에 DTO로 자동 변환됩니다.
        """
        from_attributes = True
