"""
API 의존성 함수
- Service 클래스 의존성 주입
"""
from fastapi import Depends
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.services.tryon_service import TryOnService


def get_tryon_service(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
) -> TryOnService:
    """
    Try-On Service 의존성 함수
    
    역할:
    - Try-On Service 인스턴스 생성 및 주입
    - DB 세션과 인증된 사용자 자동 주입
    
    Args:
        db: 데이터베이스 세션 (자동 주입)
        current_user: 인증된 사용자 (자동 주입)
        
    Returns:
        TryOnService 인스턴스
    """
    return TryOnService(db, current_user)

