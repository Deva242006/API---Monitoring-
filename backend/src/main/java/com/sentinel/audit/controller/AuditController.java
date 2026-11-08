package com.sentinel.audit.controller;

import com.sentinel.audit.entity.AuditLog;
import com.sentinel.audit.service.AuditService;
import com.sentinel.common.dto.ApiResponse;
import com.sentinel.common.dto.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/audit-logs")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogDto>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLog> logs = auditService.getAuditLogs(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        PagedResponse<AuditLogDto> response = toPagedResponse(logs);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private PagedResponse<AuditLogDto> toPagedResponse(Page<AuditLog> page) {
        List<AuditLogDto> dtos = page.getContent().stream().map(log -> new AuditLogDto(
                log.getId(),
                log.getUser() != null ? log.getUser().getFullName() : "System",
                log.getUser() != null ? log.getUser().getEmail() : null,
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails(),
                log.getIpAddress(),
                log.getCreatedAt().toString()
        )).collect(Collectors.toList());
        return new PagedResponse<>(dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    public record AuditLogDto(Long id, String userName, String userEmail, String action,
                                String entityType, Long entityId, String details,
                                String ipAddress, String createdAt) {}
}
