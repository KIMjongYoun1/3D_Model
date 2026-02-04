# 📚 클래스 참조 가이드

> **목적**: 기능 개발 시 어떤 클래스를 어떤 순서로 확인해야 하는지 가이드
> **사용 시점**: 새로운 기능을 개발하거나 기존 기능을 수정할 때

---

## 🎯 기본 원칙

### 클래스 확인 순서 (일반적)
```
1. Entity/Model (데이터 구조)
   ↓
2. Repository (데이터 접근)
   ↓
3. Service (비즈니스 로직)
   ↓
4. Controller/API (엔드포인트)
   ↓
5. DTO (데이터 전송 객체)
```

### 이유
- **Entity/Model**: 데이터 구조를 먼저 이해해야 비즈니스 로직을 설계할 수 있음
- **Repository**: 데이터 접근 방법을 알아야 Service에서 사용 가능
- **Service**: 비즈니스 로직의 핵심, Controller에서 호출
- **Controller**: API 엔드포인트, 클라이언트와의 인터페이스
- **DTO**: 요청/응답 데이터 구조

---

## 🔐 인증 기능 개발

### 클래스 확인 순서

#### 1단계: Entity 확인
```
backend-java/src/main/java/com/virtualtryon/entity/User.java
```
**확인 사항:**
- 사용자 데이터 구조
- 비밀번호 필드 (password_hash)
- 이메일 필드 (로그인 ID)

#### 2단계: Repository 확인
```
backend-java/src/main/java/com/virtualtryon/repository/UserRepository.java
(생성 필요)
```
**확인 사항:**
- 사용자 조회 메서드 (findByEmail 등)
- 사용자 저장 메서드

#### 3단계: Service 확인
```
backend-java/src/main/java/com/virtualtryon/service/PasswordService.java
backend-java/src/main/java/com/virtualtryon/service/AuthService.java
(생성 필요)
```
**확인 사항:**
- 비밀번호 해싱/검증 로직
- 로그인/회원가입 비즈니스 로직

#### 4단계: Controller 확인
```
backend-java/src/main/java/com/virtualtryon/controller/AuthController.java
(생성 필요)
```
**확인 사항:**
- API 엔드포인트 정의
- 요청/응답 처리

#### 5단계: DTO 확인
```
backend-java/src/main/java/com/virtualtryon/dto/AuthRequestDTO.java
backend-java/src/main/java/com/virtualtryon/dto/AuthResponseDTO.java
(생성 필요)
```
**확인 사항:**
- 요청 데이터 구조
- 응답 데이터 구조

#### 6단계: 보안 설정 확인
```
backend-java/src/main/java/com/virtualtryon/config/SecurityConfig.java
```
**확인 사항:**
- Spring Security 설정
- 비밀번호 인코더 설정
- 인증 필요 경로 설정

---

## 💳 결제 기능 개발

### 클래스 확인 순서

#### 1단계: Entity 확인
```
backend-java/src/main/java/com/virtualtryon/entity/Payment.java
backend-java/src/main/java/com/virtualtryon/entity/Subscription.java
(생성 필요)
```
**확인 사항:**
- 결제 데이터 구조
- 구독 데이터 구조
- 관계 (Payment ↔ Subscription)

#### 2단계: Repository 확인
```
backend-java/src/main/java/com/virtualtryon/repository/PaymentRepository.java
backend-java/src/main/java/com/virtualtryon/repository/SubscriptionRepository.java
(생성 필요)
```
**확인 사항:**
- 결제 조회 메서드
- 구독 조회 메서드
- 사용자별 조회 메서드

#### 3단계: Service 확인
```
backend-java/src/main/java/com/virtualtryon/service/PaymentService.java
backend-java/src/main/java/com/virtualtryon/service/SubscriptionService.java
(생성 필요)
backend-java/src/main/java/com/virtualtryon/service/UsageService.java
(생성 필요)
```
**확인 사항:**
- 결제 처리 로직
- 구독 활성화 로직
- 사용량 체크 로직

#### 4단계: Controller 확인
```
backend-java/src/main/java/com/virtualtryon/controller/PaymentController.java
```
**확인 사항:**
- 결제 API 엔드포인트
- 요청/응답 처리

#### 5단계: DTO 확인
```
backend-java/src/main/java/com/virtualtryon/controller/PaymentController.java
(내부 클래스: PaymentRequest, PaymentResponse)
```
**확인 사항:**
- 결제 요청 데이터 구조
- 결제 응답 데이터 구조

#### 6단계: 설정 확인
```
backend-java/src/main/resources/application.yml
```
**확인 사항:**
- 결제 시뮬레이션 설정
- 결제 성공 확률 설정

---

## 👤 사용자 관리 기능 개발

### 클래스 확인 순서

#### 1단계: Entity 확인
```
backend-java/src/main/java/com/virtualtryon/entity/User.java
```
**확인 사항:**
- 사용자 데이터 구조
- 필드 타입 및 제약조건

#### 2단계: Repository 확인
```
backend-java/src/main/java/com/virtualtryon/repository/UserRepository.java
(생성 필요)
```
**확인 사항:**
- 사용자 CRUD 메서드
- 커스텀 쿼리 메서드

#### 3단계: Service 확인
```
backend-java/src/main/java/com/virtualtryon/service/UserService.java
(생성 필요)
```
**확인 사항:**
- 사용자 조회 로직
- 사용자 수정 로직
- 프로필 관리 로직

#### 4단계: Controller 확인
```
backend-java/src/main/java/com/virtualtryon/controller/UserController.java
(생성 필요)
```
**확인 사항:**
- 사용자 API 엔드포인트
- 인증/인가 처리

---

## 🤖 Try-On 기능 개발

### 클래스 확인 순서

#### 1단계: Entity 확인
```
backend-java/src/main/java/com/virtualtryon/entity/Garment.java
backend-java/src/main/java/com/virtualtryon/entity/TryOnResult.java
(생성 필요)
```
**확인 사항:**
- 의상 데이터 구조
- Try-On 결과 데이터 구조

#### 2단계: Repository 확인
```
backend-java/src/main/java/com/virtualtryon/repository/GarmentRepository.java
backend-java/src/main/java/com/virtualtryon/repository/TryOnResultRepository.java
(생성 필요)
```
**확인 사항:**
- 의상 조회/저장 메서드
- Try-On 결과 조회/저장 메서드

#### 3단계: Service 확인 (Java)
```
backend-java/src/main/java/com/virtualtryon/service/TryOnService.java
backend-java/src/main/java/com/virtualtryon/service/GarmentService.java
backend-java/src/main/java/com/virtualtryon/service/UsageService.java
(생성 필요)
```
**확인 사항:**
- Try-On 요청 처리 로직
- 사용량 체크 로직
- Python Backend 연동 로직

#### 4단계: Service 확인 (Python)
```
backend-python/app/services/ai_service.py
backend-python/app/services/storage_service.py
(생성 필요)
```
**확인 사항:**
- AI 모델 실행 로직
- 이미지 처리 로직
- 스토리지 저장 로직

#### 5단계: Controller 확인
```
backend-java/src/main/java/com/virtualtryon/controller/TryOnController.java
backend-python/app/api/v1/tryon.py
(생성 필요)
```
**확인 사항:**
- Try-On API 엔드포인트
- 작업 큐 등록 로직

---

## 📊 구독 관리 기능 개발

### 클래스 확인 순서

#### 1단계: Entity 확인
```
backend-java/src/main/java/com/virtualtryon/entity/Subscription.java
(생성 필요)
backend-java/src/main/java/com/virtualtryon/entity/User.java
```
**확인 사항:**
- 구독 데이터 구조
- 사용자와의 관계

#### 2단계: Repository 확인
```
backend-java/src/main/java/com/virtualtryon/repository/SubscriptionRepository.java
(생성 필요)
```
**확인 사항:**
- 구독 조회 메서드
- 사용자별 구독 조회

#### 3단계: Service 확인
```
backend-java/src/main/java/com/virtualtryon/service/SubscriptionService.java
backend-java/src/main/java/com/virtualtryon/service/UsageService.java
(생성 필요)
```
**확인 사항:**
- 구독 생성/수정 로직
- 구독 상태 관리 로직
- 사용량 제한 로직

#### 4단계: Controller 확인
```
backend-java/src/main/java/com/virtualtryon/controller/SubscriptionController.java
(생성 필요)
```
**확인 사항:**
- 구독 API 엔드포인트
- 구독 플랜 조회

---

## 🎨 아바타 기능 개발

### 클래스 확인 순서

#### 1단계: Entity 확인
```
backend-java/src/main/java/com/virtualtryon/entity/Avatar.java
(생성 필요)
```
**확인 사항:**
- 아바타 데이터 구조
- 사용자와의 관계

#### 2단계: Repository 확인
```
backend-java/src/main/java/com/virtualtryon/repository/AvatarRepository.java
(생성 필요)
```
**확인 사항:**
- 아바타 조회/저장 메서드

#### 3단계: Service 확인 (Java)
```
backend-java/src/main/java/com/virtualtryon/service/AvatarService.java
(생성 필요)
```
**확인 사항:**
- 아바타 생성 요청 처리
- Python Backend 연동

#### 4단계: Service 확인 (Python)
```
backend-python/app/services/avatar_service.py
(생성 필요)
```
**확인 사항:**
- MediaPipe Face Mesh 처리
- 3D 아바타 생성 로직

#### 5단계: Controller 확인
```
backend-java/src/main/java/com/virtualtryon/controller/AvatarController.java
(생성 필요)
```
**확인 사항:**
- 아바타 API 엔드포인트

---

## 🔄 공통 확인 사항

### 모든 기능 개발 시 공통으로 확인

#### 1. 설정 파일
```
backend-java/src/main/resources/application.yml
backend-python/app/core/config.py
env.example
```
**확인 사항:**
- 데이터베이스 연결 설정
- 환경 변수 설정
- 기능별 설정

#### 2. 데이터베이스 마이그레이션
```
backend-java/src/main/resources/db/migration/
backend-python/alembic/versions/
```
**확인 사항:**
- 테이블 생성 마이그레이션
- 컬럼 추가/수정 마이그레이션

#### 3. 보안 설정
```
backend-java/src/main/java/com/virtualtryon/config/SecurityConfig.java
backend-python/app/core/security.py
```
**확인 사항:**
- 인증/인가 설정
- 비밀번호 암호화 설정
- JWT 토큰 설정

---

## 📝 체크리스트 템플릿

### 새 기능 개발 시 사용

```
[ ] 1. Entity/Model 확인 및 생성
    - 데이터 구조 설계
    - 필드 타입 및 제약조건 정의

[ ] 2. Repository 확인 및 생성
    - 데이터 접근 메서드 정의
    - 커스텀 쿼리 작성 (필요 시)

[ ] 3. Service 확인 및 생성
    - 비즈니스 로직 구현
    - 다른 Service와의 연동

[ ] 4. Controller/API 확인 및 생성
    - API 엔드포인트 정의
    - 요청/응답 처리

[ ] 5. DTO 확인 및 생성
    - 요청 DTO 정의
    - 응답 DTO 정의

[ ] 6. 마이그레이션 작성
    - 테이블 생성/수정 SQL
    - 인덱스 생성

[ ] 7. 테스트 작성
    - 단위 테스트
    - 통합 테스트 (선택)
```

---

## 💡 팁

### 효율적인 개발 방법

1. **상향식 개발**: Entity → Repository → Service → Controller 순서로 개발
2. **하향식 검증**: Controller → Service → Repository → Entity 순서로 검증
3. **점진적 구현**: 한 번에 모든 것을 만들지 말고, 단계별로 구현하고 테스트

### 코드 이해 방법

1. **주석 읽기**: 각 클래스와 메서드의 주석을 먼저 읽기
2. **데이터 흐름 추적**: 요청이 들어와서 응답이 나가는 전체 흐름 파악
3. **의존성 확인**: 어떤 클래스가 어떤 클래스를 사용하는지 확인

---

*이 가이드는 기능 개발 시 클래스를 어떤 순서로 확인해야 하는지 안내합니다.*





