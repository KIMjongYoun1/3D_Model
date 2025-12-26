"""
보안 관련 유틸리티
- 비밀번호 해싱 (BCrypt)
- JWT 토큰 생성/검증
"""
from passlib.context import CryptContext
from datetime import datetime, timedelta
from typing import Optional
from jose import JWTError, jwt
from app.core.config import settings

# 비밀번호 해싱 컨텍스트
# - BCrypt 알고리즘 사용 (업계 표준)
# - 자동으로 salt 생성 및 적용
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def hash_password(password: str) -> str:
    """
    비밀번호 해싱
    
    알고리즘: BCrypt
    - salt 자동 생성
    - 단방향 해싱 (복호화 불가능)
    - rainbow table 공격 방지
    
    Args:
        password: 평문 비밀번호
        
    Returns:
        해시된 비밀번호
    """
    return pwd_context.hash(password)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """
    비밀번호 검증
    
    Args:
        plain_password: 입력받은 평문 비밀번호
        hashed_password: 저장된 해시된 비밀번호
        
    Returns:
        비밀번호 일치 여부
    """
    return pwd_context.verify(plain_password, hashed_password)


def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    """
    JWT 액세스 토큰 생성
    
    ⭐ 직접 구현 필요: 이 함수의 로직은 직접 구현해야 합니다.
    - JWT 토큰 구조 이해
    - 만료 시간 설정 로직
    - 서명 생성 로직
    
    구현 힌트:
    1. JWT 구조: Header.Payload.Signature
    2. Payload에 exp(만료 시간) 필드 추가
    3. jwt.encode()로 서명 생성
    
    알고리즘: HS256 (HMAC-SHA256)
    - 서버에서만 검증 가능 (비밀키 필요)
    - 비밀키로 서명하여 위조 방지
    
    JWT 구조:
    - Header: {"alg": "HS256", "typ": "JWT"}
    - Payload: {"sub": "user_id", "exp": 1234567890}
    - Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
    
    Args:
        data: 토큰에 포함할 데이터 (예: {"sub": "user_id"})
               - sub: subject (사용자 ID)
               - iat: issued at (발급 시간, 선택)
        expires_delta: 만료 시간 (timedelta 객체)
                      - None이면 settings.jwt_expire_minutes 사용
                      - 예: timedelta(minutes=60) = 60분 후 만료
        
    Returns:
        JWT 토큰 문자열 (예: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    
    사용 예시:
        token = create_access_token({"sub": "user123"})
        # 만료 시간: 기본값 (60분)
        
        token = create_access_token(
            {"sub": "user123"},
            expires_delta=timedelta(hours=24)
        )
        # 만료 시간: 24시간
    """
    # 데이터 복사 (원본 데이터 변경 방지)
    # - dict.copy()로 얕은 복사
    # - to_encode에 exp 필드를 추가하므로 원본 보호
    to_encode = data.copy()
    
    # 만료 시간 설정
    # - expires_delta가 제공되면 사용
    # - 없으면 설정 파일의 jwt_expire_minutes 사용 (기본 60분)
    if expires_delta:
        # 사용자 지정 만료 시간
        expire = datetime.utcnow() + expires_delta
    else:
        # 설정 파일의 기본 만료 시간 사용
        expire = datetime.utcnow() + timedelta(minutes=settings.jwt_expire_minutes)
    
    # Payload에 만료 시간 추가
    # - exp: expiration time (Unix timestamp)
    # - JWT 표준 필드
    to_encode.update({"exp": expire})
    
    # JWT 토큰 생성
    # - jwt.encode(): Payload와 Secret을 사용하여 서명 생성
    # - algorithm: HS256 (HMAC-SHA256)
    # - 반환값: Base64 URL 인코딩된 문자열 (Header.Payload.Signature)
    encoded_jwt = jwt.encode(
        to_encode,                    # Payload (토큰에 포함할 데이터)
        settings.jwt_secret,          # Secret Key (서명용 비밀키)
        algorithm=settings.jwt_algorithm  # 알고리즘 (HS256)
    )
    return encoded_jwt


def decode_access_token(token: str) -> Optional[dict]:
    """
    JWT 토큰 디코딩 및 검증
    
    ⭐ 직접 구현 필요: 이 함수의 로직은 직접 구현해야 합니다.
    - 토큰 검증 로직 이해
    - 만료 시간 검증
    - 서명 검증
    
    구현 힌트:
    1. jwt.decode()로 토큰 검증 및 디코딩
    2. 만료 시간 자동 검증 (exp 필드)
    3. 서명 자동 검증 (secret key로)
    4. 검증 실패 시 예외 발생 (JWTError)
    
    검증 항목:
    - 서명 검증: Secret Key로 서명이 올바른지 확인
    - 만료 시간 검증: exp 필드가 현재 시간보다 이후인지 확인
    - 알고리즘 검증: 지정된 알고리즘(HS256)으로 생성되었는지 확인
    
    Args:
        token: JWT 토큰 문자열
               - 형식: "Header.Payload.Signature"
               - 예: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        
    Returns:
        디코딩된 토큰 데이터 (Payload)
        - 성공: {"sub": "user_id", "exp": 1234567890, ...}
        - 실패: None (만료, 서명 오류, 형식 오류 등)
    
    예외 처리:
        - JWTError: 토큰 검증 실패 (만료, 서명 오류 등)
        - 예외 발생 시 None 반환 (안전한 실패 처리)
    
    사용 예시:
        payload = decode_access_token(token)
        if payload:
            user_id = payload.get("sub")
        else:
            # 토큰이 유효하지 않음
            raise UnauthorizedException()
    """
    try:
        # JWT 토큰 검증 및 디코딩
        # - jwt.decode(): 토큰을 검증하고 Payload 반환
        # - 자동 검증 항목:
        #   1. 서명 검증 (secret key로)
        #   2. 만료 시간 검증 (exp 필드)
        #   3. 알고리즘 검증 (HS256)
        # - 검증 실패 시 JWTError 예외 발생
        payload = jwt.decode(
            token,                        # JWT 토큰 문자열
            settings.jwt_secret,          # Secret Key (서명 검증용)
            algorithms=[settings.jwt_algorithm]  # 허용할 알고리즘 (HS256만 허용)
        )
        # 검증 성공: Payload 반환
        # - Payload에는 원래 encode() 시 전달한 데이터가 포함됨
        # - 예: {"sub": "user_id", "exp": 1234567890}
        return payload
    except JWTError:
        # 토큰 검증 실패
        # - 만료된 토큰
        # - 잘못된 서명
        # - 형식 오류
        # - None 반환으로 안전하게 처리
        return None

