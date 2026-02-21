package tech.derbent.bab.policybase.action.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** Repository for BAB policy actions. */
@Profile ("bab")
public interface IBabPolicyActionRepository extends IEntityOfProjectRepository<CBabPolicyAction> {

	@Override
	@Query ("""
			SELECT DISTINCT a FROM CBabPolicyAction a
			LEFT JOIN FETCH a.project
			LEFT JOIN FETCH a.createdBy
			LEFT JOIN FETCH a.attachments
			LEFT JOIN FETCH a.comments
			LEFT JOIN FETCH a.links
			LEFT JOIN FETCH a.destinationNode
			LEFT JOIN FETCH a.actionMask m
			LEFT JOIN FETCH m.parentNode
			WHERE a.id = :id
			""")
	Optional<CBabPolicyAction> findById(@Param ("id") Long id);

	@Query ("""
			SELECT DISTINCT a FROM CBabPolicyAction a
			LEFT JOIN FETCH a.destinationNode
			LEFT JOIN FETCH a.actionMask m
			LEFT JOIN FETCH m.parentNode
			WHERE a.project = :project
			AND a.destinationNode = :destinationNode
			ORDER BY a.executionOrder ASC, a.executionPriority DESC, a.name ASC
			""")
	List<CBabPolicyAction> findByProjectAndDestinationNode(@Param ("project") CProject<?> project,
			@Param ("destinationNode") CBabNodeEntity<?> destinationNode);

	@Query ("""
			SELECT DISTINCT a FROM CBabPolicyAction a
			LEFT JOIN FETCH a.destinationNode
			LEFT JOIN FETCH a.actionMask m
			LEFT JOIN FETCH m.parentNode
			WHERE a.project = :project
			AND a.actionMask = :actionMask
			ORDER BY a.executionOrder ASC, a.executionPriority DESC
			""")
	List<CBabPolicyAction> findByProjectAndActionMask(@Param ("project") CProject<?> project,
			@Param ("actionMask") CBabPolicyActionMaskBase<?> actionMask);

	@Query ("""
			SELECT DISTINCT a FROM CBabPolicyAction a
			LEFT JOIN FETCH a.destinationNode
			LEFT JOIN FETCH a.actionMask m
			LEFT JOIN FETCH m.parentNode
			WHERE a.project = :project
			AND a.active = true
			ORDER BY a.executionOrder ASC, a.executionPriority DESC
			""")
	List<CBabPolicyAction> findEnabledByProject(@Param ("project") CProject<?> project);

	@Override
	@Query ("""
			SELECT DISTINCT a FROM CBabPolicyAction a
			LEFT JOIN FETCH a.project
			LEFT JOIN FETCH a.createdBy
			LEFT JOIN FETCH a.attachments
			LEFT JOIN FETCH a.comments
			LEFT JOIN FETCH a.links
			LEFT JOIN FETCH a.destinationNode
			LEFT JOIN FETCH a.actionMask m
			LEFT JOIN FETCH m.parentNode
			WHERE a.project = :project
			ORDER BY a.executionOrder ASC, a.executionPriority DESC, a.name ASC
			""")
	List<CBabPolicyAction> listByProjectForPageView(@Param ("project") CProject<?> project);
}
