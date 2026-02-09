package com.virtualtryon.admin.controller;

import com.virtualtryon.core.entity.Knowledge;
import com.virtualtryon.admin.service.BokEcosApiService;
import com.virtualtryon.admin.service.DartApiService;
import com.virtualtryon.admin.service.KnowledgeService;
import com.virtualtryon.admin.service.LawApiService;
import org.springframework.http.ResponseEntity;
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
    private final LawApiService lawApiService;
    private final BokEcosApiService bokEcosApiService;
    private final DartApiService dartApiService;

    public AdminController(KnowledgeService knowledgeService, 
                           LawApiService lawApiService, 
                           BokEcosApiService bokEcosApiService,
                           DartApiService dartApiService) {
        this.knowledgeService = knowledgeService;
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
    public ResponseEntity<Knowledge> addKnowledge(@RequestBody Map<String, String> request) {
        Knowledge knowledge = knowledgeService.createKnowledge(
                request.get("category"),
                request.get("title"),
                request.get("content"),
                request.get("sourceUrl")
        );
        return ResponseEntity.ok(knowledge);
    }

    /**
     * 국가법령 API를 통한 지식 수집 및 저장
     */
    @PostMapping("/fetch-law")
    public ResponseEntity<List<Knowledge>> fetchAndSaveLaw(@RequestParam(required = false) String lawName) {
        if (lawName == null || lawName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<Knowledge> laws = lawApiService.updateLawManually(lawName);
        return ResponseEntity.ok(laws);
    }

    /**
     * 전체 지식 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<Knowledge>> getAllKnowledge() {
        return ResponseEntity.ok(knowledgeService.getAllActiveKnowledge());
    }

    /**
     * 지식 데이터 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKnowledge(@PathVariable UUID id) {
        knowledgeService.deleteKnowledge(id);
        return ResponseEntity.noContent().build();
    }
}
