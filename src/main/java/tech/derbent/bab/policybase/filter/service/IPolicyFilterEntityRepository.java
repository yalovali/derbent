package tech.derbent.bab.policybase.filter.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractNamedRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** Base repository contract for policy-filter entities. */
@Profile ("bab")
@NoRepositoryBean
public interface IPolicyFilterEntityRepository<FilterType extends CBabPolicyFilterBase<FilterType>> extends IAbstractNamedRepository<FilterType> {

	@Query ("SELECT COUNT(e) FROM #{#entityName} e WHERE e.parentNode.project = :project")
	long countByProject(@Param ("project") CProject<?> project);

	@Query ("SELECT e FROM #{#entityName} e WHERE e.parentNode.project = :project AND e.active = true ORDER BY e.name ASC")
	List<FilterType> findEnabledByProject(@Param ("project") CProject<?> project);

	@Query ("SELECT e FROM #{#entityName} e WHERE e.parentNode.project = :project ORDER BY e.name ASC")
	List<FilterType> listByProject(@Param ("project") CProject<?> project);

	@Query ("SELECT e FROM #{#entityName} e WHERE e.parentNode = :parentNode ORDER BY e.name ASC")
	List<FilterType> findByParentNode(@Param ("parentNode") CBabNodeEntity<?> parentNode);

	@Query ("SELECT e FROM #{#entityName} e WHERE e.parentNode = :parentNode AND e.active = true ORDER BY e.name ASC")
	List<FilterType> findEnabledByParentNode(@Param ("parentNode") CBabNodeEntity<?> parentNode);

	@Query ("SELECT e FROM #{#entityName} e WHERE e.parentNode = :parentNode AND lower(e.name) = lower(:name)")
	Optional<FilterType> findByNameAndParentNode(@Param ("name") String name, @Param ("parentNode") CBabNodeEntity<?> parentNode);

	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.parentNode.project = :project
			AND e.active = true
			AND (
				(:nodeType = 'can' AND e.canNodeEnabled = true) OR
				(:nodeType = 'modbus' AND e.modbusNodeEnabled = true) OR
				(:nodeType = 'http' AND e.httpNodeEnabled = true) OR
				(:nodeType = 'file' AND e.fileNodeEnabled = true) OR
				(:nodeType = 'syslog' AND e.syslogNodeEnabled = true) OR
				(:nodeType = 'ros' AND e.rosNodeEnabled = true)
			)
			ORDER BY e.name ASC
			""")
	List<FilterType> findEnabledForNodeType(@Param ("project") CProject<?> project, @Param ("nodeType") String nodeType);

	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.parentNode
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			WHERE e.id = :id
			""")
	Optional<FilterType> findById(@Param ("id") Long id);

	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.parentNode p
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			WHERE p.project = :project
			ORDER BY e.name ASC
			""")
	List<FilterType> listByProjectForPageView(@Param ("project") CProject<?> project);
}
