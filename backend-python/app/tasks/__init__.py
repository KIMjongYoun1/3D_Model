# Celery 비동기 작업 (데이터 시각화 관련 작업 추가 시 사용)
from .celery_app import celery_app

__all__ = ["celery_app"]
