package com.sentinel.project.repository;

import com.sentinel.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByActive(Boolean active, Pageable pageable);
    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.id = :userId AND p.active = true")
    Page<Project> findByMemberId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN p.members m WHERE (p.owner.id = :userId OR m.id = :userId) AND p.active = true")
    Page<Project> findAccessibleByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Project p LEFT JOIN p.members m WHERE p.id = :projectId AND (p.owner.id = :userId OR m.id = :userId)")
    boolean isUserMemberOrOwner(@Param("projectId") Long projectId, @Param("userId") Long userId);

    Page<Project> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
