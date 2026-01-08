# `::` 문법 상세 설명 - 단계별 동작 원리

## 핵심 개념

**`Claims::getSubject`는 "어떻게 값을 가져올지"를 정의하는 함수입니다.**
**실제로 값을 가져오는 것은 `extractClaim` 메서드 내부에서 일어납니다.**

---

## 전체 흐름 (단계별)

### 예시: `extractUserId(token)` 호출 시

```java
// 1단계: 호출
UUID userId = extractUserId(token);
```

### 2단계: `extractUserId` 메서드 실행

```105:108:backend-java/src/main/java/com/virtualtryon/service/JwtService.java
    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, Claims::getSubject);
        return UUID.fromString(userIdStr);
    }
```

**여기서 일어나는 일:**
- `Claims::getSubject`를 **함수로 전달**합니다
- 이것은 "나중에 Claims 객체가 생기면, 그 객체의 `getSubject()` 메서드를 호출하라"는 **지시**입니다
- 아직 실제로 값을 가져오지는 **않습니다**

### 3단계: `extractClaim` 메서드 실행

```127:130:backend-java/src/main/java/com/virtualtryon/service/JwtService.java
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
```

**여기서 일어나는 일:**

1. **`extractAllClaims(token)` 호출**
   ```java
   Claims claims = extractAllClaims(token);
   ```
   - 토큰에서 **모든 클레임을 추출**하여 `Claims` 객체를 만듭니다
   - 이제 `claims` 객체에는 `sub`, `exp`, `iat` 등의 값이 들어있습니다

2. **`claimsResolver.apply(claims)` 호출**
   ```java
   return claimsResolver.apply(claims);
   ```
   - 여기서 `claimsResolver`는 `Claims::getSubject`입니다
   - `apply(claims)`를 호출하면:
     ```java
     // 실제로는 이렇게 동작:
     claims.getSubject()  // Claims 객체에서 sub 값을 가져옴
     ```
   - **이제야 실제로 값을 가져옵니다!**

### 4단계: `extractAllClaims` 메서드 실행

```140:146:backend-java/src/main/java/com/virtualtryon/service/JwtService.java
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // 서명 검증
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
```

**여기서 일어나는 일:**
- JWT 토큰 문자열을 **파싱**합니다
- 서명을 **검증**합니다
- **Claims 객체**를 생성합니다 (이 안에 `sub`, `exp`, `iat` 등이 들어있음)

---

## 시각적 흐름도

```
1. extractUserId(token) 호출
   ↓
2. extractClaim(token, Claims::getSubject) 호출
   ↓
3. extractAllClaims(token) 실행
   ↓
   [토큰 문자열] → [Claims 객체 생성]
   예: "eyJhbGc..." → Claims { sub: "user-123", exp: 1234571490, ... }
   ↓
4. claimsResolver.apply(claims) 실행
   ↓
   Claims::getSubject → claims.getSubject() 호출
   ↓
5. "user-123" 반환
   ↓
6. UUID.fromString("user-123") → UUID 객체 반환
```

---

## 핵심 포인트

### ❌ 잘못된 이해
```
Claims::getSubject가 직접 토큰에서 값을 가져온다
```

### ✅ 올바른 이해
```
1. Claims::getSubject는 "어떻게 값을 가져올지"를 정의하는 함수
2. extractAllClaims(token)이 토큰에서 Claims 객체를 만듦
3. claimsResolver.apply(claims)가 실제로 claims.getSubject()를 호출하여 값을 가져옴
```

---

## 비유로 이해하기

### 레스토랑 비유

```java
// 주문: "나중에 음식이 나오면, 그 음식에서 국물을 가져와라"
Function<음식, 국물> 국물가져오기 = 음식::get국물;

// 1. 주방에서 음식 만들기
음식 음식객체 = 주방.음식만들기(주문서);

// 2. 주문한 대로 국물 가져오기
국물 국물값 = 국물가져오기.apply(음식객체);
// 실제로는: 음식객체.get국물() 호출
```

### 코드와 비교

```java
// 주문: "나중에 Claims 객체가 나오면, 그 객체에서 sub 값을 가져와라"
Function<Claims, String> getSubject = Claims::getSubject;

// 1. 토큰에서 Claims 객체 만들기
Claims claims = extractAllClaims(token);

// 2. 주문한 대로 값 가져오기
String sub = getSubject.apply(claims);
// 실제로는: claims.getSubject() 호출
```

---

## 실제 코드 실행 순서

```java
// 호출
extractUserId("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");

// ↓ 1단계: extractUserId 내부
extractClaim("eyJhbGc...", Claims::getSubject);

// ↓ 2단계: extractClaim 내부
Claims claims = extractAllClaims("eyJhbGc...");
// → Claims 객체 생성: { sub: "user-123", exp: 1234571490, iat: 1234567890 }

// ↓ 3단계: 함수 실행
claimsResolver.apply(claims);
// → Claims::getSubject.apply(claims)
// → claims.getSubject()
// → "user-123" 반환

// ↓ 4단계: UUID 변환
UUID.fromString("user-123");
// → UUID 객체 반환
```

---

## 요약

1. **`Claims::getSubject`** = "어떻게 값을 가져올지"를 정의하는 함수
2. **`extractAllClaims(token)`** = 토큰에서 Claims 객체를 만듦
3. **`claimsResolver.apply(claims)`** = 실제로 `claims.getSubject()`를 호출하여 값을 가져옴

**결론: `::` 문법은 "나중에 실행할 함수"를 전달하는 것이고, 실제 값 추출은 `apply()` 호출 시 일어납니다!**

