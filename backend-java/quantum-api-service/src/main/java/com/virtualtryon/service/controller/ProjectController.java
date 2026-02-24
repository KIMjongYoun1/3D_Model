package com.virtualtryon.service.controller;

import com.virtualtryon.core.entity.Project;
import com.virtualtryon.service.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 프로젝트 생성
     * POST /api/projects
     */
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Map<String, String> request) {
        // 실제 환경에서는 JWT에서 userId를 추출해야 함 (현재는 테스트용 하드코딩)
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        
        Project project = projectService.createProject(
                userId,
                request.get("name"),
                request.get("description"),
                request.get("mainCategory"),
                request.get("subCategory")
        );
        
        return ResponseEntity.ok(project);
    }

    /**
     * 사용자 프로젝트 목록 조회
     * GET /api/projects
     */
    @GetMapping
    public ResponseEntity<List<Project>> getMyProjects() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        return ResponseEntity.ok(projectService.getUserProjects(userId));
    }

    /**
     * 특정 프로젝트 상세 조회
     * GET /api/projects/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    /**
     * 프로젝트 삭제
     * DELETE /api/projects/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
