package tech.derbent.bab.policybase.trigger.service;

import tech.derbent.bab.policybase.trigger.domain.CBabPolicyTrigger;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.context.annotation.Profile;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import java.util.List;
import java.util.Optional;

/**
 * IBabPolicyTriggerRepository - Repository for CBabPolicyTrigger entities.
 * 
 * Provides data access methods for BAB policy triggers including:
 * - Basic CRUD operations via base interface
 * - Project-scoped queries
 * - Trigger type filtering
 * - Enabled status queries
 * - Node type compatibility checks
 * 
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Repository interface with @Query annotations
 */
@Profile("bab")
public interface IBabPolicyTriggerRepository extends IEntityOfProjectRepository<CBabPolicyTrigger> {

    @Override
    @Query("""
        SELECT DISTINCT e FROM #{#entityName} e
        LEFT JOIN FETCH e.project
        LEFT JOIN FETCH e.createdBy
        LEFT JOIN FETCH e.attachments
        LEFT JOIN FETCH e.comments
        LEFT JOIN FETCH e.links
        WHERE e.id = :id
        """)
    Optional<CBabPolicyTrigger> findById(@Param("id") Long id);

    @Override
    @Query("""
        SELECT DISTINCT e FROM #{#entityName} e
        LEFT JOIN FETCH e.project
        LEFT JOIN FETCH e.createdBy
        LEFT JOIN FETCH e.attachments
        LEFT JOIN FETCH e.comments
        LEFT JOIN FETCH e.links
        WHERE e.project = :project
        ORDER BY e.executionOrder ASC, e.executionPriority DESC, e.name ASC
        """)
    List<CBabPolicyTrigger> listByProjectForPageView(@Param("project") CProject<?> project);

    /**
     * Find triggers by trigger type.
     */
    @Query("""
        SELECT e FROM #{#entityName} e
        LEFT JOIN FETCH e.project
        WHERE e.project = :project 
        AND e.triggerType = :triggerType
        ORDER BY e.executionOrder ASC, e.executionPriority DESC
        """)
    List<CBabPolicyTrigger> findByProjectAndTriggerType(
        @Param("project") CProject<?> project,
        @Param("triggerType") String triggerType);

    /**
     * Find enabled triggers for execution.
     */
    @Query("""
        SELECT e FROM #{#entityName} e
        LEFT JOIN FETCH e.project
        WHERE e.project = :project 
        AND e.active = true
        ORDER BY e.executionOrder ASC, e.executionPriority DESC
        """)
    List<CBabPolicyTrigger> findEnabledByProject(@Param("project") CProject<?> project);

    /**
     * Find triggers compatible with specific node type.
     */
    @Query("""
        SELECT e FROM #{#entityName} e
        WHERE e.project = :project 
        AND e.active = true
        AND (
            (:nodeType = 'can' AND e.canNodeEnabled = true) OR
            (:nodeType = 'modbus' AND e.modbusNodeEnabled = true) OR
            (:nodeType = 'http' AND e.httpNodeEnabled = true) OR
            (:nodeType = 'file' AND e.fileNodeEnabled = true) OR
            (:nodeType = 'syslog' AND e.syslogNodeEnabled = true) OR
            (:nodeType = 'ros' AND e.rosNodeEnabled = true)
        )
        ORDER BY e.executionOrder ASC, e.executionPriority DESC
        """)
    List<CBabPolicyTrigger> findEnabledForNodeType(
        @Param("project") CProject<?> project,
        @Param("nodeType") String nodeType);

    /**
     * Find periodic triggers for scheduling.
     */
    @Query("""
        SELECT e FROM #{#entityName} e
        WHERE e.project = :project 
        AND e.active = true
        AND e.triggerType = 'periodic'
        AND e.cronExpression IS NOT NULL
        ORDER BY e.executionOrder ASC
        """)
    List<CBabPolicyTrigger> findPeriodicTriggers(@Param("project") CProject<?> project);

    /**
     * Find startup triggers.
     */
    @Query("""
        SELECT e FROM #{#entityName} e
        WHERE e.project = :project 
        AND e.active = true
        AND e.triggerType = 'at_start'
        ORDER BY e.executionOrder ASC, e.executionPriority DESC
        """)
    List<CBabPolicyTrigger> findStartupTriggers(@Param("project") CProject<?> project);

    /**
     * Count triggers by type for statistics.
     */
    @Query("""
        SELECT e.triggerType, COUNT(e)
        FROM #{#entityName} e
        WHERE e.project = :project
        GROUP BY e.triggerType
        """)
    List<Object[]> countByTriggerType(@Param("project") CProject<?> project);
}
