# JWT 클레임(Claims)과 메서드 레퍼런스(`::`) 문법 설명

## 1. JWT 클레임(Claims)이란?

### 개념
**클레임(Claims)**은 JWT 토큰에 포함된 **데이터 조각**입니다. 토큰에 저장된 정보를 의미합니다.

### JWT 구조
```
JWT = Header + Payload(Claims) + Signature
```

예시:
```json
{
  "sub": "user-123",           // 클레임: 사용자 ID
  "iat": 1234567890,           // 클레임: 발급 시간
  "exp": 1234571490,           // 클레임: 만료 시간
  "email": "user@example.com"  // 클레임: 이메일 (커스텀)
}
```

### 표준 클레임 (Standard Claims)
JWT 표준에서 정의된 클레임들:

| 클레임 | 의미 | 예시 |
|--------|------|------|
| `sub` (Subject) | 토큰 주체 (사용자 ID) | `"user-123"` |
| `iat` (Issued At) | 토큰 발급 시간 | `1234567890` |
| `exp` (Expiration) | 토큰 만료 시간 | `1234571490` |
| `iss` (Issuer) | 토큰 발급자 | `"my-app"` |
| `aud` (Audience) | 토큰 수신자 | `"api-server"` |

### 커스텀 클레임 (Custom Claims)
표준 외에 직접 추가한 클레임:
```java
claims.put("email", "user@example.com");
claims.put("role", "admin");
claims.put("subscription", "premium");
```

### 코드에서의 사용 예시

```106:108:backend-java/src/main/java/com/virtualtryon/service/JwtService.java
    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, Claims::getSubject);
        return UUID.fromString(userIdStr);
    }
```

여기서 `Claims::getSubject`는 **클레임에서 `sub` 값을 추출**하는 것입니다.

---

## 2. `::` 문법 (메서드 레퍼런스)

### 개념
`::`는 **메서드 레퍼런스(Method Reference)** 문법입니다. Java 8에서 도입된 기능으로, **함수를 직접 전달**할 수 있게 해줍니다.

### 문법 형태
```java
클래스명::메서드명
```

### 왜 사용하는가?
**람다 표현식을 더 간결하게** 만들 수 있습니다.

### 비교 예시

#### ❌ 람다 표현식 (길게)
```java
// 람다로 작성
extractClaim(token, (claims) -> claims.getSubject());
```

#### ✅ 메서드 레퍼런스 (간결하게)
```java
// 메서드 레퍼런스로 작성
extractClaim(token, Claims::getSubject);
```

### 실제 코드 분석

```127:130:backend-java/src/main/java/com/virtualtryon/service/JwtService.java
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
```

**동작 원리:**
1. `Function<Claims, T>`는 **함수 인터페이스**입니다.
   - 입력: `Claims` 객체
   - 출력: `T` 타입 (제네릭)

2. `Claims::getSubject`는 **메서드 레퍼런스**입니다.
   - `Claims` 객체의 `getSubject()` 메서드를 가리킵니다.
   - 이것이 `Function<Claims, String>`으로 변환됩니다.

3. `claimsResolver.apply(claims)` 호출 시:
   ```java
   // 실제로는 이렇게 동작:
   claims.getSubject()  // Claims 객체에서 sub 값을 추출
   ```

### 다른 사용 예시

#### 예시 1: 사용자 ID 추출
```java
// 106번 줄
String userIdStr = extractClaim(token, Claims::getSubject);
// 동일한 의미:
String userIdStr = extractClaim(token, (claims) -> claims.getSubject());
```

#### 예시 2: 만료 시간 추출
```java
// 117번 줄
Date expiration = extractClaim(token, Claims::getExpiration);
// 동일한 의미:
Date expiration = extractClaim(token, (claims) -> claims.getExpiration());
```

### 메서드 레퍼런스 종류

#### 1. 정적 메서드 레퍼런스
```java
// 정적 메서드
String::valueOf
// 동일: (x) -> String.valueOf(x)
```

#### 2. 인스턴스 메서드 레퍼런스
```java
// 인스턴스 메서드
String::length
// 동일: (str) -> str.length()
```

#### 3. 특정 객체의 메서드 레퍼런스
```java
// 특정 객체의 메서드
user::getName
// 동일: () -> user.getName()
```

#### 4. 생성자 레퍼런스
```java
// 생성자
UUID::fromString
// 동일: (str) -> UUID.fromString(str)
```

### 왜 이렇게 설계했나?

**재사용성과 유연성**을 위해:

```java
// 하나의 메서드로 여러 클레임 추출 가능
extractClaim(token, Claims::getSubject);      // 사용자 ID
extractClaim(token, Claims::getExpiration);    // 만료 시간
extractClaim(token, Claims::getIssuedAt);     // 발급 시간
```

만약 메서드 레퍼런스를 사용하지 않았다면:
```java
// 각각 별도 메서드 필요
extractSubject(token);
extractExpiration(token);
extractIssuedAt(token);
```

---

## 3. 실제 동작 흐름

### 사용자 ID 추출 과정

```java
// 1. 호출
UUID userId = extractUserId(token);

// 2. 내부 동작
public UUID extractUserId(String token) {
    // extractClaim 호출, Claims::getSubject 전달
    String userIdStr = extractClaim(token, Claims::getSubject);
    return UUID.fromString(userIdStr);
}

// 3. extractClaim 내부
private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    Claims claims = extractAllClaims(token);  // 토큰에서 모든 클레임 추출
    return claimsResolver.apply(claims);      // Claims::getSubject 실행
    // 실제로는: claims.getSubject() 호출
}

// 4. 결과
// "user-123" 문자열 반환 → UUID로 변환
```

---

## 4. 요약

### JWT 클레임(Claims)
- **역할**: JWT 토큰에 저장된 데이터
- **종류**: 표준 클레임(`sub`, `exp`, `iat`) + 커스텀 클레임
- **사용**: 토큰에서 사용자 정보, 만료 시간 등 추출

### 메서드 레퍼런스(`::`)
- **역할**: 람다 표현식을 간결하게 만드는 문법
- **형태**: `클래스명::메서드명`
- **장점**: 코드 간결성, 재사용성, 가독성 향상

### 코드에서의 활용
```java
// 클레임에서 사용자 ID 추출
extractClaim(token, Claims::getSubject);

// 클레임에서 만료 시간 추출
extractClaim(token, Claims::getExpiration);
```

이렇게 **하나의 메서드로 여러 클레임을 추출**할 수 있어 코드가 깔끔하고 재사용 가능합니다!

