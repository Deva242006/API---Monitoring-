package com.sentinel.healthcheck.repository;

import com.sentinel.common.enums.ApiStatus;
import com.sentinel.healthcheck.entity.HealthCheck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long> {
    Page<HealthCheck> findByApiIdOrderByCheckedAtDesc(Long apiId, Pageable pageable);

    List<HealthCheck> findTop10ByApiIdOrderByCheckedAtDesc(Long apiId);

    @Query("SELECT hc FROM HealthCheck hc WHERE hc.api.id = :apiId AND hc.checkedAt BETWEEN :start AND :end ORDER BY hc.checkedAt DESC")
    List<HealthCheck> findByApiIdAndDateRange(@Param("apiId") Long apiId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT AVG(hc.responseTimeMs) FROM HealthCheck hc WHERE hc.api.id = :apiId AND hc.checkedAt BETWEEN :start AND :end AND hc.responseTimeMs IS NOT NULL")
    Double findAvgResponseTime(@Param("apiId") Long apiId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(hc) FROM HealthCheck hc WHERE hc.api.id = :apiId AND hc.checkedAt BETWEEN :start AND :end")
    long countByApiIdAndDateRange(@Param("apiId") Long apiId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(hc) FROM HealthCheck hc WHERE hc.api.id = :apiId AND hc.status = :status AND hc.checkedAt BETWEEN :start AND :end")
    long countByApiIdAndStatusAndDateRange(@Param("apiId") Long apiId, @Param("status") ApiStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(hc) FROM HealthCheck hc WHERE hc.checkedAt BETWEEN :start AND :end")
    long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(hc) FROM HealthCheck hc WHERE hc.status = 'HEALTHY' AND hc.checkedAt BETWEEN :start AND :end")
    long countHealthyByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT AVG(hc.responseTimeMs) FROM HealthCheck hc WHERE hc.checkedAt BETWEEN :start AND :end AND hc.responseTimeMs IS NOT NULL")
    Double findOverallAvgResponseTime(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT response_time_ms FROM health_checks WHERE checked_at BETWEEN :start AND :end AND response_time_ms IS NOT NULL ORDER BY response_time_ms", nativeQuery = true)
    List<Long> findAllResponseTimesInRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(hc) FROM HealthCheck hc WHERE hc.status != 'HEALTHY' AND hc.checkedAt BETWEEN :start AND :end")
    long countFailedByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    Page<HealthCheck> findAllByOrderByCheckedAtDesc(Pageable pageable);
}
