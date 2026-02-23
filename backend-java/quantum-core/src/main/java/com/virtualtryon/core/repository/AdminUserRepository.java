package com.virtualtryon.core.repository;

import com.virtualtryon.core.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * AdminUser 리포지토리
 * 
 * 역할:
 * - 관리자 데이터 접근
 * - 이메일로 관리자 조회
 * - 활성 상태 관리자 조회
 */
@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {

    /**
     * 이메일로 관리자 조회
     */
    Optional<AdminUser> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 이메일 + 활성 상태로 관리자 조회
     */
    Optional<AdminUser> findByEmailAndIsActiveTrue(String email);
}
