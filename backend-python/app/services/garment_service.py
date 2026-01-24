"""
의류 관리 서비스
- 의류 이미지 업로드, DB 저장 및 조회 로직을 담당합니다.
"""
from sqlalchemy.orm import Session
from fastapi import UploadFile
from uuid import UUID
from typing import List, Optional

from app.models.garment import Garment
from app.models.user import User
from app.schemas.garment import GarmentCreate, GarmentUpdate
from app.services.storage_service import storage_service

class GarmentService:
    """
    의류 비즈니스 로직
    - storage_service를 사용하여 파일을 저장하고
    - db 세션을 사용하여 메타데이터를 관리합니다.
    """

    def __init__(self, db: Session):
        self.db = db

    async def upload_garment(
        self, 
        user_id: UUID, 
        file: UploadFile, 
        name: Optional[str] = None,
        category: Optional[str] = None
    ) -> Garment:
        """
        [핵심 로직] 의류 이미지를 업로드하고 DB에 정보를 저장합니다.
        
        1. 파일을 서버에 저장 (StorageService 호출)
        2. 저장된 경로를 포함하여 DB 엔티티 생성
        3. DB에 저장 및 확정(Commit)
        """
        # 1. 물리적 파일 저장 (uploads/garments 폴더에 저장)
        file_url = await storage_service.save_file(file, folder="garments")

        # 2. DB 엔티티 생성 (자바의 new Garment()와 같음)
        db_garment = Garment(
            user_id=user_id,
            name=name or file.filename, # 이름이 없으면 파일명 사용
            category=category,
            image_url=file_url
        )

        # 3. DB 저장 및 커밋
        self.db.add(db_garment)
        self.db.commit()
        self.db.refresh(db_garment) # 저장된 후 생성된 ID 등을 다시 읽어옴

        return db_garment

    def get_user_garments(self, user_id: UUID) -> List[Garment]:
        """특정 사용자가 등록한 모든 옷 목록을 조회합니다."""
        return self.db.query(Garment).filter(Garment.user_id == user_id).all()

    def get_garment(self, garment_id: UUID) -> Optional[Garment]:
        """특정 옷의 상세 정보를 조회합니다."""
        return self.db.query(Garment).filter(Garment.id == garment_id).first()

    def delete_garment(self, garment_id: UUID):
        """옷 정보를 삭제하고 물리 파일도 함께 삭제합니다."""
        garment = self.get_garment(garment_id)
        if garment:
            # 1. 물리적 파일 삭제
            storage_service.delete_file(garment.image_url)
            # 2. DB 데이터 삭제
            self.db.delete(garment)
            self.db.commit()

