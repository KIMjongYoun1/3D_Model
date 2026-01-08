# 🔄 백엔드 역할 분담

> **버전**: v1.0  
> **작성일**: 2025.12.06  
> **목적**: Java Backend와 Python Backend의 역할 명확화

---

## 📋 역할 분담

### Java Backend (8080) - 비즈니스 로직

```
✅ 담당 영역:
├── 사용자 관리 (User Service)
│   ├── 회원가입/로그인 ⭐
│   ├── 프로필 관리
│   └── 인증/인가 ⭐
│
├── 구독 관리 (Subscription Service)
│   ├── 구독 플랜 관리
│   └── 구독 상태 관리
│
├── 결제 서비스 (Payment Service)
│   ├── 결제 요청 처리
│   └── PG사 연동
│
├── 사용량 관리 (Usage Service)
│   └── Try-On 사용량 추적
│
└── 의상 관리 (Garment Service)
    └── 의상 업로드/관리
```

**인증 관련:**
- ✅ 로그인/회원가입 처리
- ✅ JWT 토큰 발급
- ✅ 비밀번호 해싱 (BCrypt)
- ✅ 소셜 로그인 (네이버, 카카오)
- ✅ Rate Limiting (로그인 시도 제한)

---

### Python Backend (8000) - AI 처리

```
✅ 담당 영역:
├── AI 모델 서비스
│   ├── Try-On 실행 (IDM-VTON)
│   ├── 이미지 세그멘테이션 (SAM)
│   └── 얼굴 메시 처리 (MediaPipe)
│
├── 이미지 처리 서비스
│   ├── 이미지 다운로드
│   ├── 이미지 리사이즈
│   └── 썸네일 생성
│
└── 스토리지 서비스
    └── 파일 저장 (로컬/S3)
```

**인증 관련:**
- ✅ JWT 토큰 검증만 (Java에서 발급한 토큰)
- ❌ 로그인/회원가입 처리 안 함
- ❌ 비밀번호 해싱 안 함
- ❌ 토큰 발급 안 함

---

## 🔐 인증 흐름

### 전체 흐름

```
1. 사용자 로그인
   Frontend ──▶ Java Backend (8080)
                POST /api/v1/auth/login
                ↓
                ✅ 비밀번호 검증
                ✅ JWT 토큰 발급
                ↓
   Frontend ◀── JWT 토큰 반환
   
2. AI API 호출
   Frontend ──▶ Python Backend (8000)
                POST /api/v1/tryon
                Authorization: Bearer {JWT_TOKEN}
                ↓
                ✅ JWT 토큰 검증 (Java와 같은 Secret Key)
                ✅ 사용자 조회
                ✅ AI 처리
                ↓
   Frontend ◀── 결과 반환
```

---

## 🔑 JWT 토큰 관리

### Java Backend (토큰 발급)

```java
// backend-java/src/main/java/com/virtualtryon/service/AuthService.java
@Service
public class AuthService {
    
    public String login(String email, String password) {
        // 1. 사용자 조회
        User user = userRepository.findByEmail(email);
        
        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException();
        }
        
        // 3. JWT 토큰 발급
        String token = jwtService.generateToken(user);
        
        return token;
    }
}
```

### Python Backend (토큰 검증만)

```python
# backend-python/app/core/security.py
def get_current_user(token: str = Depends(oauth2_scheme)):
    """JWT 토큰 검증만 수행"""
    # 1. 토큰 검증 (Java와 같은 Secret Key 사용)
    payload = decode_access_token(token)
    
    # 2. 사용자 조회
    user = db.query(User).filter(User.id == payload['sub']).first()
    
    return user
```

---

## ⚠️ 중요 사항

### 1. JWT Secret Key 공유

```yaml
# Java Backend (application.yml)
jwt:
  secret: your-super-secret-key  # ⚠️ Python과 동일해야 함
  algorithm: HS256
```

```python
# Python Backend (.env)
JWT_SECRET=your-super-secret-key  # ⚠️ Java와 동일해야 함
JWT_ALGORITHM=HS256
```

**이유:** Python Backend가 Java에서 발급한 토큰을 검증하려면 같은 Secret Key가 필요

### 2. 로그인은 Java에서만

```
❌ Python Backend에 로그인 API 만들지 않음
❌ Python Backend에 비밀번호 해싱 로직 없음
❌ Python Backend에 회원가입 로직 없음

✅ Python Backend는 JWT 검증만
```

### 3. Rate Limiting 분리

```
Java Backend:
- 로그인 Rate Limiting (무차별 대입 공격 방지)
- 일반 API Rate Limiting

Python Backend:
- AI API Rate Limiting (무차별 호출 방지)
- 로그인 Rate Limiting 없음 (로그인 API가 없으므로)
```

---

## 📊 역할 비교표

| 기능 | Java Backend | Python Backend |
|------|------------|----------------|
| **로그인/회원가입** | ✅ 담당 | ❌ 없음 |
| **JWT 토큰 발급** | ✅ 담당 | ❌ 없음 |
| **JWT 토큰 검증** | ✅ 담당 | ✅ 담당 (Java 토큰 검증) |
| **비밀번호 해싱** | ✅ 담당 | ❌ 없음 |
| **소셜 로그인** | ✅ 담당 | ❌ 없음 |
| **AI 처리** | ❌ 없음 | ✅ 담당 |
| **이미지 처리** | ❌ 없음 | ✅ 담당 |

---

## 요약

### Java Backend
- **인증 담당**: 로그인, 회원가입, JWT 발급
- **비즈니스 로직**: 구독, 결제, 사용량 관리

### Python Backend
- **AI 처리 담당**: Try-On, 이미지 처리
- **인증**: JWT 검증만 (Java에서 발급한 토큰)

**핵심:** 로그인은 Java, Python은 AI 처리만!

---

*이 문서는 백엔드 역할 분담을 명확히 정의합니다.*

