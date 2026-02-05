package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CBabPolicyRule;

/** IPolicyRuleRepository - Repository interface for BAB policy rule entities. Layer: Service (MVC) Active when: 'bab' profile is active Following
 * Derbent pattern: Concrete repository with HQL queries. Handles policy rule persistence with complete eager loading for drag-and-drop UI
 * performance. Provides specialized queries for rule relationships, node references, and policy management. */
@Profile ("bab")
public interface IBabPolicyRuleRepository extends IEntityOfProjectRepository<CBabPolicyRule> {

	/** Count active rules by policy. Critical for policy validation. */
	@Query ("SELECT COUNT(e) FROM #{#entityName} e WHERE e.policyId = :policyId AND e.project = :project AND e.isActive = true")
	long countActiveByPolicy(@Param ("policyId") Long policyId, @Param ("project") CProject<?> project);
	/** Count rules by policy. Critical for policy rule count updates. */
	@Query ("SELECT COUNT(e) FROM #{#entityName} e WHERE e.policyId = :policyId AND e.project = :project")
	long countByPolicy(@Param ("policyId") Long policyId, @Param ("project") CProject<?> project);
	/** Count complete rules by policy. Critical for policy readiness validation. */
	@Query ("""
			SELECT COUNT(e) FROM #{#entityName} e
			WHERE e.policyId = :policyId AND e.project = :project
			AND e.sourceNodeName IS NOT NULL AND e.sourceNodeName != ''
			AND e.destinationNodeName IS NOT NULL AND e.destinationNodeName != ''
			AND e.triggerEntityString IS NOT NULL AND e.triggerEntityString != ''
			AND e.actionEntityName IS NOT NULL AND e.actionEntityName != ''
			""")
	long countCompleteByPolicy(@Param ("policyId") Long policyId, @Param ("project") CProject<?> project);
	/** Find rules by execution order range. Useful for execution sequence management. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.executionOrder BETWEEN :minOrder AND :maxOrder
			AND e.project = :project
			ORDER BY e.executionOrder ASC, e.policyId ASC
			""")
	List<CBabPolicyRule> findByExecutionOrderRange(@Param ("minOrder") Integer minOrder, @Param ("maxOrder") Integer maxOrder,
			@Param ("project") CProject<?> project);
	@Override
	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.assignedTo
			LEFT JOIN FETCH e.createdBy
			LEFT JOIN FETCH e.policy
			LEFT JOIN FETCH e.comments
			WHERE e.id = :id
			""")
	Optional<CBabPolicyRule> findById(@Param ("id") Long id);
	/** Find rules by policy and project. Useful for policy-specific rule management. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.policyId = :policyId AND e.project = :project
			ORDER BY e.executionOrder ASC, e.rulePriority DESC
			""")
	List<CBabPolicyRule> findByPolicyAndProject(@Param ("policyId") Long policyId, @Param ("project") CProject<?> project);
	@Override
	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.assignedTo
			LEFT JOIN FETCH e.createdBy
			LEFT JOIN FETCH e.policy
			LEFT JOIN FETCH e.comments
			WHERE e.project = :project
			ORDER BY e.rulePriority DESC, e.executionOrder ASC, e.id DESC
			""")
	List<CBabPolicyRule> listByProjectForPageView(@Param ("project") CProject<?> project);
}
