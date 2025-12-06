# 🔐 Python/FastAPI 보안 가이드

> **버전**: v0.1  
> **작성일**: 2025.11.30  
> **대상**: Virtual Try-On 프로젝트 보안 설계

---

## 📊 Spring Boot vs FastAPI 보안 비교

| 보안 영역 | Spring Boot (Java) | FastAPI (Python) |
|-----------|-------------------|------------------|
| **인증 토큰** | Spring Security + JWT | python-jose + JWT |
| **OAuth2** | Spring Security OAuth2 | FastAPI OAuth2 (내장) |
| **비밀번호 해싱** | BCryptPasswordEncoder | passlib + bcrypt |
| **입력 검증** | @Valid, Bean Validation | Pydantic (내장) |
| **SQL Injection** | JPA/Hibernate | SQLAlchemy ORM |
| **CORS** | @CrossOrigin | FastAPI CORSMiddleware |
| **Rate Limiting** | Bucket4j | slowapi |

---

## 1️⃣ 인증 (Authentication)

### JWT (JSON Web Token) - 가장 권장

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  JWT 인증 흐름                                                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  [로그인 요청]                                                               │
│  Client ──────────────────▶ Server                                          │
│          email + password                                                   │
│                                                                              │
│  [토큰 발급]                                                                 │
│  Client ◀────────────────── Server                                          │
│          access_token + refresh_token                                       │
│                                                                              │
│  [API 요청]                                                                  │
│  Client ──────────────────▶ Server                                          │
│          Authorization: Bearer {token}                                      │
│                                                                              │
│  [토큰 검증 후 응답]                                                         │
│  Client ◀────────────────── Server                                          │
│          데이터 반환                                                         │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 필요한 라이브러리

```bash
pip install python-jose[cryptography]  # JWT 생성/검증
pip install passlib[bcrypt]            # 비밀번호 해싱
pip install python-multipart           # Form 데이터 처리
```

### FastAPI JWT 구현 예시

```python
from datetime import datetime, timedelta
from jose import JWTError, jwt
from passlib.context import CryptContext
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer

# 설정
SECRET_KEY = "your-secret-key-here"  # 환경변수로 관리!
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

# 비밀번호 해싱
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# OAuth2 스키마
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

# 비밀번호 검증
def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)

# 비밀번호 해싱
def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)

# 토큰 생성
def create_access_token(data: dict, expires_delta: timedelta = None):
    to_encode = data.copy()
    expire = datetime.utcnow() + (expires_delta or timedelta(minutes=15))
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

# 토큰 검증 (의존성 주입)
async def get_current_user(token: str = Depends(oauth2_scheme)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: str = payload.get("sub")
        if user_id is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    
    # DB에서 사용자 조회
    user = await get_user_by_id(user_id)
    if user is None:
        raise credentials_exception
    return user
```

### Spring Boot와 비교

| Spring Boot | FastAPI |
|-------------|---------|
| `@PreAuthorize` | `Depends(get_current_user)` |
| `SecurityContext` | 의존성 주입으로 user 전달 |
| `JwtTokenProvider` | `python-jose` 직접 사용 |

---

## 2️⃣ OAuth2 소셜 로그인

### 지원 방식

```
┌─────────────────────────────────────────────────────────────┐
│  OAuth2 소셜 로그인 옵션                                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  옵션 A: 직접 구현                                           │
│  ├── authlib 라이브러리 사용                                │
│  ├── Google, Kakao, Naver 각각 구현                        │
│  └── 복잡하지만 커스터마이징 가능                           │
│                                                              │
│  옵션 B: Supabase Auth (권장) ⭐                            │
│  ├── 소셜 로그인 원클릭 설정                                │
│  ├── Google, Kakao, GitHub 등 지원                         │
│  └── JWT 토큰 자동 관리                                     │
│                                                              │
│  옵션 C: NextAuth.js (Frontend에서 처리)                    │
│  ├── Next.js 사용 시 최적                                   │
│  └── Backend는 토큰 검증만                                  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 권장: Supabase Auth

```python
# Supabase 사용 시 Backend는 토큰 검증만 하면 됨
from supabase import create_client

supabase = create_client(SUPABASE_URL, SUPABASE_KEY)

async def verify_supabase_token(token: str):
    try:
        user = supabase.auth.get_user(token)
        return user
    except Exception:
        raise HTTPException(status_code=401, detail="Invalid token")
```

---

## 3️⃣ 비밀번호 보안

### 해싱 알고리즘 비교

| 알고리즘 | 보안성 | 속도 | 권장 |
|----------|--------|------|------|
| **bcrypt** | ⭐⭐⭐⭐ | 적당 | ✅ 표준 |
| **argon2** | ⭐⭐⭐⭐⭐ | 느림 | ✅ 최신 권장 |
| scrypt | ⭐⭐⭐⭐ | 느림 | 대안 |
| SHA-256 | ⭐⭐ | 빠름 | ❌ 비밀번호용 부적합 |
| MD5 | ⭐ | 빠름 | ❌ 절대 사용 금지 |

### passlib 사용 (권장)

```python
from passlib.context import CryptContext

# bcrypt 사용
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# 또는 argon2 사용 (더 안전)
# pip install argon2-cffi
pwd_context = CryptContext(schemes=["argon2"], deprecated="auto")

# 해싱
hashed = pwd_context.hash("user_password")
# 결과: $2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.G...

# 검증
is_valid = pwd_context.verify("user_password", hashed)
```

---

## 4️⃣ 입력 검증 (Pydantic)

### SQL Injection, XSS 방지의 첫 단계

```python
from pydantic import BaseModel, EmailStr, Field, validator
import re

class UserCreate(BaseModel):
    email: EmailStr  # 이메일 형식 자동 검증
    password: str = Field(..., min_length=8, max_length=100)
    name: str = Field(..., min_length=2, max_length=50)
    
    @validator('password')
    def password_strength(cls, v):
        if not re.search(r'[A-Z]', v):
            raise ValueError('비밀번호에 대문자가 포함되어야 합니다')
        if not re.search(r'[a-z]', v):
            raise ValueError('비밀번호에 소문자가 포함되어야 합니다')
        if not re.search(r'\d', v):
            raise ValueError('비밀번호에 숫자가 포함되어야 합니다')
        return v
    
    @validator('name')
    def name_alphanumeric(cls, v):
        # XSS 방지: 특수문자 제거
        if re.search(r'[<>"\'/;]', v):
            raise ValueError('이름에 특수문자를 사용할 수 없습니다')
        return v

class GarmentUpload(BaseModel):
    name: str = Field(..., max_length=200)
    category: str = Field(..., pattern="^(top|bottom|dress|outer)$")
    
# FastAPI에서 자동 검증
@app.post("/users")
async def create_user(user: UserCreate):
    # Pydantic이 자동으로 검증
    # 잘못된 데이터는 422 에러 반환
    ...
```

---

## 5️⃣ SQL Injection 방지

### SQLAlchemy ORM 사용 (권장)

```python
# ❌ 위험: Raw SQL
query = f"SELECT * FROM users WHERE email = '{email}'"

# ✅ 안전: SQLAlchemy ORM
from sqlalchemy.orm import Session

def get_user_by_email(db: Session, email: str):
    return db.query(User).filter(User.email == email).first()

# ✅ 안전: 파라미터 바인딩
from sqlalchemy import text

result = db.execute(
    text("SELECT * FROM users WHERE email = :email"),
    {"email": email}
)
```

---

## 6️⃣ API 보안

### CORS 설정

```python
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

# 개발 환경
origins = [
    "http://localhost:3000",
    "http://localhost:8000",
]

# 프로덕션 환경
# origins = ["https://yourdomain.com"]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE"],
    allow_headers=["*"],
)
```

### Rate Limiting (속도 제한)

```python
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded

limiter = Limiter(key_func=get_remote_address)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

@app.post("/tryon")
@limiter.limit("10/minute")  # 분당 10회 제한
async def try_on(request: Request, ...):
    ...

@app.post("/login")
@limiter.limit("5/minute")  # 로그인 시도 제한 (브루트포스 방지)
async def login(request: Request, ...):
    ...
```

### API Key 인증 (B2B용)

```python
from fastapi import Security, HTTPException
from fastapi.security import APIKeyHeader

api_key_header = APIKeyHeader(name="X-API-Key")

async def verify_api_key(api_key: str = Security(api_key_header)):
    # DB에서 API Key 검증
    if not await is_valid_api_key(api_key):
        raise HTTPException(status_code=403, detail="Invalid API Key")
    return api_key

@app.post("/api/v1/tryon")
async def api_try_on(api_key: str = Depends(verify_api_key)):
    ...
```

---

## 7️⃣ 파일 업로드 보안

### 이미지 업로드 검증

```python
from fastapi import UploadFile, HTTPException
import magic  # pip install python-magic

ALLOWED_TYPES = ["image/jpeg", "image/png", "image/webp"]
MAX_FILE_SIZE = 10 * 1024 * 1024  # 10MB

async def validate_image(file: UploadFile):
    # 1. 파일 크기 검증
    content = await file.read()
    if len(content) > MAX_FILE_SIZE:
        raise HTTPException(400, "File too large (max 10MB)")
    
    # 2. MIME 타입 검증 (확장자가 아닌 실제 파일 내용으로)
    mime_type = magic.from_buffer(content, mime=True)
    if mime_type not in ALLOWED_TYPES:
        raise HTTPException(400, f"Invalid file type: {mime_type}")
    
    # 3. 파일 포인터 리셋
    await file.seek(0)
    
    return content

@app.post("/upload")
async def upload_image(file: UploadFile):
    content = await validate_image(file)
    # 저장 로직...
```

---

## 8️⃣ 환경 변수 관리

### .env 파일 사용

```bash
# .env (절대 Git에 커밋하지 않음!)
DATABASE_URL=postgresql://user:pass@localhost/db
SECRET_KEY=your-super-secret-key-at-least-32-chars
JWT_ALGORITHM=HS256
JWT_EXPIRE_MINUTES=30

# Supabase
SUPABASE_URL=https://xxx.supabase.co
SUPABASE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# API Keys
HUGGINGFACE_TOKEN=hf_xxx
```

### pydantic-settings 사용

```python
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    database_url: str
    secret_key: str
    jwt_algorithm: str = "HS256"
    jwt_expire_minutes: int = 30
    supabase_url: str
    supabase_key: str
    
    class Config:
        env_file = ".env"

settings = Settings()

# 사용
SECRET_KEY = settings.secret_key
```

---

## 9️⃣ HTTPS 강제 (프로덕션)

### Vercel/Railway 사용 시

```
✅ 자동으로 HTTPS 적용됨
✅ SSL 인증서 자동 갱신
✅ 추가 설정 불필요
```

### 직접 서버 운영 시

```python
# HTTP → HTTPS 리다이렉트
from fastapi import Request
from starlette.middleware.httpsredirect import HTTPSRedirectMiddleware

# 프로덕션에서만 적용
if settings.environment == "production":
    app.add_middleware(HTTPSRedirectMiddleware)
```

---

## 🔟 보안 헤더 설정

### 기본 보안 헤더

```python
from fastapi import FastAPI
from starlette.middleware.base import BaseHTTPMiddleware

class SecurityHeadersMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request, call_next):
        response = await call_next(request)
        response.headers["X-Content-Type-Options"] = "nosniff"
        response.headers["X-Frame-Options"] = "DENY"
        response.headers["X-XSS-Protection"] = "1; mode=block"
        response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
        return response

app.add_middleware(SecurityHeadersMiddleware)
```

---

## 📊 보안 체크리스트

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  프로젝트 보안 체크리스트                                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  인증/인가                                                                   │
│  □ JWT 토큰 기반 인증 구현                                                  │
│  □ Refresh Token 구현 (선택)                                                │
│  □ 비밀번호 bcrypt/argon2 해싱                                              │
│  □ 권한 체크 (일반 사용자 vs 관리자)                                        │
│                                                                              │
│  API 보안                                                                    │
│  □ CORS 설정 (허용 도메인 제한)                                             │
│  □ Rate Limiting (로그인, API 호출)                                         │
│  □ HTTPS 강제 (프로덕션)                                                    │
│  □ 보안 헤더 설정                                                           │
│                                                                              │
│  입력 검증                                                                   │
│  □ Pydantic으로 모든 입력 검증                                              │
│  □ 파일 업로드 MIME 타입 검증                                               │
│  □ 파일 크기 제한                                                           │
│                                                                              │
│  데이터베이스                                                                │
│  □ SQLAlchemy ORM 사용 (SQL Injection 방지)                                 │
│  □ 민감 데이터 암호화 저장                                                  │
│                                                                              │
│  환경 설정                                                                   │
│  □ .env 파일로 시크릿 관리                                                  │
│  □ .gitignore에 .env 추가                                                   │
│  □ 프로덕션 시크릿 별도 관리                                                │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 💡 권장 보안 스택 요약

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  Virtual Try-On 프로젝트 보안 스택                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  인증:       Supabase Auth (소셜 로그인 포함) ⭐                            │
│             또는 python-jose + JWT 직접 구현                                │
│                                                                              │
│  비밀번호:   passlib + bcrypt                                               │
│                                                                              │
│  입력검증:   Pydantic (FastAPI 내장)                                        │
│                                                                              │
│  DB 보안:    SQLAlchemy ORM                                                 │
│                                                                              │
│  API 보안:   CORS + Rate Limiting (slowapi)                                 │
│                                                                              │
│  파일검증:   python-magic (MIME 타입)                                       │
│                                                                              │
│  시크릿:     pydantic-settings + .env                                       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📦 필요한 라이브러리 목록

```bash
# requirements.txt에 추가

# 인증/보안
python-jose[cryptography]==3.3.0
passlib[bcrypt]==1.7.4
python-multipart==0.0.6

# Rate Limiting
slowapi==0.1.9

# 파일 검증
python-magic==0.4.27

# 환경변수
pydantic-settings==2.1.0

# Supabase (선택)
supabase==2.3.0
```

---

## 🚨 MVP 단계 필수 vs 선택

### MVP 필수 (반드시 구현)

```
✅ JWT 인증 (로그인/회원가입)
✅ 비밀번호 해싱 (bcrypt)
✅ Pydantic 입력 검증
✅ CORS 설정
✅ .env 환경변수 관리
✅ SQLAlchemy ORM 사용
```

### MVP 선택 (나중에 추가)

```
📌 Refresh Token
📌 Rate Limiting
📌 보안 헤더
📌 파일 MIME 검증 (python-magic)
📌 API Key 인증 (B2B)
📌 소셜 로그인
```

---

*이 문서는 보안 요구사항 변경 시 업데이트됩니다.*

