# 보안 가이드 (JWT, HTTPS, XSS)

Quantum Studio/Admin의 인증·통신 보안 권장사항을 정리한 문서입니다.

---

## 1. HTTPS 사용

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

## 2. XSS와 JWT 저장

### 현재 방식

- JWT는 **Authorization: Bearer \<token\>** 헤더로 전송 (표준적).
- 토큰은 **localStorage**에 저장 (Studio: `accessToken`, Admin: `adminToken`).

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

현재 코드는 **개발 편의**를 위해 localStorage + Authorization 헤더 방식을 유지하고 있으며,  
**프로덕션 배포 시** 위와 같이 HttpOnly 쿠키 전환을 권장합니다.

---

## 3. 개발 vs 운영 체크리스트

| 항목 | 개발 | 운영 |
|------|------|------|
| 프로토콜 | HTTP (localhost) | **HTTPS만** |
| HTTPS 강제 | 비활성 | `app.https-only=true` (또는 프록시에서 리다이렉트) |
| HSTS | 비활성 | `app.hsts=true` 권장 |
| 토큰 저장 | localStorage | **HttpOnly 쿠키** 권장 |
| CORS | localhost 허용 | 실제 프론트 도메인만 허용 |
| JWT Secret | 강한 비밀키 사용 | **환경변수로 관리**, 공개 저장소에 노출 금지 |

---

## 4. 설정 요약

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

## 5. 참고 링크

- [OWASP JWT Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [OWASP XSS Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)
