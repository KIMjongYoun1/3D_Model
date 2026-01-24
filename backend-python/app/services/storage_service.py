"""
Storage 서비스
- 로컬 파일 시스템 또는 클라우드 스토리지에 파일을 저장하고 관리합니다.
"""
import os
import uuid
import shutil
from datetime import datetime
from fastapi import UploadFile
from app.core.config import settings

class StorageService:
    """
    파일 저장 서비스
    
    주요 기능:
    - 파일을 지정된 경로에 저장합니다.
    - 웹에서 접근 가능한 URL 경로를 생성합니다.
    - 파일 확장자를 검증합니다.
    """

    def __init__(self):
        # 파일을 저장할 루트 디렉토리 설정 (app/core/config.py에서 설정한 경로)
        self.upload_dir = settings.storage_path
        
        # 서버 시작 시 저장 폴더가 없으면 자동으로 생성 (자바의 mkdirs()와 같음)
        if not os.path.exists(self.upload_dir):
            os.makedirs(self.upload_dir, exist_ok=True)

    async def save_file(self, file: UploadFile, folder: str = "uploads") -> str:
        """
        [핵심 로직] 파일을 서버에 저장합니다.
        
        Args:
            file: 사용자가 업로드한 파일 객체 (FastAPI 제공)
            folder: 저장할 하위 폴더 이름 (예: garments, avatars)
            
        Returns:
            str: 저장된 파일의 접근 URL 또는 상대 경로
        """
        # 1. 저장할 하위 디렉토리 경로 생성
        target_dir = os.path.join(self.upload_dir, folder)
        os.makedirs(target_dir, exist_ok=True)

        # 2. 파일 이름 보안 처리 (고유한 ID 부여)
        # 원본 이름에서 확장자만 추출 (예: .jpg)
        extension = os.path.splitext(file.filename)[1]
        # 고유한 파일명 생성 (예: 550e8400-e29b-41d4-a716.jpg)
        unique_filename = f"{uuid.uuid4()}{extension}"
        
        # 3. 전체 물리적 경로 결정
        file_path = os.path.join(target_dir, unique_filename)

        # 4. 파일 데이터 쓰기 (자바의 FileOutputStream 작업)
        # 'wb'는 Write Binary의 약자입니다.
        with open(file_path, "wb") as buffer:
            # 파일 내용을 버퍼에 복사
            shutil.copyfileobj(file.file, buffer)

        # 5. 접근 가능한 상대 경로 반환 (DB 저장용)
        # 예: /storage/garments/unique_id.jpg
        return f"/{folder}/{unique_filename}"

    def delete_file(self, file_path: str):
        """저장된 파일을 삭제합니다."""
        # 저장 경로에서 설정된 루트 경로를 합쳐 물리적 위치 파악
        full_path = os.path.join(self.upload_dir, file_path.lstrip("/"))
        if os.path.exists(full_path):
            os.remove(full_path)

# 전역 서비스 인스턴스 생성
storage_service = StorageService()
