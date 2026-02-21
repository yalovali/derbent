package tech.derbent.bab.policybase.action.service;

import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.context.annotation.Profile;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import java.util.List;
import java.util.Optional;

/**
 * IBabPolicyActionRepository - Repository for CBabPolicyAction entities.
 * 
 * Provides data access methods for BAB policy actions including:
 * - Basic CRUD operations via base interface
 * - Project-scoped queries
 * - Action type filtering
 * - Enabled status queries
 * - Node type compatibility checks
 * - Execution priority ordering
 * 
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Repository interface with @Query annotations
 */
@Profile("bab")
public interface IBabPolicyActionRepository extends IEntityOfProjectRepository<CBabPolicyAction> {

    @Override
    @Query("""
        SELECT a FROM CBabPolicyAction a
        LEFT JOIN FETCH a.project
        LEFT JOIN FETCH a.createdBy
        LEFT JOIN FETCH a.attachments
        LEFT JOIN FETCH a.comments
        LEFT JOIN FETCH a.links
        WHERE a.id = :id
        """)
    Optional<CBabPolicyAction> findById(@Param("id") Long id);

    @Override
    @Query("""
        SELECT a FROM CBabPolicyAction a
        LEFT JOIN FETCH a.project
        LEFT JOIN FETCH a.createdBy
        LEFT JOIN FETCH a.attachments
        LEFT JOIN FETCH a.comments
        LEFT JOIN FETCH a.links
        WHERE a.project = :project
        ORDER BY a.executionOrder ASC, a.executionPriority DESC, a.name ASC
        """)
    List<CBabPolicyAction> listByProjectForPageView(@Param("project") CProject<?> project);

    /**
     * Find actions by action type.
     */
    @Query("""
        SELECT a FROM CBabPolicyAction a
        LEFT JOIN FETCH a.project
        WHERE a.project = :project 
        AND a.actionType = :actionType
        ORDER BY a.executionOrder ASC, a.executionPriority DESC
        """)
    List<CBabPolicyAction> findByProjectAndActionType(
        @Param("project") CProject<?> project,
        @Param("actionType") String actionType);

    /**
     * Find enabled actions for execution.
     */
    @Query("""
        SELECT a FROM CBabPolicyAction a
        LEFT JOIN FETCH a.project
        WHERE a.project = :project 
        AND a.active = true
        ORDER BY a.executionOrder ASC, a.executionPriority DESC
        """)
    List<CBabPolicyAction> findEnabledByProject(@Param("project") CProject<?> project);

    /**
     * Find actions compatible with specific node type.
     */
    @Query("""
        SELECT a FROM CBabPolicyAction a
        WHERE a.project = :project 
        AND a.active = true
        AND (
            (:nodeType = 'can' AND a.canNodeEnabled = true) OR
            (:nodeType = 'modbus' AND a.modbusNodeEnabled = true) OR
            (:nodeType = 'http' AND a.httpNodeEnabled = true) OR
            (:nodeType = 'file' AND a.fileNodeEnabled = true) OR
            (:nodeType = 'syslog' AND a.syslogNodeEnabled = true) OR
            (:nodeType = 'ros' AND a.rosNodeEnabled = true)
        )
        ORDER BY a.executionOrder ASC, a.executionPriority DESC
        """)
    List<CBabPolicyAction> findEnabledForNodeType(
        @Param("project") CProject<?> project,
        @Param("nodeType") String nodeType);

    /**
     * Find synchronous actions (not async).
     */
    @Query("""
        SELECT a FROM CBabPolicyAction a
        WHERE a.project = :project 
        AND a.active = true
        AND (a.asyncExecution IS NULL OR a.asyncExecution = false)
        ORDER BY a.executionOrder ASC, a.executionPriority DESC
        """)
    List<CBabPolicyAction> findSynchronousActions(@Param("project") CProject<?> project);

    /**
     * Find asynchronous actions.
     */
    @Query("""
        SELECT a FROM CBabPolicyAction a
        WHERE a.project = :project 
        AND a.active = true
        AND a.asyncExecution = true
        ORDER BY a.executionOrder ASC, a.executionPriority DESC
        """)
    List<CBabPolicyAction> findAsynchronousActions(@Param("project") CProject<?> project);

    /**
     * Count actions by type for statistics.
     */
    @Query("""
        SELECT a.actionType, COUNT(a)
        FROM CBabPolicyAction a
        WHERE a.project = :project
        GROUP BY a.actionType
        """)
    List<Object[]> countByActionType(@Param("project") CProject<?> project);
}
