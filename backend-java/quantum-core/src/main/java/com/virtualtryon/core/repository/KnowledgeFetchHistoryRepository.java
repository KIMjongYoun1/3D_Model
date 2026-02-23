package com.virtualtryon.core.repository;

import com.virtualtryon.core.entity.KnowledgeFetchHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeFetchHistoryRepository extends JpaRepository<KnowledgeFetchHistory, UUID> {
    List<KnowledgeFetchHistory> findBySourceTypeOrderByFetchedAtDesc(String sourceType, Pageable pageable);

    /** 어디서·무엇을·언제 받아왔는지 상세 로우 목록 (최신순) */
    List<KnowledgeFetchHistory> findAllByOrderByFetchedAtDesc();
}
