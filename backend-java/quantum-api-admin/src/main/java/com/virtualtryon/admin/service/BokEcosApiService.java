package com.virtualtryon.admin.service;

import com.virtualtryon.core.entity.Knowledge;
import com.virtualtryon.core.repository.KnowledgeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 한국은행 경제통계시스템(ECOS) API 연동 서비스
 */
@Service
public class BokEcosApiService {

    private final KnowledgeRepository knowledgeRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${external.bok-ecos-key:}")
    private String bokEcosKey;

    private static final String BASE_URL = "https://ecos.bok.or.kr/api/StatisticSearch";

    public BokEcosApiService(KnowledgeRepository knowledgeRepository) {
        this.knowledgeRepository = knowledgeRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 한국은행 API를 통해 특정 통계 수집 (예: 기준금리)
     * 호출 예시: /0000001/JSON/kr/1/1/722Y001/D/20240101/20240131/0101000
     */
    @Transactional
    @SuppressWarnings("null")
    public List<Knowledge> fetchBaseInterestRate() {
        if (bokEcosKey == null || bokEcosKey.isEmpty()) {
            throw new IllegalStateException("한국은행 API 키가 설정되지 않았습니다.");
        }

        // 한국은행 기준금리 통계 코드: 722Y001
        String url = String.format("%s/%s/JSON/kr/1/10/722Y001/D/20240101/20241231/0101000", 
                                    BASE_URL, bokEcosKey);

        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) return new ArrayList<>();
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode row = root.path("StatisticSearch").path("row");

            List<Knowledge> results = new ArrayList<>();
            if (row.isArray() && row.size() > 0) {
                JsonNode latest = row.get(0); // 최신 데이터
                String rate = latest.path("DATA_VALUE").asText();
                String date = latest.path("TIME").asText();

                Knowledge k = Knowledge.builder()
                        .category("FINANCE_ECONOMY")
                        .title("한국은행 기준금리 (최신)")
                        .content(String.format("현재 한국은행 기준금리는 %s%%입니다. (기준일자: %s). 이 수치는 기업의 조달 금리 및 재무 분석의 기준 지표로 활용됩니다.", rate, date))
                        .sourceUrl("https://ecos.bok.or.kr")
                        .build();
                
                Knowledge saved = knowledgeRepository.save(k);
                if (saved != null) {
                    results.add(saved);
                }
            }
            return results;
        } catch (Exception e) {
            System.err.println("❌ 한국은행 API 호출 실패: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
