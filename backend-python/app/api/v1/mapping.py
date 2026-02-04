"""
매핑 API 라우터 (안정화 버전)
"""
from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File
from sqlalchemy.orm import Session
from typing import List, Optional, Any
from uuid import UUID
import json

from app.core.database import get_db
from app.schemas.mapping import MappingCreate, MappingResponse
from app.models.mapping import MappingData
from app.services.mapping_service import mapping_orchestrator
from app.core.document_processor import document_processor

FAKE_USER_ID = UUID("550e8400-e29b-41d4-a716-446655440000")
router = APIRouter(prefix="/mapping", tags=["Mapping"])

@router.post("", response_model=MappingResponse, status_code=status.HTTP_201_CREATED)
async def create_mapping(
    request: MappingCreate,
    db: Session = Depends(get_db)
):
    try:
        # 1. 3D 좌표 데이터 생성
        mapping_data = await mapping_orchestrator.process_data_to_3d(
            request.data_type, 
            request.raw_data
        )

        # 2. DB 객체 생성
        db_mapping = MappingData(
            user_id=FAKE_USER_ID,
            data_type=request.data_type,
            raw_data=request.raw_data,
            mapping_data=mapping_data
        )
        
        # 3. 명시적 커밋 및 리프레시
        db.add(db_mapping)
        db.commit()
        db.refresh(db_mapping)
        
        return db_mapping
    except Exception as e:
        db.rollback()
        print(f"❌ API 에러: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/upload", response_model=MappingResponse, status_code=status.HTTP_201_CREATED)
async def create_mapping_from_file(
    file: UploadFile = File(...),
    db: Session = Depends(get_db)
):
    try:
        content = await file.read()
        extracted_data = document_processor.extract_data(content, file.filename)
        
        if not extracted_data:
            raise HTTPException(status_code=400, detail="데이터 추출 실패")

        mapping_data = await mapping_orchestrator.process_data_to_3d(
            "file_analysis", 
            extracted_data
        )

        db_mapping = MappingData(
            user_id=FAKE_USER_ID,
            data_type=f"file ({file.filename})",
            raw_data={"filename": file.filename},
            mapping_data=mapping_data
        )
        
        db.add(db_mapping)
        db.commit()
        db.refresh(db_mapping)
        
        return db_mapping
    except Exception as e:
        db.rollback()
        print(f"❌ 업로드 에러: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@router.get("", response_model=List[MappingResponse])
def list_mappings(db: Session = Depends(get_db)):
    return db.query(MappingData).filter(MappingData.user_id == FAKE_USER_ID).order_by(MappingData.created_at.desc()).all()
