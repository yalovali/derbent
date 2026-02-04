package tech.derbent.bab.dashboard.dashboardinterfaces.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.bab.dashboard.dashboardinterfaces.domain.CDashboardInterfaces;

/**
 * IDashboardInterfacesRepository - Repository interface for CDashboardInterfaces entity.
 * <p>
 * Layer: Repository (MVC)
 * Profile: bab
 * <p>
 * Following Derbent pattern: Interface extending base repository with custom queries.
 * Provides data access methods for BAB interface dashboard entities.
 */
@Profile("bab")
public interface IDashboardInterfacesRepository extends IEntityOfProjectRepository<CDashboardInterfaces> {

    /**
     * Find interface dashboards by project and active status.
     * Eager loads project, createdBy, and composition fields.
     * 
     * @param projectId the project identifier
     * @param isActive the active status
     * @return list of matching dashboards
     */
    @Query("""
        SELECT DISTINCT d FROM #{#entityName} d
        LEFT JOIN FETCH d.project
        LEFT JOIN FETCH d.createdBy
        LEFT JOIN FETCH d.attachments
        LEFT JOIN FETCH d.comments
        LEFT JOIN FETCH d.links
        WHERE d.project.id = :projectId AND d.isActive = :isActive
        ORDER BY d.name ASC
        """)
    List<CDashboardInterfaces> findByProjectIdAndIsActive(@Param("projectId") Long projectId,
                                                          @Param("isActive") Boolean isActive);

    /**
     * Find interface dashboards by configuration mode.
     * Eager loads project and related entities.
     * 
     * @param configurationMode the configuration mode
     * @return list of matching dashboards
     */
    @Query("""
        SELECT DISTINCT d FROM #{#entityName} d
        LEFT JOIN FETCH d.project
        LEFT JOIN FETCH d.createdBy
        LEFT JOIN FETCH d.attachments
        LEFT JOIN FETCH d.comments
        LEFT JOIN FETCH d.links
        WHERE d.configurationMode = :configurationMode
        ORDER BY d.name ASC
        """)
    List<CDashboardInterfaces> findByConfigurationMode(@Param("configurationMode") String configurationMode);

    /**
     * Find active interface dashboards for a project with eager loading.
     * Override base method with eager loading for UI performance.
     * 
     * @param projectId the project identifier
     * @return list of active dashboards
     */
    @Query("""
        SELECT DISTINCT d FROM #{#entityName} d
        LEFT JOIN FETCH d.project
        LEFT JOIN FETCH d.createdBy
        LEFT JOIN FETCH d.attachments
        LEFT JOIN FETCH d.comments
        LEFT JOIN FETCH d.links
        WHERE d.project.id = :projectId AND d.isActive = true
        ORDER BY d.id DESC
        """)
    List<CDashboardInterfaces> listByProjectForPageView(@Param("projectId") Long projectId);

    /**
     * Find dashboard by ID with complete eager loading.
     * Override base method to prevent N+1 queries in forms.
     * 
     * @param id the dashboard identifier
     * @return dashboard with all relationships loaded
     */
    @Override
    @Query("""
        SELECT d FROM #{#entityName} d
        LEFT JOIN FETCH d.project
        LEFT JOIN FETCH d.createdBy
        LEFT JOIN FETCH d.attachments
        LEFT JOIN FETCH d.comments
        LEFT JOIN FETCH d.links
        WHERE d.id = :id
        """)
    Optional<CDashboardInterfaces> findById(@Param("id") Long id);

    /**
     * Find all dashboards with eager loading for page view.
     * 
     * @return list of all dashboards with relationships loaded
     */
    @Query("""
        SELECT DISTINCT d FROM #{#entityName} d
        LEFT JOIN FETCH d.project
        LEFT JOIN FETCH d.createdBy
        LEFT JOIN FETCH d.attachments
        LEFT JOIN FETCH d.comments
        LEFT JOIN FETCH d.links
        ORDER BY d.id DESC
        """)
    List<CDashboardInterfaces> findAllForPageView();

    /**
     * Count active interface dashboards by project.
     * 
     * @param projectId the project identifier
     * @return count of active dashboards
     */
    @Query("SELECT COUNT(d) FROM #{#entityName} d WHERE d.project.id = :projectId AND d.isActive = true")
    long countActiveByProject(@Param("projectId") Long projectId);
}