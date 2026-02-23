package com.virtualtryon.admin.service;

import com.virtualtryon.core.entity.Knowledge;
import com.virtualtryon.core.entity.KnowledgeFetchHistory;
import com.virtualtryon.core.entity.KnowledgeLaw;
import com.virtualtryon.core.repository.KnowledgeFetchHistoryRepository;
import com.virtualtryon.core.repository.KnowledgeLawRepository;
import com.virtualtryon.core.repository.KnowledgeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 국가법령정보센터 오픈 API 연동 서비스
 * 
 * API 문서: https://open.law.go.kr
 * 인증: 기관코드(OC) = 가입 이메일 ID
 * 
 * 법령 검색 → knowledge_base에 저장 → AI RAG에서 법률 근거로 활용
 */
@Service
public class LawApiService {

    private static final Logger log = LoggerFactory.getLogger(LawApiService.class);

    private static final String SOURCE_TYPE = "law_api";

    private final KnowledgeRepository knowledgeRepository;
    private final KnowledgeFetchHistoryRepository fetchHistoryRepository;
    private final KnowledgeLawRepository knowledgeLawRepository;
    private final KnowledgeService knowledgeService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${external.law-api-oc:}")
    private String lawApiOc;

    // 국가법령정보센터 오픈 API
    private static final String LAW_SEARCH_URL = "http://www.law.go.kr/DRF/lawSearch.do";
    private static final String LAW_SERVICE_URL = "http://www.law.go.kr/DRF/lawService.do";

    public LawApiService(KnowledgeRepository knowledgeRepository,
                         KnowledgeFetchHistoryRepository fetchHistoryRepository,
                         KnowledgeLawRepository knowledgeLawRepository,
                         KnowledgeService knowledgeService) {
        this.knowledgeRepository = Objects.requireNonNull(knowledgeRepository, "knowledgeRepository must not be null");
        this.fetchHistoryRepository = fetchHistoryRepository;
        this.knowledgeLawRepository = knowledgeLawRepository;
        this.knowledgeService = knowledgeService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * OC 키 유효성 검사
     */
    private void validateApiKey() {
        if (lawApiOc == null || lawApiOc.isBlank()) {
            throw new IllegalStateException("국가법령정보센터 API 키(OC)가 설정되지 않았습니다. .env에 LAW_API_OC를 설정하세요.");
        }
    }

    /**
     * 법령 API 호출 결과를 그대로 반환 (어떤 데이터가 오는지 확인용)
     * 저장하지 않고 검색 API만 호출해 원본 JSON 구조를 반환
     */
    public Object fetchLawApiPreview(String lawName) {
        if (lawName == null || lawName.isBlank()) {
            throw new IllegalArgumentException("법령명을 입력하세요.");
        }
        validateApiKey();
        String encodedQuery = URLEncoder.encode(lawName.trim(), StandardCharsets.UTF_8);
        String url = String.format("%s?OC=%s&target=law&type=JSON&query=%s&display=100&page=1",
                LAW_SEARCH_URL, lawApiOc, encodedQuery);
        log.info("법령 API 미리보기 호출: query={}", lawName);
        String response = callExternalApi(url);
        if (response.isBlank()) {
            return java.util.Map.of("error", "API 응답 없음", "url", url);
        }
        try {
            return objectMapper.readValue(response, Object.class);
        } catch (Exception e) {
            return java.util.Map.of("error", "JSON 파싱 실패: " + e.getMessage(), "raw", response.substring(0, Math.min(500, response.length())));
        }
    }

    /**
     * 전체 법령 수집: 법령 체계도(lsStmd) API로 전체 목록 페이지네이션 조회
     * query 없이 target=lsStmd 호출 → 전체 법령 목록 수집
     *
     * @return 저장된 Knowledge 목록
     */
    @Transactional
    public List<Knowledge> fetchAllLawsManually() {
        validateApiKey();

        KnowledgeFetchHistory history = new KnowledgeFetchHistory();
        history.setSourceType(SOURCE_TYPE);
        history.setStatus("RUNNING");
        history.setParamsJson("{\"mode\":\"all\",\"target\":\"lsStmd\"}");
        try {
            history = fetchHistoryRepository.save(history);
        } catch (Exception ex) {
            log.warn("수집 히스토리 저장 실패: {}", ex.getMessage());
        }

        try {
            List<Knowledge> laws = fetchAllLawsFromLsStmd();
            List<Knowledge> saved = saveOrUpdateLaws(laws);
            if (history.getId() != null) {
                history.setStatus("SUCCESS");
                history.setItemCount(saved.size());
                try { fetchHistoryRepository.save(history); } catch (Exception ignored) {}
            }
            return saved;
        } catch (Exception e) {
            if (history.getId() != null) {
                history.setStatus("FAILED");
                history.setErrorMessage(e.getMessage());
                try { fetchHistoryRepository.save(history); } catch (Exception ignored) {}
            }
            throw e;
        }
    }

    /**
     * 수동 업데이트: 특정 법령명으로 검색 및 저장
     * 
     * @param lawName 검색할 법령명 (예: "부가가치세법", "소득세법", "근로기준법")
     * @return 저장된 Knowledge 목록
     */
    @Transactional
    public List<Knowledge> updateLawManually(String lawName) {
        Objects.requireNonNull(lawName, "lawName must not be null");
        if (lawName.isBlank()) {
            throw new IllegalArgumentException("법령명이 비어있습니다.");
        }
        validateApiKey();

        KnowledgeFetchHistory history = new KnowledgeFetchHistory();
        history.setSourceType(SOURCE_TYPE);
        history.setStatus("RUNNING");
        history.setParamsJson("{\"lawName\":\"" + lawName.replace("\"", "\\\"") + "\"}");
        try {
            history = fetchHistoryRepository.save(history);
        } catch (Exception ex) {
            log.warn("수집 히스토리 저장 실패(테이블 없을 수 있음): {}", ex.getMessage());
        }

        try {
            List<Knowledge> laws = searchLawFromApi(lawName);
            List<Knowledge> saved = saveOrUpdateLaws(laws);
            if (history.getId() != null) {
                history.setStatus("SUCCESS");
                history.setItemCount(saved.size());
                try { fetchHistoryRepository.save(history); } catch (Exception ignored) {}
            }
            return saved;
        } catch (Exception e) {
            if (history.getId() != null) {
                history.setStatus("FAILED");
                history.setErrorMessage(e.getMessage());
                try { fetchHistoryRepository.save(history); } catch (Exception ignored) {}
            }
            throw e;
        }
    }

    /**
     * 자동 배치 업데이트: 매주 일요일 새벽 2시에 실행
     * 기존 활성화된 법령들의 최신 버전을 체크하여 업데이트
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    @Transactional
    public void scheduledUpdate() {
        if (lawApiOc == null || lawApiOc.isBlank()) {
            log.warn("[Batch] LAW_API_OC가 설정되지 않아 법령 배치 업데이트를 건너뜁니다.");
            return;
        }

        log.info("[Batch] 법령 지식 배치 업데이트 시작...");
        List<Knowledge> activeLaws = knowledgeRepository.findByIsActiveTrueOrderByUpdatedAtDesc();
        if (activeLaws == null || activeLaws.isEmpty()) {
            log.info("[Batch] 업데이트할 활성 법령이 없습니다.");
            return;
        }

        int updated = 0;
        for (Knowledge law : activeLaws) {
            String category = law.getCategory();
            String title = law.getTitle();

            if (category == null || title == null) continue;

            if (category.equals("LAW") || category.startsWith("LAW_")) {
                try {
                    List<Knowledge> freshData = searchLawFromApi(title);
                    saveOrUpdateLaws(freshData);
                    updated++;
                } catch (Exception e) {
                    log.error("[Batch] 법령 업데이트 실패: {} - {}", title,
                            e.getMessage() != null ? e.getMessage() : "알 수 없음");
                }
            }
        }
        log.info("[Batch] 법령 지식 배치 업데이트 완료: {}건 처리", updated);
    }

    /**
     * 국가법령정보센터 API로 법령 검색
     * 
     * API: http://www.law.go.kr/DRF/lawSearch.do?OC={OC}&target=law&type=JSON&query={검색어}
     */
    private List<Knowledge> searchLawFromApi(String lawName) {
        if (lawName == null || lawName.isBlank()) {
            return Collections.emptyList();
        }

        List<Knowledge> results = new ArrayList<>();

        try {
            String encodedQuery = URLEncoder.encode(lawName, StandardCharsets.UTF_8);
            int display = 100; // API 최대 100건
            int page = 1;

            List<KnowledgeLaw> lawEntities = new ArrayList<>();
            while (true) {
                String url = String.format("%s?OC=%s&target=law&type=JSON&query=%s&display=%d&page=%d",
                        LAW_SEARCH_URL, lawApiOc, encodedQuery, display, page);

                String maskedUrl = maskApiKeyInUrl(url);
                log.info("법령 검색 API 호출: query={}, page={}, url={}", lawName, page, maskedUrl);
                String response = callExternalApi(url);
                if (response.isBlank()) {
                    if (page == 1) {
                        log.warn("법령 검색 API 응답이 비어있습니다: query={}, url={}", lawName, maskedUrl);
                    }
                    break;
                }
                log.info("법령 API 응답 길이={}, 미리보기={}", response.length(),
                    response.length() > 400 ? response.substring(0, 400) + "..." : response);

                JsonNode root = objectMapper.readTree(response);
                if (root == null) {
                    log.warn("법령 API JSON 파싱 결과 root=null");
                    break;
                }

                JsonNode lawSearch = root.path("LawSearch");
                String resultCode = lawSearch.path("resultCode").asText("");
                String totalCnt = lawSearch.path("totalCnt").asText("");
                if (page == 1) {
                    log.info("법령 API resultCode={}, totalCnt={}", resultCode, totalCnt);
                }
                if (!"00".equals(resultCode)) {
                    String resultMsg = lawSearch.path("resultMsg").asText("");
                    log.warn("법령 API 오류: resultCode={}, resultMsg={}", resultCode, resultMsg);
                }

                JsonNode lawList = lawSearch.path("law");
                if (lawList.isMissingNode() || lawList.isNull()) {
                    java.util.Iterator<String> keys = lawSearch.isObject() ? lawSearch.fieldNames() : java.util.Collections.emptyIterator();
                    StringBuilder keyList = new StringBuilder();
                    keys.forEachRemaining(k -> keyList.append(k).append(","));
                    log.warn("법령 API law 노드 없음 또는 null. LawSearch 하위 키: {}", keyList);
                    break;
                }

                // 단일 객체 또는 배열 모두 처리
                int fetched = 0;
                if (lawList.isArray()) {
                    for (JsonNode lawNode : lawList) {
                        if (lawNode == null) continue;
                        Knowledge k = parseLawNode(lawNode);
                        if (k != null) {
                            results.add(k);
                            KnowledgeLaw kl = parseLawNodeToKnowledgeLaw(lawNode);
                            if (kl != null) {
                                KnowledgeLaw toSave = Objects.requireNonNull(
                                        knowledgeLawRepository.findByMst(kl.getMst())
                                                .map(existing -> {
                                                    existing.setLawNameKo(kl.getLawNameKo());
                                                    existing.setLawType(kl.getLawType());
                                                    existing.setDeptName(kl.getDeptName());
                                                    existing.setProclamationNo(kl.getProclamationNo());
                                                    existing.setProclamationDate(kl.getProclamationDate());
                                                    existing.setEnforceDate(kl.getEnforceDate());
                                                    existing.setLawId(kl.getLawId());
                                                    existing.setContent(kl.getContent());
                                                    existing.setSourceUrl(kl.getSourceUrl());
                                                    return existing;
                                                })
                                                .orElse(kl),
                                        "toSave must not be null");
                                lawEntities.add(knowledgeLawRepository.save(toSave));
                            }
                        }
                        fetched++;
                    }
                } else {
                    Knowledge k = parseLawNode(lawList);
                    if (k != null) {
                        results.add(k);
                        KnowledgeLaw kl = parseLawNodeToKnowledgeLaw(lawList);
                        if (kl != null) {
                            KnowledgeLaw toSave = Objects.requireNonNull(
                                    knowledgeLawRepository.findByMst(kl.getMst())
                                            .map(existing -> {
                                                existing.setLawNameKo(kl.getLawNameKo());
                                                existing.setLawType(kl.getLawType());
                                                existing.setDeptName(kl.getDeptName());
                                                existing.setProclamationNo(kl.getProclamationNo());
                                                existing.setProclamationDate(kl.getProclamationDate());
                                                existing.setEnforceDate(kl.getEnforceDate());
                                                existing.setLawId(kl.getLawId());
                                                existing.setContent(kl.getContent());
                                                existing.setSourceUrl(kl.getSourceUrl());
                                                return existing;
                                            })
                                            .orElse(kl),
                                    "toSave must not be null");
                            lawEntities.add(knowledgeLawRepository.save(toSave));
                        }
                    }
                    fetched = 1;
                }
                if (fetched < display) break;
                page++;
                if (page > 20) break; // 무한루프 방지 (최대 20페이지 = 2000건)
            }

            // 전체 결과에 대해 법령 본문(조문) 조회 및 저장
            for (int i = 0; i < results.size(); i++) {
                Knowledge law = safeGet(results, i);
                if (law == null) continue;
                enrichLawDetail(law);
                if (i < lawEntities.size()) {
                    KnowledgeLaw entity = safeGet(lawEntities, i);
                    String articleBody = law.getArticleBody();
                    if (entity != null && articleBody != null) {
                        entity.setArticleBody(articleBody);
                        knowledgeLawRepository.save(entity);
                    }
                }
            }

            log.info("법령 검색 완료: {}건 조회됨", results.size());

        } catch (Exception e) {
            log.error("법령 검색 API 호출 실패: {} - {} | 예외상세: ", lawName,
                e.getMessage() != null ? e.getMessage() : "알 수 없음", e);
        }

        return results;
    }

    /**
     * 법령 체계도(lsStmd) API로 전체 법령 목록 페이지네이션 조회
     * target=lsStmd, query 없음 → 전체 목록 (display=100, page=1,2,...)
     */
    private List<Knowledge> fetchAllLawsFromLsStmd() {
        List<Knowledge> results = new ArrayList<>();
        int display = 100;
        int page = 1;
        int maxPages = 500; // 무한루프 방지 (최대 5만 건)

        try {
            while (page <= maxPages) {
                String url = String.format("%s?OC=%s&target=lsStmd&type=JSON&display=%d&page=%d",
                        LAW_SEARCH_URL, lawApiOc, display, page);

                log.info("법령 전체 수집(lsStmd) API 호출: page={}", page);
                String response = callExternalApi(url);
                if (response.isBlank()) {
                    if (page == 1) log.warn("법령 lsStmd API 응답이 비어있습니다.");
                    break;
                }

                JsonNode root = objectMapper.readTree(response);
                if (root == null) break;

                JsonNode lawSearch = root.path("LawSearch");
                if (lawSearch.isMissingNode()) lawSearch = root.path("LsStmdSearch");
                if (lawSearch.isMissingNode()) lawSearch = root.path("lsStmd");
                String resultCode = lawSearch.path("resultCode").asText("");
                int totalCnt = parseIntSafe(lawSearch.path("totalCnt"), 0);
                if (page == 1) {
                    log.info("법령 lsStmd API resultCode={}, totalCnt={}", resultCode, totalCnt);
                }
                if (!"00".equals(resultCode)) {
                    log.warn("법령 lsStmd API 오류: resultCode={}", resultCode);
                    break;
                }

                JsonNode lawList = lawSearch.path("law");
                if (lawList.isMissingNode()) lawList = lawSearch.path("lsStmd");
                if (lawList.isMissingNode() || lawList.isNull()) break;

                int fetched = 0;
                if (lawList.isArray()) {
                    for (JsonNode node : lawList) {
                        if (node == null) continue;
                        Knowledge k = parseLsStmdNode(node);
                        if (k != null) {
                            results.add(k);
                            KnowledgeLaw kl = parseLsStmdNodeToKnowledgeLaw(node);
                            if (kl != null) {
                                KnowledgeLaw toSave = Objects.requireNonNull(
                                        knowledgeLawRepository.findByMst(kl.getMst())
                                                .map(existing -> {
                                                    existing.setLawNameKo(kl.getLawNameKo());
                                                    existing.setLawType(kl.getLawType());
                                                    existing.setDeptName(kl.getDeptName());
                                                    existing.setProclamationNo(kl.getProclamationNo());
                                                    existing.setProclamationDate(kl.getProclamationDate());
                                                    existing.setEnforceDate(kl.getEnforceDate());
                                                    existing.setLawId(kl.getLawId());
                                                    existing.setContent(kl.getContent());
                                                    existing.setSourceUrl(kl.getSourceUrl());
                                                    return existing;
                                                })
                                                .orElse(kl),
                                        "toSave must not be null");
                                knowledgeLawRepository.save(toSave);
                            }
                            fetched++;
                        }
                    }
                } else {
                    Knowledge k = parseLsStmdNode(lawList);
                    if (k != null) {
                        results.add(k);
                        KnowledgeLaw kl = parseLsStmdNodeToKnowledgeLaw(lawList);
                        if (kl != null) {
                            KnowledgeLaw toSave = Objects.requireNonNull(
                                    knowledgeLawRepository.findByMst(kl.getMst())
                                            .map(existing -> {
                                                existing.setLawNameKo(kl.getLawNameKo());
                                                existing.setLawType(kl.getLawType());
                                                existing.setDeptName(kl.getDeptName());
                                                existing.setProclamationNo(kl.getProclamationNo());
                                                existing.setProclamationDate(kl.getProclamationDate());
                                                existing.setEnforceDate(kl.getEnforceDate());
                                                existing.setLawId(kl.getLawId());
                                                existing.setContent(kl.getContent());
                                                existing.setSourceUrl(kl.getSourceUrl());
                                                return existing;
                                            })
                                            .orElse(kl),
                                    "toSave must not be null");
                            knowledgeLawRepository.save(toSave);
                        }
                        fetched = 1;
                    }
                }

                if (fetched == 0) break;
                if (fetched < display || results.size() >= totalCnt) break;
                page++;
            }

            // lsStmd: 처음 30건에 대해 본문(조문) enrichment (전체 시 timeout 방지)
            int lsStmdEnrichLimit = Math.min(results.size(), 30);
            for (int i = 0; i < lsStmdEnrichLimit; i++) {
                Knowledge k = safeGet(results, i);
                if (k != null) {
                    enrichLawDetail(k);
                    String mst = k.getExternalId();
                    if (mst != null && !mst.isBlank() && k.getArticleBody() != null) {
                        knowledgeLawRepository.findByMst(mst).ifPresent(entity -> {
                            entity.setArticleBody(k.getArticleBody());
                            knowledgeLawRepository.save(entity);
                        });
                    }
                }
            }
            if (lsStmdEnrichLimit > 0) {
                log.info("법령 lsStmd 본문 enrichment: {}건", lsStmdEnrichLimit);
            }

            log.info("법령 전체 수집 완료: {}건", results.size());
        } catch (Exception e) {
            log.error("법령 lsStmd 전체 수집 실패: {}", e.getMessage() != null ? e.getMessage() : "알 수 없음", e);
            throw new RuntimeException("법령 전체 수집 실패: " + (e.getMessage() != null ? e.getMessage() : "알 수 없음"), e);
        }

        return results;
    }

    /** lsStmd 응답 형식 파싱 (법령명, 법령일련번호 등) */
    private Knowledge parseLsStmdNode(JsonNode node) {
        if (node == null) return null;
        String lawNameKo = getNodeText(node, "법령명한글", "lawNameKo");
        if (lawNameKo.isEmpty()) lawNameKo = getNodeText(node, "법령명", "lawNm");
        String mst = getNodeText(node, "법령일련번호", "lawMST");
        if (mst.isEmpty()) mst = String.valueOf(parseIntSafe(node.path("법령 일련번호"), 0));
        if ("0".equals(mst)) mst = "";
        String proclamationDate = getNodeText(node, "공포일자", "proclamationDate");
        if (proclamationDate.isEmpty() && node.has("공포일자"))
            proclamationDate = String.valueOf(node.path("공포일자").asInt(0));
        String lawType = getNodeText(node, "법령구분명", "법령구분");
        if (lawType.isEmpty()) lawType = "법률";

        if (lawNameKo.isEmpty()) return null;

        String category = determineLawCategory(lawNameKo);
        return Knowledge.builder()
                .category(category)
                .title(lawNameKo)
                .content(String.format("[%s] %s (공포일자: %s, 법령일련번호: %s)", lawType, lawNameKo, proclamationDate, mst))
                .sourceUrl("https://www.law.go.kr/법령/" + lawNameKo)
                .sourceType(SOURCE_TYPE)
                .externalId(mst.isEmpty() ? null : mst)
                .build();
    }

    private KnowledgeLaw parseLsStmdNodeToKnowledgeLaw(JsonNode node) {
        if (node == null) return null;
        String lawNameKo = getNodeText(node, "법령명한글", "lawNameKo");
        if (lawNameKo.isEmpty()) lawNameKo = getNodeText(node, "법령명", "lawNm");
        String mst = getNodeText(node, "법령일련번호", "lawMST");
        if (mst.isEmpty()) mst = String.valueOf(parseIntSafe(node.path("법령 일련번호"), 0));
        if (lawNameKo.isEmpty() || mst.isEmpty() || "0".equals(mst)) return null;

        String lawType = getNodeText(node, "법령구분명", "법령구분");
        if (lawType.isEmpty()) lawType = "법률";
        String proclamationDate = getNodeText(node, "공포일자", "proclamationDate");
        String proclamationNo = getNodeText(node, "공포번호", "proclamationNo");
        String enforceDate = getNodeText(node, "시행일자", "enforceDate");
        String lawId = getNodeText(node, "법령ID", "lawId");
        String deptName = getNodeText(node, "소관부처명", "deptName");

        KnowledgeLaw kl = new KnowledgeLaw();
        kl.setMst(mst);
        kl.setLawNameKo(lawNameKo);
        kl.setLawType(lawType);
        kl.setProclamationDate(proclamationDate);
        kl.setProclamationNo(proclamationNo.isEmpty() ? null : proclamationNo);
        kl.setEnforceDate(enforceDate.isEmpty() ? null : enforceDate);
        kl.setLawId(lawId.isEmpty() ? null : lawId);
        kl.setDeptName(deptName.isEmpty() ? null : deptName);
        kl.setContent(String.format("[%s] %s (공포일자: %s)", lawType, lawNameKo, proclamationDate));
        kl.setSourceUrl("https://www.law.go.kr/법령/" + lawNameKo);
        return kl;
    }

    /**
     * 검색 결과 JSON 노드를 Knowledge 객체로 변환
     */
    private Knowledge parseLawNode(JsonNode lawNode) {
        if (lawNode == null) return null;

        try {
            String lawNameKo = getNodeText(lawNode, "법령명한글", "lawNameKo");
            String lawMst = getNodeText(lawNode, "법령일련번호", "lawMST");
            String proclamationDate = getNodeText(lawNode, "공포일자", "proclamationDate");
            String lawType = getNodeText(lawNode, "법령구분명", "법령구분");
            if (lawType.isEmpty()) lawType = lawNode.path("lawType").asText("법률");

            if (lawNameKo.isEmpty()) return null;

            String category = determineLawCategory(lawNameKo);

            return Knowledge.builder()
                    .category(category)
                    .title(lawNameKo)
                    .content(String.format("[%s] %s (공포일자: %s, 법령일련번호: %s)",
                            lawType, lawNameKo, proclamationDate, lawMst))
                    .sourceUrl("https://www.law.go.kr/법령/" + lawNameKo)
                    .sourceType(SOURCE_TYPE)
                    .externalId(lawMst.isEmpty() ? null : lawMst)
                    .build();
        } catch (Exception e) {
            log.warn("법령 노드 파싱 실패: {}", e.getMessage() != null ? e.getMessage() : "알 수 없음");
            return null;
        }
    }

    /**
     * JSON 노드를 KnowledgeLaw 엔티티로 변환 (knowledge_law 테이블 저장용)
     */
    private KnowledgeLaw parseLawNodeToKnowledgeLaw(JsonNode lawNode) {
        if (lawNode == null) return null;

        try {
            String lawNameKo = getNodeText(lawNode, "법령명한글", "lawNameKo");
            String mst = getNodeText(lawNode, "법령일련번호", "lawMST");
            String lawType = getNodeText(lawNode, "법령구분명", "법령구분");
            if (lawType.isEmpty()) lawType = lawNode.path("lawType").asText("법률");
            String proclamationDate = getNodeText(lawNode, "공포일자", "proclamationDate");
            String proclamationNo = getNodeText(lawNode, "공포번호", "proclamationNo");
            String enforceDate = getNodeText(lawNode, "시행일자", "enforceDate");
            String lawId = getNodeText(lawNode, "법령ID", "lawId");
            String deptName = getNodeText(lawNode, "소관부처명", "deptName");

            if (lawNameKo.isEmpty()) return null;
            if (mst.isEmpty()) return null;

            KnowledgeLaw kl = new KnowledgeLaw();
            kl.setMst(mst);
            kl.setLawNameKo(lawNameKo);
            kl.setLawType(lawType);
            kl.setProclamationDate(proclamationDate);
            kl.setProclamationNo(proclamationNo.isEmpty() ? null : proclamationNo);
            kl.setEnforceDate(enforceDate.isEmpty() ? null : enforceDate);
            kl.setLawId(lawId.isEmpty() ? null : lawId);
            kl.setDeptName(deptName.isEmpty() ? null : deptName);
            kl.setContent(String.format("[%s] %s (공포일자: %s)", lawType, lawNameKo, proclamationDate));
            kl.setSourceUrl("https://www.law.go.kr/법령/" + lawNameKo);
            return kl;
        } catch (Exception e) {
            log.warn("법령 KnowledgeLaw 파싱 실패: {}", e.getMessage() != null ? e.getMessage() : "알 수 없음");
            return null;
        }
    }

    private String maskApiKeyInUrl(String url) {
        if (url == null) return "null";
        if (lawApiOc == null || lawApiOc.isBlank()) return url;
        return url.replace(lawApiOc, "***");
    }

    private static <T> T safeGet(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) return null;
        return list.get(index);
    }

    private int parseIntSafe(JsonNode node, int defaultValue) {
        if (node == null || node.isMissingNode()) return defaultValue;
        if (node.isNumber()) return node.asInt();
        String s = node.asText("");
        if (s.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * JSON 노드에서 한글 키 → 영문 키 순서로 텍스트 추출 (null-safe)
     */
    private String getNodeText(JsonNode node, String koreanKey, String englishKey) {
        if (node == null) return "";
        String value = node.path(koreanKey).asText("");
        if (value.isEmpty()) {
            value = node.path(englishKey).asText("");
        }
        return value;
    }

    /** 본문 저장 시 조문 최대 개수 */
    private static final int MAX_ARTICLES_ENRICH = 80;
    /** 본문 저장 시 content 최대 길이 (DB TEXT 제한 고려) */
    private static final int MAX_CONTENT_LENGTH = 30_000;

    /**
     * 개별 법령의 상세 내용(조문) 조회하여 Knowledge의 content를 본문까지 저장
     *
     * API: http://www.law.go.kr/DRF/lawService.do?OC={OC}&target=law&MST={법령일련번호}&type=JSON
     */
    private void enrichLawDetail(Knowledge law) {
        if (law == null) return;

        String mst = law.getExternalId();
        if (mst == null || mst.isBlank()) {
            String content = law.getContent();
            if (content != null && content.contains("법령일련번호: ")) {
                int startIdx = content.lastIndexOf("법령일련번호: ") + "법령일련번호: ".length();
                int endIdx = content.indexOf(")", startIdx);
                if (endIdx < 0) endIdx = content.length();
                mst = content.substring(startIdx, endIdx).trim();
            }
        }
        if (mst == null || mst.isEmpty()) return;

        try {
            String articleBody = fetchLawDetailContent(mst);
            if (articleBody != null && !articleBody.isEmpty()) {
                law.setArticleBody(articleBody);
                log.debug("법령 본문 저장 완료: {} ({}자)", law.getTitle(), articleBody.length());
            }
        } catch (Exception e) {
            log.warn("법령 상세 조회 실패: {} - {}",
                    (law.getTitle() != null ? law.getTitle() : "unknown"),
                    e.getMessage() != null ? e.getMessage() : "알 수 없음");
        }
    }

    /**
     * lawService.do API로 MST에 해당하는 법령 본문(조문) 전체 조회
     *
     * @return 메타정보 + 조문 본문 (최대 MAX_CONTENT_LENGTH자), 실패 시 null
     */
    private String fetchLawDetailContent(String mst) {
        if (mst == null || mst.isBlank()) return null;

        String url = String.format("%s?OC=%s&target=law&MST=%s&type=JSON",
                LAW_SERVICE_URL, lawApiOc, mst);
        String response = callExternalApi(url);
        if (response.isBlank()) return null;

        try {
            JsonNode root = objectMapper.readTree(response);
            if (root == null) return null;

            JsonNode lawInfo = root.path("법령");
            if (lawInfo.isMissingNode()) {
                lawInfo = root.path("Law");
            }
            if (lawInfo.isMissingNode()) return null;

            StringBuilder sb = new StringBuilder(2048);
            JsonNode basicInfo = lawInfo.path("기본정보");
            if (!basicInfo.isMissingNode()) {
                String lawName = getNodeText(basicInfo, "법령명_한글", "lawNm");
                String pubDate = getNodeText(basicInfo, "공포일자", "proclamationDate");
                String enforceDate = getNodeText(basicInfo, "시행일자", "enforceDate");
                String lawType = "법률";
                JsonNode ltNode = basicInfo.path("법종구분");
                if (!ltNode.isMissingNode()) {
                    String fromContent = ltNode.path("content").asText("");
                    if (!fromContent.isEmpty()) lawType = fromContent;
                }
                sb.append(String.format("[%s] %s (공포: %s, 시행: %s, MST: %s)",
                        lawType,
                        lawName.isEmpty() ? "법령" : lawName,
                        pubDate, enforceDate, mst));
            } else {
                sb.append("[법령] (MST: ").append(mst).append(")");
            }

            JsonNode articles = lawInfo.path("조문").path("조문단위");
            if (articles.isMissingNode()) {
                articles = lawInfo.path("article").path("articleUnit");
            }

            if (articles.isArray() && articles.size() > 0) {
                sb.append("\n\n[조문 본문]\n");
                int count = 0;
                for (JsonNode article : articles) {
                    if (count >= MAX_ARTICLES_ENRICH || sb.length() >= MAX_CONTENT_LENGTH) break;
                    if (article == null) continue;

                    String articleNo = getNodeText(article, "조문번호", "articleNo");
                    String articleTitle = getNodeText(article, "조문제목", "articleTitle");
                    String articleContent = getNodeText(article, "조문내용", "articleContent");
                    String articleType = getNodeText(article, "조문여부", "articleType");

                    if (articleContent == null) articleContent = "";
                    if (articleContent.isEmpty()) continue;

                    int remaining = MAX_CONTENT_LENGTH - sb.length();
                    if (remaining <= 0) break;

                    String toAppend = articleContent;
                    if (toAppend.length() > remaining) {
                        toAppend = toAppend.substring(0, remaining) + "...";
                    }

                    if ("조문".equals(articleType) && !articleNo.isEmpty()) {
                        sb.append(String.format("\n제%s조", articleNo));
                        if (!articleTitle.isEmpty()) sb.append("(").append(articleTitle).append(")");
                        sb.append(" ").append(toAppend.trim());
                    } else {
                        sb.append("\n").append(toAppend.trim());
                    }
                    count++;
                }
            }

            String result = sb.toString();
            return result.length() > MAX_CONTENT_LENGTH
                    ? result.substring(0, MAX_CONTENT_LENGTH) + "..."
                    : result;
        } catch (Exception e) {
            log.warn("법령 상세 파싱 실패 MST={}: {}", mst, e.getMessage());
            return null;
        }
    }

    /**
     * RestTemplate API 호출 (null-safe)
     * 
     * restTemplate.getForObject()의 반환 타입이 @Nullable이므로
     * 이 메서드에서 null을 빈 문자열로 변환하여 호출부의 null safety를 보장합니다.
     * 
     * @param url 호출할 API URL
     * @return 응답 문자열 (null이면 빈 문자열)
     */
    @SuppressWarnings("null")
    private String callExternalApi(String url) {
        try {
            String result = restTemplate.getForObject(url, String.class);
            return result != null ? result : "";
        } catch (Exception e) {
            log.error("법령 API HTTP 호출 실패: url={} | error={}", maskApiKeyInUrl(url),
                e.getMessage() != null ? e.getMessage() : "알 수 없음", e);
            throw e;
        }
    }

    /**
     * 법령명에 따라 카테고리 자동 분류
     */
    private String determineLawCategory(String lawName) {
        if (lawName == null || lawName.isEmpty()) return "LAW_GENERAL";

        if (lawName.contains("세법") || lawName.contains("조세") || lawName.contains("관세")) {
            return "LAW_TAX";
        } else if (lawName.contains("근로") || lawName.contains("노동") || lawName.contains("산업안전")) {
            return "LAW_LABOR";
        } else if (lawName.contains("상법") || lawName.contains("회사") || lawName.contains("기업")) {
            return "LAW_CORPORATE";
        } else if (lawName.contains("금융") || lawName.contains("은행") || lawName.contains("자본시장") || lawName.contains("보험")) {
            return "LAW_FINANCE";
        } else if (lawName.contains("민법") || lawName.contains("민사")) {
            return "LAW_CIVIL";
        } else if (lawName.contains("형법") || lawName.contains("형사")) {
            return "LAW_CRIMINAL";
        } else if (lawName.contains("행정") || lawName.contains("공무원")) {
            return "LAW_ADMIN";
        } else if (lawName.contains("환경") || lawName.contains("대기") || lawName.contains("수질")) {
            return "LAW_ENVIRONMENT";
        }
        return "LAW_GENERAL";
    }

    /**
     * Knowledge 목록을 저장/업데이트
     */
    @Transactional
    private List<Knowledge> saveOrUpdateLaws(List<Knowledge> laws) {
        if (laws == null || laws.isEmpty()) {
            return Collections.emptyList();
        }

        List<Knowledge> saved = new ArrayList<>();
        for (Knowledge law : laws) {
            if (law == null) continue;
            try {
                Knowledge savedLaw = knowledgeService.saveOrUpdateKnowledge(law);
                if (savedLaw != null) saved.add(savedLaw);
            } catch (Exception e) {
                log.error("법령 지식 저장 실패: {} - {}",
                        (law.getTitle() != null ? law.getTitle() : "unknown"),
                        e.getMessage() != null ? e.getMessage() : "알 수 없음");
            }
        }
        return saved;
    }
}
