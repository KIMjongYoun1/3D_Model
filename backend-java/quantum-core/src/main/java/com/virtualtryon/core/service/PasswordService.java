package com.virtualtryon.core.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 비밀번호 암호화 서비스
 * 
 * 역할:
 * - 비밀번호 해싱 (BCrypt)
 * - 비밀번호 검증
 * 
 * 알고리즘: BCrypt
 * - 자동 salt 생성: 각 비밀번호마다 다른 salt 자동 생성
 * - 단방향 해싱: 복호화 불가능 (보안 강화)
 * - Cost factor: 10 (기본값, 보안과 성능의 균형)
 * 
 * ⭐ 이해 필요: BCrypt 동작 원리 이해
 * - BCrypt는 어떻게 salt를 생성하는가?
 * - Cost factor가 높아지면 어떤 영향이 있는가?
 * - 왜 BCrypt를 사용하는가? (MD5, SHA1과의 차이)
 */
@Service
public class PasswordService {
    
    /**
     * 비밀번호 인코더
     * - BCryptPasswordEncoder: Spring Security에서 제공하는 BCrypt 구현
     * - 싱글톤으로 관리 (한 번만 생성)
     */
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 생성자
     * - BCryptPasswordEncoder 인스턴스 생성
     * - 기본 cost factor: 10
     *   - 높을수록 보안 강화, 하지만 처리 시간 증가
     *   - 10: 일반적으로 권장되는 값 (약 100ms 소요)
     */
    public PasswordService() {
        // BCryptPasswordEncoder 인스턴스 생성
        // - 기본 cost factor: 10 (보안과 성능의 균형)
        // - 필요 시 생성자에서 cost factor 조정 가능
        //   예: new BCryptPasswordEncoder(12) - 더 강력하지만 느림
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    /**
     * 비밀번호 해싱
     * 
     * 동작 원리:
     * 1. BCrypt가 자동으로 랜덤 salt 생성
     * 2. salt와 비밀번호를 결합하여 해시 생성
     * 3. 해시 결과: $2b$10$salt + hash (60자)
     * 
     * 특징:
     * - 같은 비밀번호를 해싱해도 매번 다른 결과 (salt 때문)
     * - rainbow table 공격 방지
     * 
     * @param rawPassword 평문 비밀번호 (사용자가 입력한 원본 비밀번호)
     * @return 해시된 비밀번호 (BCrypt 형식, 60자)
     * 
     * 예시:
     * 입력: "password123"
     * 출력: "$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     */
    public String encode(String rawPassword) {
        // BCryptPasswordEncoder.encode() 호출
        // - 내부적으로 랜덤 salt 생성
        // - salt + 비밀번호를 해시하여 반환
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * 비밀번호 검증
     * 
     * 동작 원리:
     * 1. 저장된 해시에서 salt 추출
     * 2. 입력받은 평문 비밀번호 + salt로 해시 생성
     * 3. 생성된 해시와 저장된 해시 비교
     * 
     * 특징:
     * - 해시에서 salt를 추출하여 사용 (salt는 해시에 포함됨)
     * - 평문 비밀번호를 저장하지 않아도 검증 가능
     * 
     * @param rawPassword 입력받은 평문 비밀번호 (로그인 시 사용자가 입력)
     * @param encodedPassword 저장된 해시된 비밀번호 (DB에 저장된 값)
     * @return 비밀번호 일치 여부 (true: 일치, false: 불일치)
     * 
     * 예시:
     * rawPassword: "password123"
     * encodedPassword: "$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     * 결과: true (일치)
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        // BCryptPasswordEncoder.matches() 호출
        // - 내부적으로 salt 추출 및 해시 비교 수행
        // - 상수 시간 비교 (타이밍 공격 방지)
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

