package tech.derbent.bab.policybase.policy.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.policy.domain.CBabPolicy;

/** IPolicyRepository - Repository interface for BAB policy entities. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent
 * pattern: Concrete repository with HQL queries. Handles policy persistence with complete eager loading for dashboard performance. Provides
 * specialized queries for policy management, rule relationships, and Calimero integration. */
@Profile ("bab")
public interface IPolicyRepository extends IEntityOfProjectRepository<CBabPolicy> {

	/** Count active rules per policy. Critical for policy completeness validation. */
	@Query ("""
			SELECT COUNT(r) FROM CBabPolicy p
			JOIN p.babPolicyRules r
			WHERE p.id = :policyId AND r.isActive = true
			""")
	long countActiveRulesByPolicy(@Param ("policyId") Long policyId);
	/** Find active policies by project. Critical for policy application and dashboard display. */
	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.babPolicyRules
			WHERE e.isActive = true AND e.project = :project
			ORDER BY e.priorityLevel DESC, e.name ASC
			""")
	List<CBabPolicy> findActiveByProject(@Param ("project") CProject<?> project);
	@Override
	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.assignedTo
			LEFT JOIN FETCH e.createdBy
			LEFT JOIN FETCH e.babPolicyRules
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			WHERE e.id = :id
			""")
	Optional<CBabPolicy> findById(@Param ("id") Long id);
}
