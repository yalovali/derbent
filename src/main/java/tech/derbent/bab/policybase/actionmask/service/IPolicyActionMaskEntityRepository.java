package tech.derbent.bab.policybase.actionmask.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractNamedRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;

/** Generic repository contract for action mask entities. */
@NoRepositoryBean
@Profile ("bab")
public interface IPolicyActionMaskEntityRepository<MaskType extends CBabPolicyActionMaskBase<MaskType>> extends IAbstractNamedRepository<MaskType> {

	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.policyAction = :policyAction
			ORDER BY e.executionOrder ASC, e.name ASC
			""")
	List<MaskType> findByPolicyAction(@Param ("policyAction") CBabPolicyAction policyAction);

	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.active = true
			AND e.policyAction = :policyAction
			ORDER BY e.executionOrder ASC, e.name ASC
			""")
	List<MaskType> findEnabledByPolicyAction(@Param ("policyAction") CBabPolicyAction policyAction);

	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.policyAction.policyRule.project = :project
			ORDER BY e.executionOrder ASC, e.name ASC
			""")
	List<MaskType> listByProject(@Param ("project") CProject<?> project);

	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.name = :name AND e.policyAction = :policyAction
			""")
	Optional<MaskType> findByNameAndPolicyAction(@Param ("name") String name, @Param ("policyAction") CBabPolicyAction policyAction);

	@Override
	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.policyAction a
			LEFT JOIN FETCH a.destinationNode
			WHERE e.id = :id
			""")
	Optional<MaskType> findById(@Param ("id") Long id);

	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.policyAction a
			LEFT JOIN FETCH a.policyRule r
			LEFT JOIN FETCH a.destinationNode
			WHERE r.project = :project
			ORDER BY e.executionOrder ASC, e.name ASC
			""")
	List<MaskType> listByProjectForPageView(@Param ("project") CProject<?> project);
}
