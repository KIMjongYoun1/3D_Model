package com.virtualtryon.admin.service;

import com.virtualtryon.core.entity.Knowledge;
import com.virtualtryon.core.entity.KnowledgeBok;
import com.virtualtryon.core.entity.KnowledgeFetchHistory;
import com.virtualtryon.core.repository.KnowledgeBokRepository;
import com.virtualtryon.core.repository.KnowledgeFetchHistoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 한국은행 경제통계시스템(ECOS) API 연동 서비스
 */
@Service
public class BokEcosApiService {

    private final KnowledgeFetchHistoryRepository fetchHistoryRepository;
    private final KnowledgeBokRepository knowledgeBokRepository;
    private final KnowledgeService knowledgeService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${external.bok-ecos-key:}")
    private String bokEcosKey;

    private static final Logger log = LoggerFactory.getLogger(BokEcosApiService.class);

    private static final String BASE_URL = "https://ecos.bok.or.kr/api/StatisticSearch";
    private static final String SOURCE_TYPE = "bok_ecos";

    public BokEcosApiService(KnowledgeFetchHistoryRepository fetchHistoryRepository,
                             KnowledgeBokRepository knowledgeBokRepository,
                             KnowledgeService knowledgeService) {
        this.fetchHistoryRepository = fetchHistoryRepository;
        this.knowledgeBokRepository = knowledgeBokRepository;
        this.knowledgeService = knowledgeService;
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

        // 한국은행 기준금리 통계 코드: 722Y001 (ECOS API 최대 1000건/요청)
        String url = String.format("%s/%s/JSON/kr/1/1000/722Y001/D/20240101/20241231/0101000", 
                                    BASE_URL, bokEcosKey);
        log.info("한국은행 ECOS API 호출: statCode=722Y001, 건수=1~1000 (기준금리)");

        KnowledgeFetchHistory history = new KnowledgeFetchHistory();
        history.setSourceType(SOURCE_TYPE);
        history.setStatus("RUNNING");
        try {
            history = fetchHistoryRepository.save(history);
        } catch (Exception ex) {
            log.warn("수집 히스토리 저장 실패(테이블 없을 수 있음): {}", ex.getMessage());
        }

        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                if (history.getId() != null) {
                    history.setStatus("FAILED");
                    history.setErrorMessage("API 응답 없음");
                    try { fetchHistoryRepository.save(history); } catch (Exception ignored) {}
                }
                return new ArrayList<>();
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode row = root.path("StatisticSearch").path("row");
            int rowCount = row.isArray() ? row.size() : 0;
            log.info("한국은행 API 응답: row 건수={}", rowCount);

            List<Knowledge> results = new ArrayList<>();
            List<KnowledgeBok> bokSaved = new ArrayList<>();
            String statCode = "722Y001"; // 기본값 (기준금리)

            if (row.isArray()) {
                for (JsonNode node : row) {
                    if (statCode.equals("722Y001")) {
                        String sc = getNodeText(node, "STAT_CODE", "차트코드");
                        if (!sc.isEmpty()) statCode = sc;
                    }
                    String dataValue = getNodeText(node, "DATA_VALUE", "데이터값");
                    String time = getNodeText(node, "TIME", "시점");
                    String statName = getNodeText(node, "STAT_NAME", "차트이름");
                    String itemCode1 = getNodeText(node, "ITEM_CODE1", "항목코드1");
                    String itemName1 = getNodeText(node, "ITEM_NAME1", "항목명1");
                    String itemCode2 = getNodeText(node, "ITEM_CODE2", "항목코드2");
                    String itemName2 = getNodeText(node, "ITEM_NAME2", "항목명2");
                    String unitName = getNodeText(node, "UNIT_NAME", "단위");

                    if (dataValue.isEmpty() || time.isEmpty()) continue;

                    // knowledge_bok 테이블에 아이템별 저장 (진짜 지식)
                    KnowledgeBok bok = knowledgeBokRepository.findByStatCodeAndItemCode1AndTime(statCode, orNull(itemCode1), time)
                            .orElse(new KnowledgeBok());
                    bok.setStatCode(statCode);
                    bok.setStatName(statName);
                    bok.setItemCode1(orNull(itemCode1));
                    bok.setItemName1(orNull(itemName1));
                    bok.setItemCode2(orNull(itemCode2));
                    bok.setItemName2(orNull(itemName2));
                    bok.setTime(time);
                    bok.setDataValue(dataValue);
                    bok.setUnitName(orNull(unitName));
                    KnowledgeBok savedBok = knowledgeBokRepository.save(bok);
                    bokSaved.add(savedBok);

                    // knowledge_base에도 저장 (RAG 연동용)
                    String extId = "bok_" + statCode + "_" + (itemCode1 != null ? itemCode1 : "x") + "_" + time;
                    Knowledge k = Knowledge.builder()
                            .category("FINANCE_ECONOMY")
                            .title(itemName1 != null && !itemName1.isEmpty() ? itemName1 : "한국은행 기준금리")
                            .content(String.format("기준금리 %s%% (기준일자: %s). 기업 조달 금리·재무 분석의 기준 지표.", dataValue, time))
                            .sourceUrl("https://ecos.bok.or.kr")
                            .sourceType(SOURCE_TYPE)
                            .externalId(extId)
                            .build();
                    Knowledge saved = knowledgeService.saveOrUpdateKnowledge(k);
                    if (saved != null) results.add(saved);
                }
            }

            int itemCount = Math.max(results.size(), bokSaved.size());
            log.info("한국은행 수집 완료: knowledge={}, knowledge_bok={}건 저장", results.size(), bokSaved.size());

            if (history.getId() != null) {
                history.setStatus("SUCCESS");
                history.setItemCount(itemCount);
                try { fetchHistoryRepository.save(history); } catch (Exception ignored) {}
            }
            return results;
        } catch (Exception e) {
            if (history.getId() != null) {
                history.setStatus("FAILED");
                history.setErrorMessage(e.getMessage());
                try { fetchHistoryRepository.save(history); } catch (Exception ignored) {}
            }
            log.error("한국은행 API 호출 실패: {}", e.getMessage() != null ? e.getMessage() : "알 수 없음", e);
            return new ArrayList<>();
        }
    }

    private String getNodeText(JsonNode node, String enKey, String koKey) {
        if (node == null) return "";
        String v = node.path(enKey).asText("");
        if (v.isEmpty()) v = node.path(koKey).asText("");
        return v == null ? "" : v.trim();
    }

    private String orNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}
