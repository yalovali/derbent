package tech.derbent.bab.dashboard.dashboardproject_bab.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.dashboard.dashboardproject_bab.domain.CDashboardProject_Bab;

/**
 * IDashboardProject_BabRepository - Repository interface for BAB dashboard projects.
 * Layer: Service (MVC)
 * Following Derbent pattern: Concrete repository with HQL queries.
 */
@Profile("bab")
public interface IDashboardProject_BabRepository extends IEntityOfProjectRepository<CDashboardProject_Bab> {
    
    @Override
    @Query("""
        SELECT DISTINCT d FROM CDashboardProject_Bab d
        LEFT JOIN FETCH d.project
        LEFT JOIN FETCH d.assignedTo
        LEFT JOIN FETCH d.status
        LEFT JOIN FETCH d.attachments
        LEFT JOIN FETCH d.comments
        LEFT JOIN FETCH d.links
        WHERE d.project = :project
        ORDER BY d.name ASC
        """)
    List<CDashboardProject_Bab> listByProject(@Param("project") CProject<?> project);
    
    @Override
    @Query("""
        SELECT DISTINCT d FROM CDashboardProject_Bab d
        LEFT JOIN FETCH d.project
        LEFT JOIN FETCH d.assignedTo
        LEFT JOIN FETCH d.status
        LEFT JOIN FETCH d.attachments
        LEFT JOIN FETCH d.comments
        LEFT JOIN FETCH d.links
        WHERE d.project = :project
        ORDER BY d.name ASC
        """)
    List<CDashboardProject_Bab> listByProjectForPageView(@Param("project") CProject<?> project);
    
    @Override
    @Query("""
        SELECT DISTINCT d FROM CDashboardProject_Bab d
        LEFT JOIN FETCH d.project
        LEFT JOIN FETCH d.assignedTo
        LEFT JOIN FETCH d.status
        LEFT JOIN FETCH d.attachments
        LEFT JOIN FETCH d.comments
        LEFT JOIN FETCH d.links
        WHERE d.name = :name AND d.project = :project
        """)
    Optional<CDashboardProject_Bab> findByNameAndProject(@Param("name") String name, @Param("project") CProject<?> project);
    
    @Query("SELECT d FROM CDashboardProject_Bab d WHERE d.isActive = true AND d.project = :project ORDER BY d.name ASC")
    List<CDashboardProject_Bab> findByIsActiveTrueAndProject(@Param("project") CProject<?> project);
    
    @Query("SELECT d FROM CDashboardProject_Bab d WHERE d.isActive = true ORDER BY d.name ASC")
    List<CDashboardProject_Bab> findByIsActiveTrue();
    
    @Query("SELECT d FROM CDashboardProject_Bab d WHERE d.dashboardType = :type AND d.project = :project ORDER BY d.name ASC")
    List<CDashboardProject_Bab> findByDashboardTypeAndProject(@Param("type") String type, @Param("project") CProject<?> project);

    @Override
    @Query("""
        SELECT DISTINCT d FROM CDashboardProject_Bab d
        LEFT JOIN FETCH d.project
        LEFT JOIN FETCH d.assignedTo
        LEFT JOIN FETCH d.status
        LEFT JOIN FETCH d.attachments
        LEFT JOIN FETCH d.comments
        LEFT JOIN FETCH d.links
        WHERE d.id = :id
        """)
    Optional<CDashboardProject_Bab> findById(@Param("id") Long id);
}
