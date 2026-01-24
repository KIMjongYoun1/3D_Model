"""
시각화 API 라우터
- 추상 데이터(JSON 등)의 3D 매핑 요청을 처리합니다.
"""
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from uuid import UUID

from app.core.database import get_db
from app.schemas.visualization import VisualizationCreate, VisualizationResponse
from app.models.visualization import VisualizationData

# 임시 유저 ID (나중에 JWT 연동)
FAKE_USER_ID = UUID("550e8400-e29b-41d4-a716-446655440000")

router = APIRouter(prefix="/visualizations", tags=["Visualizations"])

@router.post("", response_model=VisualizationResponse, status_code=status.HTTP_201_CREATED)
async def create_visualization(
    request: VisualizationCreate,
    db: Session = Depends(get_db)
):
    """
    [POST] 새로운 시각화 데이터 등록
    """
    db_viz = VisualizationData(
        user_id=FAKE_USER_ID,
        data_type=request.data_type,
        raw_data=request.raw_data
    )
    db.add(db_viz)
    db.commit()
    db.refresh(db_viz)
    return db_viz

@router.get("", response_model=List[VisualizationResponse])
def list_visualizations(db: Session = Depends(get_db)):
    """
    [GET] 내 시각화 목록 조회
    """
    return db.query(VisualizationData).filter(VisualizationData.user_id == FAKE_USER_ID).all()

