# JWT 클레임 검증 패턴 - 인자와 비교하기

## 핵심 개념

**클레임을 추출한 후, 인자로 받은 값과 비교하여 검증하는 패턴**

```
1. 클레임 세팅 (어떻게 가져올지 정의)
2. extractAllClaims 호출 (값 가져오기)
3. 인자와 비교 (검증)
```

---

## 기본 패턴

### 예시 1: 사용자 ID 검증

```java
/**
 * 토큰의 사용자 ID와 인자로 받은 사용자 ID 비교
 * 
 * @param token JWT 토큰
 * @param expectedUserId 예상되는 사용자 ID
 * @return 일치 여부
 */
public boolean validateUserId(String token, UUID expectedUserId) {
    // 1. 클레임에서 사용자 ID 추출
    UUID tokenUserId = extractUserId(token);
    
    // 2. 인자와 비교
    return tokenUserId.equals(expectedUserId);
}
```

**사용 예시:**
```java
// API 요청에서 받은 사용자 ID
UUID requestUserId = UUID.fromString("user-123");

// 토큰에서 추출한 사용자 ID와 비교
if (!jwtService.validateUserId(token, requestUserId)) {
    throw new SecurityException("사용자 ID가 일치하지 않습니다.");
}
```

---

### 예시 2: 이메일 검증

```java
/**
 * 토큰의 이메일과 인자로 받은 이메일 비교
 * 
 * @param token JWT 토큰
 * @param expectedEmail 예상되는 이메일
 * @return 일치 여부
 */
public boolean validateEmail(String token, String expectedEmail) {
    // 1. 클레임에서 이메일 추출 (커스텀 클레임)
    Claims claims = extractAllClaims(token);
    String tokenEmail = (String) claims.get("email");
    
    // 2. 인자와 비교
    return expectedEmail.equals(tokenEmail);
}
```

---

### 예시 3: 역할(Role) 검증

```java
/**
 * 토큰의 역할과 인자로 받은 역할 비교
 * 
 * @param token JWT 토큰
 * @param expectedRole 예상되는 역할
 * @return 일치 여부
 */
public boolean validateRole(String token, String expectedRole) {
    // 1. 클레임에서 역할 추출
    Claims claims = extractAllClaims(token);
    String tokenRole = (String) claims.get("role");
    
    // 2. 인자와 비교
    return expectedRole.equals(tokenRole);
}
```

---

## 실제 사용 시나리오

### 시나리오 1: 사용자 정보 수정 API

```java
@PutMapping("/api/v1/users/{userId}")
public ResponseEntity<UserResponse> updateUser(
        @PathVariable UUID userId,
        @RequestBody UpdateUserRequest request,
        @RequestHeader("Authorization") String authHeader) {
    
    // 1. 토큰 추출
    String token = authHeader.replace("Bearer ", "");
    
    // 2. 토큰 검증 (서명, 만료 시간)
    if (!jwtService.validateToken(token)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
    // 3. 사용자 ID 검증 (토큰의 사용자 ID와 요청의 사용자 ID 비교)
    if (!jwtService.validateUserId(token, userId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    
    // 4. 사용자 정보 수정
    User user = userService.updateUser(userId, request);
    return ResponseEntity.ok(user);
}
```

---

### 시나리오 2: 리소스 접근 권한 검증

```java
@GetMapping("/api/v1/users/{userId}/orders")
public ResponseEntity<List<Order>> getUserOrders(
        @PathVariable UUID userId,
        @RequestHeader("Authorization") String authHeader) {
    
    String token = authHeader.replace("Bearer ", "");
    
    // 1. 토큰 기본 검증
    if (!jwtService.validateToken(token)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
    // 2. 사용자 ID 검증 (자신의 주문만 조회 가능)
    if (!jwtService.validateUserId(token, userId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    
    // 3. 주문 조회
    List<Order> orders = orderService.getUserOrders(userId);
    return ResponseEntity.ok(orders);
}
```

---

## 통합 검증 메서드

### 여러 클레임을 한 번에 검증

```java
/**
 * 토큰의 여러 클레임을 한 번에 검증
 * 
 * @param token JWT 토큰
 * @param expectedUserId 예상되는 사용자 ID
 * @param expectedEmail 예상되는 이메일
 * @return 모든 검증 통과 여부
 */
public boolean validateClaims(String token, UUID expectedUserId, String expectedEmail) {
    // 1. 토큰 기본 검증 (서명, 만료 시간)
    if (!validateToken(token)) {
        return false;
    }
    
    // 2. 모든 클레임 추출
    Claims claims = extractAllClaims(token);
    
    // 3. 사용자 ID 검증
    UUID tokenUserId = UUID.fromString(claims.getSubject());
    if (!tokenUserId.equals(expectedUserId)) {
        return false;
    }
    
    // 4. 이메일 검증
    String tokenEmail = (String) claims.get("email");
    if (!expectedEmail.equals(tokenEmail)) {
        return false;
    }
    
    return true;
}
```

---

## JwtService에 추가할 수 있는 메서드

```java
/**
 * 사용자 ID 검증
 */
public boolean validateUserId(String token, UUID expectedUserId) {
    try {
        UUID tokenUserId = extractUserId(token);
        return tokenUserId.equals(expectedUserId);
    } catch (Exception e) {
        return false;
    }
}

/**
 * 커스텀 클레임 검증
 */
public boolean validateClaim(String token, String claimName, Object expectedValue) {
    try {
        Claims claims = extractAllClaims(token);
        Object claimValue = claims.get(claimName);
        return expectedValue.equals(claimValue);
    } catch (Exception e) {
        return false;
    }
}

/**
 * 여러 클레임 검증
 */
public boolean validateClaims(String token, Map<String, Object> expectedClaims) {
    try {
        Claims claims = extractAllClaims(token);
        for (Map.Entry<String, Object> entry : expectedClaims.entrySet()) {
            Object claimValue = claims.get(entry.getKey());
            if (!entry.getValue().equals(claimValue)) {
                return false;
            }
        }
        return true;
    } catch (Exception e) {
        return false;
    }
}
```

---

## 검증 순서 (권장)

```
1. 토큰 기본 검증
   validateToken(token)
   → 서명 검증, 만료 시간 검증

2. 클레임 추출
   extractAllClaims(token)
   → 모든 클레임 가져오기

3. 인자와 비교
   if (expectedValue != extractedValue)
   → 검증 실패 처리
```

---

## 요약

### 패턴
```java
// 1. 클레임 추출
Claims claims = extractAllClaims(token);
// 또는
UUID userId = extractUserId(token);

// 2. 인자와 비교
if (!userId.equals(expectedUserId)) {
    throw new SecurityException("검증 실패");
}
```

### 장점
- ✅ 명확한 검증 로직
- ✅ 재사용 가능한 메서드
- ✅ 다양한 클레임 검증 가능

### 사용 예시
```java
// 사용자 ID 검증
if (!jwtService.validateUserId(token, requestUserId)) {
    throw new SecurityException("사용자 ID 불일치");
}

// 커스텀 클레임 검증
if (!jwtService.validateClaim(token, "role", "admin")) {
    throw new SecurityException("권한 없음");
}
```

**결론: 클레임을 추출한 후 인자와 비교하는 패턴으로 다양한 검증을 수행할 수 있습니다!**

