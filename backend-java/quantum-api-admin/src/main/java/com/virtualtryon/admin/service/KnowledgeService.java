package com.virtualtryon.admin.service;

import com.virtualtryon.core.entity.Knowledge;
import com.virtualtryon.core.repository.KnowledgeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class KnowledgeService {

    private final KnowledgeRepository knowledgeRepository;

    public KnowledgeService(KnowledgeRepository knowledgeRepository) {
        this.knowledgeRepository = knowledgeRepository;
    }

    @Transactional
    @SuppressWarnings("null")
    public Knowledge createKnowledge(String category, String title, String content, String sourceUrl) {
        if (category == null || title == null || content == null) {
            throw new IllegalArgumentException("필수 필드(카테고리, 제목, 내용)가 누락되었습니다.");
        }
        
        Knowledge knowledge = Knowledge.builder()
                .category(category)
                .title(title)
                .content(content)
                .sourceUrl(sourceUrl)
                .build();
        
        return knowledgeRepository.save(knowledge);
    }

    @Transactional(readOnly = true)
    public List<Knowledge> getAllActiveKnowledge() {
        return knowledgeRepository.findByIsActiveTrueOrderByUpdatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Page<Knowledge> searchKnowledge(List<String> categories, String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        if (categories == null || categories.isEmpty()) {
            return knowledgeRepository.searchActiveByQuery(q != null ? q.trim() : "", pageable);
        }
        return knowledgeRepository.searchActiveByCategoriesAndQuery(categories, q != null ? q.trim() : "", pageable);
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctCategories() {
        return knowledgeRepository.findDistinctCategories();
    }

    @Transactional(readOnly = true)
    public Knowledge getKnowledgeById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("지식 ID가 누락되었습니다.");
        }
        return knowledgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지식을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<Knowledge> getKnowledgeByCategory(String category) {
        return knowledgeRepository.findByCategoryAndIsActiveTrueOrderByUpdatedAtDesc(category);
    }

    @Transactional
    public void deleteKnowledge(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("삭제할 ID가 누락되었습니다.");
        }
        knowledgeRepository.deleteById(id);
    }

    /**
     * API에서 불러온 항목을 항목별·카테고리별로 저장.
     * source_type + external_id 가 있으면 기존 행 갱신(upsert), 없으면 신규 삽입.
     */
    @Transactional
    public Knowledge saveOrUpdateKnowledge(Knowledge k) {
        if (k == null) return null;
        String sourceType = k.getSourceType();
        String externalId = k.getExternalId();
        if (sourceType != null && externalId != null && !externalId.isBlank()) {
            return knowledgeRepository.findBySourceTypeAndExternalId(sourceType, externalId)
                    .map(existing -> {
                        existing.setTitle(k.getTitle());
                        existing.setContent(k.getContent());
                        existing.setArticleBody(k.getArticleBody());
                        existing.setSourceUrl(k.getSourceUrl());
                        existing.setCategory(k.getCategory());
                        return knowledgeRepository.save(existing);
                    })
                    .orElseGet(() -> knowledgeRepository.save(k));
        }
        return knowledgeRepository.save(k);
    }
}
