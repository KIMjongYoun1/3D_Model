package com.virtualtryon.core.repository;

import com.virtualtryon.core.entity.KnowledgeDart;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KnowledgeDartRepository extends JpaRepository<KnowledgeDart, UUID> {
    List<KnowledgeDart> findAllByOrderByRceptDtDesc(Pageable pageable);
    List<KnowledgeDart> findByCorpCodeOrderByRceptDtDesc(String corpCode, Pageable pageable);
    Optional<KnowledgeDart> findByRceptNo(String rceptNo);
}
