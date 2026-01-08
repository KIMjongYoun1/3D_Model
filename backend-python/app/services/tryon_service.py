"""
Try-On 서비스
- Try-On 비즈니스 로직 처리
"""
from sqlalchemy.orm import Session
from uuid import UUID
from typing import List
from app.models.user import User
from app.models.tryon_result import TryOnResult
from app.schemas.tryon import TryOnRequest
from app.services.ai_service import AIService
from app.services.storage_service import StorageService


class TryOnService:
    """
    Try-On 비즈니스 로직 서비스
    
    역할:
    - Try-On 요청 처리
    - AI 서비스 연동
    - 결과 저장 및 조회
    """
    
    def __init__(self, db: Session, current_user: User):
        """
        Try-On 서비스 초기화
        
        Args:
            db: 데이터베이스 세션
            current_user: 현재 인증된 사용자
        """
        self.db = db
        self.current_user = current_user
        self.ai_service = AIService()
        self.storage_service = StorageService()
    
    async def create_tryon(self, request: TryOnRequest) -> TryOnResult:
        """
        Try-On 생성
        
        Args:
            request: Try-On 요청 데이터
            
        Returns:
            Try-On 결과
            
        TODO: 비즈니스 로직 구현
        1. 이미지 다운로드
        2. AI 처리 (ai_service.run_tryon)
        3. 결과 저장
        4. DB에 결과 저장
        """
        pass
    
    def get_tryon(self, result_id: UUID) -> TryOnResult:
        """
        Try-On 결과 조회
        
        Args:
            result_id: 결과 ID
            
        Returns:
            Try-On 결과
            
        TODO: 조회 로직 구현
        """
        pass
    
    def list_tryon_results(self) -> List[TryOnResult]:
        """
        사용자의 Try-On 결과 목록 조회
        
        Returns:
            Try-On 결과 목록
            
        TODO: 목록 조회 로직 구현
        """
        pass

