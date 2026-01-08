"""
AI 처리 비동기 작업 (Celery)
- 오래 걸리는 AI 작업을 백그라운드에서 처리
"""
from app.tasks.celery_app import celery_app
from app.services.ai_service import AIService


@celery_app.task(name="process_tryon")
def process_tryon_async(
    person_image_url: str,
    garment_image_url: str
) -> str:
    """
    Try-On 비동기 처리
    
    역할:
    - Try-On 작업을 백그라운드에서 처리
    - 오래 걸리는 AI 처리를 비동기로 실행
    
    Args:
        person_image_url: 사람 이미지 URL
        garment_image_url: 의상 이미지 URL
        
    Returns:
        결과 이미지 URL
        
    TODO: 비동기 처리 로직 구현
    """
    ai_service = AIService()
    result = ai_service.run_tryon(person_image_url, garment_image_url)
    return result


@celery_app.task(name="segment_garment")
def segment_garment_async(image_url: str) -> str:
    """
    의상 세그멘테이션 비동기 처리
    
    역할:
    - 의상 세그멘테이션 작업을 백그라운드에서 처리
    
    Args:
        image_url: 의상 이미지 URL
        
    Returns:
        세그멘테이션된 이미지 URL
        
    TODO: 비동기 처리 로직 구현
    """
    ai_service = AIService()
    result = ai_service.segment_garment(image_url)
    return result

