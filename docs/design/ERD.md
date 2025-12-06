# 📊 데이터베이스 설계 (ERD)

> **버전**: v0.1 (초안)  
> **최종 수정**: 2025.11.30  
> **DB**: PostgreSQL 16.x

---

## 📐 ERD 다이어그램

### 전체 구조

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CORE ENTITIES                                   │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│      users       │       │     avatars      │       │    garments      │
├──────────────────┤       ├──────────────────┤       ├──────────────────┤
│ id (PK)          │──┐    │ id (PK)          │       │ id (PK)          │
│ email            │  │    │ user_id (FK)     │◀──┐   │ user_id (FK)     │◀──┐
│ password_hash    │  └───▶│ name             │   │   │ name             │   │
│ name             │       │ face_image_url   │   │   │ original_url     │   │
│ profile_image    │       │ mesh_data_url    │   │   │ segmented_url    │   │
│ subscription     │       │ body_height      │   │   │ category         │   │
│ created_at       │       │ body_weight      │   │   │ color            │   │
│ updated_at       │       │ body_type        │   │   │ created_at       │   │
│ deleted_at       │       │ is_default       │   │   └──────────────────┘   │
└──────────────────┘       │ created_at       │   │            │             │
         │                 │ updated_at       │   │            │             │
         │                 └──────────────────┘   │            │             │
         │                          │             │            │             │
         │                          │             │            │             │
         │                          ▼             │            ▼             │
         │                 ┌──────────────────┐   │   ┌──────────────────┐   │
         │                 │  tryon_results   │   │   │ garment_metadata │   │
         │                 ├──────────────────┤   │   ├──────────────────┤   │
         │                 │ id (PK)          │   │   │ id (PK)          │   │
         └────────────────▶│ user_id (FK)     │   │   │ garment_id (FK)  │◀──┘
                           │ avatar_id (FK)   │◀──┘   │ brand            │
                           │ garment_id (FK)  │◀──────│ size             │
                           │ result_image_url │       │ material         │
                           │ thumbnail_url    │       │ price            │
                           │ processing_time  │       │ external_url     │
                           │ status           │       └──────────────────┘
                           │ is_favorite      │
                           │ created_at       │
                           └──────────────────┘


┌─────────────────────────────────────────────────────────────────────────────┐
│                           SYSTEM ENTITIES                                    │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│   job_queue      │       │   usage_logs     │       │  subscriptions   │
├──────────────────┤       ├──────────────────┤       ├──────────────────┤
│ id (PK)          │       │ id (PK)          │       │ id (PK)          │
│ user_id (FK)     │       │ user_id (FK)     │       │ user_id (FK)     │
│ job_type         │       │ action           │       │ plan_type        │
│ status           │       │ resource_type    │       │ status           │
│ input_data       │       │ resource_id      │       │ started_at       │
│ output_data      │       │ ip_address       │       │ expires_at       │
│ error_message    │       │ user_agent       │       │ payment_id       │
│ created_at       │       │ created_at       │       │ created_at       │
│ started_at       │       └──────────────────┘       └──────────────────┘
│ completed_at     │
└──────────────────┘
```

---

## 📝 테이블 상세 명세

### 1. users (사용자)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | UUID | PK | 고유 식별자 |
| email | VARCHAR(255) | UNIQUE, NOT NULL | 이메일 (로그인 ID) |
| password_hash | VARCHAR(255) | NOT NULL | 암호화된 비밀번호 |
| name | VARCHAR(100) | | 사용자 이름 |
| profile_image | VARCHAR(500) | | 프로필 이미지 URL |
| subscription | VARCHAR(20) | DEFAULT 'free' | 구독 유형 |
| created_at | TIMESTAMP | DEFAULT NOW() | 생성일 |
| updated_at | TIMESTAMP | | 수정일 |
| deleted_at | TIMESTAMP | | 삭제일 (소프트삭제) |

**인덱스**:
- `idx_users_email` (email)
- `idx_users_subscription` (subscription)

---

### 2. avatars (아바타)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | UUID | PK | 고유 식별자 |
| user_id | UUID | FK → users | 소유자 |
| name | VARCHAR(100) | DEFAULT 'My Avatar' | 아바타 이름 |
| face_image_url | VARCHAR(500) | NOT NULL | 얼굴 원본 이미지 |
| mesh_data_url | VARCHAR(500) | | 3D 메시 데이터 URL |
| body_height | INT | | 키 (cm) |
| body_weight | INT | | 몸무게 (kg) |
| body_type | VARCHAR(20) | | 체형 (slim/regular/athletic) |
| is_default | BOOLEAN | DEFAULT FALSE | 기본 아바타 여부 |
| created_at | TIMESTAMP | DEFAULT NOW() | 생성일 |
| updated_at | TIMESTAMP | | 수정일 |

**인덱스**:
- `idx_avatars_user_id` (user_id)
- `idx_avatars_is_default` (user_id, is_default)

---

### 3. garments (의상)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | UUID | PK | 고유 식별자 |
| user_id | UUID | FK → users | 업로더 |
| name | VARCHAR(200) | | 의상 이름 |
| original_url | VARCHAR(500) | NOT NULL | 원본 이미지 URL |
| segmented_url | VARCHAR(500) | | 세그멘트된 이미지 URL |
| category | VARCHAR(50) | | 카테고리 (top/bottom/dress) |
| color | VARCHAR(50) | | 주요 색상 |
| status | VARCHAR(20) | DEFAULT 'pending' | 처리 상태 |
| created_at | TIMESTAMP | DEFAULT NOW() | 생성일 |

**인덱스**:
- `idx_garments_user_id` (user_id)
- `idx_garments_category` (category)
- `idx_garments_status` (status)

---

### 4. tryon_results (착용 결과)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | UUID | PK | 고유 식별자 |
| user_id | UUID | FK → users | 소유자 |
| avatar_id | UUID | FK → avatars | 사용된 아바타 |
| garment_id | UUID | FK → garments | 사용된 의상 |
| result_image_url | VARCHAR(500) | | 결과 이미지 URL |
| thumbnail_url | VARCHAR(500) | | 썸네일 URL |
| processing_time | INT | | 처리 시간 (ms) |
| status | VARCHAR(20) | DEFAULT 'pending' | 상태 |
| is_favorite | BOOLEAN | DEFAULT FALSE | 즐겨찾기 |
| created_at | TIMESTAMP | DEFAULT NOW() | 생성일 |

**인덱스**:
- `idx_results_user_id` (user_id)
- `idx_results_status` (status)
- `idx_results_favorite` (user_id, is_favorite)

---

### 5. job_queue (작업 큐)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | UUID | PK | 고유 식별자 |
| user_id | UUID | FK → users | 요청자 |
| job_type | VARCHAR(50) | NOT NULL | 작업 유형 |
| status | VARCHAR(20) | DEFAULT 'pending' | 상태 |
| input_data | JSONB | | 입력 데이터 |
| output_data | JSONB | | 출력 데이터 |
| error_message | TEXT | | 에러 메시지 |
| priority | INT | DEFAULT 0 | 우선순위 |
| created_at | TIMESTAMP | DEFAULT NOW() | 생성일 |
| started_at | TIMESTAMP | | 시작일 |
| completed_at | TIMESTAMP | | 완료일 |

**상태값**:
- `pending`: 대기중
- `processing`: 처리중
- `completed`: 완료
- `failed`: 실패
- `cancelled`: 취소됨

---

### 6. subscriptions (구독)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | UUID | PK | 고유 식별자 |
| user_id | UUID | FK → users | 구독자 |
| plan_type | VARCHAR(20) | NOT NULL | 플랜 유형 |
| status | VARCHAR(20) | DEFAULT 'active' | 상태 |
| tryon_limit | INT | | 월 Try-On 제한 |
| tryon_used | INT | DEFAULT 0 | 사용량 |
| started_at | TIMESTAMP | | 시작일 |
| expires_at | TIMESTAMP | | 만료일 |
| payment_id | VARCHAR(100) | | 결제 ID |
| created_at | TIMESTAMP | DEFAULT NOW() | 생성일 |

**플랜 유형**:
- `free`: 무료 (월 5회)
- `basic`: 기본 (월 50회, ~₩9,900)
- `pro`: 프로 (월 200회, ~₩29,900)
- `unlimited`: 무제한 (B2B)

---

## 🔗 관계 정의

```sql
-- Foreign Keys
ALTER TABLE avatars ADD CONSTRAINT fk_avatars_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE garments ADD CONSTRAINT fk_garments_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE tryon_results ADD CONSTRAINT fk_results_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE tryon_results ADD CONSTRAINT fk_results_avatar 
    FOREIGN KEY (avatar_id) REFERENCES avatars(id) ON DELETE SET NULL;

ALTER TABLE tryon_results ADD CONSTRAINT fk_results_garment 
    FOREIGN KEY (garment_id) REFERENCES garments(id) ON DELETE SET NULL;

ALTER TABLE job_queue ADD CONSTRAINT fk_jobs_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE subscriptions ADD CONSTRAINT fk_subscriptions_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
```

---

## 📋 dbdiagram.io 코드

아래 코드를 [dbdiagram.io](https://dbdiagram.io)에 붙여넣으면 시각적 ERD를 볼 수 있습니다.

```dbml
// Virtual Try-On ERD

Table users {
  id uuid [pk]
  email varchar(255) [unique, not null]
  password_hash varchar(255) [not null]
  name varchar(100)
  profile_image varchar(500)
  subscription varchar(20) [default: 'free']
  created_at timestamp [default: `now()`]
  updated_at timestamp
  deleted_at timestamp
}

Table avatars {
  id uuid [pk]
  user_id uuid [ref: > users.id]
  name varchar(100) [default: 'My Avatar']
  face_image_url varchar(500) [not null]
  mesh_data_url varchar(500)
  body_height int
  body_weight int
  body_type varchar(20)
  is_default boolean [default: false]
  created_at timestamp [default: `now()`]
  updated_at timestamp
}

Table garments {
  id uuid [pk]
  user_id uuid [ref: > users.id]
  name varchar(200)
  original_url varchar(500) [not null]
  segmented_url varchar(500)
  category varchar(50)
  color varchar(50)
  status varchar(20) [default: 'pending']
  created_at timestamp [default: `now()`]
}

Table tryon_results {
  id uuid [pk]
  user_id uuid [ref: > users.id]
  avatar_id uuid [ref: > avatars.id]
  garment_id uuid [ref: > garments.id]
  result_image_url varchar(500)
  thumbnail_url varchar(500)
  processing_time int
  status varchar(20) [default: 'pending']
  is_favorite boolean [default: false]
  created_at timestamp [default: `now()`]
}

Table job_queue {
  id uuid [pk]
  user_id uuid [ref: > users.id]
  job_type varchar(50) [not null]
  status varchar(20) [default: 'pending']
  input_data jsonb
  output_data jsonb
  error_message text
  priority int [default: 0]
  created_at timestamp [default: `now()`]
  started_at timestamp
  completed_at timestamp
}

Table subscriptions {
  id uuid [pk]
  user_id uuid [ref: > users.id]
  plan_type varchar(20) [not null]
  status varchar(20) [default: 'active']
  tryon_limit int
  tryon_used int [default: 0]
  started_at timestamp
  expires_at timestamp
  payment_id varchar(100)
  created_at timestamp [default: `now()`]
}
```

---

## 📈 확장 고려사항

### Phase 2 추가 테이블
- `outfit_collections`: 의상 조합 저장
- `shared_results`: 공유된 결과물
- `feedback`: 사용자 피드백

### Phase 3 추가 테이블
- `external_products`: 외부 쇼핑몰 연동
- `recommendations`: 추천 데이터
- `analytics`: 분석 데이터

---

*이 문서는 초안이며, 개발 진행에 따라 업데이트됩니다.*

