# 🛠️ 개발 가이드 - 핵심 기능 직접 구현

> **목적**: 취업용 포트폴리오를 위한 핵심 기능 직접 구현 가이드
> **대상**: 프로젝트 개발자

---

## 📋 핵심 기능 직접 구현 목록

### ⭐ 필수 직접 구현 기능

다음 기능들은 **반드시 직접 구현**하거나 **깊이 이해**해야 합니다.

#### 1. 인증/인가 로직 (Java Backend) ⭐⭐⭐
**위치**: `backend-java/src/main/java/com/virtualtryon/service/AuthService.java`

**직접 구현해야 할 부분:**
- JWT 토큰 생성/검증 로직
- 비밀번호 해싱/검증 로직
- 로그인/회원가입 비즈니스 로직
- 토큰 갱신 로직

**구현 힌트:**
```java
// 1. 로그인 로직 직접 구현
public String login(String email, String password) {
    // 1. 사용자 조회
    // 2. 비밀번호 검증 (PasswordService 사용)
    // 3. JWT 토큰 생성
    // 4. 토큰 반환
}

// 2. JWT 토큰 검증 직접 구현
public boolean validateToken(String token) {
    // 1. 토큰 파싱
    // 2. 만료 시간 확인
    // 3. 서명 검증
    // 4. 사용자 정보 추출
}
```

**참고 파일:**
- `backend-java/src/main/java/com/virtualtryon/service/PasswordService.java` (비밀번호 해싱)
- `backend-python/app/core/security.py` (Python 버전 참고)

---

#### 2. 결제 서비스 비즈니스 로직 (Java Backend) ⭐⭐⭐
**위치**: `backend-java/src/main/java/com/virtualtryon/service/PaymentService.java`

**직접 구현해야 할 부분:**
- 결제 요청 처리 로직
- 결제 상태 관리 로직
- 구독 활성화 연동 로직
- 결제 검증 로직

**구현 힌트:**
```java
// 1. 결제 요청 처리 직접 구현
public Payment createPayment(PaymentRequest request) {
    // 1. 사용량 제한 체크 (UsageService)
    // 2. 결제 엔티티 생성
    // 3. 결제 시뮬레이션 처리 (성공/실패 결정)
    // 4. 구독 활성화 (성공 시)
    // 5. 사용량 업데이트
}

// 2. 구독 활성화 로직 직접 구현
private void activateSubscription(UUID paymentId) {
    // 1. 결제 정보 조회
    // 2. 구독 정보 업데이트
    // 3. 사용량 제한 업데이트
    // 4. 알림 발송 (선택)
}
```

**참고 파일:**
- `backend-java/src/main/java/com/virtualtryon/service/PaymentService.java`
- `backend-java/src/main/java/com/virtualtryon/controller/PaymentController.java`

---

#### 3. 사용량 관리 로직 (Java Backend) ⭐⭐
**위치**: `backend-java/src/main/java/com/virtualtryon/service/UsageService.java`

**직접 구현해야 할 부분:**
- 사용량 체크 로직
- 사용량 증가 로직
- 사용량 제한 검증 로직

**구현 힌트:**
```java
// 1. 사용량 체크 직접 구현
public boolean checkUsageLimit(UUID userId, String serviceType) {
    // 1. 사용자 구독 정보 조회
    // 2. 현재 사용량 조회 (Redis 또는 DB)
    // 3. 제한 확인
    // 4. 결과 반환
}

// 2. 사용량 증가 직접 구현
public void incrementUsage(UUID userId, String serviceType) {
    // 1. Redis에 사용량 증가 (원자적 연산)
    // 2. DB에 사용량 업데이트 (비동기)
    // 3. 제한 도달 시 알림 (선택)
}
```

**참고 파일:**
- `backend-java/src/main/java/com/virtualtryon/entity/Subscription.java`
- Redis 연동 코드

---

#### 4. AI 모델 연동 로직 (Python Backend) ⭐⭐⭐
**위치**: `backend-python/app/services/ai_service.py`

**직접 구현해야 할 부분:**
- AI 모델 로딩 로직
- 이미지 전처리 로직
- AI 모델 실행 로직
- 결과 후처리 로직

**구현 힌트:**
```python
# 1. AI 모델 실행 직접 구현
async def process_tryon(self, person_image: bytes, garment_image: bytes):
    # 1. 이미지 전처리
    #    - 크기 조정
    #    - 형식 변환 (PIL → Tensor)
    #    - 정규화
    
    # 2. AI 모델 실행
    #    - 모델에 입력 전달
    #    - GPU 메모리 관리
    #    - 결과 받기
    
    # 3. 결과 후처리
    #    - Tensor → PIL Image
    #    - 크기 조정
    #    - 포맷 변환
    
    # 4. 결과 반환
```

**참고 파일:**
- `backend-python/app/services/ai_service.py` (생성 필요)
- HuggingFace Diffusers 문서

---

#### 5. 이미지 업로드/처리 로직 (Python Backend) ⭐⭐
**위치**: `backend-python/app/services/storage_service.py`

**직접 구현해야 할 부분:**
- 파일 업로드 처리
- 이미지 검증 로직
- 썸네일 생성 로직
- 로컬 스토리지 저장 로직

**구현 힌트:**
```python
# 1. 이미지 업로드 직접 구현
async def upload_image(self, file: UploadFile, user_id: UUID) -> str:
    # 1. 파일 검증
    #    - 파일 크기 체크
    #    - 이미지 형식 검증 (PIL 사용)
    #    - 악성 파일 검사
    
    # 2. 이미지 처리
    #    - 크기 조정 (최대 크기 제한)
    #    - 썸네일 생성
    
    # 3. 저장
    #    - 로컬 파일 시스템에 저장
    #    - 파일명 생성 (UUID)
    #    - 경로 반환
```

**참고 파일:**
- `backend-python/app/services/storage_service.py` (생성 필요)
- PIL/Pillow 라이브러리 문서

---

#### 6. Try-On 요청 처리 로직 (Java Backend) ⭐⭐⭐
**위치**: `backend-java/src/main/java/com/virtualtryon/service/TryOnService.java`

**직접 구현해야 할 부분:**
- Try-On 요청 검증 로직
- 작업 큐 등록 로직
- Python Backend 연동 로직
- 결과 조회 로직

**구현 힌트:**
```java
// 1. Try-On 요청 처리 직접 구현
public TryOnResult requestTryOn(TryOnRequest request) {
    // 1. 사용량 체크 (UsageService)
    // 2. 이미지 검증
    // 3. 작업 큐에 등록 (Redis)
    // 4. Python Backend에 요청 (HTTP)
    // 5. 작업 ID 반환
}

// 2. Python Backend 연동 직접 구현
private TryOnResult callPythonBackend(TryOnRequest request) {
    // 1. HTTP 클라이언트 생성
    // 2. 요청 데이터 준비
    // 3. POST 요청 전송
    // 4. 응답 파싱
    // 5. 결과 반환
}
```

**참고 파일:**
- `backend-java/src/main/java/com/virtualtryon/service/TryOnService.java` (생성 필요)
- RestTemplate 또는 WebClient 사용

---

### 📝 선택적 직접 구현 기능

다음 기능들은 **이해**만 해도 되지만, 직접 구현하면 더 좋습니다.

#### 7. 데이터베이스 쿼리 최적화
- 복잡한 JOIN 쿼리 직접 작성
- 인덱스 활용 쿼리 작성
- N+1 문제 해결

#### 8. 에러 처리 및 예외 처리
- 커스텀 예외 클래스 작성
- 전역 예외 처리 핸들러
- 에러 응답 포맷 정의

#### 9. 로깅 및 모니터링
- 구조화된 로깅
- 성능 모니터링
- 에러 추적

---

## 💡 직접 구현 방법

### 1단계: 기존 코드 이해
```bash
# 1. AI가 작성한 코드 읽기
# 2. 각 메서드의 역할 이해
# 3. 데이터 흐름 파악
```

### 2단계: 비즈니스 로직 추출
```java
// AI가 작성한 코드에서
// 비즈니스 로직 부분만 추출하여
// 직접 구현
```

### 3단계: 테스트 작성
```java
// 직접 구현한 로직에 대한
// 단위 테스트 작성
// - JUnit (Java)
// - pytest (Python)
```

### 4단계: 리팩토링
```java
// AI가 작성한 코드를
// 직접 구현한 로직으로 교체
// - 성능 개선
// - 가독성 향상
// - 에러 처리 강화
```

---

## 📚 학습 자료

### Java/Spring Boot
- Spring Security 공식 문서
- JWT 구현 가이드
- Redis 연동 가이드

### Python/FastAPI
- FastAPI 공식 문서
- HuggingFace Diffusers 문서
- PIL/Pillow 이미지 처리

### 일반
- RESTful API 설계 원칙
- 데이터베이스 최적화
- 에러 처리 패턴

---

## ⚠️ 주의사항

1. **코드 이해 우선**: 직접 구현 전에 기존 코드를 완전히 이해하세요.
2. **점진적 개선**: 한 번에 모든 것을 바꾸지 말고, 하나씩 개선하세요.
3. **테스트 필수**: 직접 구현한 코드는 반드시 테스트하세요.
4. **문서화**: 변경 사항과 이유를 문서로 남기세요.

---

## 📚 관련 문서

- [클래스 참조 가이드](./CLASS_REFERENCE_GUIDE.md) - 기능 개발 시 클래스 확인 순서
- [프로젝트 개선 방안](./PROJECT_IMPROVEMENT.md) - 프로젝트 품질 향상 방안

---

*이 가이드는 취업용 포트폴리오를 위한 핵심 기능 직접 구현을 돕기 위해 작성되었습니다.*

