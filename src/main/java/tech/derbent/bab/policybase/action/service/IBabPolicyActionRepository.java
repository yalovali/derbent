package tech.derbent.bab.policybase.action.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractNamedRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.policybase.rule.domain.CBabPolicyRule;

/** Repository for BAB policy actions owned by policy rules. */
@Profile ("bab")
public interface IBabPolicyActionRepository extends IAbstractNamedRepository<CBabPolicyAction> {

	@Override
	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.policyRule r
			LEFT JOIN FETCH r.project
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			LEFT JOIN FETCH e.destinationNode
			LEFT JOIN FETCH e.actionMask m
			WHERE e.id = :id
			""")
	Optional<CBabPolicyAction> findById(@Param ("id") Long id);

	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.policyRule r
			LEFT JOIN FETCH r.project
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			LEFT JOIN FETCH e.destinationNode
			LEFT JOIN FETCH e.actionMask m
			WHERE r.project = :project
			ORDER BY e.executionOrder ASC, e.executionPriority DESC, e.name ASC
			""")
	List<CBabPolicyAction> findByProject(@Param ("project") CProject<?> project);

	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.policyRule
			LEFT JOIN FETCH e.destinationNode
			LEFT JOIN FETCH e.actionMask m
			WHERE e.policyRule = :policyRule
			ORDER BY e.executionOrder ASC, e.executionPriority DESC, e.name ASC
			""")
	List<CBabPolicyAction> findByPolicyRule(@Param ("policyRule") CBabPolicyRule policyRule);

	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.policyRule
			LEFT JOIN FETCH e.destinationNode
			LEFT JOIN FETCH e.actionMask m
			WHERE e.policyRule = :policyRule
			AND e.destinationNode = :destinationNode
			ORDER BY e.executionOrder ASC, e.executionPriority DESC, e.name ASC
			""")
	List<CBabPolicyAction> findByPolicyRuleAndDestinationNode(@Param ("policyRule") CBabPolicyRule policyRule,
			@Param ("destinationNode") CBabNodeEntity<?> destinationNode);

	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.policyRule
			LEFT JOIN FETCH e.destinationNode
			LEFT JOIN FETCH e.actionMask m
			WHERE e.policyRule = :policyRule
			AND e.actionMask = :actionMask
			ORDER BY e.executionOrder ASC, e.executionPriority DESC
			""")
	List<CBabPolicyAction> findByPolicyRuleAndActionMask(@Param ("policyRule") CBabPolicyRule policyRule,
			@Param ("actionMask") CBabPolicyActionMaskBase<?> actionMask);

	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.policyRule
			LEFT JOIN FETCH e.destinationNode
			LEFT JOIN FETCH e.actionMask m
			WHERE e.policyRule = :policyRule
			AND e.active = true
			ORDER BY e.executionOrder ASC, e.executionPriority DESC
			""")
	List<CBabPolicyAction> findEnabledByPolicyRule(@Param ("policyRule") CBabPolicyRule policyRule);

	@Query ("SELECT e FROM #{#entityName} e WHERE e.policyRule = :policyRule AND LOWER(e.name) = LOWER(:name)")
	Optional<CBabPolicyAction> findByNameAndPolicyRule(@Param ("name") String name, @Param ("policyRule") CBabPolicyRule policyRule);
}
