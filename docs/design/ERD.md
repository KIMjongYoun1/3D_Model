# 📊 데이터베이스 설계 (ERD)

Quantum Studio의 데이터베이스는 사용자 인증, 결제, 그리고 AI 분석 기반의 3D 시각화 데이터를 효율적으로 관리하도록 설계되었습니다.

> **최종 업데이트**: 2026-02-24

---

## 📐 ERD 다이어그램

### 전체 구조

```text
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│      users       │       │    projects      │       │      nodes       │
├──────────────────┤       ├──────────────────┤       ├──────────────────┤
│ id (PK)          │──┐    │ id (PK)          │──┐    │ id (PK)          │
│ email            │  │    │ user_id (FK)     │  │    │ project_id (FK)  │
│ password_hash    │  └───▶│ name             │  └───▶│ label            │
│ provider         │       │ description      │       │ value            │
│ refresh_token    │       │ created_at       │       │ position_x       │
│ created_at       │       └──────────────────┘       │ position_y       │
└──────────────────┘                                  │ position_z       │
         │                                            │ created_at       │
         │                                            └──────────────────┘
         ▼
┌──────────────────┐
│  subscriptions   │
├──────────────────┤
│ id (PK)          │
│ user_id (FK)     │
│ plan_type        │
│ status           │
│ expires_at       │
└──────────────────┘
```

---

## 📝 테이블 상세 명세

### 1. users (사용자)
- **id**: UUID (Primary Key)
- **email**: VARCHAR (Unique, Not Null)
- **password_hash**: VARCHAR (Null for Social Login)
- **provider**: VARCHAR (LOCAL, NAVER, KAKAO)
- **refresh_token**: VARCHAR (JWT Refresh Token)
- **suspended_at**: TIMESTAMP (정지 시각, NULL이면 활성)
- **deleted_at**: TIMESTAMP (소프트 삭제)
- **created_at**: TIMESTAMP

### 2. projects (시각화 프로젝트)
- **id**: UUID (Primary Key)
- **user_id**: UUID (Foreign Key → users.id)
- **name**: VARCHAR (프로젝트 명)
- **description**: TEXT (설명)
- **created_at**: TIMESTAMP

### 3. nodes (3D 시각화 노드)
- **id**: UUID (Primary Key)
- **project_id**: UUID (Foreign Key → projects.id)
- **label**: VARCHAR (노드 라벨)
- **value**: TEXT (노드 데이터/값)
- **position_x/y/z**: FLOAT (3D 공간 좌표)
- **created_at**: TIMESTAMP

### 4. subscriptions (구독 정보)
- **id**: UUID (Primary Key)
- **user_id**: UUID (Foreign Key → users.id)
- **plan_type**: VARCHAR (FREE, BASIC, PRO)
- **status**: VARCHAR (ACTIVE, EXPIRED, CANCELLED)
- **expires_at**: TIMESTAMP
- **cancelled_at**: TIMESTAMP

### 5. plan_config (요금제/플랜 설정)
- **id**: UUID (Primary Key)
- **plan_code**: VARCHAR (Unique, free, pro 등)
- **plan_name**: VARCHAR (표시명)
- **price_monthly**: BIGINT (월 요금, 원)
- **token_limit**: INT (월 토큰 한도, NULL=무제한)
- **is_active**: BOOLEAN (노출/판매 여부)
- **sort_order**: INT (정렬 순서)

### 6. terms (약관)
- **id**: UUID (Primary Key)
- **type**: VARCHAR (TERMS_OF_SERVICE, PRIVACY_POLICY 등)
- **version**: VARCHAR (1.0, 2024.01 등)
- **category**: VARCHAR (SIGNUP 가입, PAYMENT 결제)
- **required**: BOOLEAN (필수/선택)
- **content**: TEXT (약관 전문)

---

## 🏛 마이그레이션 관리
- **Java (backend-java)**: Flyway V1~V19. `users`, `subscriptions`, `payments`, `plan_config`, `terms`, `user_terms_agreement` 등.
- **Python (backend-python)**: Alembic을 사용하여 `projects`, `nodes` 등 분석 데이터 테이블 관리.

---

*이 설계는 서비스 고도화에 따라 지속적으로 업데이트됩니다.*
