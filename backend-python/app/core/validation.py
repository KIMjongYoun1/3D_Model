"""
입력 검증 유틸리티
- 위변조 방지
- SQL Injection 방지
- XSS 방지
"""
import re
from typing import Optional, Tuple


def validate_email(email: str) -> bool:
    """
    이메일 형식 검증
    
    ⭐ 위변조 방지: 정규식으로 형식 강제
    """
    pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    return bool(re.match(pattern, email))


def sanitize_input(text: str) -> str:
    """
    입력값 정제 (XSS 방지)
    
    ⭐ 위변조 방지: 특수문자 이스케이프
    
    Args:
        text: 입력 문자열
        
    Returns:
        정제된 문자열
    """
    # HTML 태그 제거
    text = re.sub(r'<[^>]+>', '', text)
    # 특수문자 이스케이프
    text = text.replace('&', '&amp;')
    text = text.replace('<', '&lt;')
    text = text.replace('>', '&gt;')
    text = text.replace('"', '&quot;')
    text = text.replace("'", '&#x27;')
    return text


def validate_password_strength(password: str) -> Tuple[bool, Optional[str]]:
    """
    비밀번호 강도 검증
    
    ⭐ 위변조 방지: 비밀번호 정책 강제
    
    Args:
        password: 비밀번호
        
    Returns:
        (유효 여부, 오류 메시지)
    """
    if len(password) < 8:
        return False, "비밀번호는 8자 이상이어야 합니다"
    
    if not re.search(r'[A-Z]', password):
        return False, "비밀번호에 대문자가 포함되어야 합니다"
    
    if not re.search(r'[a-z]', password):
        return False, "비밀번호에 소문자가 포함되어야 합니다"
    
    if not re.search(r'\d', password):
        return False, "비밀번호에 숫자가 포함되어야 합니다"
    
    if not re.search(r'[!@#$%^&*(),.?":{}|<>]', password):
        return False, "비밀번호에 특수문자가 포함되어야 합니다"
    
    return True, None


def validate_uuid(uuid_string: str) -> bool:
    """
    UUID 형식 검증
    
    ⭐ 위변조 방지: UUID 형식 강제
    """
    pattern = r'^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'
    return bool(re.match(pattern, uuid_string.lower()))

