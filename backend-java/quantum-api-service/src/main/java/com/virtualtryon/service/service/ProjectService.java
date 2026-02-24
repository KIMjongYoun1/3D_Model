package com.virtualtryon.service.service;

import com.virtualtryon.core.entity.Project;
import com.virtualtryon.core.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /** 프로젝트 생성 */
    @Transactional
    @SuppressWarnings("null")
    public Project createProject(UUID userId, String name, String description, String mainCategory, String subCategory) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("프로젝트 이름은 필수이며 비어있을 수 없습니다.");
        }
        Project project = Project.builder()
                .userId(userId)
                .name(name.trim())
                .description(description != null ? description.trim() : "")
                .mainCategory(mainCategory != null ? mainCategory : "GENERAL")
                .subCategory(subCategory != null ? subCategory : "DOC")
                .build();
        return projectRepository.save(project);
    }

    /** 사용자별 프로젝트 목록 조회 (최신순) */
    @Transactional(readOnly = true)
    public List<Project> getUserProjects(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        return projectRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /** 프로젝트 단건 조회 */
    @Transactional(readOnly = true)
    public Project getProject(UUID projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("프로젝트 ID는 필수입니다.");
        }
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 프로젝트를 찾을 수 없습니다: " + projectId));
    }

    /** 프로젝트 삭제 */
    @Transactional
    public void deleteProject(UUID projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("삭제할 프로젝트 ID는 필수입니다.");
        }
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("삭제하려는 프로젝트가 존재하지 않습니다: " + projectId);
        }
        projectRepository.deleteById(projectId);
    }
}
