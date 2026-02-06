# 🔐 소셜 로그인 개발 준비 가이드 (Kakao & Naver)

Quantum Studio의 통합 인증 시스템 구축을 위해 카카오 및 네이버 개발자 센터에서 발급받아야 할 키와 설정 항목을 정리합니다.

---

## 1. 🟡 카카오 소셜 로그인 (Kakao Developers)

### [단계 1] 애플리케이션 등록
1. [카카오 개발자 센터](https://developers.kakao.com/) 접속 및 로그인
2. **내 애플리케이션** > **애플리케이션 추가하기**
3. 앱 이름, 사업자명 입력 후 생성

### [단계 2] 플랫폼 설정
1. **플랫폼** > **Web** 등록
2. 사이트 도메인 등록: `http://localhost:3000` (개발 환경)

### [단계 3] 카카오 로그인 활성화
1. **카카오 로그인** > **활성화 설정**을 `ON`으로 변경
2. **Redirect URI** 등록: `http://localhost:3000/api/auth/callback/kakao`

### [단계 4] 동의항목 설정
1. **카카오 로그인** > **동의항목**
2. 닉네임, 프로필 사진, 이메일(필요 시) 등을 `필수 동의` 또는 `선택 동의`로 설정

### [단계 5] 필수 키 수집
*   **REST API 키**: (Java 백엔드 설정용)
*   **Client Secret**: (보안 강화용, '카카오 로그인 > 보안' 탭에서 생성 가능)

---

## 2. 🟢 네이버 소셜 로그인 (Naver Developers)

### [단계 1] 애플리케이션 등록
1. [네이버 개발자 센터](https://developers.naver.com/main/) 접속 및 로그인
2. **Application** > **애플리케이션 등록**
3. 애플리케이션 이름 입력
4. 사용 API: `네이버 로그인` 선택

### [단계 2] 권한 설정
1. 회원이름, 이메일, 프로필 사진 등 필요한 정보 선택 (필수/추가)

### [단계 3] 환경 설정 (로그인 오픈 API 서비스 환경)
1. 서비스 환경: `PC 웹` 선택
2. 서비스 URL: `http://localhost:3000`
3. **네이버 로그인 Callback URL**: `http://localhost:3000/api/auth/callback/naver`

### [단계 4] 필수 키 수집
*   **Client ID**: (Java 백엔드 설정용)
*   **Client Secret**: (Java 백엔드 설정용)

---

## 3. 🛠️ 백엔드 연동 준비 (Java & Python)

발급받은 키들은 프로젝트 루트의 `.env` 파일에 다음과 같이 추가될 예정입니다.

```env
# Kakao
KAKAO_CLIENT_ID=여러분의_REST_API_키
KAKAO_CLIENT_SECRET=여러분의_Client_Secret
KAKAO_REDIRECT_URI=http://localhost:3000/api/auth/callback/kakao

# Naver
NAVER_CLIENT_ID=여러분의_Client_ID
NAVER_CLIENT_SECRET=여러분의_Client_Secret
NAVER_REDIRECT_URI=http://localhost:3000/api/auth/callback/naver
```

---

## 🚀 다음 작업 예고
키 발급이 완료되면, Java 백엔드에서 위 정보를 바탕으로 유저 정보를 가져오고 **우리 서비스 전용 JWT**를 발행하는 로직을 구현할 것입니다.
