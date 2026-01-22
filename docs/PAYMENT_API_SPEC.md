# 💳 결제 API 연동 규격서

결제 시뮬레이션 시스템의 API 규격서입니다.

---

## 📋 개요

### 결제 모드
- **시뮬레이션 모드**: 실제 PG사 연동 없이 결제 성공/실패 테스트
- **비용**: 무료 (실제 결제 수수료 없음)

### 기본 설정
```yaml
PAYMENT_SIMULATION_MODE=true
PAYMENT_SUCCESS_RATE=0.9  # 90% 성공 확률
```

---

## 🔌 API 엔드포인트

### Base URL
```
http://localhost:8080/api/v1/payments
```

---

## 📤 1. 결제 요청 생성

### `POST /api/v1/payments/request`

결제 요청을 생성하고 시뮬레이션 처리합니다.

#### Request Body
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "subscriptionId": "660e8400-e29b-41d4-a716-446655440000",
  "paymentMethod": "card",
  "amount": 10000
}
```

#### 필수 파라미터

| 파라미터 | 타입 | 설명 | 예시 |
|---------|------|------|------|
| `userId` | UUID (String) | 결제하는 사용자 ID | `"550e8400-e29b-41d4-a716-446655440000"` |
| `subscriptionId` | UUID (String) | 구독 ID (선택사항) | `"660e8400-e29b-41d4-a716-446655440000"` |
| `paymentMethod` | String | 결제 수단 | `"card"` (카드), `"bank_transfer"` (계좌이체) |
| `amount` | Long | 결제 금액 (원 단위) | `10000` |

#### Response (200 OK)
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "subscriptionId": "660e8400-e29b-41d4-a716-446655440000",
  "paymentMethod": "card",
  "amount": 10000,
  "status": "completed",
  "pgProvider": "simulation",
  "pgTransactionId": "SIM_ABC12345",
  "pgResponse": "{\"status\":\"success\",\"message\":\"결제가 성공적으로 완료되었습니다.\",\"transaction_id\":\"SIM_ABC12345\"}",
  "createdAt": "2025-12-26T10:30:00",
  "completedAt": "2025-12-26T10:30:01",
  "cancelledAt": null
}
```

#### Response 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | UUID | 결제 ID |
| `userId` | UUID | 사용자 ID |
| `subscriptionId` | UUID | 구독 ID |
| `paymentMethod` | String | 결제 수단 |
| `amount` | Long | 결제 금액 (원) |
| `status` | String | 결제 상태: `pending`, `completed`, `failed`, `cancelled` |
| `pgProvider` | String | PG사 (시뮬레이션: `"simulation"`) |
| `pgTransactionId` | String | 거래 ID (시뮬레이션용) |
| `pgResponse` | String (JSON) | 응답 데이터 (JSON 문자열) |
| `createdAt` | String (ISO 8601) | 생성일시 |
| `completedAt` | String (ISO 8601) | 완료일시 (성공 시) |
| `cancelledAt` | String (ISO 8601) | 취소일시 (취소 시) |

#### 결제 상태 (`status`)

| 상태 | 설명 |
|------|------|
| `pending` | 결제 대기 중 |
| `completed` | 결제 완료 |
| `failed` | 결제 실패 |
| `cancelled` | 결제 취소 |

#### 에러 응답 (400 Bad Request)
```json
{
  "error": "Bad Request",
  "message": "필수 파라미터가 누락되었습니다.",
  "details": {
    "missingFields": ["userId", "amount"]
  }
}
```

---

## 📥 2. 결제 정보 조회

### `GET /api/v1/payments/{paymentId}`

특정 결제 정보를 조회합니다.

#### Path Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `paymentId` | UUID | 결제 ID |

#### Response (200 OK)
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "subscriptionId": "660e8400-e29b-41d4-a716-446655440000",
  "paymentMethod": "card",
  "amount": 10000,
  "status": "completed",
  "pgProvider": "simulation",
  "pgTransactionId": "SIM_ABC12345",
  "pgResponse": "{\"status\":\"success\",\"message\":\"결제가 성공적으로 완료되었습니다.\"}",
  "createdAt": "2025-12-26T10:30:00",
  "completedAt": "2025-12-26T10:30:01",
  "cancelledAt": null
}
```

#### 에러 응답 (404 Not Found)
```json
{
  "error": "Not Found",
  "message": "결제를 찾을 수 없습니다."
}
```

---

## 📋 3. 사용자 결제 이력 조회

### `GET /api/v1/payments/user/{userId}`

사용자의 결제 이력을 조회합니다 (최신순).

#### Path Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `userId` | UUID | 사용자 ID |

#### Response (200 OK)
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440000",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "subscriptionId": "660e8400-e29b-41d4-a716-446655440000",
    "paymentMethod": "card",
    "amount": 10000,
    "status": "completed",
    "pgProvider": "simulation",
    "pgTransactionId": "SIM_ABC12345",
    "pgResponse": "{\"status\":\"success\",\"message\":\"결제가 성공적으로 완료되었습니다.\"}",
    "createdAt": "2025-12-26T10:30:00",
    "completedAt": "2025-12-26T10:30:01",
    "cancelledAt": null
  },
  {
    "id": "880e8400-e29b-41d4-a716-446655440000",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "subscriptionId": null,
    "paymentMethod": "bank_transfer",
    "amount": 5000,
    "status": "failed",
    "pgProvider": "simulation",
    "pgTransactionId": "SIM_XYZ67890",
    "pgResponse": "{\"status\":\"failed\",\"message\":\"결제가 실패했습니다. (시뮬레이션)\"}",
    "createdAt": "2025-12-25T15:20:00",
    "completedAt": null,
    "cancelledAt": null
  }
]
```

---

## 🧪 4. 결제 강제 성공 (테스트용)

### `POST /api/v1/payments/{paymentId}/force-success`

결제를 강제로 성공 처리합니다 (테스트용).

#### Path Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `paymentId` | UUID | 결제 ID |

#### Response (200 OK)
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "status": "completed",
  "pgResponse": "{\"status\":\"success\",\"message\":\"결제가 강제로 성공 처리되었습니다.\"}",
  "completedAt": "2025-12-26T10:35:00"
}
```

---

## ❌ 5. 결제 강제 실패 (테스트용)

### `POST /api/v1/payments/{paymentId}/force-failure`

결제를 강제로 실패 처리합니다 (테스트용).

#### Path Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `paymentId` | UUID | 결제 ID |

#### Response (200 OK)
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "status": "failed",
  "pgResponse": "{\"status\":\"failed\",\"message\":\"결제가 강제로 실패 처리되었습니다.\"}",
  "completedAt": null
}
```

---

## 📊 데이터 모델

### Payment Entity

```java
{
  "id": UUID,
  "userId": UUID,
  "subscriptionId": UUID (nullable),
  "paymentMethod": String,  // "card", "bank_transfer"
  "amount": Long,           // 원 단위
  "status": String,         // "pending", "completed", "failed", "cancelled"
  "pgProvider": String,     // "simulation"
  "pgTransactionId": String,
  "pgResponse": String (JSON),
  "createdAt": LocalDateTime,
  "completedAt": LocalDateTime (nullable),
  "cancelledAt": LocalDateTime (nullable)
}
```

---

## 🔄 결제 플로우

```
1. 클라이언트 → POST /api/v1/payments/request
   └─▶ 결제 요청 생성

2. 서버 → 결제 시뮬레이션 처리
   ├─▶ 성공 확률에 따라 성공/실패 결정
   ├─▶ 성공: status = "completed"
   └─▶ 실패: status = "failed"

3. 서버 → Response 반환
   └─▶ 결제 정보 (성공/실패 포함)

4. (선택) 테스트용 강제 처리
   ├─▶ POST /api/v1/payments/{id}/force-success
   └─▶ POST /api/v1/payments/{id}/force-failure
```

---

## ⚙️ 설정

### 환경 변수

| 변수명 | 타입 | 기본값 | 설명 |
|--------|------|--------|------|
| `PAYMENT_SIMULATION_MODE` | Boolean | `true` | 시뮬레이션 모드 활성화 |
| `PAYMENT_SUCCESS_RATE` | Double | `0.9` | 결제 성공 확률 (0.0 ~ 1.0) |

### application.yml
```yaml
payment:
  success-rate: ${PAYMENT_SUCCESS_RATE:0.9}
```

---

## 📝 사용 예시

### cURL 예시

#### 결제 요청
```bash
curl -X POST http://localhost:8080/api/v1/payments/request \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "subscriptionId": "660e8400-e29b-41d4-a716-446655440000",
    "paymentMethod": "card",
    "amount": 10000
  }'
```

#### 결제 조회
```bash
curl http://localhost:8080/api/v1/payments/770e8400-e29b-41d4-a716-446655440000
```

#### 강제 성공 (테스트)
```bash
curl -X POST http://localhost:8080/api/v1/payments/770e8400-e29b-41d4-a716-446655440000/force-success
```

---

## ⚠️ 주의사항

1. **시뮬레이션 모드**: 실제 결제가 발생하지 않습니다.
2. **테스트용 API**: `force-success`, `force-failure`는 개발/테스트 환경에서만 사용하세요.
3. **프로덕션**: 실제 PG사 연동 시 이 규격서를 참고하여 구현하세요.

---

*이 규격서는 결제 시뮬레이션 시스템의 API 명세입니다.*




