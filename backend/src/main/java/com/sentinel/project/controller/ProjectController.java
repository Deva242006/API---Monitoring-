package com.sentinel.project.controller;

import com.sentinel.common.dto.ApiResponse;
import com.sentinel.common.dto.PagedResponse;
import com.sentinel.common.exception.ResourceNotFoundException;
import com.sentinel.project.dto.*;
import com.sentinel.project.service.ProjectService;
import com.sentinel.security.CustomUserDetails;
import com.sentinel.user.entity.User;
import com.sentinel.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    public ProjectController(ProjectService projectService, UserRepository userRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<ProjectDto>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Project created", projectService.createProject(request, user)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProjectDto>>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success(projectService.getProjects(user, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> getProject(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success(projectService.getProjectById(id, user)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<ProjectDto>> updateProject(
            @PathVariable Long id, @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Project updated", projectService.updateProject(id, request, user)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        projectService.deleteProject(id, user);
        return ResponseEntity.ok(ApiResponse.success("Project deleted"));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<ProjectDto>> addMembers(
            @PathVariable Long id, @Valid @RequestBody ProjectMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Members added", projectService.addMembers(id, request.getUserIds(), user)));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<ProjectDto>> removeMember(
            @PathVariable Long id, @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Member removed", projectService.removeMember(id, userId, user)));
    }

    private User getUser(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
    }
}

