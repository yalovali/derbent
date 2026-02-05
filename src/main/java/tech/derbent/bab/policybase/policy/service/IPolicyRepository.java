package tech.derbent.bab.policybase.policy.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CPolicy;

/**
 * IPolicyRepository - Repository interface for BAB policy entities.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries.
 * 
 * Handles policy persistence with complete eager loading for dashboard performance.
 * Provides specialized queries for policy management, rule relationships, and Calimero integration.
 */
@Profile("bab")
public interface IPolicyRepository extends IEntityOfProjectRepository<CPolicy> {
    
    @Override
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.assignedTo
	LEFT JOIN FETCH e.createdBy
	LEFT JOIN FETCH e.rules r
	LEFT JOIN FETCH r.sourceNode
	LEFT JOIN FETCH r.destinationNode
	LEFT JOIN FETCH r.triggerEntity
	LEFT JOIN FETCH r.actionEntity
	LEFT JOIN FETCH e.attachments
	LEFT JOIN FETCH e.comments
	LEFT JOIN FETCH e.links
	WHERE e.id = :id
	""")
    Optional<CPolicy> findById(@Param("id") Long id);
    
    @Override
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.assignedTo
	LEFT JOIN FETCH e.createdBy
	LEFT JOIN FETCH e.rules r
	LEFT JOIN FETCH r.sourceNode
	LEFT JOIN FETCH r.destinationNode
	LEFT JOIN FETCH r.triggerEntity
	LEFT JOIN FETCH r.actionEntity
	LEFT JOIN FETCH e.attachments
	LEFT JOIN FETCH e.comments
	LEFT JOIN FETCH e.links
	WHERE e.project = :project
	ORDER BY e.priorityLevel DESC, e.id DESC
	""")
    List<CPolicy> listByProjectForPageView(@Param("project") CProject<?> project);
    
    /**
     * Find active policies by project.
     * Critical for policy application and dashboard display.
     */
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.rules r
	LEFT JOIN FETCH r.sourceNode
	LEFT JOIN FETCH r.destinationNode
	WHERE e.isActive = true AND e.project = :project
	ORDER BY e.priorityLevel DESC, e.name ASC
	""")
    List<CPolicy> findActiveByProject(@Param("project") CProject<?> project);
    
    /**
     * Find policies by application status.
     * Useful for monitoring Calimero application status.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.applicationStatus = :status AND e.project = :project
	ORDER BY e.lastAppliedDate DESC NULLS LAST
	""")
    List<CPolicy> findByApplicationStatusAndProject(@Param("status") String status, @Param("project") CProject<?> project);
    
    /**
     * Find policies ready for application.
     * Critical for auto-apply functionality.
     */
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.rules r
	WHERE e.isActive = true 
	AND e.autoApply = true 
	AND e.project = :project
	AND EXISTS (SELECT 1 FROM e.rules r2 WHERE r2.isActive = true)
	ORDER BY e.priorityLevel DESC
	""")
    List<CPolicy> findReadyForApplication(@Param("project") CProject<?> project);
    
    /**
     * Find policies by priority level.
     * Useful for priority-based policy management.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.priorityLevel = :priorityLevel AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CPolicy> findByPriorityLevelAndProject(@Param("priorityLevel") Integer priorityLevel, @Param("project") CProject<?> project);
    
    /**
     * Find policies by version.
     * Useful for version-based policy tracking.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.policyVersion = :version AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CPolicy> findByVersionAndProject(@Param("version") String version, @Param("project") CProject<?> project);
    
    /**
     * Find policies with auto-apply enabled.
     * Critical for automated policy deployment.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.autoApply = true AND e.project = :project
	ORDER BY e.priorityLevel DESC, e.name ASC
	""")
    List<CPolicy> findAutoApplyPolicies(@Param("project") CProject<?> project);
    
    /**
     * Find policies applied successfully.
     * Useful for deployment status monitoring.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.applicationStatus = 'APPLIED' 
	AND e.lastAppliedDate IS NOT NULL 
	AND e.project = :project
	ORDER BY e.lastAppliedDate DESC
	""")
    List<CPolicy> findSuccessfullyApplied(@Param("project") CProject<?> project);
    
    /**
     * Find policies with failed applications.
     * Critical for troubleshooting policy deployment.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.applicationStatus = 'FAILED' AND e.project = :project
	ORDER BY e.lastAppliedDate DESC NULLS LAST
	""")
    List<CPolicy> findFailedApplications(@Param("project") CProject<?> project);
    
    /**
     * Find policies pending application.
     * Useful for deployment queue management.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.applicationStatus = 'PENDING' AND e.project = :project
	ORDER BY e.priorityLevel DESC, e.id ASC
	""")
    List<CPolicy> findPendingApplication(@Param("project") CProject<?> project);
    
    /**
     * Count active rules per policy.
     * Critical for policy completeness validation.
     */
    @Query("""
	SELECT COUNT(r) FROM CPolicy p 
	JOIN p.rules r 
	WHERE p.id = :policyId AND r.isActive = true
	""")
    long countActiveRulesByPolicy(@Param("policyId") Long policyId);
    
    /**
     * Count total rules per policy.
     * Useful for policy complexity metrics.
     */
    @Query("""
	SELECT COUNT(r) FROM CPolicy p 
	JOIN p.rules r 
	WHERE p.id = :policyId
	""")
    long countTotalRulesByPolicy(@Param("policyId") Long policyId);
    
    /**
     * Find policies containing specific node.
     * Critical for node deletion validation and impact analysis.
     */
    @Query("""
	SELECT DISTINCT p FROM CPolicy p 
	JOIN p.rules r 
	WHERE (r.sourceNode.id = :nodeId 
	    OR r.destinationNode.id = :nodeId 
	    OR r.triggerEntity.id = :nodeId 
	    OR r.actionEntity.id = :nodeId)
	AND p.project = :project
	ORDER BY p.name ASC
	""")
    List<CPolicy> findPoliciesUsingNode(@Param("nodeId") Long nodeId, @Param("project") CProject<?> project);
    
    /**
     * Find policies by rule count range.
     * Useful for policy complexity analysis.
     */
    @Query("""
	SELECT p FROM CPolicy p 
	WHERE p.project = :project
	AND (SELECT COUNT(r) FROM p.rules r) BETWEEN :minRules AND :maxRules
	ORDER BY p.name ASC
	""")
    List<CPolicy> findByRuleCountRange(@Param("minRules") long minRules, @Param("maxRules") long maxRules, @Param("project") CProject<?> project);
    
    /**
     * Find high priority policies.
     * Critical for priority-based policy enforcement.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.priorityLevel >= :minPriority AND e.project = :project
	ORDER BY e.priorityLevel DESC, e.name ASC
	""")
    List<CPolicy> findHighPriorityPolicies(@Param("minPriority") Integer minPriority, @Param("project") CProject<?> project);
    
    /**
     * Find policies modified after date.
     * Useful for change tracking and incremental deployment.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.lastModifiedDate > :sinceDate AND e.project = :project
	ORDER BY e.lastModifiedDate DESC
	""")
    List<CPolicy> findModifiedSince(@Param("sinceDate") java.time.LocalDateTime sinceDate, @Param("project") CProject<?> project);
    
    /**
     * Get distinct policy versions in project.
     * Useful for version filter dropdown.
     */
    @Query("SELECT DISTINCT e.policyVersion FROM #{#entityName} e WHERE e.policyVersion IS NOT NULL AND e.project = :project ORDER BY e.policyVersion")
    List<String> findDistinctVersionsByProject(@Param("project") CProject<?> project);
    
    /**
     * Get policy statistics for dashboard.
     * Returns: [totalCount, activeCount, appliedCount, failedCount]
     */
    @Query("""
	SELECT 
	    COUNT(e),
	    SUM(CASE WHEN e.isActive = true THEN 1 ELSE 0 END),
	    SUM(CASE WHEN e.applicationStatus = 'APPLIED' THEN 1 ELSE 0 END),
	    SUM(CASE WHEN e.applicationStatus = 'FAILED' THEN 1 ELSE 0 END)
	FROM #{#entityName} e 
	WHERE e.project = :project
	""")
    List<Long> getPolicyStatistics(@Param("project") CProject<?> project);
}