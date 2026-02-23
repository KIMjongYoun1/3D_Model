package com.virtualtryon.core.repository;

import com.virtualtryon.core.entity.Knowledge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KnowledgeRepository extends JpaRepository<Knowledge, UUID> {
    List<Knowledge> findByCategoryAndIsActiveTrueOrderByUpdatedAtDesc(String category);
    List<Knowledge> findByIsActiveTrueOrderByUpdatedAtDesc();

    /** 항목별 upsert: source_type + external_id 로 동일 항목 조회 (카테고리별 저장 시 사용) */
    Optional<Knowledge> findBySourceTypeAndExternalId(String sourceType, String externalId);

    /** 카테고리 목록 (필터용) */
    @Query("SELECT DISTINCT k.category FROM Knowledge k WHERE k.isActive = true ORDER BY k.category")
    List<String> findDistinctCategories();

    /** 검색 + 페이징 (카테고리 필터 없음) */
    @Query("SELECT k FROM Knowledge k WHERE k.isActive = true " +
            "AND (COALESCE(:q, '') = '' OR LOWER(k.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(k.content) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "ORDER BY k.updatedAt DESC")
    Page<Knowledge> searchActiveByQuery(@Param("q") String q, Pageable pageable);

    /** 검색 + 카테고리 필터 + 페이징 */
    @Query("SELECT k FROM Knowledge k WHERE k.isActive = true AND k.category IN :categories " +
            "AND (COALESCE(:q, '') = '' OR LOWER(k.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(k.content) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "ORDER BY k.updatedAt DESC")
    Page<Knowledge> searchActiveByCategoriesAndQuery(@Param("categories") List<String> categories, @Param("q") String q, Pageable pageable);
}
