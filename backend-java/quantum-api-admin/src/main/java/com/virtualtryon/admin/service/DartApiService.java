package com.virtualtryon.admin.service;

import com.virtualtryon.core.entity.Knowledge;
import com.virtualtryon.core.entity.KnowledgeDart;
import com.virtualtryon.core.entity.KnowledgeFetchHistory;
import com.virtualtryon.core.repository.KnowledgeDartRepository;
import com.virtualtryon.core.repository.KnowledgeFetchHistoryRepository;
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

    private final KnowledgeFetchHistoryRepository fetchHistoryRepository;
    private final KnowledgeDartRepository knowledgeDartRepository;
    private final KnowledgeService knowledgeService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${external.fss-dart-key:}")
    private String dartApiKey;

    private static final String BASE_URL = "https://opendart.fss.or.kr/api";
    private static final String SOURCE_TYPE = "fss_dart";

    public DartApiService(KnowledgeFetchHistoryRepository fetchHistoryRepository,
                          KnowledgeDartRepository knowledgeDartRepository,
                          KnowledgeService knowledgeService) {
        this.fetchHistoryRepository = fetchHistoryRepository;
        this.knowledgeDartRepository = knowledgeDartRepository;
        this.knowledgeService = knowledgeService;
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

        // list.json API: bgn_de, end_de 필수. corp_code 없으면 검색기간 3개월 제한. 페이지네이션으로 전건 수집
        int pageCount = 100;
        KnowledgeFetchHistory history = new KnowledgeFetchHistory();
        history.setSourceType(SOURCE_TYPE);
        history.setStatus("RUNNING");
        history.setParamsJson(corpName != null ? "{\"corpName\":\"" + corpName + "\"}" : "{}");
        try {
            history = fetchHistoryRepository.save(history);
        } catch (Exception ex) {
            System.err.println("⚠️ 수집 히스토리 저장 실패(테이블 없을 수 있음): " + ex.getMessage());
        }

        List<Knowledge> results = new ArrayList<>();
        try {
            int pageNo = 1;
            while (true) {
                String url = String.format("%s/list.json?crtfc_key=%s&bgn_de=20250101&end_de=20250331&page_no=%d&page_count=%d",
                        BASE_URL, dartApiKey, pageNo, pageCount);
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
                String status = root.path("status").asText("");
                if (!"000".equals(status)) {
                    String msg = root.path("message").asText("API 오류");
                    if (history.getId() != null) {
                        history.setStatus("FAILED");
                        history.setErrorMessage(msg);
                        try { fetchHistoryRepository.save(history); } catch (Exception ignored) {}
                    }
                    System.err.println("❌ DART API 오류: status=" + status + ", " + msg);
                    return new ArrayList<>();
                }

                JsonNode list = root.path("list");
                if (!list.isArray() || list.size() == 0) break;

                for (JsonNode node : list) {
                    String reportNm = node.path("report_nm").asText("");
                    String itemCorpName = node.path("corp_name").asText("");
                    String rceptDt = node.path("rcept_dt").asText("");
                    String rceptNo = node.path("rcept_no").asText("");
                    String corpCode = node.path("corp_code").asText("");
                    String flrNm = node.path("flr_nm").asText("");
                    String rm = node.path("rm").asText("");

                    if (rceptNo.isEmpty()) continue;

                    // knowledge_dart 테이블에 아이템별 저장 (진짜 지식)
                    KnowledgeDart dart = knowledgeDartRepository.findByRceptNo(rceptNo)
                            .orElse(new KnowledgeDart());
                    dart.setCorpCode(corpCode.isEmpty() ? "UNKNOWN" : corpCode);
                    dart.setCorpName(itemCorpName);
                    dart.setRceptNo(rceptNo);
                    dart.setRceptDt(rceptDt);
                    dart.setReportNm(reportNm);
                    dart.setFlrNm(flrNm.length() > 100 ? flrNm.substring(0, 100) : flrNm);
                    dart.setRm(rm.length() > 500 ? rm.substring(0, 500) : rm);
                    knowledgeDartRepository.save(dart);

                    // knowledge_base에도 저장 (RAG 연동용)
                    Knowledge k = Knowledge.builder()
                            .category("FINANCE_DART")
                            .title(String.format("[%s] %s", itemCorpName, reportNm))
                            .content(String.format("기업 %s의 공시 보고서. 접수일자: %s, 보고서명: %s. 상세 내용은 DART에서 확인 가능.", itemCorpName, rceptDt, reportNm))
                            .sourceUrl("https://dart.fss.or.kr/dsaf001/main.do?rcpNo=" + rceptNo)
                            .sourceType(SOURCE_TYPE)
                            .externalId(rceptNo)
                            .build();
                    Knowledge saved = knowledgeService.saveOrUpdateKnowledge(k);
                    if (saved != null) results.add(saved);
                }
                if (list.size() < pageCount) break;
                pageNo++;
                if (pageNo > 50) break; // 무한루프 방지 (최대 50페이지 = 5000건)
            }

            if (history.getId() != null) {
                history.setStatus("SUCCESS");
                history.setItemCount(results.size());
                try { fetchHistoryRepository.save(history); } catch (Exception ignored) {}
            }
            return results;
        } catch (Exception e) {
            if (history.getId() != null) {
                history.setStatus("FAILED");
                history.setErrorMessage(e.getMessage());
                try { fetchHistoryRepository.save(history); } catch (Exception ignored) {}
            }
            System.err.println("❌ DART API 호출 실패: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
