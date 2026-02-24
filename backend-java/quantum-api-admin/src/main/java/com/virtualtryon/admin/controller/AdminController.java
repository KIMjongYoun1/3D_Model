package com.virtualtryon.admin.controller;

import com.virtualtryon.core.entity.DartCorpCode;
import com.virtualtryon.core.entity.Knowledge;
import com.virtualtryon.core.entity.KnowledgeBok;
import com.virtualtryon.core.entity.KnowledgeDart;
import com.virtualtryon.core.entity.KnowledgeFetchHistory;
import com.virtualtryon.core.entity.KnowledgeLaw;
import com.virtualtryon.core.repository.DartCorpCodeRepository;
import com.virtualtryon.core.repository.KnowledgeBokRepository;
import com.virtualtryon.core.repository.KnowledgeDartRepository;
import com.virtualtryon.core.repository.KnowledgeFetchHistoryRepository;
import com.virtualtryon.core.repository.KnowledgeLawRepository;
import com.virtualtryon.admin.service.BokEcosApiService;
import com.virtualtryon.admin.service.DartApiService;
import com.virtualtryon.admin.service.KnowledgeService;
import com.virtualtryon.admin.service.LawApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 관리자용 지식 베이스 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/knowledge")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminController {

    private final KnowledgeService knowledgeService;
    private final KnowledgeFetchHistoryRepository fetchHistoryRepository;
    private final KnowledgeBokRepository knowledgeBokRepository;
    private final KnowledgeDartRepository knowledgeDartRepository;
    private final KnowledgeLawRepository knowledgeLawRepository;
    private final DartCorpCodeRepository dartCorpCodeRepository;
    private final LawApiService lawApiService;
    private final BokEcosApiService bokEcosApiService;
    private final DartApiService dartApiService;

    public AdminController(KnowledgeService knowledgeService,
                           KnowledgeFetchHistoryRepository fetchHistoryRepository,
                           KnowledgeBokRepository knowledgeBokRepository,
                           KnowledgeDartRepository knowledgeDartRepository,
                           KnowledgeLawRepository knowledgeLawRepository,
                           DartCorpCodeRepository dartCorpCodeRepository,
                           LawApiService lawApiService,
                           BokEcosApiService bokEcosApiService,
                           DartApiService dartApiService) {
        this.knowledgeService = knowledgeService;
        this.fetchHistoryRepository = fetchHistoryRepository;
        this.knowledgeBokRepository = knowledgeBokRepository;
        this.knowledgeDartRepository = knowledgeDartRepository;
        this.knowledgeLawRepository = knowledgeLawRepository;
        this.dartCorpCodeRepository = dartCorpCodeRepository;
        this.lawApiService = lawApiService;
        this.bokEcosApiService = bokEcosApiService;
        this.dartApiService = dartApiService;
    }

    /**
     * 금감원 DART 공시 정보 수집
     */
    @PostMapping("/fetch-dart")
    public ResponseEntity<List<Knowledge>> fetchDartData(@RequestParam(required = false) String corpName) {
        List<Knowledge> data = dartApiService.fetchCompanyReports(corpName);
        return ResponseEntity.ok(data);
    }

    /**
     * 한국은행 API를 통한 경제 지표 수집
     */
    @PostMapping("/fetch-bok")
    public ResponseEntity<List<Knowledge>> fetchBokData() {
        List<Knowledge> data = bokEcosApiService.fetchBaseInterestRate();
        return ResponseEntity.ok(data);
    }

    /**
     * 지식 데이터 직접 추가
     */
    @PostMapping
    public ResponseEntity<?> addKnowledge(@RequestBody(required = false) Map<String, String> request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "요청 본문이 없습니다."));
        }
        String category = request.get("category");
        String title = request.get("title");
        String content = request.get("content");
        String sourceUrl = request.get("sourceUrl");
        if (category == null || category.isBlank() || title == null || title.isBlank() || content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "필수 필드(카테고리, 제목, 내용)가 누락되었습니다."));
        }
        try {
            Knowledge knowledge = knowledgeService.createKnowledge(
                    category, title, content, sourceUrl != null ? sourceUrl : ""
            );
            return ResponseEntity.ok(knowledge);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "요청 오류"));
        }
    }

    /**
     * 법령 API 설정 상태 확인 (디버깅용)
     * GET /api/admin/knowledge/law-status
     * - configured: LAW_API_OC가 설정되어 있는지
     */
    @GetMapping("/law-status")
    public ResponseEntity<Map<String, Object>> getLawApiStatus() {
        try {
            Object preview = lawApiService.fetchLawApiPreview("부가가치세법");
            if (preview instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) preview;
                if (m.containsKey("error")) {
                    return ResponseEntity.ok(Map.of(
                        "configured", true,
                        "apiReachable", false,
                        "message", String.valueOf(m.get("error"))
                    ));
                }
                return ResponseEntity.ok(Map.of(
                    "configured", true,
                    "apiReachable", true,
                    "message", "법령 API 정상 연동됨"
                ));
            }
            return ResponseEntity.ok(Map.of("configured", true, "apiReachable", true));
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(Map.of(
                "configured", false,
                "apiReachable", false,
                "message", e.getMessage() != null ? e.getMessage() : "LAW_API_OC 미설정"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "configured", true,
                "apiReachable", false,
                "message", e.getMessage() != null ? e.getMessage() : "API 호출 실패"
            ));
        }
    }

    /**
     * 법령 API 미리보기: 호출만 하고 저장하지 않음. 받아오는 데이터 구조 확인용
     * GET /api/admin/knowledge/law-preview?lawName=부가가치세법
     */
    @GetMapping("/law-preview")
    public ResponseEntity<?> getLawApiPreview(@RequestParam(required = false) String lawName) {
        if (lawName == null || lawName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "lawName 쿼리 파라미터를 입력하세요. 예: ?lawName=부가가치세법"));
        }
        try {
            Object data = lawApiService.fetchLawApiPreview(lawName.trim());
            return ResponseEntity.ok(data);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "알 수 없음"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "알 수 없음"));
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("error", "법령 API 호출 실패: " + (e.getMessage() != null ? e.getMessage() : "알 수 없음")));
        }
    }

    /**
     * 국가법령 API를 통한 지식 수집 및 저장
     * - lawName 있음: 해당 법령명 검색(target=law)
     * - lawName 없음: 전체 법령 수집(target=lsStmd, 페이지네이션)
     * 실패 시 400/503 + { "error": "원인 메시지" } 반환
     */
    @PostMapping("/fetch-law")
    public ResponseEntity<?> fetchAndSaveLaw(@RequestParam(required = false) String lawName) {
        try {
            List<Knowledge> laws;
            if (lawName != null && !lawName.trim().isEmpty()) {
                laws = lawApiService.updateLawManually(lawName.trim());
            } else {
                laws = lawApiService.fetchAllLawsManually();
            }
            return ResponseEntity.ok(laws);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "알 수 없음"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "알 수 없음"));
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of(
                "error", "법령 API 호출 실패: " + (e.getMessage() != null ? e.getMessage() : "알 수 없음")
            ));
        }
    }

    /**
     * 지식 목록 조회 (페이징, 카테고리 필터, 검색)
     * GET /api/admin/knowledge?page=0&size=20&category=LAW_GENERAL&category=LAW_TAX&q=검색어
     */
    @GetMapping
    public ResponseEntity<?> getAllKnowledge(
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = knowledgeService.searchKnowledge(category, q, page, size);
        return ResponseEntity.ok(java.util.Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "number", result.getNumber(),
                "size", result.getSize()
        ));
    }

    /**
     * 지식 카테고리 목록 (필터용)
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getKnowledgeCategories() {
        return ResponseEntity.ok(knowledgeService.getDistinctCategories());
    }

    /**
     * 수집 히스토리 조회 (어디서·무엇을·언제 받아왔는지 상세 로우)
     * GET /api/admin/knowledge/fetch-history
     */
    @GetMapping("/fetch-history")
    public ResponseEntity<List<KnowledgeFetchHistory>> getFetchHistory() {
        try {
            List<KnowledgeFetchHistory> list = fetchHistoryRepository.findAllByOrderByFetchedAtDesc();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    /**
     * 단일 지식 세부 조회 (세부 페이지용)
     * 잘못된 UUID면 400, 없으면 404, 기타 예외는 500 + error 메시지
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getKnowledgeById(@PathVariable String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "잘못된 지식 ID입니다.", "code", "INVALID_ID"));
        }
        try {
            Knowledge knowledge = knowledgeService.getKnowledgeById(uuid);
            return ResponseEntity.ok(knowledge);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "해당 지식을 찾을 수 없습니다.", "code", "NOT_FOUND"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "지식 조회 중 오류가 발생했습니다: " + (e.getMessage() != null ? e.getMessage() : "알 수 없음"),
                "code", "SERVER_ERROR"
            ));
        }
    }

    /**
     * 지식 데이터 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKnowledge(@PathVariable UUID id) {
        if (id == null) return ResponseEntity.badRequest().build();
        knowledgeService.deleteKnowledge(id);
        return ResponseEntity.noContent().build();
    }

    // ---- 소스별 상세 목록 조회 (응답 형식에 맞춘 테이블) ----

    /** 한국은행 경제지표 목록 조회 */
    @GetMapping("/bok")
    public ResponseEntity<List<KnowledgeBok>> listBok(@RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(knowledgeBokRepository.findAllByOrderByTimeDesc(PageRequest.of(0, Math.min(size, 200))));
    }

    /** 한국은행 경제지표 단건 조회 */
    @GetMapping("/bok/{id}")
    public ResponseEntity<?> getBok(@PathVariable UUID id) {
        if (id == null) return ResponseEntity.badRequest().build();
        return knowledgeBokRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** DART 공시 정보 목록 조회 */
    @GetMapping("/dart")
    public ResponseEntity<List<KnowledgeDart>> listDart(@RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(knowledgeDartRepository.findAllByOrderByRceptDtDesc(PageRequest.of(0, Math.min(size, 200))));
    }

    /** DART 공시 정보 단건 조회 */
    @GetMapping("/dart/{id}")
    public ResponseEntity<?> getDart(@PathVariable UUID id) {
        if (id == null) return ResponseEntity.badRequest().build();
        return knowledgeDartRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 법령 정보 목록 조회 */
    @GetMapping("/law")
    public ResponseEntity<List<KnowledgeLaw>> listLaw(@RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(knowledgeLawRepository.findAllByOrderByProclamationDateDesc(PageRequest.of(0, Math.min(size, 200))));
    }

    /** 법령 정보 단건 조회 */
    @GetMapping("/law/{id}")
    public ResponseEntity<?> getLaw(@PathVariable UUID id) {
        if (id == null) return ResponseEntity.badRequest().build();
        return knowledgeLawRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** DART 기업 코드 목록 조회 */
    @GetMapping("/dart/corp-codes")
    public ResponseEntity<List<DartCorpCode>> listDartCorpCodes(@RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(dartCorpCodeRepository.findAll(PageRequest.of(0, Math.min(size, 500))).getContent());
    }
}
