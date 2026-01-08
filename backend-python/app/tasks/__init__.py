# Celery 비동기 작업
# - 오래 걸리는 작업을 백그라운드에서 처리

from .celery_app import celery_app
from .ai_tasks import process_tryon_async, segment_garment_async

__all__ = ["celery_app", "process_tryon_async", "segment_garment_async"]

