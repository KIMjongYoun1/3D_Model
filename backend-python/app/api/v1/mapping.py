"""
매핑 API 라우터 (DB 분리 버전)

DB 접근 구조:
- db (AI DB): visualization_data 저장, correlation_rules 조회
- service_db (Service DB): knowledge_base RAG 조회 (READ ONLY)
"""
from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File
from sqlalchemy.orm import Session
from typing import List, Optional, Any
from uuid import UUID
import json

from app.core.database import get_db, get_service_db
from app.schemas.mapping import MappingCreate, MappingResponse
from app.models.mapping import MappingData
from app.services.mapping_service import mapping_orchestrator
from app.core.document_processor import document_processor

FAKE_USER_ID = UUID("550e8400-e29b-41d4-a716-446655440000")
router = APIRouter(prefix="/mapping", tags=["Mapping"])

@router.post("", response_model=MappingResponse, status_code=status.HTTP_201_CREATED)
async def create_mapping(
    request: MappingCreate,
    db: Session = Depends(get_db),
    service_db: Session = Depends(get_service_db)
):
    try:
        # 1. 시각화 옵션에 카테고리 정보 병합
        options = request.options or {}
        if request.main_category: options["main_category"] = request.main_category
        if request.sub_category: options["sub_category"] = request.sub_category

        # 2. 3D 좌표 데이터 생성 (service_db로 knowledge_base RAG 조회)
        mapping_data = await mapping_orchestrator.process_data_to_3d(
            request.data_type, 
            request.raw_data,
            db=db,
            service_db=service_db,
            options=options
        )

        # 3. AI DB에 결과 저장
        db_mapping = MappingData(
            user_id=FAKE_USER_ID,
            data_type=request.data_type,
            raw_data=request.raw_data,
            mapping_data=mapping_data,
            category=mapping_data.get("detected_category") or f"{request.main_category}_{request.sub_category}",
            model_used=mapping_data.get("model_tier"),
            processing_time_ms={"total": mapping_data.get("processing_time_ms")}
        )
        
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
    main_category: Optional[str] = None,
    sub_category: Optional[str] = None,
    render_type: Optional[str] = "auto",
    db: Session = Depends(get_db),
    service_db: Session = Depends(get_service_db)
):
    try:
        content = await file.read()
        extracted_data = document_processor.extract_data(content, file.filename)
        
        if not extracted_data:
            raise HTTPException(status_code=400, detail="데이터 추출 실패")

        # 시각화 옵션 구성 (render_type: auto | diagram | settlement)
        options = {
            "filename": file.filename,
            "main_category": main_category,
            "sub_category": sub_category,
            "render_type": render_type or "auto",
        }

        # 3D 좌표 데이터 생성 (service_db로 knowledge_base RAG 조회)
        mapping_data = await mapping_orchestrator.process_data_to_3d(
            "file_analysis", 
            extracted_data,
            db=db,
            service_db=service_db,
            options=options
        )

        # AI DB에 결과 저장
        db_mapping = MappingData(
            user_id=FAKE_USER_ID,
            data_type=f"file ({file.filename})",
            raw_data={"filename": file.filename},
            mapping_data=mapping_data,
            category=mapping_data.get("detected_category") or f"{main_category}_{sub_category}",
            model_used=mapping_data.get("model_tier"),
            processing_time_ms={"total": mapping_data.get("processing_time_ms")}
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
