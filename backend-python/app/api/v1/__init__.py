# API v1 라우터
# - API 엔드포인트 정의

from .tryon import router as tryon_router
from .garments import router as garments_router
from .avatars import router as avatars_router
from .mapping import router as mapping_router

__all__ = ["tryon_router", "garments_router", "avatars_router", "mapping_router"]

