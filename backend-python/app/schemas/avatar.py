"""
Avatar 스키마 (DTO: Data Transfer Object)
- 사용자 아바타 및 신체 정보의 유효성 검사와 직렬화를 담당합니다.
"""
from pydantic import BaseModel, Field
from typing import Optional
from uuid import UUID
from datetime import datetime

# ==========================================
# 1. AvatarBase: 공통 필드 정의
# ==========================================
class AvatarBase(BaseModel):
    """
    [공통 필드] 아바타의 기본 신체 파라미터를 정의합니다.
    - 키, 몸무게 등 수치 중심의 데이터가 포함됩니다.
    """
    height: Optional[float] = Field(None, description="사용자의 키 (단위: cm)")
    weight: Optional[float] = Field(None, description="사용자의 몸무게 (단위: kg)")
    body_type: Optional[str] = Field(None, description="체형 구분 (예: slim, muscular, regular)")
    is_default: bool = Field(False, description="여러 아바타 중 현재 주력으로 사용하는지 여부")

# ==========================================
# 2. AvatarCreate: 아바타 생성용 DTO
# ==========================================
class AvatarCreate(AvatarBase):
    """
    [입력 DTO] 처음 아바타를 생성할 때 클라이언트가 보내야 하는 규격입니다.
    - 아바타의 원형이 되는 얼굴 사진(face_image_url)이 필수입니다.
    """
    face_image_url: str = Field(..., description="아바타 생성의 기반이 되는 원본 얼굴 이미지 URL")

# ==========================================
# 3. AvatarUpdate: 아바타 수정용 DTO
# ==========================================
class AvatarUpdate(AvatarBase):
    """
    [입력 DTO] 아바타의 신체 치수 등을 수정할 때 사용하는 규격입니다.
    """
    pass

# ==========================================
# 4. AvatarResponse: 아바타 응답용 DTO
# ==========================================
class AvatarResponse(AvatarBase):
    """
    [출력 DTO] 생성된 아바타 정보를 반환할 때 사용하는 규격입니다.
    - 시스템이 생성한 ID와 3D 모델링 데이터(mesh_data) 경로를 포함합니다.
    """
    id: UUID = Field(..., description="아바타 고유 식별자")
    user_id: UUID = Field(..., description="아바타 소유 사용자 ID")
    face_image_url: str = Field(..., description="업로드된 얼굴 원본 이미지 경로")
    mesh_data_url: Optional[str] = Field(None, description="Three.js에서 렌더링할 3D 메시(GLB/OBJ) 파일 경로")
    created_at: datetime = Field(..., description="아바타 생성 일시")
    updated_at: Optional[datetime] = Field(None, description="아바타 최종 수정 일시")

    class Config:
        """
        엔티티 자동 변환 설정
        - SQLAlchemy 객체로부터 데이터를 읽어올 수 있도록 허용합니다.
        """
        from_attributes = True
