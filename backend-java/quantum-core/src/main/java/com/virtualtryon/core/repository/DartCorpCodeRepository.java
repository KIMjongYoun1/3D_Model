package com.virtualtryon.core.repository;

import com.virtualtryon.core.entity.DartCorpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DartCorpCodeRepository extends JpaRepository<DartCorpCode, String> {
    Optional<DartCorpCode> findByCorpCode(String corpCode);
    List<DartCorpCode> findByCorpNameContaining(String corpName);
}
