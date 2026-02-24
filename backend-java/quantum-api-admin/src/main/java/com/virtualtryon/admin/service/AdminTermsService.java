package com.virtualtryon.admin.service;

import com.virtualtryon.core.dto.terms.TermsDetailDto;
import com.virtualtryon.core.entity.Terms;
import com.virtualtryon.core.repository.TermsRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Admin용 약관 CRUD 서비스.
 * DB 저장 후 Service API(/api/v1/terms)가 동일 DB를 참조하므로
 * 수정 즉시 Studio 동의 화면에 반영됨 (재배포 불필요).
 */
@Service
public class AdminTermsService {

    private final TermsRepository termsRepository;

    public AdminTermsService(TermsRepository termsRepository) {
        this.termsRepository = termsRepository;
    }

    /** 전체 약관 목록 (시행일 내림차순) */
    public List<TermsDetailDto> findAll() {
        return termsRepository.findAll(Sort.by(Sort.Direction.DESC, "effectiveAt"))
                .stream()
                .map(this::toDetail)
                .toList();
    }

    /** 약관 상세 조회 */
    public Optional<TermsDetailDto> findById(UUID id) {
        if (id == null) return Optional.empty();
        return termsRepository.findById(id).map(this::toDetail);
    }

    /** 약관 등록 */
    @Transactional
    public TermsDetailDto create(String type, String version, String title, String content,
                                 java.time.LocalDateTime effectiveAt, String category, Boolean required, Boolean isActive) {
        if (type == null || type.isBlank()) throw new IllegalArgumentException("type은 필수입니다.");
        if (version == null || version.isBlank()) throw new IllegalArgumentException("version은 필수입니다.");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title은 필수입니다.");
        if (content == null) content = "";
        if (effectiveAt == null) effectiveAt = java.time.LocalDateTime.now();
        if (category == null || category.isBlank()) category = "SIGNUP";
        if (required == null) required = true;
        if (isActive == null) isActive = true;

        Terms terms = new Terms();
        terms.setType(type.trim());
        terms.setVersion(version.trim());
        terms.setTitle(title.trim());
        terms.setContent(content);
        terms.setEffectiveAt(effectiveAt);
        terms.setCategory(category.trim());
        terms.setRequired(required);
        terms.setIsActive(isActive);
        terms = termsRepository.save(Objects.requireNonNull(terms, "저장할 Terms는 null일 수 없습니다."));
        return toDetail(terms);
    }

    /** 약관 수정 */
    @Transactional
    public TermsDetailDto update(UUID id, String type, String version, String title,
                                 String content, java.time.LocalDateTime effectiveAt, String category, Boolean required, Boolean isActive) {
        Terms terms = termsRepository.findById(Objects.requireNonNull(id, "약관 ID는 null일 수 없습니다."))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관 ID: " + id));

        if (type != null && !type.isBlank()) terms.setType(type.trim());
        if (version != null && !version.isBlank()) terms.setVersion(version.trim());
        if (title != null && !title.isBlank()) terms.setTitle(title.trim());
        if (content != null) terms.setContent(content);
        if (effectiveAt != null) terms.setEffectiveAt(effectiveAt);
        if (category != null && !category.isBlank()) terms.setCategory(category.trim());
        if (required != null) terms.setRequired(required);
        if (isActive != null) terms.setIsActive(isActive);

        terms = termsRepository.save(Objects.requireNonNull(terms, "저장할 Terms는 null일 수 없습니다."));
        return toDetail(terms);
    }

    /** 새 버전 등록 (기존 약관 복사, version·effectiveAt만 새로) */
    @Transactional
    public TermsDetailDto createNewVersion(UUID sourceId, String newVersion, java.time.LocalDateTime effectiveAt) {
        Terms source = termsRepository.findById(Objects.requireNonNull(sourceId, "sourceId는 null일 수 없습니다."))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관 ID: " + sourceId));
        if (newVersion == null || newVersion.isBlank()) throw new IllegalArgumentException("새 버전을 입력하세요.");
        if (effectiveAt == null) effectiveAt = java.time.LocalDateTime.now();

        Terms terms = new Terms();
        terms.setType(source.getType());
        terms.setVersion(newVersion.trim());
        terms.setTitle(source.getTitle());
        terms.setContent(source.getContent() != null ? source.getContent() : "");
        terms.setEffectiveAt(effectiveAt);
        terms.setCategory(source.getCategory() != null ? source.getCategory() : "SIGNUP");
        terms.setRequired(source.getRequired() != null ? source.getRequired() : true);
        terms.setIsActive(source.getIsActive() != null ? source.getIsActive() : true);
        terms = termsRepository.save(terms);
        return toDetail(terms);
    }

    /** 약관 삭제 */
    @Transactional
    public void delete(UUID id) {
        UUID validId = Objects.requireNonNull(id, "약관 ID는 null일 수 없습니다.");
        if (!termsRepository.existsById(validId)) {
            throw new IllegalArgumentException("존재하지 않는 약관 ID: " + validId);
        }
        termsRepository.deleteById(validId);
    }

    private TermsDetailDto toDetail(Terms t) {
        Objects.requireNonNull(t, "Terms는 null일 수 없습니다.");
        TermsDetailDto d = new TermsDetailDto();
        d.setId(t.getId());
        d.setType(t.getType() != null ? t.getType() : "");
        d.setTitle(t.getTitle() != null ? t.getTitle() : "");
        d.setVersion(t.getVersion() != null ? t.getVersion() : "");
        d.setContent(t.getContent() != null ? t.getContent() : "");
        d.setEffectiveAt(t.getEffectiveAt());
        d.setCategory(t.getCategory() != null ? t.getCategory() : "SIGNUP");
        d.setRequired(t.getRequired() != null ? t.getRequired() : true);
        d.setIsActive(t.getIsActive() != null ? t.getIsActive() : true);
        return d;
    }
}
