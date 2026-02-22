package tech.derbent.bab.policybase.actionmask.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractNamedRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** Generic repository contract for action mask entities. */
@NoRepositoryBean
@Profile ("bab")
public interface IPolicyActionMaskEntityRepository<MaskType extends CBabPolicyActionMaskBase<MaskType>> extends IAbstractNamedRepository<MaskType> {

	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.parentNode = :parentNode
			ORDER BY e.executionOrder ASC, e.name ASC
			""")
	List<MaskType> findByParentNode(@Param ("parentNode") CBabNodeEntity<?> parentNode);

	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.active = true
			AND e.parentNode = :parentNode
			ORDER BY e.executionOrder ASC, e.name ASC
			""")
	List<MaskType> findEnabledByParentNode(@Param ("parentNode") CBabNodeEntity<?> parentNode);

	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.parentNode.project = :project
			ORDER BY e.executionOrder ASC, e.name ASC
			""")
	List<MaskType> listByProject(@Param ("project") CProject<?> project);

	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.name = :name AND e.parentNode = :parentNode
			""")
	Optional<MaskType> findByNameAndParentNode(@Param ("name") String name, @Param ("parentNode") CBabNodeEntity<?> parentNode);

	@Override
	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.parentNode
			WHERE e.id = :id
			""")
	Optional<MaskType> findById(@Param ("id") Long id);

	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.parentNode p
			WHERE p.project = :project
			ORDER BY e.executionOrder ASC, e.name ASC
			""")
	List<MaskType> listByProjectForPageView(@Param ("project") CProject<?> project);
}
