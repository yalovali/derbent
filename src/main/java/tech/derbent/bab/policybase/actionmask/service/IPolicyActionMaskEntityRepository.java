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
			SELECT m FROM #{#entityName} m
			WHERE m.parentNode = :parentNode
			ORDER BY m.executionOrder ASC, m.name ASC
			""")
	List<MaskType> findByParentNode(@Param ("parentNode") CBabNodeEntity<?> parentNode);

	@Query ("""
			SELECT m FROM #{#entityName} m
			WHERE m.active = true
			AND m.parentNode = :parentNode
			ORDER BY m.executionOrder ASC, m.name ASC
			""")
	List<MaskType> findEnabledByParentNode(@Param ("parentNode") CBabNodeEntity<?> parentNode);

	@Query ("""
			SELECT m FROM #{#entityName} m
			WHERE m.parentNode.project = :project
			ORDER BY m.executionOrder ASC, m.name ASC
			""")
	List<MaskType> listByProject(@Param ("project") CProject<?> project);

	@Query ("""
			SELECT m FROM #{#entityName} m
			WHERE m.name = :name AND m.parentNode = :parentNode
			""")
	Optional<MaskType> findByNameAndParentNode(@Param ("name") String name, @Param ("parentNode") CBabNodeEntity<?> parentNode);

	@Override
	@Query ("""
			SELECT DISTINCT m FROM #{#entityName} m
			LEFT JOIN FETCH m.parentNode
			WHERE m.id = :id
			""")
	Optional<MaskType> findById(@Param ("id") Long id);

	@Query ("""
			SELECT DISTINCT m FROM #{#entityName} m
			LEFT JOIN FETCH m.parentNode p
			WHERE p.project = :project
			ORDER BY m.executionOrder ASC, m.name ASC
			""")
	List<MaskType> listByProjectForPageView(@Param ("project") CProject<?> project);
}
