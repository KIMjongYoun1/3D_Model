# 🔌 백엔드에서 데이터베이스 접근 방법

백엔드에서는 ORM(Object-Relational Mapping)을 사용하여 데이터베이스에 접근합니다.
직접 SQL을 작성하지 않고, 객체 지향 방식으로 데이터를 조작합니다.

---

## 🐍 Python Backend (SQLAlchemy)

### 구조

```
backend-python/app/
├── core/
│   └── database.py      # DB 연결 및 세션 관리
├── models/              # SQLAlchemy 모델 정의
│   ├── __init__.py
│   ├── user.py         # User 모델
│   ├── subscription.py # Subscription 모델
│   └── ...
└── api/
    └── v1/
        └── users.py    # API 엔드포인트 (ORM 사용)
```

### 사용 방법

#### 1. 모델 정의 (`app/models/user.py`)

```python
from sqlalchemy import Column, String, DateTime
from app.core.database import Base

class User(Base):
    __tablename__ = "users"
    
    id = Column(UUID, primary_key=True)
    email = Column(String(255), unique=True, nullable=False)
    password_hash = Column(String(255), nullable=False)
    # ...
```

#### 2. API에서 사용 (`app/api/v1/users.py`)

```python
from fastapi import Depends
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.models.user import User

@app.get("/users")
def get_users(db: Session = Depends(get_db)):
    """모든 사용자 조회"""
    # ORM 쿼리: SQL 작성 불필요
    users = db.query(User).all()
    return users

@app.get("/users/{user_id}")
def get_user(user_id: UUID, db: Session = Depends(get_db)):
    """특정 사용자 조회"""
    user = db.query(User).filter(User.id == user_id).first()
    return user

@app.post("/users")
def create_user(user_data: UserCreate, db: Session = Depends(get_db)):
    """새 사용자 생성"""
    user = User(**user_data.dict())
    db.add(user)
    db.commit()
    db.refresh(user)
    return user
```

### 장점
- ✅ 타입 안정성 (Python 타입 힌트)
- ✅ SQL 작성 불필요
- ✅ 자동 마이그레이션 (Alembic)
- ✅ 관계 관리 자동화

---

## ☕ Java Backend (JPA/Hibernate)

### 구조

```
backend-java/src/main/java/com/virtualtryon/
├── entity/              # JPA 엔티티 정의
│   ├── User.java
│   ├── Subscription.java
│   └── ...
├── repository/          # Repository 인터페이스
│   ├── UserRepository.java
│   └── ...
└── service/             # 비즈니스 로직
    └── UserService.java
```

### 사용 방법

#### 1. 엔티티 정의 (`entity/User.java`)

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;
    // ...
}
```

#### 2. Repository 정의 (`repository/UserRepository.java`)

```java
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    List<User> findBySubscription(String subscription);
}
```

#### 3. Service에서 사용 (`service/UserService.java`)

```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    public List<User> getAllUsers() {
        // JPA 메서드: SQL 작성 불필요
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }
    
    public User createUser(User user) {
        return userRepository.save(user);
    }
}
```

### 장점
- ✅ 타입 안정성 (Java 타입 시스템)
- ✅ SQL 작성 불필요
- ✅ 자동 마이그레이션 (Flyway)
- ✅ Spring Data JPA 메서드 자동 생성

---

## 🗄️ 데이터베이스 마이그레이션

### Python (Alembic)

```bash
# 마이그레이션 생성
alembic revision --autogenerate -m "create users table"

# 마이그레이션 실행
alembic upgrade head

# 마이그레이션 롤백
alembic downgrade -1
```

### Java (Flyway)

```sql
-- src/main/resources/db/migration/V1__Create_users_table.sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    ...
);
```

Flyway는 애플리케이션 시작 시 자동으로 마이그레이션을 실행합니다.

---

## 📊 DB 툴 사용 목적

**Cursor의 Database Client 2는 다음 목적로만 사용:**

1. ✅ **데이터 확인**: 테이블 데이터 시각적 확인
2. ✅ **스키마 확인**: 테이블 구조 확인
3. ✅ **디버깅**: 쿼리 결과 확인
4. ✅ **데이터 편집**: 테스트 데이터 직접 입력/수정

**백엔드 개발 시:**
- ❌ 직접 SQL 작성하지 않음
- ✅ ORM을 통해 데이터 접근
- ✅ 모델/엔티티 정의로 스키마 관리

---

## 🔄 워크플로우

1. **모델/엔티티 정의** → ORM으로 테이블 구조 정의
2. **마이그레이션 생성** → Alembic/Flyway로 스키마 생성
3. **Repository/Service 작성** → ORM 쿼리로 데이터 접근
4. **DB 툴로 확인** → Cursor에서 데이터 확인 및 디버깅

---

*백엔드에서는 ORM을 사용하고, DB 툴은 시각화 및 관리 목적으로만 사용합니다.*



