package com.sentinel.incident.controller;

import com.sentinel.common.dto.ApiResponse;
import com.sentinel.common.dto.PagedResponse;
import com.sentinel.common.exception.ResourceNotFoundException;
import com.sentinel.incident.dto.*;
import com.sentinel.incident.service.IncidentService;
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
@RequestMapping("/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final UserRepository userRepository;

    public IncidentController(IncidentService incidentService, UserRepository userRepository) {
        this.incidentService = incidentService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<IncidentDto>>> getIncidents(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status, @RequestParam(required = false) String severity) {
        return ResponseEntity.ok(ApiResponse.success(incidentService.getIncidents(page, size, status, severity)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentDto>> getIncident(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(incidentService.getIncidentById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<IncidentDto>> createIncident(
            @Valid @RequestBody CreateIncidentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Incident created", incidentService.createManualIncident(request, user)));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<IncidentDto>> assignIncident(
            @PathVariable Long id, @Valid @RequestBody AssignIncidentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Incident assigned", incidentService.assignIncident(id, request.getUserId(), user)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<IncidentDto>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateIncidentStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Status updated", incidentService.updateIncidentStatus(id, request.getStatus(), user)));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<IncidentCommentDto>> addComment(
            @PathVariable Long id, @Valid @RequestBody IncidentCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added", incidentService.addComment(id, request.getContent(), user)));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<PagedResponse<IncidentCommentDto>>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(incidentService.getComments(id, page, size)));
    }

    private User getUser(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
    }
}

