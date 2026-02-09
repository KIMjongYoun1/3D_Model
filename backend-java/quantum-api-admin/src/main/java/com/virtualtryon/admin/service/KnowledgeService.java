package com.virtualtryon.admin.service;

import com.virtualtryon.core.entity.Knowledge;
import com.virtualtryon.core.repository.KnowledgeRepository;
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
}
