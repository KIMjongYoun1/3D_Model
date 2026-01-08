"""
Celery 애플리케이션 설정
- 비동기 작업 큐 관리
"""
from celery import Celery
from app.core.config import settings

# Celery 앱 생성
celery_app = Celery(
    "virtual_tryon",
    broker=settings.redis_url,
    backend=settings.redis_url
)

# Celery 설정
celery_app.conf.update(
    task_serializer='json',
    accept_content=['json'],
    result_serializer='json',
    timezone='UTC',
    enable_utc=True,
)

