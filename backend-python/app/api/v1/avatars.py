"""
Avatars API 엔드포인트
"""
from fastapi import APIRouter, Depends, HTTPException
from uuid import UUID
from typing import List
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.models.avatar import Avatar
from app.schemas.avatar import AvatarCreate, AvatarResponse

router = APIRouter(prefix="/avatars", tags=["Avatars"])


@router.post("/", response_model=AvatarResponse, status_code=201)
async def create_avatar(
    request: AvatarCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    아바타 생성
    
    역할:
    - 아바타 생성 및 저장
    - 얼굴 메시 처리
    
    TODO: 구현 필요
    """
    pass


@router.get("/", response_model=List[AvatarResponse])
async def list_avatars(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    아바타 목록 조회
    
    역할:
    - 현재 사용자의 아바타 목록 조회
    
    TODO: 구현 필요
    """
    pass


@router.get("/{avatar_id}", response_model=AvatarResponse)
async def get_avatar(
    avatar_id: UUID,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    아바타 조회
    
    역할:
    - 특정 아바타 정보 조회
    
    TODO: 구현 필요
    """
    pass

