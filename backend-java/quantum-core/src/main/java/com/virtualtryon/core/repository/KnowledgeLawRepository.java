package com.virtualtryon.core.repository;

import com.virtualtryon.core.entity.KnowledgeLaw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KnowledgeLawRepository extends JpaRepository<KnowledgeLaw, UUID> {
    List<KnowledgeLaw> findAllByOrderByProclamationDateDesc(org.springframework.data.domain.Pageable pageable);
    List<KnowledgeLaw> findByLawNameKoContaining(String lawNameKo);
    Optional<KnowledgeLaw> findByMst(String mst);
}
