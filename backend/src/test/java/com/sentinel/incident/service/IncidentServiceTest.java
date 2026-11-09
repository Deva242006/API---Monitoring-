package com.sentinel.incident.service;

import com.sentinel.audit.service.AuditService;
import com.sentinel.common.enums.ApiStatus;
import com.sentinel.common.enums.IncidentSeverity;
import com.sentinel.common.enums.IncidentStatus;
import com.sentinel.incident.dto.CreateIncidentRequest;
import com.sentinel.incident.dto.IncidentDto;
import com.sentinel.incident.entity.Incident;
import com.sentinel.incident.repository.IncidentCommentRepository;
import com.sentinel.incident.repository.IncidentRepository;
import com.sentinel.monitoring.entity.MonitoredApi;
import com.sentinel.monitoring.repository.MonitoredApiRepository;
import com.sentinel.notification.service.NotificationService;
import com.sentinel.project.entity.Project;
import com.sentinel.user.entity.User;
import com.sentinel.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentCommentRepository commentRepository;

    @Mock
    private MonitoredApiRepository apiRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private IncidentService incidentService;

    private MonitoredApi sampleApi;
    private Project sampleProject;
    private User sampleOwner;

    @BeforeEach
    void setUp() {
        sampleOwner = new User("admin@sentinel.io", "hash", "Admin User", com.sentinel.common.enums.UserRole.ADMIN);
        sampleOwner.setId(1L);

        sampleProject = new Project("Core Services", "Main project", sampleOwner);
        sampleProject.setId(5L);

        sampleApi = new MonitoredApi();
        sampleApi.setId(100L);
        sampleApi.setName("Auth Service API");
        sampleApi.setEndpointUrl("https://auth.company.internal/health");
        sampleApi.setProject(sampleProject);
        sampleApi.setConsecutiveFailures(3);
    }

    @Test
    @DisplayName("Should auto-create incident on consecutive failures if no open incident exists")
    void testCreateAutoIncident() {
        when(incidentRepository.findActiveByApiId(100L)).thenReturn(Collections.emptyList());
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> {
            Incident inc = inv.getArgument(0);
            inc.setId(500L);
            return inc;
        });

        incidentService.createAutoIncident(sampleApi);

        verify(incidentRepository).save(any(Incident.class));
        verify(notificationService).notifyIncidentCreated(any(Incident.class));
    }

    @Test
    @DisplayName("Should prevent duplicate incidents for the same ongoing outage")
    void testPreventDuplicateIncidents() {
        Incident existing = new Incident();
        existing.setId(400L);
        existing.setStatus(IncidentStatus.OPEN);

        when(incidentRepository.findActiveByApiId(100L)).thenReturn(List.of(existing));

        incidentService.createAutoIncident(sampleApi);

        verify(incidentRepository, never()).save(any(Incident.class));
        verify(notificationService, never()).notifyIncidentCreated(any());
    }

    @Test
    @DisplayName("Should update status to RESOLVED and notify")
    void testResolveIncident() {
        Incident incident = new Incident();
        incident.setId(500L);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setApi(sampleApi);

        when(incidentRepository.findById(500L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenReturn(incident);

        IncidentDto result = incidentService.updateIncidentStatus(500L, IncidentStatus.RESOLVED, sampleOwner);

        assertEquals(IncidentStatus.RESOLVED, result.getStatus());
        assertNotNull(incident.getResolvedAt());
        verify(notificationService).notifyIncidentResolved(incident);
    }
}

