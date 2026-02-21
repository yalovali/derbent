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
        SELECT t FROM CBabPolicyTrigger t
        LEFT JOIN FETCH t.project
        LEFT JOIN FETCH t.createdBy
        LEFT JOIN FETCH t.attachments
        LEFT JOIN FETCH t.comments
        LEFT JOIN FETCH t.links
        WHERE t.id = :id
        """)
    Optional<CBabPolicyTrigger> findById(@Param("id") Long id);

    @Override
    @Query("""
        SELECT t FROM CBabPolicyTrigger t
        LEFT JOIN FETCH t.project
        LEFT JOIN FETCH t.createdBy
        LEFT JOIN FETCH t.attachments
        LEFT JOIN FETCH t.comments
        LEFT JOIN FETCH t.links
        WHERE t.project = :project
        ORDER BY t.executionOrder ASC, t.executionPriority DESC, t.name ASC
        """)
    List<CBabPolicyTrigger> listByProjectForPageView(@Param("project") CProject<?> project);

    /**
     * Find triggers by trigger type.
     */
    @Query("""
        SELECT t FROM CBabPolicyTrigger t
        LEFT JOIN FETCH t.project
        WHERE t.project = :project 
        AND t.triggerType = :triggerType
        ORDER BY t.executionOrder ASC, t.executionPriority DESC
        """)
    List<CBabPolicyTrigger> findByProjectAndTriggerType(
        @Param("project") CProject<?> project,
        @Param("triggerType") String triggerType);

    /**
     * Find enabled triggers for execution.
     */
    @Query("""
        SELECT t FROM CBabPolicyTrigger t
        LEFT JOIN FETCH t.project
        WHERE t.project = :project 
        AND t.active = true
        ORDER BY t.executionOrder ASC, t.executionPriority DESC
        """)
    List<CBabPolicyTrigger> findEnabledByProject(@Param("project") CProject<?> project);

    /**
     * Find triggers compatible with specific node type.
     */
    @Query("""
        SELECT t FROM CBabPolicyTrigger t
        WHERE t.project = :project 
        AND t.active = true
        AND (
            (:nodeType = 'can' AND t.canNodeEnabled = true) OR
            (:nodeType = 'modbus' AND t.modbusNodeEnabled = true) OR
            (:nodeType = 'http' AND t.httpNodeEnabled = true) OR
            (:nodeType = 'file' AND t.fileNodeEnabled = true) OR
            (:nodeType = 'syslog' AND t.syslogNodeEnabled = true) OR
            (:nodeType = 'ros' AND t.rosNodeEnabled = true)
        )
        ORDER BY t.executionOrder ASC, t.executionPriority DESC
        """)
    List<CBabPolicyTrigger> findEnabledForNodeType(
        @Param("project") CProject<?> project,
        @Param("nodeType") String nodeType);

    /**
     * Find periodic triggers for scheduling.
     */
    @Query("""
        SELECT t FROM CBabPolicyTrigger t
        WHERE t.project = :project 
        AND t.active = true
        AND t.triggerType = 'periodic'
        AND t.cronExpression IS NOT NULL
        ORDER BY t.executionOrder ASC
        """)
    List<CBabPolicyTrigger> findPeriodicTriggers(@Param("project") CProject<?> project);

    /**
     * Find startup triggers.
     */
    @Query("""
        SELECT t FROM CBabPolicyTrigger t
        WHERE t.project = :project 
        AND t.active = true
        AND t.triggerType = 'at_start'
        ORDER BY t.executionOrder ASC, t.executionPriority DESC
        """)
    List<CBabPolicyTrigger> findStartupTriggers(@Param("project") CProject<?> project);

    /**
     * Count triggers by type for statistics.
     */
    @Query("""
        SELECT t.triggerType, COUNT(t)
        FROM CBabPolicyTrigger t
        WHERE t.project = :project
        GROUP BY t.triggerType
        """)
    List<Object[]> countByTriggerType(@Param("project") CProject<?> project);
}
