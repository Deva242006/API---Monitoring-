package com.sentinel.project.service;

import com.sentinel.common.dto.PagedResponse;
import com.sentinel.common.enums.UserRole;
import com.sentinel.common.exception.ForbiddenException;
import com.sentinel.common.exception.ResourceNotFoundException;
import com.sentinel.project.dto.*;
import com.sentinel.project.entity.Project;
import com.sentinel.project.repository.ProjectRepository;
import com.sentinel.user.dto.UserDto;
import com.sentinel.user.entity.User;
import com.sentinel.user.repository.UserRepository;
import com.sentinel.user.service.UserService;
import com.sentinel.audit.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, AuditService auditService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ProjectDto createProject(CreateProjectRequest request, User currentUser) {
        Project project = new Project(request.getName(), request.getDescription(), currentUser);
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            List<User> members = userRepository.findByIdIn(request.getMemberIds());
            members.forEach(project::addMember);
        }
        project.addMember(currentUser);
        project = projectRepository.save(project);
        log.info("Project created: {} by user: {}", project.getName(), currentUser.getEmail());
        auditService.log(currentUser, "CREATE", "PROJECT", project.getId(), "Project created: " + project.getName(), null);
        return toDto(project);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProjectDto> getProjects(User currentUser, int page, int size) {
        Page<Project> projects;
        if (currentUser.getRole() == UserRole.ADMIN) {
            projects = projectRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            projects = projectRepository.findAccessibleByUserId(currentUser.getId(),
                    PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }
        return toPagedResponse(projects);
    }

    @Transactional(readOnly = true)
    public ProjectDto getProjectById(Long id, User currentUser) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        checkAccess(project, currentUser);
        return toDto(project);
    }

    @Transactional
    public ProjectDto updateProject(Long id, UpdateProjectRequest request, User currentUser) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        checkOwnerOrAdmin(project, currentUser);
        if (request.getName() != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getActive() != null) project.setActive(request.getActive());
        project = projectRepository.save(project);
        auditService.log(currentUser, "UPDATE", "PROJECT", project.getId(), "Project updated", null);
        return toDto(project);
    }

    @Transactional
    public void deleteProject(Long id, User currentUser) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        checkOwnerOrAdmin(project, currentUser);
        project.setActive(false);
        projectRepository.save(project);
        auditService.log(currentUser, "DELETE", "PROJECT", project.getId(), "Project deactivated", null);
    }

    @Transactional
    public ProjectDto addMembers(Long projectId, List<Long> userIds, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        checkOwnerOrAdmin(project, currentUser);
        List<User> users = userRepository.findByIdIn(userIds);
        users.forEach(project::addMember);
        project = projectRepository.save(project);
        auditService.log(currentUser, "ADD_MEMBERS", "PROJECT", project.getId(), "Members added", null);
        return toDto(project);
    }

    @Transactional
    public ProjectDto removeMember(Long projectId, Long userId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        checkOwnerOrAdmin(project, currentUser);
        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        project.removeMember(userToRemove);
        project = projectRepository.save(project);
        return toDto(project);
    }

    private void checkAccess(Project project, User user) {
        if (user.getRole() == UserRole.ADMIN) return;
        if (!projectRepository.isUserMemberOrOwner(project.getId(), user.getId())) {
            throw new ForbiddenException("You don't have access to this project");
        }
    }

    private void checkOwnerOrAdmin(Project project, User user) {
        if (user.getRole() == UserRole.ADMIN) return;
        if (!project.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenException("Only project owner or admin can perform this action");
        }
    }

    private ProjectDto toDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setOwner(UserService.toDto(project.getOwner()));
        dto.setActive(project.getActive());
        dto.setMembers(project.getMembers().stream().map(UserService::toDto).collect(Collectors.toList()));
        dto.setCreatedAt(project.getCreatedAt() != null ? project.getCreatedAt().toString() : null);
        dto.setUpdatedAt(project.getUpdatedAt() != null ? project.getUpdatedAt().toString() : null);
        return dto;
    }

    private PagedResponse<ProjectDto> toPagedResponse(Page<Project> page) {
        return new PagedResponse<>(page.getContent().stream().map(this::toDto).collect(Collectors.toList()),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}
