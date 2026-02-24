# 보안 가이드 (JWT, HTTPS, XSS, Redirect, URL)

Quantum Studio/Admin의 인증·통신 보안 권장사항을 정리한 문서입니다.

> **최종 업데이트**: 2026-02-24 — Open Redirect, References URL 검증 반영

---

## 1. Open Redirect 방지

### 왜 필요한가?

로그인 후 원래 보던 페이지로 돌아가기 위해 `?redirect=/payment` 같은 파라미터를 사용한다. 이 값을 **검증하지 않으면** 공격자가 `/login?redirect=https://가짜은행.com` 같은 링크를 만들어 사용자를 유도할 수 있다. 사용자가 로그인하면 우리 사이트를 거쳐 가짜 사이트로 이동하게 되고, "방금 로그인한 사이트에서 보내준 거니까 믿을 만하다"고 느끼게 되어 **피싱**에 악용될 수 있다.

검증을 하면 `redirect`가 `/payment`, `/mypage` 같은 **우리 사이트 내부 경로**인지 확인하고, `https://...` 같은 **외부 주소**는 무시하여 기본 페이지로 보낸다. 따라서 "우리 사이트를 경유해 다른 사이트로 보내는" 일이 발생하지 않는다.

### 적용
- **authRedirect** (`frontend-studio/lib/authRedirect.ts`, `frontend-admin/lib/authRedirect.ts`)
- `?redirect=` 파라미터 검증. 동일 출처 경로(`/`로 시작)만 허용.
- `//`, `://`, `javascript:`, `data:`, `..` 등 차단.

### 적용 위치
| 위치 | 용도 |
|------|------|
| Studio 로그인 | `?redirect=` 검증 |
| Admin 로그인 | `?redirect=` 검증 |
| Naver 콜백, auth/agree | sessionStorage 값 검증 |
| Header, MyPage | pathname 저장 시 검증 |

---

## 2. References URL 검증 (외부 링크)

### 왜 필요한가?

AI 분석 결과의 "참고 링크"는 AI가 생성하거나 API 응답에서 오는 값이라 **우리가 직접 제어할 수 없다**. 검증을 하지 않으면:

1. **악성 URL**: `javascript:alert('해킹')` 같은 스크립트 URL이 들어가 사용자가 클릭 시 브라우저가 실행하여 **XSS**가 발생할 수 있다.
2. **MITM(중간자 공격)**: 네트워크를 가로채 API 응답을 조작해 `https://악성사이트.com` 같은 주소를 넣을 수 있다.
3. **Tab-nabbing**: 새 탭으로 열린 페이지가 `window.opener`로 우리 페이지를 조작할 수 있다. `rel="noopener noreferrer"`를 붙이지 않으면 이런 위험이 있다.

검증을 하면 `http://`, `https://` 같은 **일반 웹 링크만** 허용하고, `javascript:`, `data:` 같은 **실행 가능한 URL**은 차단한다. 검증 실패 시 링크를 비활성화하고 텍스트만 표시하여 **클릭으로 인한 공격**을 막을 수 있다.

### 적용
- **safeUrl** (`frontend-studio/lib/safeUrl.ts`)
- **url_sanitizer** (`backend-python/app/core/url_sanitizer.py`)

### 규칙
- http/https만 허용. javascript:, data:, file: 등 차단.
- MITM으로 조작된 응답에서 악성 URL 차단.
- 외부 링크에 `rel="noopener noreferrer"` 적용 (Tab-nabbing 방지).

### 적용 위치
- **프론트**: DraggableWindow의 `ref.url`. 검증 실패 시 링크 비활성화(텍스트만 표시).
- **백엔드**: 매핑 결과 반환·저장 시 `sanitize_mapping_result()` 적용.

---

## 3. HTTPS 사용

### 왜 HTTPS인가?

- **JWT·쿠키 탈취 방지**: HTTP는 평문 전송이라 중간에서 토큰이 노출될 수 있음. HTTPS로 암호화해야 함.
- **MITM 방지**: 중간자 공격으로 로그인·API 요청이 변조되는 것을 막을 수 있음.
- **브라우저 정책**: `Secure` 쿠키, 일부 Web API는 HTTPS에서만 동작.

### 현재 구조

- **개발**: `http://localhost` 사용 (편의상 HTTP 허용).
- **운영**: 반드시 **HTTPS만** 사용할 것.

### 운영 적용 방법

1. **리버스 프록시에서 HTTPS 종료** (권장)  
   Nginx/Cloudflare 등에서 SSL 종료 후 `X-Forwarded-Proto: https` 헤더로 백엔드에 전달.

2. **백엔드 HTTPS 강제**  
   `app.https-only=true` 설정 시, HTTP 요청은 403 처리.  
   (Service/Admin 공통, `application.yml` 또는 환경변수 `APP_HTTPS_ONLY=true`)

3. **HSTS 헤더**  
   `app.hsts=true` 설정 시 응답에 `Strict-Transport-Security` 추가.  
   브라우저가 이후 요청을 자동으로 HTTPS로 보냄.

---

## 4. XSS와 JWT 저장

### 현재 방식

- JWT는 **Authorization: Bearer \<token\>** 헤더로 전송 (표준적).
- Studio: JWT는 **HttpOnly 쿠키**로 전달. Admin: JWT는 **HttpOnly 쿠키**(`admin_token`)로 전달.

### localStorage의 위험

- **XSS(Cross-Site Scripting)** 발생 시, 악성 스크립트가 `localStorage`를 읽어 토큰을 탈취할 수 있음.
- 따라서 **운영 환경에서는 XSS 방지 + 가능하면 토큰을 JS에서 접근 불가하게** 하는 것이 좋음.

### 권장: HttpOnly + Secure + SameSite 쿠키

| 구분 | localStorage (현재) | HttpOnly 쿠키 (권장) |
|------|---------------------|----------------------|
| XSS | JS로 읽기 가능 → 탈취 가능 | JS로 읽기 불가 → 탈취 어려움 |
| 전송 | 프론트에서 헤더에 직접 설정 | 브라우저가 자동으로 Cookie 헤더에 포함 |
| HTTPS | 별도 설정 필요 | `Secure` 플래그로 HTTPS에서만 전송 |

**운영에서 권장하는 흐름:**

1. 로그인 응답에서 JWT를 **쿠키로 설정**  
   - `Set-Cookie: access_token=<JWT>; HttpOnly; Secure; SameSite=Strict; Path=/api`
2. 백엔드 JWT 필터에서 **Cookie** 또는 **Authorization** 헤더 둘 다 처리 (기존 클라이언트 호환 가능).
3. 프론트는 `credentials: 'include'`로 요청만 보내고, 토큰을 JS로 다루지 않음.

Studio와 Admin 모두 **HttpOnly 쿠키** 방식으로 전환되어 있습니다.  
Authorization 헤더도 계속 지원하여 기존 클라이언트와의 호환성을 유지합니다.

---

## 5. 개발 vs 운영 체크리스트

| 항목 | 개발 | 운영 |
|------|------|------|
| 프로토콜 | HTTP (localhost) | **HTTPS만** |
| HTTPS 강제 | 비활성 | `app.https-only=true` (또는 프록시에서 리다이렉트) |
| HSTS | 비활성 | `app.hsts=true` 권장 |
| 토큰 저장 | ~~localStorage~~ | **HttpOnly 쿠키** (Studio, Admin 모두 적용됨) |
| CORS | localhost 허용 | 실제 프론트 도메인만 허용 |
| JWT Secret | 강한 비밀키 사용 | **환경변수로 관리**, 공개 저장소에 노출 금지 |

---

## 6. 설정 요약

### application.yml (또는 환경변수)

```yaml
# 프로덕션 예시 (application-prod.yml 또는 환경변수)
app:
  https-only: true   # HTTP 요청 403 (프록시 뒤에서 X-Forwarded-Proto 사용)
  hsts: true         # Strict-Transport-Security 헤더 추가
```

- `app.https-only`: `true`이면 비-HTTPS 요청 403.
- `app.hsts`: `true`이면 응답에 HSTS 헤더 추가.
- 개발 환경에서는 두 값 모두 `false` 또는 미설정.

---

## 7. 참고 링크

- [OWASP JWT Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [OWASP XSS Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)
