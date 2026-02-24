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

    List<Terms> findByTypeAndIsActiveTrueOrderByEffectiveAtDesc(String type);

    Optional<Terms> findByTypeAndVersion(String type, String version);

    List<Terms> findByTypeInOrderByTypeAscEffectiveAtDesc(List<String> types);

    /** category별 약관 목록 (최신순). PAYMENT, SIGNUP 등 */
    List<Terms> findByCategoryOrderByEffectiveAtDesc(String category);

    /** category + required 조건 */
    List<Terms> findByCategoryAndRequiredOrderByEffectiveAtDesc(String category, Boolean required);

    /** 동일 type의 모든 버전 (시행일 내림차순) */
    List<Terms> findByCategoryAndTypeOrderByEffectiveAtDesc(String category, String type);

    /** category + 노출중인 약관만 */
    List<Terms> findByCategoryAndIsActiveTrueOrderByEffectiveAtDesc(String category);

    /** category + required + 노출중 */
    List<Terms> findByCategoryAndRequiredAndIsActiveTrueOrderByEffectiveAtDesc(String category, Boolean required);

    /** 동일 type의 모든 버전 (노출중만) */
    List<Terms> findByCategoryAndTypeAndIsActiveTrueOrderByEffectiveAtDesc(String category, String type);
}
