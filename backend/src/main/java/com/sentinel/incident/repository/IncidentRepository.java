package com.sentinel.incident.repository;

import com.sentinel.common.enums.IncidentSeverity;
import com.sentinel.common.enums.IncidentStatus;
import com.sentinel.incident.entity.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);
    Page<Incident> findBySeverity(IncidentSeverity severity, Pageable pageable);
    Page<Incident> findByApiId(Long apiId, Pageable pageable);
    Page<Incident> findByAssignedToId(Long userId, Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE i.api.id = :apiId AND i.status != 'RESOLVED' ORDER BY i.createdAt DESC")
    List<Incident> findActiveByApiId(@Param("apiId") Long apiId);

    Optional<Incident> findFirstByApiIdAndStatusNotOrderByCreatedAtDesc(Long apiId, IncidentStatus status);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.status != 'RESOLVED'")
    long countActive();

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.status = 'RESOLVED'")
    long countResolved();

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.createdAt BETWEEN :start AND :end")
    long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.status = 'RESOLVED' AND i.createdAt BETWEEN :start AND :end")
    long countResolvedByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT i FROM Incident i WHERE i.createdAt BETWEEN :start AND :end ORDER BY i.createdAt DESC")
    List<Incident> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    Page<Incident> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE i.api.project.id = :projectId ORDER BY i.createdAt DESC")
    Page<Incident> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);
}
