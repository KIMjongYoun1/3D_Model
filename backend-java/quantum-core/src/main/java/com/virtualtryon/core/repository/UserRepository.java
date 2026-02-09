package com.virtualtryon.core.repository;

import com.virtualtryon.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * User 리포지토리
 * 
 * 역할:
 * - 사용자 데이터 접근
 * - 이메일로 사용자 조회
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * 이메일로 사용자 조회
     * 
     * @param email 이메일
     * @return 사용자 (Optional)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일 존재 여부 확인
     * 
     * @param email 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);

    /**
     * 소셜 로그인 정보로 사용자 조회
     * 
     * @param provider 제공자 (NAVER, KAKAO 등)
     * @param providerId 제공자 고유 ID
     * @return 사용자 (Optional)
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    /**
     * Refresh Token으로 사용자 조회
     */
    Optional<User> findByRefreshToken(String refreshToken);
}

