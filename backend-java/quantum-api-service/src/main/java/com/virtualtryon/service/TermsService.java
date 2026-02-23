package com.virtualtryon.service;

import com.virtualtryon.core.dto.TermsDetailDto;
import com.virtualtryon.core.dto.TermsSummaryDto;
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

/** 약관 조회 및 동의 처리 */
@Service
public class TermsService {

    private static final List<String> REQUIRED_TERMS_TYPES = List.of("TERMS_OF_SERVICE", "PRIVACY_POLICY");

    private final TermsRepository termsRepository;
    private final UserTermsAgreementRepository agreementRepository;

    public TermsService(TermsRepository termsRepository, UserTermsAgreementRepository agreementRepository) {
        this.termsRepository = termsRepository;
        this.agreementRepository = agreementRepository;
    }

    /** 필수 약관 목록 (최신 버전) */
    public List<TermsSummaryDto> getRequiredTermsSummary() {
        List<TermsSummaryDto> result = new ArrayList<>();
        for (String type : REQUIRED_TERMS_TYPES) {
            termsRepository.findByTypeOrderByEffectiveAtDesc(type).stream()
                .findFirst()
                .map(this::toSummary)
                .ifPresent(result::add);
        }
        return result;
    }

    /** 약관 상세 조회 */
    public Optional<TermsDetailDto> getTermsDetail(UUID termsId) {
        if (termsId == null) return Optional.empty();
        return termsRepository.findById(termsId).map(this::toDetail);
    }

    /** 사용자가 필수 약관 전체에 동의했는지 */
    public boolean hasUserAgreedToAllRequired(User user) {
        List<TermsSummaryDto> required = getRequiredTermsSummary();
        if (required.isEmpty()) return true;
        List<UUID> agreedIds = agreementRepository.findAgreedTermsIdsByUserId(user.getId());
        return required.stream()
            .allMatch(t -> agreedIds.contains(t.getId()));
    }

    /** 동의 저장 (필수 약관 전체 동의 검증) */
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

    private TermsSummaryDto toSummary(Terms t) {
        Objects.requireNonNull(t, "terms must not be null");
        return new TermsSummaryDto(
                t.getId(),
                t.getType() != null ? t.getType() : "",
                t.getTitle() != null ? t.getTitle() : "",
                t.getVersion() != null ? t.getVersion() : ""
        );
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
        return d;
    }
}
