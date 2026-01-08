"""
보안 미들웨어
- 위변조/탈취 방지
- Rate Limiting
- 보안 헤더 설정
"""
from fastapi import Request, Response
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.responses import JSONResponse
import time
from collections import defaultdict
from typing import Dict, Tuple

# Rate Limiting을 위한 저장소 (메모리 기반)
# 프로덕션에서는 Redis 사용 권장
_rate_limit_store: Dict[str, list] = defaultdict(list)


class SecurityHeadersMiddleware(BaseHTTPMiddleware):
    """
    보안 헤더 미들웨어
    
    역할:
    - XSS 공격 방지
    - 클릭재킹 방지
    - MIME 타입 스니핑 방지
    """
    
    async def dispatch(self, request: Request, call_next):
        response = await call_next(request)
        
        # 보안 헤더 추가
        response.headers["X-Content-Type-Options"] = "nosniff"  # MIME 타입 스니핑 방지
        response.headers["X-Frame-Options"] = "DENY"  # 클릭재킹 방지
        response.headers["X-XSS-Protection"] = "1; mode=block"  # XSS 방지
        response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"  # HTTPS 강제
        response.headers["Content-Security-Policy"] = "default-src 'self'"  # CSP
        
        return response


class RateLimitMiddleware(BaseHTTPMiddleware):
    """
    Rate Limiting 미들웨어
    
    역할:
    - 무차별 대입 공격 방지
    - DDoS 공격 방지
    - API 남용 방지
    
    ⚠️ 프로덕션에서는 Redis 사용 권장
    """
    
    def __init__(self, app, requests_per_minute: int = 60):
        super().__init__(app)
        self.requests_per_minute = requests_per_minute
    
    async def dispatch(self, request: Request, call_next):
        # 클라이언트 IP 추출
        client_ip = request.client.host if request.client else "unknown"
        
        # 현재 시간
        current_time = time.time()
        
        # 1분 이전 요청 제거
        _rate_limit_store[client_ip] = [
            req_time for req_time in _rate_limit_store[client_ip]
            if current_time - req_time < 60
        ]
        
        # 요청 횟수 확인
        if len(_rate_limit_store[client_ip]) >= self.requests_per_minute:
            return JSONResponse(
                status_code=429,
                content={
                    "error": "Too Many Requests",
                    "message": "요청 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."
                }
            )
        
        # 요청 시간 기록
        _rate_limit_store[client_ip].append(current_time)
        
        # 다음 미들웨어 실행
        response = await call_next(request)
        
        # Rate Limit 헤더 추가
        remaining = self.requests_per_minute - len(_rate_limit_store[client_ip])
        response.headers["X-RateLimit-Limit"] = str(self.requests_per_minute)
        response.headers["X-RateLimit-Remaining"] = str(remaining)
        
        return response


class LoginRateLimitMiddleware(BaseHTTPMiddleware):
    """
    로그인 전용 Rate Limiting
    
    ⚠️ 주의: 로그인은 Java Backend에서 처리
    - 이 미들웨어는 Python Backend의 AI API 보호용
    - 실제 로그인 Rate Limiting은 Java Backend에 있음
    
    역할:
    - AI API 무차별 호출 방지
    - 계정 잠금 기능
    """
    
    def __init__(self, app, max_attempts: int = 5, lockout_minutes: int = 15):
        super().__init__(app)
        self.max_attempts = max_attempts
        self.lockout_minutes = lockout_minutes
        self._failed_attempts: Dict[str, list] = defaultdict(list)
        self._locked_accounts: Dict[str, float] = {}
    
    async def dispatch(self, request: Request, call_next):
        # ⚠️ 주의: 로그인은 Java Backend에서 처리
        # Python Backend에는 로그인 엔드포인트가 없음
        # 이 미들웨어는 AI API 보호용
        if "/api/v1/tryon" not in request.url.path:
            return await call_next(request)
        
        # 이메일 추출 (요청 바디에서)
        # 실제로는 요청 바디를 파싱해야 함
        client_ip = request.client.host if request.client else "unknown"
        current_time = time.time()
        
        # 계정 잠금 확인
        if client_ip in self._locked_accounts:
            lockout_until = self._locked_accounts[client_ip]
            if current_time < lockout_until:
                remaining_minutes = int((lockout_until - current_time) / 60) + 1
                return JSONResponse(
                    status_code=429,
                    content={
                        "error": "Account Locked",
                        "message": f"너무 많은 로그인 시도로 인해 {remaining_minutes}분간 계정이 잠금되었습니다."
                    }
                )
            else:
                # 잠금 해제
                del self._locked_accounts[client_ip]
                self._failed_attempts[client_ip] = []
        
        # 요청 처리
        response = await call_next(request)
        
        # 로그인 실패 시 기록
        if response.status_code == 401:
            self._failed_attempts[client_ip].append(current_time)
            
            # 15분 이내 실패 횟수 확인
            recent_failures = [
                fail_time for fail_time in self._failed_attempts[client_ip]
                if current_time - fail_time < (self.lockout_minutes * 60)
            ]
            self._failed_attempts[client_ip] = recent_failures
            
            # 최대 시도 횟수 초과 시 계정 잠금
            if len(recent_failures) >= self.max_attempts:
                self._locked_accounts[client_ip] = current_time + (self.lockout_minutes * 60)
                return JSONResponse(
                    status_code=429,
                    content={
                        "error": "Account Locked",
                        "message": f"{self.max_attempts}회 이상 로그인 실패로 {self.lockout_minutes}분간 계정이 잠금되었습니다."
                    }
                )
        
        # 로그인 성공 시 실패 기록 초기화
        if response.status_code == 200:
            self._failed_attempts[client_ip] = []
        
        return response

