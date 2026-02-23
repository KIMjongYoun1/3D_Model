package com.virtualtryon.core.repository;

import com.virtualtryon.core.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TermsRepository extends JpaRepository<Terms, UUID> {

    List<Terms> findByTypeOrderByEffectiveAtDesc(String type);

    Optional<Terms> findByTypeAndVersion(String type, String version);

    List<Terms> findByTypeInOrderByTypeAscEffectiveAtDesc(List<String> types);
}
