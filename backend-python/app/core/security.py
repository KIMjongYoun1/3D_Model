"""
보안 관련 유틸리티 (Python Backend)
- JWT 토큰 검증만 (토큰 발급은 Java Backend에서)
- 위변조 방지

⚠️ 주의: 로그인/회원가입은 Java Backend에서 처리
- Python Backend는 Java에서 발급한 JWT 토큰을 검증만 함
- 비밀번호 해싱, 로그인 로직은 Java Backend에 있음
"""
from passlib.context import CryptContext
from datetime import datetime, timedelta
from typing import Optional
from jose import JWTError, jwt
from app.core.config import settings

# ⚠️ 주의: 비밀번호 해싱과 토큰 발급은 Java Backend에서 처리
# Python Backend는 JWT 토큰 검증만 수행


def decode_access_token(token: str) -> Optional[dict]:
    """
    JWT 액세스 토큰 생성
    
    ⭐ 위변조 방지:
    - HS256 알고리즘으로 서명 생성
    - 비밀키로 서명하여 위조 불가능
    - 만료 시간 포함하여 재사용 방지
    
    알고리즘: HS256 (HMAC-SHA256)
    - 서버에서만 검증 가능 (비밀키 필요)
    - 비밀키로 서명하여 위조 방지
    
    JWT 구조:
    - Header: {"alg": "HS256", "typ": "JWT"}
    - Payload: {"sub": "user_id", "exp": 1234567890, "iat": 1234567890}
    - Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
    
    Args:
        data: 토큰에 포함할 데이터 (예: {"sub": "user_id"})
               - sub: subject (사용자 ID)
               - iat: issued at (발급 시간, 자동 추가)
        expires_delta: 만료 시간 (timedelta 객체)
                      - None이면 settings.jwt_expire_minutes 사용
                      - 예: timedelta(minutes=60) = 60분 후 만료
        
    Returns:
        JWT 토큰 문자열 (예: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    """
    # 데이터 복사 (원본 데이터 변경 방지)
    to_encode = data.copy()
    
    # 발급 시간 추가 (위변조 탐지용)
    to_encode.update({"iat": datetime.utcnow()})
    
    # 만료 시간 설정
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=settings.jwt_expire_minutes)
    
    # Payload에 만료 시간 추가
    to_encode.update({"exp": expire})
    
    # JWT 토큰 생성 (서명 포함)
    # ⭐ 위변조 방지: 비밀키로 서명하여 위조 불가능
    encoded_jwt = jwt.encode(
        to_encode,                    # Payload
        settings.jwt_secret,          # Secret Key (서명용 비밀키)
        algorithm=settings.jwt_algorithm  # 알고리즘 (HS256)
    )
    return encoded_jwt


def decode_access_token(token: str) -> Optional[dict]:
    """
    JWT 토큰 디코딩 및 검증
    
    ⚠️ 역할: Java Backend에서 발급한 JWT 토큰을 검증만 함
    - 토큰 발급은 Java Backend에서 처리
    - Python Backend는 검증만 수행
    
    ⭐ 위변조 방지:
    - 서명 검증: 비밀키로 서명이 올바른지 확인
    - 만료 시간 검증: exp 필드 확인
    - 알고리즘 검증: HS256만 허용
    
    검증 항목:
    - 서명 검증: Secret Key로 서명이 올바른지 확인
    - 만료 시간 검증: exp 필드가 현재 시간보다 이후인지 확인
    - 알고리즘 검증: 지정된 알고리즘(HS256)으로 생성되었는지 확인
    
    Args:
        token: JWT 토큰 문자열 (Java Backend에서 발급)
        
    Returns:
        디코딩된 토큰 데이터 (Payload)
        - 성공: {"sub": "user_id", "exp": 1234567890, ...}
        - 실패: None (만료, 서명 오류, 형식 오류 등)
    """
    try:
        # JWT 토큰 검증 및 디코딩
        # ⭐ 위변조 방지: 서명 검증 자동 수행
        # ⚠️ Java Backend와 같은 JWT_SECRET 사용해야 함
        payload = jwt.decode(
            token,                        # JWT 토큰 문자열
            settings.jwt_secret,          # Secret Key (Java와 동일한 키 사용)
            algorithms=[settings.jwt_algorithm]  # 허용할 알고리즘 (HS256만 허용)
        )
        return payload
    except JWTError:
        # 토큰 검증 실패
        # - 만료된 토큰
        # - 잘못된 서명 (위변조 시도)
        # - 형식 오류
        return None


def get_current_user():
    """
    현재 인증된 사용자 조회 (의존성 함수)
    
    ⭐ 탈취/위변조 방지:
    - Authorization 헤더에서만 토큰 추출
    - 토큰 검증 강화
    - 사용자 존재 여부 확인
    
    TODO: 구현 필요
    1. HTTPBearer로 토큰 추출
    2. decode_access_token으로 토큰 검증
    3. DB에서 사용자 조회
    4. 사용자 반환 또는 예외 발생
    """
    from fastapi import Depends, HTTPException, status
    from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
    from sqlalchemy.orm import Session
    from app.core.database import get_db
    from app.models.user import User
    
    # TODO: 실제 구현 필요
    # 1. HTTPBearer로 토큰 추출
    # 2. decode_access_token으로 토큰 검증
    # 3. DB에서 사용자 조회
    # 4. 사용자 반환 또는 예외 발생
    pass
