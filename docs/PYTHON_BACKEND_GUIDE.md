# 🐍 Python 백엔드 개발 가이드

> **버전**: v1.0  
> **작성일**: 2025.12.06  
> **대상**: Python/FastAPI 백엔드 개발자

---

## 📋 목차

1. [Python 백엔드의 역할](#python-백엔드의-역할)
2. [가상환경 이해](#가상환경-이해)
3. [프로젝트 구조](#프로젝트-구조)
4. [Java vs Python FastAPI 비교](#java-vs-python-fastapi-비교)
5. [의존성 주입 패턴](#의존성-주입-패턴)
6. [실전 개발 패턴](#실전-개발-패턴)

---

## Python 백엔드의 역할

### 핵심 기능

Python 백엔드는 **AI 모델 연동**을 담당하는 핵심 부분입니다.

```
Frontend (Next.js)
    │
    ├─▶ Java Backend (8080)
    │   └─▶ 비즈니스 로직: 사용자, 구독, 결제, 의상 관리
    │
    └─▶ Python Backend (8000) ⭐ 핵심
        └─▶ AI 처리: Try-On 실행, 이미지 처리
```

### 왜 Python인가?

- **AI 라이브러리 호환성**: PyTorch, HuggingFace, Diffusers 등은 Python 기반
- **모듈 연동**: Java에서는 직접 실행이 어려워 Python 백엔드가 필요
- **핵심 기능**: Virtual Try-On의 AI 처리를 담당

### 주요 서비스

1. **Try-On AI 처리** (IDM-VTON 모델 실행)
2. **이미지 세그멘테이션** (SAM)
3. **얼굴 랜드마크 처리** (MediaPipe)

---

## 가상환경 이해

### 가상환경이란?

- 프로젝트별로 패키지를 격리하여 관리하는 환경
- 시스템 Python과 분리하여 충돌 방지
- `venv` 폴더가 가상환경입니다

### 개발 시작 순서

```bash
# 1. 가상환경 활성화
source venv/bin/activate  # macOS/Linux
# 또는
.\venv\Scripts\Activate.ps1  # Windows

# 2. 패키지 설치 확인
pip install -r requirements.txt

# 3. 환경 변수 파일 생성
cp env.example .env

# 4. 데이터베이스 실행 (Docker)
docker-compose up -d postgres redis

# 5. Python 백엔드 실행
cd backend-python
uvicorn app.main:app --reload --port 8000
```

### 가상환경 비활성화

```bash
deactivate
```

---

## 프로젝트 구조

### 디렉토리 구조

```
backend-python/app/
├── core/                    # 핵심 설정
│   ├── config.py            # 환경 변수 설정
│   ├── database.py          # DB 연결 및 세션
│   └── security.py          # JWT 인증/비밀번호 해싱
│
├── models/                  # 데이터베이스 모델 (SQLAlchemy)
│   ├── user.py              # User 모델
│   ├── garment.py           # 의상 모델
│   └── tryon_result.py      # Try-On 결과 모델
│
├── schemas/                 # API 요청/응답 스키마 (Pydantic)
│   ├── tryon.py             # Try-On 스키마
│   └── garment.py           # 의상 스키마
│
├── services/                # 비즈니스 로직
│   ├── ai_service.py        # AI 모델 실행 (핵심!)
│   ├── tryon_service.py     # Try-On 비즈니스 로직
│   ├── image_service.py    # 이미지 처리
│   └── storage_service.py  # 파일 저장
│
├── api/v1/                 # API 엔드포인트
│   ├── tryon.py            # Try-On API
│   ├── garments.py         # 의상 관리 API
│   └── avatars.py          # 아바타 API
│
├── tasks/                  # 비동기 작업 (Celery)
│   ├── celery_app.py       # Celery 설정
│   └── ai_tasks.py         # AI 처리 비동기 작업
│
└── main.py                 # FastAPI 메인 애플리케이션
```

### `__init__.py`의 역할

`__init__.py`는 **연결고리** 역할을 합니다.

#### 주요 역할

1. **패키지 인식**: 디렉토리를 Python 패키지로 인식
2. **Import 경로 단순화**: 하위 모듈을 상위에서 바로 접근 가능

#### 예시

```python
# models/__init__.py
from .user import User
from .garment import Garment

# 사용할 때:
from app.models import User, Garment  # 간단!
# 대신
from app.models.user import User      # 복잡함
```

---

## Java vs Python FastAPI 비교

### 컨트롤러/라우터 선언

| Java Spring Boot | Python FastAPI |
|-----------------|----------------|
| `@RestController`<br>`@RequestMapping("/api/v1/payments")` | `router = APIRouter(prefix="/api/v1/tryon")` |

### HTTP 메서드

| Java | Python |
|------|--------|
| `@PostMapping("/request")` | `@router.post("/")` |
| `@GetMapping("/{id}")` | `@router.get("/{id}")` |

### 요청 바디/파라미터

| Java | Python |
|------|--------|
| `@RequestBody PaymentRequest request` | `request: TryOnRequest` (함수 파라미터) |
| `@PathVariable UUID id` | `id: UUID` (함수 파라미터) |
| `@RequestParam String status` | `status: str = Query(...)` |

### 데이터베이스 세션

| Java | Python |
|------|--------|
| `@Autowired PaymentRepository repository` | `db: Session = Depends(get_db)` |
| `@Transactional` (자동) | `try-except` + `commit/rollback` (수동) |

### 의존성 주입

| Java | Python |
|------|--------|
| `@Autowired` / 생성자 주입 | `Depends()` 함수 파라미터 |
| `@RequiredArgsConstructor` (Lombok) | 의존성 함수 생성 |

### 응답 반환

| Java | Python |
|------|--------|
| `ResponseEntity<PaymentResponse>` | `return result` (자동 JSON) |

### 입력 검증

| Java | Python |
|------|--------|
| `@Valid @RequestBody DTO` | Pydantic 자동 검증 |

### 인증

| Java | Python |
|------|--------|
| `@AuthenticationPrincipal UserDetails` | `Depends(get_current_user)` |

---

## 의존성 주입 패턴

### 핵심 개념

**의존성 함수 하나 생성 → 여러 엔드포인트에서 재사용**

### 1단계: 의존성 함수 생성

```python
# api/v1/dependencies.py 또는 core/dependencies.py
from fastapi import Depends
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.services.tryon_service import TryOnService

def get_tryon_service(
    db: Session = Depends(get_db),              # DB 세션 주입
    current_user: User = Depends(get_current_user)  # 인증 주입
) -> TryOnService:
    """Try-On Service 의존성 함수"""
    # Service 인스턴스 생성하고 반환
    return TryOnService(db, current_user)
```

**역할**: Service를 생성하고 필요한 것들(DB, 인증 등)을 주입

### 2단계: Service 클래스 (비즈니스 로직)

```python
# services/tryon_service.py
from sqlalchemy.orm import Session
from app.models.user import User
from app.models.tryon_result import TryOnResult
from app.schemas.tryon import TryOnRequest

class TryOnService:
    def __init__(self, db: Session, current_user: User):
        # 인스턴스 변수로 저장 (나중에 메서드에서 사용)
        self.db = db                    # ← 인스턴스 변수로 저장
        self.current_user = current_user  # ← 인스턴스 변수로 저장
    
    async def create_tryon(self, request: TryOnRequest):
        """Try-On 생성 로직"""
        # self.db와 self.current_user 사용
        result = TryOnResult(
            user_id=self.current_user.id,  # ← self.current_user 사용
            person_image=request.person_image,
            garment_image=request.garment_image
        )
        self.db.add(result)              # ← self.db 사용
        self.db.commit()
        self.db.refresh(result)
        return result
    
    def get_tryon(self, result_id: UUID):
        """Try-On 조회 로직"""
        # 같은 self.db 사용
        return self.db.query(TryOnResult).filter(
            TryOnResult.id == result_id,
            TryOnResult.user_id == self.current_user.id
        ).first()
```

**역할**: 실제 비즈니스 로직 처리 (DB 작업, 검증, AI 처리 등)

### 3단계: API 엔드포인트 (Service 사용)

```python
# api/v1/tryon.py
from fastapi import APIRouter, Depends
from app.api.v1.dependencies import get_tryon_service
from app.services.tryon_service import TryOnService
from app.schemas.tryon import TryOnRequest, TryOnResponse

router = APIRouter(prefix="/tryon", tags=["Try-On"])

@router.post("/", response_model=TryOnResponse)
async def create_tryon(
    request: TryOnRequest,                        # 요청 데이터 (자동 검증)
    service: TryOnService = Depends(get_tryon_service)  # Service 주입
):
    """Try-On 생성 API"""
    # Service 메서드 호출
    result = await service.create_tryon(request)
    return result

@router.get("/{result_id}", response_model=TryOnResponse)
async def get_tryon(
    result_id: UUID,
    service: TryOnService = Depends(get_tryon_service)  # 같은 Service 재사용
):
    """Try-On 조회 API"""
    result = service.get_tryon(result_id)
    return result
```

**역할**: HTTP 요청을 받아서 Service 메서드 호출

---

## `self.db = db`의 역할

### 왜 필요한가?

```python
class TryOnService:
    def __init__(self, db: Session, current_user: User):
        self.db = db                    # ← 인스턴스 변수로 저장
        self.current_user = current_user  # ← 인스턴스 변수로 저장
    
    async def create_tryon(self, request: TryOnRequest):
        # self.db를 사용 (저장된 값 사용)
        self.db.add(result)              # ← self.db 사용
        self.db.commit()                 # ← self.db 사용
```

### Java vs Python

#### Java (Lombok 사용)

```java
@Service
@RequiredArgsConstructor  // ← 생성자 자동 생성
public class PaymentService {
    private final PaymentRepository paymentRepository;  // final 필드만 선언
    
    public Payment createPayment(...) {
        // this 없이 바로 사용
        return paymentRepository.save(...);
    }
}
```

#### Python (self 필수)

```python
class TryOnService:
    def __init__(self, db: Session, current_user: User):
        # self는 반드시 명시해야 함 (Python 문법)
        self.db = db                    # ← self 필수
        self.current_user = current_user  # ← self 필수
    
    async def create_tryon(self, request: TryOnRequest):
        # 메서드에서도 self 필수
        self.db.add(result)              # ← self 필수
        # db.add(result)  # ← 이렇게 쓰면 에러! (지역 변수로 인식)
```

### 요약

- **Java (Lombok)**: `@RequiredArgsConstructor`로 생성자 자동 생성, `this` 생략 가능
- **Python**: `__init__`에서 `self.변수 = 값`으로 저장, `self`는 필수 (Python 문법)

---

## 실전 개발 패턴

### 전체 흐름도

```
1. 클라이언트 요청
   POST /api/v1/tryon/
   {
     "person_image": "...",
     "garment_image": "..."
   }
   ↓
2. FastAPI 엔드포인트
   @router.post("/")
   async def create_tryon(
       request: TryOnRequest,  ← Pydantic이 자동 검증
       service: TryOnService = Depends(get_tryon_service)  ← Service 주입
   ):
   ↓
3. Depends(get_tryon_service) 실행
   def get_tryon_service(
       db: Session = Depends(get_db),           ← DB 주입
       current_user: User = Depends(get_current_user)  ← 인증 주입
   ):
       return TryOnService(db, current_user)  ← Service 생성
   ↓
4. Service 메서드 실행
   service.create_tryon(request)
   ↓
5. 비즈니스 로직 처리
   - DB 작업
   - AI 처리
   - 결과 반환
   ↓
6. 응답 반환
   return result
```

### 개발 우선순위

#### 1단계: 핵심 AI 서비스
- `services/ai_service.py` - AI 모델 연동

#### 2단계: Try-On 기능
- `models/tryon_result.py`
- `schemas/tryon.py`
- `services/tryon_service.py`
- `api/v1/tryon.py`

#### 3단계: 의상 관리
- `models/garment.py`
- `schemas/garment.py`
- `api/v1/garments.py`

#### 4단계: 비동기 처리 (선택)
- `tasks/celery_app.py`
- `tasks/ai_tasks.py`

---

## 주요 파일 경로

### 현재 존재하는 파일

```
backend-python/app/main.py                    ✅ 존재
backend-python/app/core/config.py             ✅ 존재
backend-python/app/core/database.py           ✅ 존재
backend-python/app/core/security.py           ✅ 존재
backend-python/app/models/user.py             ✅ 존재
```

### 추가로 만들어야 할 파일

#### API 라우터 (우선순위 높음)
```
backend-python/app/api/v1/tryon.py            # Try-On API 엔드포인트
backend-python/app/api/v1/garments.py         # 의상 관리 API
backend-python/app/api/v1/avatars.py          # 아바타 API
```

#### 서비스 로직 (핵심)
```
backend-python/app/services/ai_service.py     # AI 모델 실행 (핵심!)
backend-python/app/services/tryon_service.py  # Try-On 비즈니스 로직
backend-python/app/services/image_service.py  # 이미지 처리
backend-python/app/services/storage_service.py # 파일 저장
```

#### 모델 추가
```
backend-python/app/models/garment.py          # 의상 모델
backend-python/app/models/tryon_result.py     # Try-On 결과 모델
backend-python/app/models/avatar.py           # 아바타 모델
```

#### 스키마 (Pydantic)
```
backend-python/app/schemas/tryon.py           # Try-On 요청/응답 스키마
backend-python/app/schemas/garment.py         # 의상 스키마
```

---

## 요약

### 핵심 패턴

1. **의존성 함수 하나 생성** → 여러 엔드포인트에서 재사용
2. **Service 클래스에 로직 작성** → 엔드포인트는 Service 호출만
3. **`self.변수 = 값`으로 저장** → 메서드에서 재사용

### Java vs Python

| 항목 | Java (Lombok) | Python |
|------|--------------|--------|
| **생성자** | `@RequiredArgsConstructor` 자동 생성 | `__init__` 직접 작성 |
| **필드 저장** | 자동 (final 필드만) | `self.변수 = 값` 직접 작성 |
| **this/self** | 생략 가능 (Lombok) | 필수 (Python 문법) |
| **사용** | `paymentRepository.save()` | `self.db.add()` |

### 개발 시작

가장 먼저 작업할 파일:
1. `backend-python/app/services/ai_service.py` (AI 모델 연동)
2. `backend-python/app/api/v1/tryon.py` (API 엔드포인트)

---

## 참고 문서

- [서비스 아키텍처](./SERVICE_ARCHITECTURE.md) - 전체 서비스 구조
- [데이터베이스 접근](./BACKEND_DB_ACCESS.md) - ORM 사용법
- [시스템 아키텍처](./technical/ARCHITECTURE.md) - 기술 스택

---

*이 문서는 Python 백엔드 개발을 시작하는 개발자를 위한 가이드입니다.*

