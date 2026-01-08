"""
Garments API 엔드포인트
"""
from fastapi import APIRouter, Depends, HTTPException
from uuid import UUID
from typing import List
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.models.garment import Garment
from app.schemas.garment import GarmentCreate, GarmentResponse

router = APIRouter(prefix="/garments", tags=["Garments"])


@router.post("/", response_model=GarmentResponse, status_code=201)
async def create_garment(
    request: GarmentCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    의상 업로드
    
    역할:
    - 의상 이미지 업로드 및 저장
    - 의상 정보를 데이터베이스에 저장
    
    TODO: 구현 필요
    """
    pass


@router.get("/", response_model=List[GarmentResponse])
async def list_garments(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    의상 목록 조회
    
    역할:
    - 현재 사용자의 의상 목록 조회
    
    TODO: 구현 필요
    """
    pass


@router.get("/{garment_id}", response_model=GarmentResponse)
async def get_garment(
    garment_id: UUID,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    의상 조회
    
    역할:
    - 특정 의상 정보 조회
    
    TODO: 구현 필요
    """
    pass

