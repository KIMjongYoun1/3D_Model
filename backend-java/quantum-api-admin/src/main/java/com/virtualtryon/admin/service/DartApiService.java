package com.virtualtryon.admin.service;

import com.virtualtryon.core.entity.Knowledge;
import com.virtualtryon.core.repository.KnowledgeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 금융감독원 DART 오픈 API 연동 서비스
 */
@Service
public class DartApiService {

    private final KnowledgeRepository knowledgeRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${external.fss-dart-key:}")
    private String dartApiKey;

    private static final String BASE_URL = "https://opendart.fss.or.kr/api";

    public DartApiService(KnowledgeRepository knowledgeRepository) {
        this.knowledgeRepository = knowledgeRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 특정 기업의 공시 요약 정보를 수집하여 지식화
     * (예시: 삼성전자 등 주요 기업의 최근 공시 목록)
     */
    @Transactional
    @SuppressWarnings("null")
    public List<Knowledge> fetchCompanyReports(String corpName) {
        if (dartApiKey == null || dartApiKey.isEmpty()) {
            throw new IllegalStateException("DART API 키가 설정되지 않았습니다.");
        }

        // 1. 기업 고유번호 검색 (실제로는 corp_code를 먼저 찾아야 함)
        // 여기서는 단순화를 위해 최근 공시 목록(list.json) API를 사용
        String url = String.format("%s/list.json?crtfc_key=%s&bgn_de=20240101&page_count=10", 
                                    BASE_URL, dartApiKey);

        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) return new ArrayList<>();

            JsonNode root = objectMapper.readTree(response);
            JsonNode list = root.path("list");

            List<Knowledge> results = new ArrayList<>();
            if (list.isArray()) {
                for (JsonNode node : list) {
                    String title = node.path("report_nm").asText();
                    String corp = node.path("corp_name").asText();
                    String date = node.path("rcept_dt").asText();
                    String rceptNo = node.path("rcept_no").asText();

                    Knowledge k = Knowledge.builder()
                            .category("FINANCE_DART")
                            .title(String.format("[%s] %s", corp, title))
                            .content(String.format("기업 %s의 공시 보고서입니다. 접수일자: %s, 보고서명: %s. 상세 내용은 DART에서 확인 가능합니다.", corp, date, title))
                            .sourceUrl("https://dart.fss.or.kr/dsaf001/main.do?rcpNo=" + rceptNo)
                            .build();
                    
                    Knowledge saved = knowledgeRepository.save(k);
                    if (saved != null) {
                        results.add(saved);
                    }
                }
            }
            return results;
        } catch (Exception e) {
            System.err.println("❌ DART API 호출 실패: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
