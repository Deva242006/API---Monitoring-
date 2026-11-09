package com.sentinel.monitoring.service;

import com.sentinel.common.dto.PagedResponse;
import com.sentinel.common.enums.UserRole;
import com.sentinel.common.exception.ForbiddenException;
import com.sentinel.common.exception.ResourceNotFoundException;
import com.sentinel.monitoring.dto.*;
import com.sentinel.monitoring.entity.MonitoredApi;
import com.sentinel.monitoring.repository.MonitoredApiRepository;
import com.sentinel.project.entity.Project;
import com.sentinel.project.repository.ProjectRepository;
import com.sentinel.user.entity.User;
import com.sentinel.audit.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonitoredApiService {

    private static final Logger log = LoggerFactory.getLogger(MonitoredApiService.class);
    private final MonitoredApiRepository apiRepository;
    private final ProjectRepository projectRepository;
    private final UrlValidator urlValidator;
    private final AuditService auditService;

    public MonitoredApiService(MonitoredApiRepository apiRepository, ProjectRepository projectRepository,
                                UrlValidator urlValidator, AuditService auditService) {
        this.apiRepository = apiRepository;
        this.projectRepository = projectRepository;
        this.urlValidator = urlValidator;
        this.auditService = auditService;
    }

    @Transactional
    public MonitoredApiDto createApi(CreateApiRequest request, User currentUser) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));
        checkProjectAccess(project, currentUser);
        urlValidator.validate(request.getEndpointUrl());

        MonitoredApi api = new MonitoredApi();
        api.setName(request.getName());
        api.setEndpointUrl(request.getEndpointUrl());
        api.setHttpMethod(request.getHttpMethod());
        api.setExpectedStatusCode(request.getExpectedStatusCode());
        api.setTimeoutMs(request.getTimeoutMs());
        api.setMonitoringIntervalSeconds(request.getMonitoringIntervalSeconds());
        api.setProject(project);
        api.setDescription(request.getDescription());
        api.setHeadersJson(request.getHeadersJson());
        api.setCreatedBy(currentUser);

        api = apiRepository.save(api);
        log.info("API registered: {} for project: {}", api.getName(), project.getName());
        auditService.log(currentUser, "CREATE", "API", api.getId(), "API registered: " + api.getName(), null);
        return toDto(api);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MonitoredApiDto> getApis(User currentUser, int page, int size, String search) {
        Page<MonitoredApi> apis;
        if (search != null && !search.isBlank()) {
            apis = apiRepository.findByNameContainingIgnoreCaseOrEndpointUrlContainingIgnoreCase(
                    search, search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else if (currentUser.getRole() == UserRole.ADMIN) {
            apis = apiRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            apis = apiRepository.findAccessibleByUserId(currentUser.getId(),
                    PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }
        return toPagedResponse(apis);
    }

    @Transactional(readOnly = true)
    public MonitoredApiDto getApiById(Long id) {
        MonitoredApi api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API", "id", id));
        return toDto(api);
    }

    @Transactional
    public MonitoredApiDto updateApi(Long id, UpdateApiRequest request, User currentUser) {
        MonitoredApi api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API", "id", id));
        checkProjectAccess(api.getProject(), currentUser);
        if (request.getEndpointUrl() != null) { urlValidator.validate(request.getEndpointUrl()); api.setEndpointUrl(request.getEndpointUrl()); }
        if (request.getName() != null) api.setName(request.getName());
        if (request.getHttpMethod() != null) api.setHttpMethod(request.getHttpMethod());
        if (request.getExpectedStatusCode() != null) api.setExpectedStatusCode(request.getExpectedStatusCode());
        if (request.getTimeoutMs() != null) api.setTimeoutMs(request.getTimeoutMs());
        if (request.getMonitoringIntervalSeconds() != null) api.setMonitoringIntervalSeconds(request.getMonitoringIntervalSeconds());
        if (request.getDescription() != null) api.setDescription(request.getDescription());
        if (request.getHeadersJson() != null) api.setHeadersJson(request.getHeadersJson());
        api = apiRepository.save(api);
        auditService.log(currentUser, "UPDATE", "API", api.getId(), "API updated: " + api.getName(), null);
        return toDto(api);
    }

    @Transactional
    public void deleteApi(Long id, User currentUser) {
        MonitoredApi api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API", "id", id));
        checkProjectAccess(api.getProject(), currentUser);
        apiRepository.delete(api);
        auditService.log(currentUser, "DELETE", "API", id, "API deleted: " + api.getName(), null);
    }

    @Transactional
    public MonitoredApiDto toggleApiStatus(Long id, User currentUser) {
        MonitoredApi api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API", "id", id));
        checkProjectAccess(api.getProject(), currentUser);
        api.setActive(!api.getActive());
        api = apiRepository.save(api);
        return toDto(api);
    }

    private void checkProjectAccess(Project project, User user) {
        if (user.getRole() == UserRole.ADMIN) return;
        if (!projectRepository.isUserMemberOrOwner(project.getId(), user.getId())) {
            throw new ForbiddenException("You don't have access to this project");
        }
    }

    public static MonitoredApiDto toDto(MonitoredApi api) {
        MonitoredApiDto dto = new MonitoredApiDto();
        dto.setId(api.getId());
        dto.setName(api.getName());
        dto.setEndpointUrl(api.getEndpointUrl());
        dto.setHttpMethod(api.getHttpMethod());
        dto.setExpectedStatusCode(api.getExpectedStatusCode());
        dto.setTimeoutMs(api.getTimeoutMs());
        dto.setMonitoringIntervalSeconds(api.getMonitoringIntervalSeconds());
        dto.setProjectId(api.getProject().getId());
        dto.setProjectName(api.getProject().getName());
        dto.setActive(api.getActive());
        dto.setDescription(api.getDescription());
        dto.setCurrentStatus(api.getCurrentStatus());
        dto.setConsecutiveFailures(api.getConsecutiveFailures());
        dto.setLastCheckedAt(api.getLastCheckedAt() != null ? api.getLastCheckedAt().toString() : null);
        dto.setLastResponseTimeMs(api.getLastResponseTimeMs());
        dto.setCreatedAt(api.getCreatedAt() != null ? api.getCreatedAt().toString() : null);
        return dto;
    }

    private PagedResponse<MonitoredApiDto> toPagedResponse(Page<MonitoredApi> page) {
        return new PagedResponse<>(page.getContent().stream().map(MonitoredApiService::toDto).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}

