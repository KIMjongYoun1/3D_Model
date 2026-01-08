"""
스토리지 서비스
- 파일 저장 (로컬/S3)
"""
from app.core.config import settings


class StorageService:
    """
    파일 저장 서비스
    
    역할:
    - 이미지 저장 (로컬 파일 시스템 또는 S3)
    - 이미지 삭제
    - 이미지 URL 조회
    """
    
    def __init__(self):
        """
        스토리지 서비스 초기화
        """
        self.storage_type = settings.storage_type  # local or s3
    
    def save_image(self, image: bytes, filename: str) -> str:
        """
        이미지 저장
        
        Args:
            image: 이미지 바이너리 데이터
            filename: 파일명
            
        Returns:
            저장된 이미지 URL
            
        TODO: 저장 로직 구현
        - 로컬: 파일 시스템에 저장
        - S3: S3에 업로드
        """
        pass
    
    def delete_image(self, url: str):
        """
        이미지 삭제
        
        Args:
            url: 이미지 URL
            
        TODO: 삭제 로직 구현
        """
        pass
    
    def get_image_url(self, filename: str) -> str:
        """
        이미지 URL 조회
        
        Args:
            filename: 파일명
            
        Returns:
            이미지 URL
            
        TODO: URL 조회 로직 구현
        """
        pass

