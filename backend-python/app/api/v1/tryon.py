"""
Try-On API 엔드포인트
"""
from fastapi import APIRouter, Depends, HTTPException
from uuid import UUID
from typing import List
from app.api.v1.dependencies import get_tryon_service
from app.services.tryon_service import TryOnService
from app.schemas.tryon import TryOnRequest, TryOnResponse

router = APIRouter(prefix="/tryon", tags=["Try-On"])


@router.post("/", response_model=TryOnResponse, status_code=201)
async def create_tryon(
    request: TryOnRequest,
    service: TryOnService = Depends(get_tryon_service)
):
    """
    Try-On 생성
    
    역할:
    - Try-On 요청을 받아서 처리
    - AI 서비스를 통해 Try-On 실행
    - 결과를 데이터베이스에 저장
    
    TODO: 구현 필요
    """
    result = await service.create_tryon(request)
    return result


@router.get("/{result_id}", response_model=TryOnResponse)
async def get_tryon(
    result_id: UUID,
    service: TryOnService = Depends(get_tryon_service)
):
    """
    Try-On 결과 조회
    
    역할:
    - 특정 Try-On 결과 조회
    
    TODO: 구현 필요
    """
    result = service.get_tryon(result_id)
    if not result:
        raise HTTPException(status_code=404, detail="결과를 찾을 수 없습니다.")
    return result


@router.get("/", response_model=List[TryOnResponse])
async def list_tryon_results(
    service: TryOnService = Depends(get_tryon_service)
):
    """
    Try-On 결과 목록 조회
    
    역할:
    - 현재 사용자의 Try-On 결과 목록 조회
    
    TODO: 구현 필요
    """
    results = service.list_tryon_results()
    return results

