# 관리자 수동 작업 목록

결제 서비스 운영 시 관리자가 수동으로 진행할 수 있는 작업 목록입니다.

> **최종 업데이트**: 2026-02-24 — 플랜/약관 노출·버전 관리, 대시보드 기간별·전월대비, 거래 날짜 필터 반영

---

## 1. 회원관리 (`/members`)

| 작업 | 경로 | 설명 |
|------|------|------|
| **회원 정지** | `POST /api/admin/members/{id}/suspend` | 해당 회원의 `suspended_at` 설정. 정지 시 로그인 불가, `/auth/me` 403 반환 |
| **회원 정지 해제** | `POST /api/admin/members/{id}/unsuspend` | `suspended_at` 해제 |
| **회원 탈퇴 처리** | `DELETE /api/admin/members/{id}` | 소프트 삭제 (`deleted_at` 설정). 복구 불가 |

---

## 2. 거래관리 (`/transactions`)

| 작업 | 경로 | 설명 |
|------|------|------|
| **결제 목록 조회** | `GET /api/admin/payments?fromDate=&toDate=` | 날짜 범위로 거래 내역 필터 (YYYY-MM-DD) |
| **결제 취소** | `POST /api/admin/payments/{id}/cancel` | 결제 상태를 `cancelled`로 변경, `cancelled_at` 설정 |
| **결제 강제 성공** | `POST /api/v1/payments/{id}/force-success` | (api-service) 테스트/관리용. Subscription 미존재 시 생성 |
| **결제 강제 실패** | `POST /api/v1/payments/{id}/force-failure` | (api-service) 테스트/관리용 |

> ⚠️ `force-success`, `force-failure`는 api-service에 있으며, PG 시뮬레이션 환경에서 사용. 실제 PG 연동 시에는 Admin API의 cancel만 사용 권장.

---

## 3. 구독관리 (`/subscriptions`)

| 작업 | 경로 | 설명 |
|------|------|------|
| **구독 취소** | `POST /api/admin/subscriptions/{id}/cancel` | 구독 `status`를 `cancelled`로 변경, `cancelled_at` 설정. 당월 말까지 이용 가능 |

---

## 4. 플랜관리 (`/plans`)

| 작업 | 경로 | 설명 |
|------|------|------|
| **플랜 등록** | `POST /api/admin/plans` | 새 플랜 추가 (planCode, planName, priceMonthly 등). `is_active=true`면 Studio 결제 화면에 노출 |
| **플랜 수정** | `PUT /api/admin/plans/{id}` | `plan_config` 수정 (가격, 토큰 한도, 활성화 여부 등) |
| **노출 토글** | UI 배지 클릭 | 목록에서 "노출"/"미노출" 배지 클릭 시 즉시 전환 |

---

## 5. 약관관리 (`/terms`)

| 작업 | 경로 | 설명 |
|------|------|------|
| **약관 등록** | `POST /api/admin/terms` | 새 약관 추가 |
| **약관 수정** | `PUT /api/admin/terms/{id}` | 기존 약관 수정 |
| **새 버전 등록** | `POST /api/admin/terms/{id}/new-version` | 기존 약관 복사 후 새 version, effectiveAt 지정 |
| **노출 설정** | UI 토글 | `is_active` — false면 가입/결제 화면 미노출 |
| **약관 삭제** | `DELETE /api/admin/terms/{id}` | 약관 삭제 |

---

## 6. 매출 대시보드 (`/dashboard`)

| 기능 | 설명 |
|------|------|
| **총 매출** | completed 결제 금액 합계 |
| **기간별 매출** | `GET /api/admin/dashboard/revenue?period=week\|month\|quarter\|half` — 당기 vs 전기 막대+선 차트 |
| **월별 매출** | 월별 completed 결제 합계 |
| **플랜별 매출** | plan_id별 completed 결제 합계, 툴팁에 구독별 세부 표시 |

---

## 작업별 영향 요약

| 작업 | 영향 |
|------|------|
| 회원 정지 | 로그인 불가, 기존 세션 `/auth/me` 403 |
| 회원 정지 해제 | 로그인 및 서비스 이용 재개 |
| 회원 탈퇴 | 목록에서 제외, 복구 불가 |
| 결제 취소 | 결제 상태만 변경. 구독/User는 별도 처리 필요 |
| 구독 취소 | 구독 종료. User.subscription은 별도 갱신 로직 필요 |

---

## 📚 관련 문서
- [API 명세서](./API_SPECIFICATION.md) — Admin API 상세
