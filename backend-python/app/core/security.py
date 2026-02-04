"""
보안 및 인증 처리 (Mock Mode)
- 자바 백엔드의 인증 기능이 완성되기 전까지 가짜 유저를 반환하여 테스트를 지원합니다.
"""
from typing import Optional
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.models.user import User
from uuid import UUID

# Bearer 토큰 규격 선언 (Swagger 등에서 인증 버튼 표시용)
security_scheme = HTTPBearer(auto_error=False)

def get_current_user(
    token: Optional[HTTPAuthorizationCredentials] = Depends(security_scheme),
    db: Session = Depends(get_db)
) -> User:
    """
    현재 사용자 조회 (Mocking)
    
    ⭐ 개발 단계 예외 처리:
    - 실제 토큰 검증 로직을 생략합니다.
    - 데이터베이스에 이미 존재하는 'model_dev' 또는 테스트 유저를 항상 반환합니다.
    """
    
    # 1. 테스트용 고정 유저 ID (아까 DB에 수동으로 넣은 ID)
    test_user_id = UUID("550e8400-e29b-41d4-a716-446655440000")
    
    # 2. DB에서 유저 조회
    user = db.query(User).filter(User.id == test_user_id).first()
    
    # 3. 만약 유저가 없다면 테스트가 불가능하므로 에러 발생 (수동 생성 가이드)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="테스트용 유저가 DB에 없습니다. INSERT 쿼리를 실행해 주세요."
        )
        
    # 토큰이 있든 없든 일단 개발 중에는 이 유저로 로그인된 것으로 간주
    return user

def decode_access_token(token: str) -> Optional[dict]:
    """토큰 디코딩 (개발 단계에서는 항상 성공한 것으로 가정하거나 무시 가능)"""
    return {"sub": "550e8400-e29b-41d4-a716-446655440000"}
