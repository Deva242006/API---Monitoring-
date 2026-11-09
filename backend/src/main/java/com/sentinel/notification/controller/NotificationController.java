package com.sentinel.notification.controller;

import com.sentinel.common.dto.ApiResponse;
import com.sentinel.common.dto.PagedResponse;
import com.sentinel.notification.entity.Notification;
import com.sentinel.notification.service.NotificationService;
import com.sentinel.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<Map<String, Object>>>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Notification> notifications = notificationService.getUserNotifications(userDetails.getId(), page, size);
        PagedResponse<Map<String, Object>> response = new PagedResponse<>(
                notifications.getContent().stream().map(n -> Map.<String, Object>of(
                        "id", n.getId(), "title", n.getTitle(), "message", n.getMessage(),
                        "type", n.getType().name(), "isRead", n.getIsRead(),
                        "referenceId", n.getReferenceId() != null ? n.getReferenceId() : 0,
                        "referenceType", n.getReferenceType() != null ? n.getReferenceType() : "",
                        "createdAt", n.getCreatedAt().toString()
                )).toList(),
                notifications.getNumber(), notifications.getSize(), notifications.getTotalElements(),
                notifications.getTotalPages(), notifications.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(userDetails.getId())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }
}

