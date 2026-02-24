# 적용 내용 요약

> **최종 업데이트**: 2026-02-24

---

## 1. 구독 상태 표시 (마이페이지)

- **백엔드**: `UserResponse`에 `subscriptionStatus` 추가 (active/cancelled/null)
- **AuthController `/me`**: 활성 구독 → active, 해지신청(만료 전) → cancelled
- **프론트**: 마이페이지 Active Plan 카드에 "정상" / "해지 신청됨 (만료일까지 이용 가능)" 배지
- 해지 신청된 경우 "구독 해지 신청" 버튼 숨김

---

## 2. 매출 대시보드 고도화

- **막대+선 차트**: 당기 매출(막대) vs 전기 매출(선)
- **기간 선택**: 주별·월별·분기별·반기별
- **구독별 세부**: 툴팁에 플랜별 매출 표시
- **API**: `GET /api/admin/dashboard/revenue?period=month|week|quarter|half`

---

## 3. 거래내역 날짜별 조회

- **API**: `GET /api/admin/payments?fromDate=YYYY-MM-DD&toDate=YYYY-MM-DD`
- **프론트**: 거래 관리 화면에 날짜 범위 필터, 초기화 버튼

---

## 4. 플랜 관리 확장

- **새 플랜 등록**: Admin 플랜 관리 → "+ 새 플랜 등록" → `/plans/new` 폼
- **API**: `POST /api/admin/plans` (planCode, planName, priceMonthly 등)
- **노출 토글**: 목록에서 "노출"/"미노출" 배지 클릭 시 즉시 전환
- 등록한 플랜은 Studio 결제 화면에 즉시 반영 (is_active=true만)

---

## 5. 약관 관리 확장

- **노출 설정**: `terms.is_active` — false면 가입/결제 화면 미노출
- **새 버전 등록**: 편집 화면 "+ 새 버전 등록" → 기존 약관 복사, 새 version 입력
- **API**: `POST /api/admin/terms/{id}/new-version` (version, effectiveAt)
- **법령 준수**: 이전 약관 내역 전체 조회 가능 (allVersions, isActive 무관)

---

## 6. start-all.sh npm install

- 프론트엔드 기동 전 `frontend-studio`, `frontend-admin`에 `npm install` 실행
- recharts 등 새 의존성 자동 반영

---

## 7. 자동화 테스트

### 추가된 테스트

| 위치 | 테스트 대상 | 실행 방법 |
|------|-------------|-----------|
| `frontend-admin/lib/authRedirect.test.ts` | `authRedirect` (Open Redirect 방지) | `cd frontend-admin && npm run test` |
| `frontend-studio/lib/authRedirect.test.ts` | `authRedirect` | `cd frontend-studio && npm run test` |
| `frontend-studio/lib/safeUrl.test.ts` | `safeUrl` (외부 링크 URL 검증) | `cd frontend-studio && npm run test` |
| `backend-python/tests/test_url_sanitizer.py` | `url_sanitizer` (References URL 검증) | `cd backend-python && ./run_tests.sh` |

### 실행

```bash
# 프론트엔드 (Admin)
cd frontend-admin && npm run test

# 프론트엔드 (Studio)
cd frontend-studio && npm run test

# 백엔드 Python (스크립트 사용)
cd backend-python && ./run_tests.sh

# 또는 직접 pytest 실행
cd backend-python && python3 -m pytest tests/ -v
```

### Python 테스트 사전 준비

- `pytest`가 없으면 `run_tests.sh`가 자동으로 설치 시도
- 수동 설치: `pip install pytest` 또는 `pip3 install pytest`

### 테스트 내용

- **authRedirect**: 절대 경로 허용, `javascript:`, `data:`, 외부 URL, 경로 탐색 차단
- **safeUrl**: http/https만 허용, javascript/data/file 등 차단
- **url_sanitizer**: 매핑 결과 내 references의 url 검증, 유효하지 않으면 제거

---

## 8. Admin JWT HttpOnly 쿠키 전환

### 변경 전 (localStorage)

- JWT를 `localStorage.adminToken`에 저장
- XSS 시 `document.cookie` 또는 `localStorage` 접근으로 토큰 탈취 가능

### 변경 후 (HttpOnly 쿠키)

- **백엔드**: 로그인 시 `Set-Cookie: admin_token=...; HttpOnly; Secure; SameSite=Strict` 설정
- **프론트엔드**: `withCredentials: true`로 쿠키 자동 전송, JavaScript에서 토큰 접근 불가

### 수정된 파일

**백엔드 (Java)**
- `AdminCookieHelper.java`: 신규 - HttpOnly 쿠키 헬퍼
- `AdminCookieConfig.java`: 신규 - AdminCookieHelper Bean
- `AdminAuthController.java`: 로그인 시 쿠키 설정, 응답 본문에서 accessToken 제거, 로그아웃 엔드포인트 추가
- `AdminJwtAuthenticationFilter.java`: Authorization 헤더 없을 때 `admin_token` 쿠키에서 토큰 읽기

**프론트엔드 (Admin)**
- `lib/adminApi.ts`: 신규 - axios 인스턴스 withCredentials, checkAdminAuth, adminLogout
- `app/(auth)/login/page.tsx`: localStorage 제거, adminApi 사용
- `components/common/Header.tsx`: checkAdminAuth, adminLogout 사용
- `hooks/useRequireAdminAuth.ts`: checkAdminAuth로 인증 확인
- 모든 Admin API 호출 페이지: `getAdminAuthHeaders()` 제거, `adminApi` 사용

### CORS

- `AdminSecurityConfig`에 `setAllowCredentials(true)` 이미 설정됨
- `configuration.setAllowedOrigins`에 `http://localhost:3001` 포함

### 프로덕션

- `app.cookie-secure: true`로 설정 시 쿠키에 `Secure` 플래그 적용 (HTTPS 필수)
