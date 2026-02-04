# 🔐 암호화 및 보안 기준

프로젝트에서 사용하는 암호화 및 보안 기준을 정의합니다.

---

## 🔑 비밀번호 암호화

### 알고리즘: BCrypt

**이유:**
- ✅ 업계 표준 (가장 널리 사용)
- ✅ 자동 salt 생성 (각 비밀번호마다 다른 salt)
- ✅ Rainbow table 공격 방지
- ✅ 계산 비용 조정 가능 (시간이 지날수록 더 안전)

### 구현

#### Python (passlib)
```python
from passlib.context import CryptContext

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# 비밀번호 해싱
hashed = pwd_context.hash("plain_password")

# 비밀번호 검증
is_valid = pwd_context.verify("plain_password", hashed)
```

#### Java (Spring Security)
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

// 비밀번호 해싱
String hashed = encoder.encode("plain_password");

// 비밀번호 검증
boolean isValid = encoder.matches("plain_password", hashed);
```

### 저장 형식

```
password_hash: $2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYqBWVHxkd0
              └─┬─┘└─┬─┘└──────────────────────────────────────────────┐
                │    │                                                  │
              알고리즘 cost factor              salt + hash (60자)
```

---

## 🎫 JWT 토큰

### 알고리즘: HS256 (HMAC-SHA256)

**이유:**
- ✅ 서버에서만 검증 가능 (비밀키 필요)
- ✅ 빠른 검증 속도
- ✅ 토큰 크기 작음

### 토큰 구조

```
Header.Payload.Signature

Header: {
  "alg": "HS256",
  "typ": "JWT"
}

Payload: {
  "sub": "user_id",           // 사용자 ID
  "exp": 1234567890,          // 만료 시간
  "iat": 1234567890           // 발급 시간
}

Signature: HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

### 설정

```python
# Python
JWT_SECRET = "your-super-secret-key"  # 최소 32자 이상
JWT_ALGORITHM = "HS256"
JWT_EXPIRE_MINUTES = 60
```

```yaml
# Java
jwt:
  secret: your-super-secret-key  # 최소 32자 이상
  algorithm: HS256
  expire-minutes: 60
```

### 보안 주의사항

1. **비밀키 관리**
   - ❌ 코드에 하드코딩 금지
   - ✅ 환경 변수로 관리
   - ✅ 프로덕션에서는 강력한 랜덤 키 사용

2. **토큰 만료 시간**
   - 액세스 토큰: 1시간 (60분)
   - 리프레시 토큰: 7일 (선택사항)

3. **HTTPS 사용**
   - 프로덕션에서는 반드시 HTTPS 사용
   - 토큰이 네트워크를 통해 전송되므로 암호화 필요

---

## 🔒 데이터베이스 보안

### 비밀번호 저장

```sql
-- users 테이블
password_hash VARCHAR(255) NOT NULL  -- BCrypt 해시 저장
```

**절대 하지 말 것:**
- ❌ 평문 비밀번호 저장
- ❌ MD5, SHA1 같은 약한 해시 사용
- ❌ 단순 암호화 (복호화 가능)

### 민감 정보 암호화

#### 결제 정보
```sql
-- payments 테이블
pg_response JSONB  -- PG사 응답 데이터 (카드번호 마스킹 처리됨)
```

**주의사항:**
- 카드번호는 PG사에서만 처리
- 서버에는 마스킹된 정보만 저장
- PCI-DSS 규정 준수

---

## 🛡️ API 보안

### 인증/인가

1. **JWT 토큰 검증**
   - 모든 보호된 엔드포인트에서 토큰 검증
   - 만료된 토큰 거부

2. **권한 체크**
   - 사용자별 권한 확인
   - 리소스 소유권 확인

3. **Rate Limiting**
   - API 호출 횟수 제한
   - DDoS 공격 방지

---

## 📋 암호화 기준 요약

| 항목 | 알고리즘 | 구현 | 비고 |
|------|----------|------|------|
| **비밀번호 해싱** | BCrypt | passlib (Python), Spring Security (Java) | 자동 salt 생성 |
| **JWT 토큰** | HS256 | python-jose (Python), jjwt (Java) | 비밀키로 서명 |
| **데이터 전송** | HTTPS/TLS | - | 프로덕션 필수 |
| **DB 연결** | SSL (선택) | PostgreSQL SSL | 프로덕션 권장 |

---

## ⚠️ 보안 체크리스트

### 개발 환경
- [ ] 비밀키를 환경 변수로 관리
- [ ] `.env` 파일을 `.gitignore`에 추가
- [ ] 테스트용 비밀키 사용 (프로덕션과 분리)

### 프로덕션 환경
- [ ] 강력한 JWT 비밀키 사용 (32자 이상 랜덤)
- [ ] HTTPS 사용
- [ ] 비밀번호 정책 적용 (최소 길이, 복잡도)
- [ ] 로그에 민감 정보 출력 금지
- [ ] 정기적인 보안 업데이트

---

*이 기준은 프로젝트의 보안 요구사항을 정의합니다.*





