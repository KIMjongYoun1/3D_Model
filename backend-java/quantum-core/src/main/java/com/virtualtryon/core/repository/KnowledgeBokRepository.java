package com.virtualtryon.core.repository;

import com.virtualtryon.core.entity.KnowledgeBok;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KnowledgeBokRepository extends JpaRepository<KnowledgeBok, UUID> {
    List<KnowledgeBok> findAllByOrderByTimeDesc(Pageable pageable);
    List<KnowledgeBok> findByStatCodeOrderByTimeDesc(String statCode, Pageable pageable);
    Optional<KnowledgeBok> findByStatCodeAndItemCode1AndTime(String statCode, String itemCode1, String time);
}
