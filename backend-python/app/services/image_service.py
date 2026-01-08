"""
이미지 처리 서비스
- 이미지 다운로드, 리사이즈, 썸네일 생성
"""
from typing import Tuple


class ImageService:
    """
    이미지 처리 서비스
    
    역할:
    - 이미지 다운로드
    - 이미지 리사이즈
    - 썸네일 생성
    - 이미지 유효성 검사
    """
    
    def download_image(self, url: str) -> bytes:
        """
        이미지 다운로드
        
        Args:
            url: 이미지 URL
            
        Returns:
            이미지 바이너리 데이터
            
        TODO: 다운로드 로직 구현
        """
        pass
    
    def resize_image(self, image: bytes, width: int, height: int) -> bytes:
        """
        이미지 리사이즈
        
        Args:
            image: 이미지 바이너리 데이터
            width: 목표 너비
            height: 목표 높이
            
        Returns:
            리사이즈된 이미지 바이너리 데이터
            
        TODO: 리사이즈 로직 구현
        """
        pass
    
    def create_thumbnail(self, image: bytes, size: Tuple[int, int] = (200, 200)) -> bytes:
        """
        썸네일 생성
        
        Args:
            image: 이미지 바이너리 데이터
            size: 썸네일 크기 (width, height)
            
        Returns:
            썸네일 이미지 바이너리 데이터
            
        TODO: 썸네일 생성 로직 구현
        """
        pass
    
    def validate_image(self, image: bytes) -> bool:
        """
        이미지 유효성 검사
        
        Args:
            image: 이미지 바이너리 데이터
            
        Returns:
            유효성 여부
            
        TODO: 검증 로직 구현
        """
        pass

