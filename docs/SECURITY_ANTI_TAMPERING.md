# 🛡️ 위변조/탈취 방지 가이드

> **버전**: v1.0  
> **작성일**: 2025.12.06  
> **대상**: Virtual Try-On 프로젝트 보안 강화

---

## 📋 목차

1. [JWT 토큰 위변조 방지](#jwt-토큰-위변조-방지)
2. [토큰 탈취 방지](#토큰-탈취-방지)
3. [Rate Limiting](#rate-limiting)
4. [입력 검증](#입력-검증)
5. [보안 헤더](#보안-헤더)

---

## JWT 토큰 위변조 방지

### 1. 서명 검증 (HS256)

```python
# ✅ 위변조 방지: 비밀키로 서명 검증
def decode_access_token(token: str):
    payload = jwt.decode(
        token,
        settings.jwt_secret,  # 비밀키로 서명 검증
        algorithms=["HS256"]  # HS256만 허용
    )
    # 위변조 시도 시 JWTError 발생
```

**동작 원리:**
- JWT는 `Header.Payload.Signature` 구조
- Signature는 비밀키로 생성됨
- 위변조 시 서명이 맞지 않아 검증 실패

### 2. 만료 시간 검증

```python
# ✅ 위변조 방지: 만료 시간 포함
def create_access_token(data: dict):
    expire = datetime.utcnow() + timedelta(minutes=60)
    to_encode.update({"exp": expire})  # 만료 시간 포함
    # 만료된 토큰은 자동으로 거부됨
```

### 3. 발급 시간 검증 (선택)

```python
# ✅ 위변조 방지: 발급 시간 포함
to_encode.update({"iat": datetime.utcnow()})  # 발급 시간
# 너무 오래된 토큰 거부 가능
```

---

## 토큰 탈취 방지

### 1. HttpOnly Cookie 사용 (권장)

```python
# ✅ 탈취 방지: JavaScript 접근 불가
from fastapi.responses import Response

@router.post("/auth/login")
async def login(...):
    token = create_access_token({"sub": str(user.id)})
    
    response = Response(content={"message": "Login successful"})
    response.set_cookie(
        key="access_token",
        value=token,
        httponly=True,  # ✅ JavaScript 접근 불가 (XSS 방지)
        secure=True,    # ✅ HTTPS에서만 전송
        samesite="strict"  # ✅ CSRF 방지
    )
    return response
```

**장점:**
- XSS 공격으로 토큰 탈취 불가능
- JavaScript에서 접근 불가
- 브라우저가 자동으로 관리

### 2. localStorage 사용 시 주의사항

```typescript
// ⚠️ XSS 공격에 취약
localStorage.setItem('token', token)  // JavaScript로 접근 가능

// ✅ 대안: httpOnly cookie 사용
// 또는 XSS 방지 조치 필수
```

### 3. HTTPS 강제

```python
# ✅ 탈취 방지: 네트워크 전송 시 암호화
# 프로덕션에서는 반드시 HTTPS 사용
# 토큰이 네트워크를 통해 전송되므로 암호화 필요
```

---

## Rate Limiting

### 1. 일반 API Rate Limiting

```python
# ✅ 위변조/탈취 시도 제한
from app.core.middleware import RateLimitMiddleware

app.add_middleware(RateLimitMiddleware, requests_per_minute=60)
```

**역할:**
- DDoS 공격 방지
- API 남용 방지
- 서버 부하 방지

### 2. 로그인 Rate Limiting

```python
# ✅ 무차별 대입 공격 방지
from app.core.middleware import LoginRateLimitMiddleware

app.add_middleware(
    LoginRateLimitMiddleware,
    max_attempts=5,        # 최대 5회 시도
    lockout_minutes=15     # 15분간 계정 잠금
)
```

**동작:**
1. 5회 이상 로그인 실패 시
2. 15분간 계정 잠금
3. 탈취 시도 차단

---

## 입력 검증

### 1. Pydantic 검증

```python
# ✅ 위변조 방지: 입력 형식 강제
from pydantic import BaseModel, EmailStr, Field, validator

class LoginRequest(BaseModel):
    email: EmailStr  # 이메일 형식 강제
    password: str = Field(..., min_length=8)  # 최소 길이 강제
    
    @validator('email')
    def validate_email(cls, v):
        # 추가 검증 로직
        return v
```

### 2. SQL Injection 방지

```python
# ✅ 위변조 방지: ORM 사용
# ❌ 위험: Raw SQL
query = f"SELECT * FROM users WHERE email = '{email}'"

# ✅ 안전: SQLAlchemy ORM
user = db.query(User).filter(User.email == email).first()
```

### 3. XSS 방지

```python
# ✅ 위변조 방지: 입력 정제
from app.core.validation import sanitize_input

user_input = sanitize_input(user_input)
# HTML 태그 제거, 특수문자 이스케이프
```

---

## 보안 헤더

### 1. Security Headers

```python
# ✅ 위변조/탈취 방지: 보안 헤더 설정
from app.core.middleware import SecurityHeadersMiddleware

app.add_middleware(SecurityHeadersMiddleware)
```

**설정되는 헤더:**
- `X-Content-Type-Options: nosniff` - MIME 타입 스니핑 방지
- `X-Frame-Options: DENY` - 클릭재킹 방지
- `X-XSS-Protection: 1; mode=block` - XSS 방지
- `Strict-Transport-Security` - HTTPS 강제
- `Content-Security-Policy` - CSP

### 2. CORS 설정

```python
# ✅ 탈취 방지: 허용 도메인 제한
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],  # 특정 도메인만 허용
    allow_credentials=True,
    allow_methods=["GET", "POST"],  # 필요한 메서드만 허용
    allow_headers=["Authorization", "Content-Type"]  # 필요한 헤더만 허용
)
```

---

## 전체 보안 흐름

### 로그인 시

```
1. 사용자 입력 (이메일/비밀번호)
   ↓
2. Rate Limiting 체크 (5회 제한)
   ↓
3. 입력 검증 (Pydantic)
   ↓
4. 비밀번호 검증 (BCrypt)
   ↓
5. JWT 토큰 발급 (서명 포함)
   ↓
6. HttpOnly Cookie로 전송 (XSS 방지)
   ↓
7. 보안 헤더 추가
```

### API 요청 시

```
1. HttpOnly Cookie에서 토큰 추출
   ↓
2. Rate Limiting 체크
   ↓
3. JWT 서명 검증 (위변조 확인)
   ↓
4. 만료 시간 검증
   ↓
5. 사용자 조회
   ↓
6. 요청 처리
```

---

## 보안 체크리스트

### 위변조 방지
- [x] JWT 서명 검증 (HS256)
- [x] 만료 시간 검증
- [x] 입력 검증 (Pydantic)
- [x] SQL Injection 방지 (ORM)
- [x] XSS 방지 (입력 정제)

### 탈취 방지
- [x] HttpOnly Cookie 사용
- [x] HTTPS 강제
- [x] CORS 설정
- [x] 보안 헤더 설정
- [x] Rate Limiting

### 공격 방지
- [x] 무차별 대입 공격 방지 (로그인 Rate Limit)
- [x] DDoS 방지 (일반 Rate Limit)
- [x] 클릭재킹 방지 (X-Frame-Options)
- [x] MIME 스니핑 방지 (X-Content-Type-Options)

---

## 요약

### 위변조 방지
1. **JWT 서명 검증** - 비밀키로 서명 확인
2. **입력 검증** - Pydantic으로 형식 강제
3. **SQL Injection 방지** - ORM 사용

### 탈취 방지
1. **HttpOnly Cookie** - JavaScript 접근 불가
2. **HTTPS 강제** - 네트워크 암호화
3. **CORS 설정** - 허용 도메인 제한

### 공격 방지
1. **Rate Limiting** - 무차별 대입/DDoS 방지
2. **보안 헤더** - XSS/클릭재킹 방지
3. **계정 잠금** - 반복 시도 차단

---

*이 문서는 위변조 및 탈취 방지를 위한 보안 조치를 정의합니다.*

