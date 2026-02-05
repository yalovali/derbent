package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CDashboardPolicy;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CPolicyRule;

/**
 * IPolicyRuleRepository - Repository interface for BAB policy rule entities.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries.
 * 
 * Handles policy rule persistence with complete eager loading for drag-and-drop UI performance.
 * Provides specialized queries for rule relationships, node references, and policy management.
 */
@Profile("bab")
public interface IPolicyRuleRepository extends IEntityOfProjectRepository<CPolicyRule> {
    
    @Override
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.assignedTo
	LEFT JOIN FETCH e.createdBy
	LEFT JOIN FETCH e.policy
	LEFT JOIN FETCH e.comments
	WHERE e.id = :id
	""")
    Optional<CPolicyRule> findById(@Param("id") Long id);
    
    @Override
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.assignedTo
	LEFT JOIN FETCH e.createdBy
	LEFT JOIN FETCH e.policy
	LEFT JOIN FETCH e.comments
	WHERE e.project = :project
	ORDER BY e.rulePriority DESC, e.executionOrder ASC, e.id DESC
	""")
    List<CPolicyRule> listByProjectForPageView(@Param("project") CProject<?> project);
    
    /**
     * Find rules by policy with complete eager loading.
     * Critical for policy rule grid display and editing.
     */
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.comments
	WHERE e.policyName = :policyName AND e.project = :project
	ORDER BY e.executionOrder ASC, e.rulePriority DESC
	""")
    List<CPolicyRule> findByPolicyWithNodes(@Param("policyName") String policyName, @Param("project") CProject<?> project);
    
    /**
     * Find active rules by policy.
     * Critical for policy application and JSON generation.
     */
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	WHERE e.policyName = :policyName AND e.project = :project AND e.isActive = true
	ORDER BY e.rulePriority DESC, e.executionOrder ASC
	""")
    List<CPolicyRule> findActiveByPolicy(@Param("policyName") String policyName, @Param("project") CProject<?> project);
    
    /**
     * Find rules by policy and project.
     * Useful for policy-specific rule management.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.policyName = :policyName AND e.project = :project
	ORDER BY e.executionOrder ASC, e.rulePriority DESC
	""")
    List<CPolicyRule> findByPolicyAndProject(@Param("policyName") String policyName, @Param("project") CProject<?> project);
    
    /**
     * Find rules using specific node as source.
     * Critical for node deletion validation.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.sourceNodeName = :nodeName AND e.project = :project
	ORDER BY e.policyName ASC, e.name ASC
	""")
    List<CPolicyRule> findBySourceNode(@Param("nodeName") String nodeName, @Param("project") CProject<?> project);
    
    /**
     * Find rules using specific node as destination.
     * Critical for node deletion validation.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.destinationNodeName = :nodeName AND e.project = :project
	ORDER BY e.policyName ASC, e.name ASC
	""")
    List<CPolicyRule> findByDestinationNode(@Param("nodeName") String nodeName, @Param("project") CProject<?> project);
    
    /**
     * Find rules using specific node as trigger.
     * Critical for node deletion validation.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.triggerEntityString = :nodeName AND e.project = :project
	ORDER BY e.policyName ASC, e.name ASC
	""")
    List<CPolicyRule> findByTriggerEntity(@Param("nodeName") String nodeName, @Param("project") CProject<?> project);
    
    /**
     * Find rules using specific node as action.
     * Critical for node deletion validation.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.actionEntityName = :nodeName AND e.project = :project
	ORDER BY e.policyName ASC, e.name ASC
	""")
    List<CPolicyRule> findByActionEntity(@Param("nodeName") String nodeName, @Param("project") CProject<?> project);
    
    /**
     * Find all rules using specific node in any capacity.
     * Critical for comprehensive node impact analysis.
     */
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	WHERE (e.sourceNodeName = :nodeName 
	    OR e.destinationNodeName = :nodeName 
	    OR e.triggerEntityString = :nodeName 
	    OR e.actionEntityName = :nodeName)
	AND e.project = :project
	ORDER BY e.policyName ASC, e.name ASC
	""")
    List<CPolicyRule> findAllUsingNode(@Param("nodeName") String nodeName, @Param("project") CProject<?> project);
    
    /**
     * Find complete rules (all four node references set).
     * Useful for identifying fully configured rules.
     */
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	WHERE e.sourceNodeName IS NOT NULL AND e.sourceNodeName != ''
	AND e.destinationNodeName IS NOT NULL AND e.destinationNodeName != ''
	AND e.triggerEntityString IS NOT NULL AND e.triggerEntityString != ''
	AND e.actionEntityName IS NOT NULL AND e.actionEntityName != ''
	AND e.project = :project
	ORDER BY e.policyName ASC, e.executionOrder ASC
	""")
    List<CPolicyRule> findCompleteRules(@Param("project") CProject<?> project);
    
    /**
     * Find incomplete rules (missing node references).
     * Useful for identifying rules needing configuration.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE (e.sourceNodeName IS NULL OR e.sourceNodeName = ''
	    OR e.destinationNodeName IS NULL OR e.destinationNodeName = ''
	    OR e.triggerEntityString IS NULL OR e.triggerEntityString = ''
	    OR e.actionEntityName IS NULL OR e.actionEntityName = '')
	AND e.project = :project
	ORDER BY e.policyName ASC, e.name ASC
	""")
    List<CPolicyRule> findIncompleteRules(@Param("project") CProject<?> project);
    
    /**
     * Find rules by priority level.
     * Useful for priority-based rule management.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.rulePriority = :priority AND e.project = :project
	ORDER BY e.executionOrder ASC, e.policyName ASC
	""")
    List<CPolicyRule> findByPriorityAndProject(@Param("priority") Integer priority, @Param("project") CProject<?> project);
    
    /**
     * Find rules with logging enabled.
     * Useful for log management and debugging.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.logEnabled = true AND e.project = :project
	ORDER BY e.policyName ASC, e.name ASC
	""")
    List<CPolicyRule> findWithLoggingEnabled(@Param("project") CProject<?> project);
    
    /**
     * Find rules by execution status.
     * Useful for runtime monitoring and troubleshooting.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.lastExecutionStatus = :status AND e.project = :project
	ORDER BY e.lastExecutionDate DESC NULLS LAST
	""")
    List<CPolicyRule> findByExecutionStatus(@Param("status") String status, @Param("project") CProject<?> project);
    
    /**
     * Find frequently executed rules.
     * Useful for performance monitoring.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.executionCount >= :minExecutions AND e.project = :project
	ORDER BY e.executionCount DESC, e.lastExecutionDate DESC
	""")
    List<CPolicyRule> findFrequentlyExecuted(@Param("minExecutions") Long minExecutions, @Param("project") CProject<?> project);
    
    /**
     * Find rules executed recently.
     * Useful for activity monitoring.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.lastExecutionDate >= :sinceDate AND e.project = :project
	ORDER BY e.lastExecutionDate DESC
	""")
    List<CPolicyRule> findExecutedSince(@Param("sinceDate") java.time.LocalDateTime sinceDate, @Param("project") CProject<?> project);
    
    /**
     * Find rules never executed.
     * Useful for identifying unused rules.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE (e.executionCount = 0 OR e.executionCount IS NULL)
	AND e.project = :project
	ORDER BY e.createdDate ASC
	""")
    List<CPolicyRule> findNeverExecuted(@Param("project") CProject<?> project);
    
    /**
     * Get rule execution statistics for dashboard.
     * Returns: [totalRules, activeRules, completeRules, executedRules]
     */
    @Query("""
	SELECT 
	    COUNT(e),
	    SUM(CASE WHEN e.isActive = true THEN 1 ELSE 0 END),
	    SUM(CASE WHEN e.sourceNodeName IS NOT NULL AND e.sourceNodeName != ''
	            AND e.destinationNodeName IS NOT NULL AND e.destinationNodeName != ''
	            AND e.triggerEntityString IS NOT NULL AND e.triggerEntityString != ''
	            AND e.actionEntityName IS NOT NULL AND e.actionEntityName != '' THEN 1 ELSE 0 END),
	    SUM(CASE WHEN e.executionCount > 0 THEN 1 ELSE 0 END)
	FROM #{#entityName} e 
	WHERE e.project = :project
	""")
    List<Long> getRuleStatistics(@Param("project") CProject<?> project);
    
    /**
     * Count rules by policy.
     * Critical for policy rule count updates.
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.policyName = :policyName AND e.project = :project")
    long countByPolicy(@Param("policyName") String policyName, @Param("project") CProject<?> project);
    
    /**
     * Count active rules by policy.
     * Critical for policy validation.
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.policyName = :policyName AND e.project = :project AND e.isActive = true")
    long countActiveByPolicy(@Param("policyName") String policyName, @Param("project") CProject<?> project);
    
    /**
     * Count complete rules by policy.
     * Critical for policy readiness validation.
     */
    @Query("""
	SELECT COUNT(e) FROM #{#entityName} e 
	WHERE e.policyName = :policyName AND e.project = :project
	AND e.sourceNodeName IS NOT NULL AND e.sourceNodeName != ''
	AND e.destinationNodeName IS NOT NULL AND e.destinationNodeName != ''
	AND e.triggerEntityString IS NOT NULL AND e.triggerEntityString != ''
	AND e.actionEntityName IS NOT NULL AND e.actionEntityName != ''
	""")
    long countCompleteByPolicy(@Param("policyName") String policyName, @Param("project") CProject<?> project);
    
    /**
     * Find rules by execution order range.
     * Useful for execution sequence management.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.executionOrder BETWEEN :minOrder AND :maxOrder 
	AND e.project = :project
	ORDER BY e.executionOrder ASC, e.policyName ASC
	""")
    List<CPolicyRule> findByExecutionOrderRange(@Param("minOrder") Integer minOrder, @Param("maxOrder") Integer maxOrder, @Param("project") CProject<?> project);
    
    /**
     * Find next execution order for policy.
     * Useful for automatic execution order assignment.
     */
    @Query("SELECT COALESCE(MAX(e.executionOrder), -1) + 1 FROM #{#entityName} e WHERE e.policyName = :policyName AND e.project = :project")
    Integer findNextExecutionOrderForPolicy(@Param("policyName") String policyName, @Param("project") CProject<?> project);
}