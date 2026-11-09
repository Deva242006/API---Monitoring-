package com.sentinel.notification.service;

import com.sentinel.common.enums.NotificationType;
import com.sentinel.incident.entity.Incident;
import com.sentinel.monitoring.entity.MonitoredApi;
import com.sentinel.notification.entity.Notification;
import com.sentinel.notification.repository.NotificationRepository;
import com.sentinel.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                                JavaMailSender mailSender,
                                SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
        this.messagingTemplate = messagingTemplate;
    }

    @Async
    public void notifyIncidentCreated(Incident incident) {
        String title = "New Incident: " + incident.getTitle();
        String message = "A new incident has been created for API: " + incident.getApi().getName()
                + "\nSeverity: " + incident.getSeverity().name()
                + "\nDescription: " + (incident.getDescription() != null ? incident.getDescription() : "N/A");

        // Notify project members
        Set<User> members = incident.getApi().getProject().getMembers();
        User owner = incident.getApi().getProject().getOwner();

        createAndSendNotification(owner, title, message, NotificationType.INCIDENT_CREATED, incident.getId(), "INCIDENT");
        for (User member : members) {
            if (!member.getId().equals(owner.getId())) {
                createAndSendNotification(member, title, message, NotificationType.INCIDENT_CREATED, incident.getId(), "INCIDENT");
            }
        }
    }

    @Async
    public void notifyIncidentAssigned(Incident incident, User assignee) {
        String title = "Incident Assigned: " + incident.getTitle();
        String message = "You have been assigned to incident: " + incident.getTitle()
                + "\nAPI: " + incident.getApi().getName()
                + "\nSeverity: " + incident.getSeverity().name();

        createAndSendNotification(assignee, title, message, NotificationType.INCIDENT_ASSIGNED, incident.getId(), "INCIDENT");
    }

    @Async
    public void notifyIncidentResolved(Incident incident) {
        String title = "Incident Resolved: " + incident.getTitle();
        String message = "Incident has been resolved: " + incident.getTitle()
                + "\nAPI: " + incident.getApi().getName();

        if (incident.getAssignedTo() != null) {
            createAndSendNotification(incident.getAssignedTo(), title, message,
                    NotificationType.INCIDENT_RESOLVED, incident.getId(), "INCIDENT");
        }

        User owner = incident.getApi().getProject().getOwner();
        createAndSendNotification(owner, title, message, NotificationType.INCIDENT_RESOLVED, incident.getId(), "INCIDENT");
    }

    @Async
    public void notifyApiRecovery(MonitoredApi api) {
        String title = "API Recovered: " + api.getName();
        String message = "API " + api.getName() + " has recovered and is now healthy.\nEndpoint: " + api.getEndpointUrl();

        User owner = api.getProject().getOwner();
        createAndSendNotification(owner, title, message, NotificationType.API_RECOVERED, api.getId(), "API");
    }

    private void createAndSendNotification(User user, String title, String message,
                                            NotificationType type, Long refId, String refType) {
        try {
            // Create in-app notification
            Notification notification = new Notification(user, title, message, type);
            notification.setReferenceId(refId);
            notification.setReferenceType(refType);
            notificationRepository.save(notification);

            // Push via WebSocket
            try {
                messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/notifications",
                        Map.of("id", notification.getId(), "title", title, "message", message,
                                "type", type.name(), "createdAt", notification.getCreatedAt().toString()));
            } catch (Exception e) {
                log.debug("WebSocket notification failed for user {}: {}", user.getEmail(), e.getMessage());
            }

            // Send email
            sendEmail(user.getEmail(), title, message);
        } catch (Exception e) {
            log.error("Failed to send notification to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject("[Sentinel] " + subject);
            mailMessage.setText(body);
            mailMessage.setFrom("noreply@sentinel.io");
            mailSender.send(mailMessage);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Long userId, int page, int size) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}

