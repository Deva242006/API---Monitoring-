package com.sentinel.monitoring.controller;

import com.sentinel.common.dto.ApiResponse;
import com.sentinel.common.dto.PagedResponse;
import com.sentinel.common.exception.ResourceNotFoundException;
import com.sentinel.monitoring.dto.*;
import com.sentinel.monitoring.service.MonitoredApiService;
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
@RequestMapping("/apis")
public class MonitoredApiController {

    private final MonitoredApiService apiService;
    private final UserRepository userRepository;

    public MonitoredApiController(MonitoredApiService apiService, UserRepository userRepository) {
        this.apiService = apiService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<MonitoredApiDto>> createApi(
            @Valid @RequestBody CreateApiRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("API registered", apiService.createApi(request, user)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<MonitoredApiDto>>> getApis(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success(apiService.getApis(user, page, size, search)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MonitoredApiDto>> getApi(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(apiService.getApiById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<MonitoredApiDto>> updateApi(
            @PathVariable Long id, @Valid @RequestBody UpdateApiRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success("API updated", apiService.updateApi(id, request, user)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<Void>> deleteApi(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        apiService.deleteApi(id, user);
        return ResponseEntity.ok(ApiResponse.success("API deleted"));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<MonitoredApiDto>> toggleApi(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success("API status toggled", apiService.toggleApiStatus(id, user)));
    }

    private User getUser(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
    }
}

