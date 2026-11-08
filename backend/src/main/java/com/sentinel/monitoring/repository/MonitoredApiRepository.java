package com.sentinel.monitoring.repository;

import com.sentinel.common.enums.ApiStatus;
import com.sentinel.monitoring.entity.MonitoredApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MonitoredApiRepository extends JpaRepository<MonitoredApi, Long> {
    Page<MonitoredApi> findByProjectId(Long projectId, Pageable pageable);
    List<MonitoredApi> findByActiveTrue();
    Page<MonitoredApi> findByActive(Boolean active, Pageable pageable);
    Page<MonitoredApi> findByCurrentStatus(ApiStatus status, Pageable pageable);

    @Query("SELECT COUNT(a) FROM MonitoredApi a WHERE a.active = true")
    long countActive();

    @Query("SELECT COUNT(a) FROM MonitoredApi a WHERE a.currentStatus = :status AND a.active = true")
    long countByCurrentStatusAndActive(@Param("status") ApiStatus status);

    @Query("SELECT a FROM MonitoredApi a JOIN a.project p LEFT JOIN p.members m WHERE (p.owner.id = :userId OR m.id = :userId) AND a.active = true")
    Page<MonitoredApi> findAccessibleByUserId(@Param("userId") Long userId, Pageable pageable);

    Page<MonitoredApi> findByNameContainingIgnoreCaseOrEndpointUrlContainingIgnoreCase(String name, String url, Pageable pageable);
}
