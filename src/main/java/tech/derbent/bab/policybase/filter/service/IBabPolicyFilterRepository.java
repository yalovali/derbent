package tech.derbent.bab.policybase.filter.service;

import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.context.annotation.Profile;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import java.util.List;
import java.util.Optional;

/**
 * IBabPolicyFilterRepository - Repository for CBabPolicyFilter entities.
 * 
 * Provides data access methods for BAB policy filters including:
 * - Basic CRUD operations via base interface
 * - Project-scoped queries
 * - Filter type filtering
 * - Enabled status queries
 * - Node type compatibility checks
 * - Execution order optimization
 * 
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Repository interface with @Query annotations
 */
@Profile("bab")
public interface IBabPolicyFilterRepository extends IEntityOfProjectRepository<CBabPolicyFilter> {

    @Override
    @Query("""
        SELECT f FROM CBabPolicyFilter f
        LEFT JOIN FETCH f.project
        LEFT JOIN FETCH f.createdBy
        LEFT JOIN FETCH f.attachments
        LEFT JOIN FETCH f.comments
        LEFT JOIN FETCH f.links
        WHERE f.id = :id
        """)
    Optional<CBabPolicyFilter> findById(@Param("id") Long id);

    @Override
    @Query("""
        SELECT f FROM CBabPolicyFilter f
        LEFT JOIN FETCH f.project
        LEFT JOIN FETCH f.createdBy
        LEFT JOIN FETCH f.attachments
        LEFT JOIN FETCH f.comments
        LEFT JOIN FETCH f.links
        WHERE f.project = :project
        ORDER BY f.executionOrder ASC, f.name ASC
        """)
    List<CBabPolicyFilter> listByProjectForPageView(@Param("project") CProject<?> project);

    /**
     * Find filters by filter type.
     */
    @Query("""
        SELECT f FROM CBabPolicyFilter f
        LEFT JOIN FETCH f.project
        WHERE f.project = :project 
        AND f.filterType = :filterType
        ORDER BY f.executionOrder ASC
        """)
    List<CBabPolicyFilter> findByProjectAndFilterType(
        @Param("project") CProject<?> project,
        @Param("filterType") String filterType);

    /**
     * Find enabled filters for processing.
     */
    @Query("""
        SELECT f FROM CBabPolicyFilter f
        LEFT JOIN FETCH f.project
        WHERE f.project = :project 
        AND f.isEnabled = true
        ORDER BY f.executionOrder ASC
        """)
    List<CBabPolicyFilter> findEnabledByProject(@Param("project") CProject<?> project);

    /**
     * Find filters compatible with specific node type.
     */
    @Query("""
        SELECT f FROM CBabPolicyFilter f
        WHERE f.project = :project 
        AND f.isEnabled = true
        AND (
            (:nodeType = 'can' AND f.canNodeEnabled = true) OR
            (:nodeType = 'modbus' AND f.modbusNodeEnabled = true) OR
            (:nodeType = 'http' AND f.httpNodeEnabled = true) OR
            (:nodeType = 'file' AND f.fileNodeEnabled = true) OR
            (:nodeType = 'syslog' AND f.syslogNodeEnabled = true) OR
            (:nodeType = 'ros' AND f.rosNodeEnabled = true)
        )
        ORDER BY f.executionOrder ASC
        """)
    List<CBabPolicyFilter> findEnabledForNodeType(
        @Param("project") CProject<?> project,
        @Param("nodeType") String nodeType);

    /**
     * Find filters with caching enabled for performance optimization.
     */
    @Query("""
        SELECT f FROM CBabPolicyFilter f
        WHERE f.project = :project 
        AND f.isEnabled = true
        AND f.cacheEnabled = true
        ORDER BY f.executionOrder ASC
        """)
    List<CBabPolicyFilter> findCachedFilters(@Param("project") CProject<?> project);

    /**
     * Find transformation filters.
     */
    @Query("""
        SELECT f FROM CBabPolicyFilter f
        WHERE f.project = :project 
        AND f.isEnabled = true
        AND f.filterType = 'transform'
        ORDER BY f.executionOrder ASC
        """)
    List<CBabPolicyFilter> findTransformationFilters(@Param("project") CProject<?> project);

    /**
     * Count filters by type for statistics.
     */
    @Query("""
        SELECT f.filterType, COUNT(f)
        FROM CBabPolicyFilter f
        WHERE f.project = :project
        GROUP BY f.filterType
        """)
    List<Object[]> countByFilterType(@Param("project") CProject<?> project);
}