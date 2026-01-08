# Pydantic 스키마 정의
# - API 요청/응답 데이터 검증

from .tryon import TryOnRequest, TryOnResponse
from .garment import GarmentCreate, GarmentResponse
from .avatar import AvatarCreate, AvatarResponse

__all__ = [
    "TryOnRequest",
    "TryOnResponse",
    "GarmentCreate",
    "GarmentResponse",
    "AvatarCreate",
    "AvatarResponse",
]

