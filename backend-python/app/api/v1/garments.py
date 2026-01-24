"""
의류 관리 API 라우터
- 클라이언트의 요청을 받아 GarmentService를 호출합니다.
"""
from fastapi import APIRouter, Depends, UploadFile, File, Form, HTTPException, status
from sqlalchemy.orm import Session
from typing import List, Optional
from uuid import UUID

from app.core.database import get_db
from app.schemas.garment import GarmentResponse
from app.services.garment_service import GarmentService
# (주의) 현재는 가짜 유저 ID를 사용합니다. 나중에 JWT 연동 시 수정됩니다.
FAKE_USER_ID = UUID("550e8400-e29b-41d4-a716-446655440000")

router = APIRouter(prefix="/garments", tags=["Garments"])

@router.post("/upload", response_model=GarmentResponse, status_code=status.HTTP_201_CREATED)
async def upload_garment(
    name: Optional[str] = Form(None),
    category: Optional[str] = Form(None),
    file: UploadFile = File(...),
    db: Session = Depends(get_db)
):
    """
    [POST] 의류 이미지 업로드 API
    - 사용자가 보낸 파일과 정보를 받아 저장합니다.
    """
    service = GarmentService(db)
    
    # 실제 서비스 호출
    try:
        new_garment = await service.upload_garment(
            user_id=FAKE_USER_ID, 
            file=file, 
            name=name, 
            category=category
        )
        return new_garment
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"업로드 중 오류 발생: {str(e)}"
        )

@router.get("", response_model=List[GarmentResponse])
def list_my_garments(db: Session = Depends(get_db)):
    """
    [GET] 내 의류 목록 조회 API
    """
    service = GarmentService(db)
    return service.get_user_garments(FAKE_USER_ID)

@router.get("/{garment_id}", response_model=GarmentResponse)
def get_garment_detail(garment_id: UUID, db: Session = Depends(get_db)):
    """
    [GET] 특정 의류 상세 조회 API
    """
    service = GarmentService(db)
    garment = service.get_garment(garment_id)
    if not garment:
        raise HTTPException(status_code=404, detail="의류를 찾을 수 없습니다.")
    return garment
