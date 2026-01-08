package com.virtualtryon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User 엔티티 (사용자)
 * 
 * 역할:
 * - 사용자 정보 저장
 * - 인증/인가에 사용
 * - 비밀번호는 BCrypt로 해시화되어 저장
 * 
 * @Entity: JPA 엔티티로 인식
 * @Table: 테이블명 지정 (users)
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /**
     * Primary Key: UUID
     * - 자동 생성 (데이터베이스에서)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * 이메일 (로그인 ID)
     * - 유니크 제약조건
     * - NOT NULL
     */
    @Column(unique = true, nullable = false, length = 255)
    private String email;
    
    /**
     * 비밀번호 해시
     * - BCrypt로 해시화된 비밀번호 저장
     * - 평문 비밀번호는 절대 저장하지 않음
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    /**
     * 사용자 이름
     */
    @Column(length = 100)
    private String name;
    
    /**
     * 프로필 이미지 URL
     */
    @Column(name = "profile_image", length = 500)
    private String profileImage;
    
    /**
     * 구독 유형
     * - free: 무료
     * - basic: 기본
     * - pro: 프로
     * - unlimited: 무제한
     */
    @Column(length = 20)
    @Builder.Default
    private String subscription = "free";
    
    /**
     * 생성일시
     * - 자동 설정 (데이터베이스에서)
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 수정일시
     * - 자동 업데이트
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 삭제일시 (소프트 삭제)
     * - NULL이면 활성 사용자
     * - 값이 있으면 삭제된 사용자
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}



