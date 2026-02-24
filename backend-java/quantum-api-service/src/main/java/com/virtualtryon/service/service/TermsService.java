package com.virtualtryon.service.service;

import com.virtualtryon.core.dto.terms.TermsDetailDto;
import com.virtualtryon.core.dto.terms.TermsSummaryDto;
import com.virtualtryon.core.dto.terms.TermsVersionSummaryDto;
import com.virtualtryon.core.entity.Terms;
import com.virtualtryon.core.entity.User;
import com.virtualtryon.core.entity.UserTermsAgreement;
import com.virtualtryon.core.repository.TermsRepository;
import com.virtualtryon.core.repository.UserTermsAgreementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** 약관 조회 및 동의 처리. category(SIGNUP/PAYMENT), required(필수/선택)는 DB에서 관리 */
@Service
public class TermsService {

    private static final List<String> REQUIRED_TERMS_TYPES = List.of("TERMS_OF_SERVICE", "PRIVACY_POLICY");

    private final TermsRepository termsRepository;
    private final UserTermsAgreementRepository agreementRepository;

    public TermsService(TermsRepository termsRepository, UserTermsAgreementRepository agreementRepository) {
        this.termsRepository = termsRepository;
        this.agreementRepository = agreementRepository;
    }

    /** 필수 약관 목록 (가입용, 최신 버전, 노출중만). content, allVersions 포함 */
    public List<TermsDetailDto> getRequiredTermsWithContent() {
        List<Terms> byCategory = termsRepository.findByCategoryAndRequiredAndIsActiveTrueOrderByEffectiveAtDesc("SIGNUP", true);
        if (!byCategory.isEmpty()) {
            return latestPerType(byCategory).stream().map(t -> toDetailWithVersions(t, "SIGNUP")).toList();
        }
        List<TermsDetailDto> result = new ArrayList<>();
        for (String type : REQUIRED_TERMS_TYPES) {
            termsRepository.findByTypeAndIsActiveTrueOrderByEffectiveAtDesc(type).stream()
                .findFirst()
                .map(t -> toDetailWithVersions(t, "SIGNUP"))
                .ifPresent(result::add);
        }
        return result;
    }

    /** 필수 약관 ID 목록 (가입 동의 검증용) */
    public List<TermsSummaryDto> getRequiredTermsSummary() {
        return getRequiredTermsWithContent().stream()
            .map(d -> new TermsSummaryDto(d.getId(), d.getType(), d.getTitle(), d.getVersion()))
            .toList();
    }

    /** 결제 시 표시할 약관 목록 (category=PAYMENT, 노출중만, 최신 per type). allVersions 포함 */
    public List<TermsDetailDto> getPaymentTerms() {
        List<Terms> byCategory = termsRepository.findByCategoryAndIsActiveTrueOrderByEffectiveAtDesc("PAYMENT");
        return latestPerType(byCategory).stream().map(t -> toDetailWithVersions(t, "PAYMENT")).toList();
    }

    /** 결제 시 필수 약관 ID 목록 (노출중만). 이들에 모두 동의해야 주문 생성 가능 */
    public List<UUID> getRequiredPaymentTermIds() {
        List<Terms> byCategory = termsRepository.findByCategoryAndRequiredAndIsActiveTrueOrderByEffectiveAtDesc("PAYMENT", true);
        return latestPerType(byCategory).stream().map(Terms::getId).filter(Objects::nonNull).toList();
    }

    /** 동일 type의 최신 1건만 유지 (effectiveAt 내림차순 기준) */
    private List<Terms> latestPerType(List<Terms> terms) {
        List<Terms> result = new ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (Terms t : terms) {
            String type = t.getType() != null ? t.getType() : "";
            if (!seen.contains(type)) {
                seen.add(type);
                result.add(t);
            }
        }
        return result;
    }

    /** 약관 상세 조회. allVersions 포함 (이전 버전 선택용) */
    public Optional<TermsDetailDto> getTermsDetail(UUID termsId) {
        if (termsId == null) return Optional.empty();
        return termsRepository.findById(termsId)
            .map(t -> toDetailWithVersions(t, t.getCategory() != null ? t.getCategory() : "SIGNUP"));
    }

    /** 사용자가 필수 약관 전체에 동의했는지 */
    public boolean hasUserAgreedToAllRequired(User user) {
        List<TermsSummaryDto> required = getRequiredTermsSummary();
        if (required.isEmpty()) return true;
        List<UUID> agreedIds = agreementRepository.findAgreedTermsIdsByUserId(user.getId());
        return required.stream().allMatch(t -> agreedIds.contains(t.getId()));
    }

    /** 동의 저장. 필수 약관 전체 동의 검증 후 저장 */
    @Transactional
    public void saveAgreements(User user, List<UUID> agreedTermIds, String ipAddress, String userAgent) {
        Objects.requireNonNull(user, "user must not be null");
        List<UUID> ids = agreedTermIds != null
                ? agreedTermIds.stream().filter(Objects::nonNull).distinct().toList()
                : List.of();
        if (ids.isEmpty()) throw new IllegalArgumentException("동의할 약관 목록이 비어 있습니다.");
        List<TermsSummaryDto> required = getRequiredTermsSummary();
        List<UUID> requiredIds = required.stream().map(TermsSummaryDto::getId).filter(Objects::nonNull).toList();
        for (UUID termId : requiredIds) {
            if (!ids.contains(termId)) {
                throw new IllegalArgumentException("필수 약관에 모두 동의해야 합니다: " + termId);
            }
        }
        for (UUID termId : ids) {
            if (termId == null) continue;
            Terms terms = termsRepository.findById(termId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관: " + termId));
            if (agreementRepository.existsByUserAndTerms(user, terms)) continue;
            UserTermsAgreement ua = new UserTermsAgreement(user, terms);
            ua.setIpAddress(ipAddress);
            ua.setUserAgent(userAgent);
            agreementRepository.save(ua);
        }
    }

    private TermsDetailDto toDetail(Terms t) {
        Objects.requireNonNull(t, "terms must not be null");
        TermsDetailDto d = new TermsDetailDto();
        d.setId(t.getId());
        d.setType(t.getType() != null ? t.getType() : "");
        d.setTitle(t.getTitle() != null ? t.getTitle() : "");
        d.setVersion(t.getVersion() != null ? t.getVersion() : "");
        d.setContent(t.getContent() != null ? t.getContent() : "");
        d.setEffectiveAt(t.getEffectiveAt());
        d.setCategory(t.getCategory() != null ? t.getCategory() : "SIGNUP");
        d.setRequired(t.getRequired() != null ? t.getRequired() : true);
        return d;
    }

    private TermsDetailDto toDetailWithVersions(Terms t, String category) {
        TermsDetailDto d = toDetail(t);
        d.setAllVersions(getAllVersionsForType(category, t.getType()));
        return d;
    }

    /** 동일 type의 모든 버전 (법령상 이전 약관 내역 포함, isActive 무관) */
    private List<TermsVersionSummaryDto> getAllVersionsForType(String category, String type) {
        if (type == null || type.isBlank()) return List.of();
        return termsRepository.findByCategoryAndTypeOrderByEffectiveAtDesc(category, type).stream()
            .map(v -> new TermsVersionSummaryDto(v.getId(), v.getVersion(), v.getTitle(), v.getEffectiveAt()))
            .toList();
    }
}
