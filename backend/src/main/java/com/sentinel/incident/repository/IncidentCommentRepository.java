package com.sentinel.incident.repository;

import com.sentinel.incident.entity.IncidentComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentCommentRepository extends JpaRepository<IncidentComment, Long> {
    Page<IncidentComment> findByIncidentIdOrderByCreatedAtDesc(Long incidentId, Pageable pageable);
}
